package io.github.beingmartinbmc.eli5.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Stub implementation of AiService that provides dummy explanations.
 * Used as a fallback when no real AI service is configured or available.
 */
public class StubAiService implements AiService {
    
    private static final Logger logger = LoggerFactory.getLogger(StubAiService.class);
    
    @Override
    public String generateExplanation(String codeSignature, String codeBody, String customPrompt) {
        Objects.requireNonNull(codeSignature, "codeSignature cannot be null");
        
        logger.debug("Generating stub explanation for: {}", codeSignature);
        
        return "This is a placeholder explanation for: " + codeSignature +
                Optional.ofNullable(codeBody)
                        .filter(body -> !body.trim().isEmpty())
                        .map(body -> {
                            String truncated = body.length() > 100 ? body.substring(0, 100) + "..." : body;
                            return "\n\nCode body: " + truncated;
                        })
                        .orElse("") +
                Optional.ofNullable(customPrompt)
                        .filter(prompt -> !prompt.trim().isEmpty())
                        .map(prompt -> "\n\nCustom prompt: " + prompt)
                        .orElse("") +
                "\n\n[This is a stub explanation. Configure a real AI service (OpenAI, Ollama, etc.) for actual ELI5 explanations.]";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Stub service is always available
    }
    
    @Override
    public String getServiceName() {
        return "StubAiService";
    }
}
