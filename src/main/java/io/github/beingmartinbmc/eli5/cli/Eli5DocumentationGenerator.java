package io.github.beingmartinbmc.eli5.cli;

import io.github.beingmartinbmc.eli5.ai.AiService;
import io.github.beingmartinbmc.eli5.ai.OpenAiService;
import io.github.beingmartinbmc.eli5.ai.StubAiService;
import io.github.beingmartinbmc.eli5.output.MarkdownWriter;
import io.github.beingmartinbmc.eli5.output.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Standalone CLI tool for generating ELI5 documentation.
 * This can be run independently of the build process.
 */
public class Eli5DocumentationGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(Eli5DocumentationGenerator.class);
    
    public static void main(String[] args) {
        try {
            Eli5DocumentationGenerator generator = new Eli5DocumentationGenerator();
            generator.run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void run(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0];
        
        switch (command) {
            case "generate":
                generateDocumentation(args);
                break;
            case "scan":
                scanForAnnotations(args);
                break;
            case "help":
                printUsage();
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }
    
    private void generateDocumentation(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: generate <source-directory> [output-file]");
            System.exit(1);
        }
        
        String sourceDir = args[1];
        String outputFile = args.length > 2 ? args[2] : "target/eli5-docs/eli5.md";
        
        System.out.println("🔍 Scanning for @ExplainLikeImFive annotations in: " + sourceDir);
        
        // Scan for annotated elements
        List<AnnotatedElement> elements = scanForAnnotatedElements(sourceDir);
        
        if (elements.isEmpty()) {
            System.out.println("❌ No @ExplainLikeImFive annotations found.");
            return;
        }
        
        System.out.println("✅ Found " + elements.size() + " annotated elements:");
        for (AnnotatedElement element : elements) {
            System.out.println("  - " + element.type + ": " + element.name);
        }
        
        // Initialize AI service
        AiService aiService = initializeAiService();
        
        // Generate explanations
        System.out.println("🤖 Generating explanations...");
        List<OutputWriter.Explanation> explanations;
        try {
            explanations = generateExplanations(elements, aiService);
        } catch (Exception e) {
            System.out.println("⚠️  Error during explanation generation: " + e.getMessage());
            System.out.println("🔄 Falling back to stub explanations...");
            explanations = generateStubExplanations(elements);
        }
        
        // Write output
        System.out.println("📝 Writing documentation to: " + outputFile);
        OutputWriter writer = new MarkdownWriter();
        writer.writeExplanations(explanations, outputFile);
        
        System.out.println("✅ Documentation generated successfully!");
    }
    
    private void scanForAnnotations(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: scan <source-directory>");
            System.exit(1);
        }
        
        String sourceDir = args[1];
        System.out.println("🔍 Scanning for @ExplainLikeImFive annotations in: " + sourceDir);
        
        List<AnnotatedElement> elements = scanForAnnotatedElements(sourceDir);
        
        if (elements.isEmpty()) {
            System.out.println("❌ No @ExplainLikeImFive annotations found.");
        } else {
            System.out.println("✅ Found " + elements.size() + " annotated elements:");
            for (AnnotatedElement element : elements) {
                System.out.println("  - " + element.type + ": " + element.name + " (" + element.file + ")");
            }
        }
    }
    
    private List<AnnotatedElement> scanForAnnotatedElements(String sourceDir) throws Exception {
        List<AnnotatedElement> elements = new ArrayList<>();
        
        // Simple file scanning for @ExplainLikeImFive annotations
        // In a real implementation, you'd use a proper Java parser
        Path sourcePath = Paths.get(sourceDir);
        if (!Files.exists(sourcePath)) {
            throw new IllegalArgumentException("Source directory does not exist: " + sourceDir);
        }
        
        Files.walk(sourcePath)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    if (content.contains("@ExplainLikeImFive")) {
                        // Simple parsing - in reality you'd use a proper Java parser
                        String[] lines = content.split("\n");
                        for (int i = 0; i < lines.length; i++) {
                            String line = lines[i];
                            if (line.contains("@ExplainLikeImFive")) {
                                // Try to find the element name on the next few lines
                                for (int j = i + 1; j < Math.min(i + 5, lines.length); j++) {
                                    String nextLine = lines[j].trim();
                                    if (nextLine.startsWith("public") || nextLine.startsWith("private") || 
                                        nextLine.startsWith("protected") || nextLine.startsWith("class") ||
                                        nextLine.startsWith("interface")) {
                                        
                                        String elementName = extractElementName(nextLine);
                                        String elementType = extractElementType(nextLine);
                                        
                                        elements.add(new AnnotatedElement(
                                            elementName, elementType, path.toString(), i + 1
                                        ));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error scanning file: " + path, e);
                }
            });
        
        return elements;
    }
    
    private String extractElementName(String line) {
        // Simple extraction - in reality you'd use a proper parser
        String[] parts = line.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("class") || parts[i].equals("interface") || parts[i].equals("enum")) {
                if (i + 1 < parts.length) {
                    return parts[i + 1].split("[<{]")[0]; // Remove generics
                }
            } else if (parts[i].equals("public") || parts[i].equals("private") || parts[i].equals("protected")) {
                if (i + 2 < parts.length && !parts[i + 1].equals("class") && !parts[i + 1].equals("interface")) {
                    return parts[i + 2].split("\\(")[0]; // Remove parameters
                }
            }
        }
        return "Unknown";
    }
    
    private String extractElementType(String line) {
        if (line.contains("class")) return "Class";
        if (line.contains("interface")) return "Interface";
        if (line.contains("enum")) return "Enum";
        if (line.contains("(")) return "Method";
        return "Field";
    }
    
    private AiService initializeAiService() {
        try {
            AiService aiService = new OpenAiService();
            if (aiService.isAvailable()) {
                System.out.println("🤖 Using OpenAI service");
                return aiService;
            } else {
                System.out.println("⚠️  OpenAI service not available, using stub service");
                return new StubAiService();
            }
        } catch (Exception e) {
            System.out.println("⚠️  Failed to initialize OpenAI service, using stub service: " + e.getMessage());
            return new StubAiService();
        }
    }
    
    private List<OutputWriter.Explanation> generateExplanations(List<AnnotatedElement> elements, AiService aiService) throws Exception {
        List<OutputWriter.Explanation> explanations = new ArrayList<>();
        
        // Convert to batch requests
        List<AiService.ExplanationRequest> requests = new ArrayList<>();
        for (AnnotatedElement element : elements) {
            requests.add(new AiService.ExplanationRequest(
                element.name, null, "Generated from " + element.file
            ));
        }
        
        // Generate explanations in batch
        List<String> batchExplanations = aiService.generateBatchExplanations(requests);
        
        // Create explanation objects
        for (int i = 0; i < elements.size(); i++) {
            AnnotatedElement element = elements.get(i);
            String explanation = i < batchExplanations.size() ? batchExplanations.get(i) : "Explanation not generated";
            
            explanations.add(new OutputWriter.Explanation(
                element.name, element.type, element.name, null, explanation, null
            ));
        }
        
        return explanations;
    }
    
    private List<OutputWriter.Explanation> generateStubExplanations(List<AnnotatedElement> elements) {
        List<OutputWriter.Explanation> explanations = new ArrayList<>();
        
        for (AnnotatedElement element : elements) {
            String stubExplanation = "This is a placeholder explanation for: " + element.name +
                "\n\n[This is a stub explanation. Configure a real AI service for actual ELI5 explanations.]";
            
            explanations.add(new OutputWriter.Explanation(
                element.name, element.type, element.name, null, stubExplanation, null
            ));
        }
        
        return explanations;
    }
    
    private void printUsage() {
        System.out.println("ELI5 Documentation Generator");
        System.out.println("===========================");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -cp <classpath> io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  generate <source-dir> [output-file]  Generate ELI5 documentation");
        System.out.println("  scan <source-dir>                     Scan for @ExplainLikeImFive annotations");
        System.out.println("  help                                  Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -cp target/classes io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator generate src/main/java");
        System.out.println("  java -cp target/classes io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator scan src/test/java");
        System.out.println();
        System.out.println("Environment Variables:");
        System.out.println("  ELI5_API_KEY                          OpenAI API key for real explanations");
    }
    
    private static class AnnotatedElement {
        final String name;
        final String type;
        final String file;
        final int line;
        
        AnnotatedElement(String name, String type, String file, int line) {
            this.name = name;
            this.type = type;
            this.file = file;
            this.line = line;
        }
    }
}
