package io.github.beingmartinbmc.eli5.processor;

import io.github.beingmartinbmc.eli5.ai.AiService;
import io.github.beingmartinbmc.eli5.ai.OpenAiService;
import io.github.beingmartinbmc.eli5.ai.StubAiService;
import io.github.beingmartinbmc.eli5.annotations.ExplainLikeImFive;
import io.github.beingmartinbmc.eli5.output.MarkdownWriter;
import io.github.beingmartinbmc.eli5.output.OutputWriter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSR-269 annotation processor for @ExplainLikeImFive annotations.
 * Optimized to collect all annotated elements and process them in a single batch.
 */
@SupportedAnnotationTypes("io.github.beingmartinbmc.eli5.annotations.ExplainLikeImFive")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ExplainLikeImFiveProcessor extends AbstractProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ExplainLikeImFiveProcessor.class);
    
    private Messager messager;
    private Trees trees;
    private AiService aiService;
    private OutputWriter outputWriter;
    private List<OutputWriter.Explanation> explanations;
    private List<ElementData> pendingElements;
    
    // Data class to hold element information before processing
    private static class ElementData {
        final Element element;
        final String elementName;
        final String elementType;
        final String codeSignature;
        final String codeBody;
        final String customPrompt;
        
        ElementData(Element element, String elementName, String elementType, 
                   String codeSignature, String codeBody, String customPrompt) {
            this.element = element;
            this.elementName = elementName;
            this.elementType = elementType;
            this.codeSignature = codeSignature;
            this.codeBody = codeBody;
            this.customPrompt = customPrompt;
        }
    }
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = Trees.instance(processingEnv);
        this.explanations = new ArrayList<>();
        this.pendingElements = new ArrayList<>();
        
        // Initialize AI service (try OpenAI first, fallback to stub)
        try {
            this.aiService = new OpenAiService();
            if (!this.aiService.isAvailable()) {
                this.aiService = new StubAiService();
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "OpenAI service not available, using stub service. Set ELI5_API_KEY environment variable for real explanations.");
            }
        } catch (Exception e) {
            this.aiService = new StubAiService();
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Failed to initialize OpenAI service, using stub service: " + e.getMessage());
        }
        
        // Initialize output writer (Markdown for now)
        this.outputWriter = new MarkdownWriter();
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            // Processing is complete, generate all explanations in batch
            if (!pendingElements.isEmpty()) {
                generateExplanationsBatch();
            }
            writeOutput();
            return false;
        }
        
        // Collect all annotated elements without processing them yet
        for (Element element : roundEnv.getElementsAnnotatedWith(ExplainLikeImFive.class)) {
            collectElementData(element);
        }
        
        return false; // Don't claim the annotation
    }
    
    private void collectElementData(Element element) {
        try {
            ExplainLikeImFive annotation = element.getAnnotation(ExplainLikeImFive.class);
            String elementName = element.getSimpleName().toString();
            String elementType = getElementType(element);
            String codeSignature = getCodeSignature(element);
            String codeBody = annotation.includeBody() ? getCodeBody(element) : null;
            String customPrompt = annotation.prompt();
            
            // Store element data for batch processing
            pendingElements.add(new ElementData(element, elementName, elementType, 
                                              codeSignature, codeBody, customPrompt));
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "Collected ELI5 annotation for " + elementType + ": " + elementName);
                
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                "Failed to collect @ExplainLikeImFive annotation on " + element.getSimpleName() + ": " + e.getMessage());
        }
    }
    
    private void generateExplanationsBatch() {
        if (pendingElements.isEmpty()) {
            return;
        }
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "Generating ELI5 explanations for " + pendingElements.size() + " elements in batch...");
        
        try {
            // Generate all explanations in a single batch call
            List<String> batchExplanations = aiService.generateBatchExplanations(
                pendingElements.stream()
                    .map(data -> new AiService.ExplanationRequest(
                        data.codeSignature, data.codeBody, data.customPrompt))
                    .collect(Collectors.toList())
            );
            
            // Create explanation objects
            for (int i = 0; i < pendingElements.size(); i++) {
                ElementData data = pendingElements.get(i);
                String explanation = i < batchExplanations.size() ? batchExplanations.get(i) : "Explanation not generated";
                
                explanations.add(new OutputWriter.Explanation(
                    data.elementName, data.elementType, data.codeSignature, 
                    data.codeBody, explanation, data.customPrompt
                ));
                
                messager.printMessage(Diagnostic.Kind.NOTE, 
                    "Generated ELI5 explanation for " + data.elementType + ": " + data.elementName);
            }
            
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Batch processing failed, falling back to individual processing: " + e.getMessage());
            
            // Fallback to individual processing
            for (ElementData data : pendingElements) {
                try {
                    String explanation = aiService.generateExplanation(
                        data.codeSignature, data.codeBody, data.customPrompt);
                    
                    explanations.add(new OutputWriter.Explanation(
                        data.elementName, data.elementType, data.codeSignature, 
                        data.codeBody, explanation, data.customPrompt
                    ));
                    
                    messager.printMessage(Diagnostic.Kind.NOTE, 
                        "Generated ELI5 explanation for " + data.elementType + ": " + data.elementName);
                } catch (Exception ex) {
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        "Failed to generate explanation for " + data.elementName + ", using stub: " + ex.getMessage());
                    
                    // Use stub explanation as final fallback
                    String stubExplanation = "This is a placeholder explanation for: " + data.codeSignature +
                        "\n\n[This is a stub explanation. Configure a real AI service for actual ELI5 explanations.]";
                    
                    explanations.add(new OutputWriter.Explanation(
                        data.elementName, data.elementType, data.codeSignature, 
                        data.codeBody, stubExplanation, data.customPrompt
                    ));
                }
            }
        }
        
        pendingElements.clear();
    }
    
    private String getElementType(Element element) {
        switch (element.getKind()) {
            case METHOD:
                return "Method";
            case CLASS:
            case INTERFACE:
                return "Class";
            case FIELD:
                return "Field";
            default:
                return "Element";
        }
    }
    
    private String getCodeSignature(Element element) {
        switch (element.getKind()) {
            case METHOD:
                return buildMethodSignature((ExecutableElement) element);
            case CLASS:
            case INTERFACE:
                return "class " + ((TypeElement) element).getSimpleName();
            case FIELD:
                return buildFieldSignature((VariableElement) element);
            default:
                return element.getSimpleName().toString();
        }
    }
    
    private String buildMethodSignature(ExecutableElement method) {
        String parameters = method.getParameters().stream()
                .map(param -> param.asType() + " " + param.getSimpleName())
                .collect(Collectors.joining(", "));
        
        return method.getReturnType() + " " + method.getSimpleName() + "(" + parameters + ")";
    }
    
    private String buildFieldSignature(VariableElement field) {
        return field.asType() + " " + field.getSimpleName();
    }
    
    private String getCodeBody(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            return null;
        }
        
        try {
            TreePath path = trees.getPath(element);
            if (path == null) {
                return null;
            }
            
            Tree leaf = path.getLeaf();
            if (leaf instanceof MethodTree) {
                MethodTree methodTree = (MethodTree) leaf;
                Tree body = methodTree.getBody();
                
                if (body instanceof BlockTree) {
                    BlockTree blockTree = (BlockTree) body;
                    StringBuilder bodyText = new StringBuilder();
                    
                    for (Tree statement : blockTree.getStatements()) {
                        bodyText.append(statement.toString()).append("\n");
                    }
                    
                    return bodyText.toString().trim();
                }
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Could not extract method body for " + element.getSimpleName() + ": " + e.getMessage());
        }
        
        return null;
    }
    
    private void writeOutput() {
        if (explanations.isEmpty()) {
            return;
        }
        
        try {
            // Write to target/eli5-docs/eli5.md
            String outputPath = "target/eli5-docs/" + outputWriter.getDefaultFilename();
            outputWriter.writeExplanations(explanations, outputPath);
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "ELI5 documentation written to: " + outputPath);
                
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                "Failed to write ELI5 documentation: " + e.getMessage());
        }
    }
}
