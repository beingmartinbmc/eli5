package io.github.beingmartinbmc.eli5.ai;

import java.util.List;

/**
 * Interface for AI service implementations that can generate ELI5 explanations.
 * This allows for pluggable AI backends (OpenAI, Ollama, HuggingFace, etc.).
 */
public interface AiService {
    
    /**
     * Data class for explanation requests in batch processing.
     */
    class ExplanationRequest {
        private final String codeSignature;
        private final String codeBody;
        private final String customPrompt;
        
        public ExplanationRequest(String codeSignature, String codeBody, String customPrompt) {
            this.codeSignature = codeSignature;
            this.codeBody = codeBody;
            this.customPrompt = customPrompt;
        }
        
        public String getCodeSignature() { return codeSignature; }
        public String getCodeBody() { return codeBody; }
        public String getCustomPrompt() { return customPrompt; }
    }
    
    /**
     * Generates an ELI5 explanation for the given code.
     * 
     * @param codeSignature The signature of the code element (method, class, field)
     * @param codeBody The body/content of the code element (optional)
     * @param customPrompt Optional custom prompt to guide the explanation
     * @return The generated ELI5 explanation
     * @throws Exception if the AI service fails to generate an explanation
     */
    String generateExplanation(String codeSignature, String codeBody, String customPrompt) throws Exception;
    
    /**
     * Generates ELI5 explanations for multiple code elements in a single batch request.
     * This is more efficient than making individual calls for each element.
     * 
     * @param requests List of explanation requests
     * @return List of generated explanations in the same order as the requests
     * @throws Exception if the AI service fails to generate explanations
     */
    default List<String> generateBatchExplanations(List<ExplanationRequest> requests) throws Exception {
        // Default implementation falls back to individual calls
        return requests.stream()
            .map(request -> {
                try {
                    return generateExplanation(request.getCodeSignature(), request.getCodeBody(), request.getCustomPrompt());
                } catch (Exception e) {
                    return "Error generating explanation: " + e.getMessage();
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Checks if the AI service is available and properly configured.
     * 
     * @return true if the service is available, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Gets the name of the AI service implementation.
     * 
     * @return The service name
     */
    String getServiceName();
}
