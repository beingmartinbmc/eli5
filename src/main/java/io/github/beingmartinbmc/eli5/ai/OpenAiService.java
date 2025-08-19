package io.github.beingmartinbmc.eli5.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.beingmartinbmc.eli5.config.Eli5Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * OpenAI service implementation using JDK 11 HttpClient.
 * Generates ELI5 explanations using OpenAI's API.
 */
public class OpenAiService implements AiService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int TIMEOUT_SECONDS = 30;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    
    public OpenAiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
        
        // Load configuration using the new config system
        this.apiKey = Eli5Config.get(Eli5Config.Keys.OPENAI_API_KEY, null);
        this.model = Eli5Config.get(Eli5Config.Keys.OPENAI_MODEL, Eli5Config.Defaults.OPENAI_MODEL);
        this.maxTokens = Eli5Config.getInt(Eli5Config.Keys.OPENAI_MAX_TOKENS, Eli5Config.Defaults.OPENAI_MAX_TOKENS);
        this.temperature = Double.parseDouble(Eli5Config.get(Eli5Config.Keys.OPENAI_TEMPERATURE, 
                String.valueOf(Eli5Config.Defaults.OPENAI_TEMPERATURE)));
        
        logger.debug("OpenAI Service initialized with model: {}, maxTokens: {}, temperature: {}", 
                model, maxTokens, temperature);
    }
    
    @Override
    public String generateExplanation(String codeSignature, String codeBody, String customPrompt) throws Exception {
        Objects.requireNonNull(codeSignature, "codeSignature cannot be null");
        
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI service is not available. Check API key configuration.");
        }
        
        logger.debug("Generating explanation for signature: {}", codeSignature);
        
        String prompt = buildPrompt(codeSignature, codeBody, customPrompt);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.set("messages", messages);
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        
        logger.debug("Sending request to OpenAI API");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            logger.error("OpenAI API request failed with status: {} and body: {}", 
                    response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API request failed with status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
        
        JsonNode responseJson = objectMapper.readTree(response.body());
        String explanation = responseJson.path("choices").path(0).path("message").path("content").asText();
        
        logger.debug("Successfully generated explanation (length: {})", explanation.length());
        return explanation;
    }
    
    @Override
    public List<String> generateBatchExplanations(List<ExplanationRequest> requests) throws Exception {
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI service is not available. Check API key configuration.");
        }
        
        logger.debug("Generating batch explanations for {} elements", requests.size());
        
        // Build a single comprehensive prompt for all elements
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append("Explain these Java code elements like I'm 5 years old. For each element, provide a simple, easy-to-understand explanation:\n\n");
        
        for (int i = 0; i < requests.size(); i++) {
            ExplanationRequest request = requests.get(i);
            batchPrompt.append("--- Element ").append(i + 1).append(" ---\n");
            batchPrompt.append("Code: ").append(request.getCodeSignature());
            
            if (request.getCodeBody() != null && !request.getCodeBody().trim().isEmpty()) {
                batchPrompt.append("\n\nImplementation:\n").append(request.getCodeBody());
            }
            
            if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
                batchPrompt.append("\n\nAdditional context: ").append(request.getCustomPrompt());
            }
            
            batchPrompt.append("\n\n");
        }
        
        batchPrompt.append("Please provide explanations for each element, separated by '---EXPLANATION---' markers.");
        
        // Make single API call with the batch prompt
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens * requests.size()); // Increase tokens for batch
        requestBody.put("temperature", temperature);
        
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", batchPrompt.toString());
        messages.add(message);
        requestBody.set("messages", messages);
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS * 2)) // Increase timeout for batch
                .build();
        
        logger.debug("Sending batch request to OpenAI API");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            logger.error("OpenAI API batch request failed with status: {} and body: {}", 
                    response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API batch request failed with status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
        
        JsonNode responseJson = objectMapper.readTree(response.body());
        String batchResponse = responseJson.path("choices").path(0).path("message").path("content").asText();
        
        // Parse the batch response into individual explanations
        List<String> explanations = parseBatchResponse(batchResponse, requests.size());
        
        logger.debug("Successfully generated {} batch explanations", explanations.size());
        return explanations;
    }
    
    private List<String> parseBatchResponse(String batchResponse, int expectedCount) {
        List<String> explanations = new ArrayList<>();
        
        // Split by the marker and clean up
        String[] parts = batchResponse.split("---EXPLANATION---");
        
        for (int i = 0; i < Math.min(parts.length, expectedCount); i++) {
            String explanation = parts[i].trim();
            if (!explanation.isEmpty()) {
                explanations.add(explanation);
            } else {
                explanations.add("Explanation not generated for element " + (i + 1));
            }
        }
        
        // If we got fewer explanations than expected, add placeholders
        while (explanations.size() < expectedCount) {
            explanations.add("Explanation not generated for element " + (explanations.size() + 1));
        }
        
        return explanations;
    }
    
    private String buildPrompt(String codeSignature, String codeBody, String customPrompt) {
        return "Explain this Java code like I'm 5 years old:\n\n" +
                "Code: " + codeSignature +
                Optional.ofNullable(codeBody)
                        .filter(body -> !body.trim().isEmpty())
                        .map(body -> "\n\nImplementation:\n" + body)
                        .orElse("") +
                Optional.ofNullable(customPrompt)
                        .filter(prompt -> !prompt.trim().isEmpty())
                        .map(prompt -> "\n\nAdditional context: " + prompt)
                        .orElse("") +
                "\n\nPlease provide a simple, easy-to-understand explanation that a 5-year-old could grasp.";
    }
    
    @Override
    public boolean isAvailable() {
        return Optional.ofNullable(apiKey)
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .isPresent();
    }
    
    @Override
    public String getServiceName() {
        return "OpenAI Service";
    }
}
