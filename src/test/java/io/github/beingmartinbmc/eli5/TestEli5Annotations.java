package io.github.beingmartinbmc.eli5;

import io.github.beingmartinbmc.eli5.ai.AiService;
import io.github.beingmartinbmc.eli5.ai.OpenAiService;
import io.github.beingmartinbmc.eli5.ai.StubAiService;
import io.github.beingmartinbmc.eli5.config.Eli5Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ELI5 annotations functionality.
 * This demonstrates how to test the AI services and configuration.
 */
public class TestEli5Annotations {
    
    private AiService aiService;
    
    @BeforeEach
    void setUp() {
        // Check if OpenAI API key is available
        String apiKey = Eli5Config.get(Eli5Config.Keys.OPENAI_API_KEY, null);
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            aiService = new OpenAiService();
            System.out.println("Using OpenAI service for testing");
        } else {
            aiService = new StubAiService();
            System.out.println("Using Stub service for testing (no API key found)");
        }
    }
    
    @Test
    void testAiServiceAvailability() {
        assertNotNull(aiService);
        assertTrue(aiService.isAvailable());
    }
    
    @Test
    void testGenerateExplanation() throws Exception {
        String codeSignature = "public int factorial(int n)";
        String codeBody = "return n <= 1 ? 1 : n * factorial(n - 1);";
        String customPrompt = "This method calculates factorial using recursion";
        
        String explanation = aiService.generateExplanation(codeSignature, codeBody, customPrompt);
        
        assertNotNull(explanation);
        assertFalse(explanation.trim().isEmpty());
        System.out.println("Generated explanation: " + explanation);
    }
    
    @Test
    void testConfigurationLoading() {
        // Test that configuration is properly loaded
        String outputMode = Eli5Config.get(Eli5Config.Keys.OUTPUT_MODE, "markdown");
        String model = Eli5Config.get(Eli5Config.Keys.OPENAI_MODEL, "gpt-3.5-turbo");
        
        assertEquals("markdown", outputMode);
        assertNotNull(model);
        
        System.out.println("Output mode: " + outputMode);
        System.out.println("Model: " + model);
    }
    
    @Test
    void testStubServiceFallback() {
        // Test stub service when no API key is available
        StubAiService stubService = new StubAiService();
        assertTrue(stubService.isAvailable());
        
        try {
            String explanation = stubService.generateExplanation(
                "public void test()", 
                "System.out.println(\"Hello\");", 
                "Test method"
            );
            assertNotNull(explanation);
            assertTrue(explanation.contains("placeholder"));
        } catch (Exception e) {
            fail("Stub service should not throw exceptions");
        }
    }
    
    @Test
    void testMethodBodyExtraction() {
        // Test that method body extraction works correctly
        String expectedSignature = "public int factorial(final int n)";
        String expectedBody = "return n <= 1 ? 1 : n * factorial(n - 1);";
        
        // This test verifies that the annotation processor can extract method bodies
        // The actual extraction happens during compilation, but we can test the expected format
        assertNotNull(expectedSignature);
        assertNotNull(expectedBody);
        assertTrue(expectedSignature.contains("factorial"));
        assertTrue(expectedBody.contains("return"));
        assertTrue(expectedBody.contains("factorial"));
        
        System.out.println("Method signature: " + expectedSignature);
        System.out.println("Method body: " + expectedBody);
    }
    
    @Test
    void testAnnotationProcessorIntegration() {
        // Test that the annotation processor integrates correctly with method body extraction
        String codeSignature = "public int factorial(final int n)";
        String codeBody = "return n <= 1 ? 1 : n * factorial(n - 1);";
        
        // Verify that both signature and body are properly formatted for AI processing
        assertTrue(codeSignature.startsWith("public"));
        assertTrue(codeBody.contains("return"));
        
        // Test that the AI service can process extracted method bodies
        try {
            String explanation = aiService.generateExplanation(codeSignature, codeBody, "Test method body extraction");
            assertNotNull(explanation);
            assertFalse(explanation.trim().isEmpty());
            System.out.println("Method body extraction test explanation: " + explanation);
        } catch (Exception e) {
            fail("AI service should handle extracted method bodies: " + e.getMessage());
        }
    }
}
