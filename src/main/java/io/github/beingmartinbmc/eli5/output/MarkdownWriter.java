package io.github.beingmartinbmc.eli5.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Writes ELI5 explanations to Markdown format.
 */
public class MarkdownWriter implements OutputWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(MarkdownWriter.class);
    
    @Override
    public void writeExplanations(List<Explanation> explanations, String outputPath) throws Exception {
        Objects.requireNonNull(explanations, "explanations cannot be null");
        Objects.requireNonNull(outputPath, "outputPath cannot be null");
        
        logger.debug("Writing {} explanations to: {}", explanations.size(), outputPath);
        Path path = Paths.get(outputPath);
        
        // Ensure parent directory exists
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        
        String markdown = buildMarkdown(explanations);
        
        // Write to file
        Files.write(path, markdown.getBytes());
        logger.info("Successfully wrote ELI5 documentation to: {}", outputPath);
    }
    
    private String buildMarkdown(List<Explanation> explanations) {
        StringBuilder markdown = new StringBuilder();
        
        // Header
        markdown.append("# ELI5 Documentation\n\n");
        markdown.append("*Generated on ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("*\n\n");
        markdown.append("This documentation explains the code in simple terms, as if explaining to a 5-year-old.\n\n");
        
        // Table of contents
        if (!explanations.isEmpty()) {
            markdown.append("## Table of Contents\n\n");
            explanations.stream()
                    .map(explanation -> String.format("- [%s](#%s)\n", 
                            explanation.getElementName(),
                            explanation.getElementName().toLowerCase().replaceAll("[^a-z0-9]", "-")))
                    .forEach(markdown::append);
            markdown.append("\n");
        }
        
        // Explanations
        explanations.stream()
                .map(this::buildExplanationSection)
                .forEach(markdown::append);
        
        return markdown.toString();
    }
    
    private String buildExplanationSection(Explanation explanation) {
        return new StringBuilder()
                .append("## ").append(explanation.getElementType()).append(": ").append(explanation.getElementName()).append("\n\n")
                .append("**Code:**\n```java\n").append(explanation.getCodeSignature())
                .append(Optional.ofNullable(explanation.getCodeBody())
                        .filter(body -> !body.trim().isEmpty())
                        .map(body -> "\n" + body)
                        .orElse(""))
                .append("\n```\n\n")
                .append(Optional.ofNullable(explanation.getCustomPrompt())
                        .filter(prompt -> !prompt.trim().isEmpty())
                        .map(prompt -> "**Custom Context:** " + prompt + "\n\n")
                        .orElse(""))
                .append("**Explanation:**\n").append(explanation.getExplanation()).append("\n\n")
                .append("---\n\n")
                .toString();
    }
    
    @Override
    public String getFileExtension() {
        return ".md";
    }
    
    @Override
    public String getDefaultFilename() {
        return "eli5.md";
    }
}
