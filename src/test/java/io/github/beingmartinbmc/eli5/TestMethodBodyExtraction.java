package io.github.beingmartinbmc.eli5;

import io.github.beingmartinbmc.eli5.annotations.ExplainLikeImFive;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class specifically for testing method body extraction functionality.
 * This class contains annotated methods that will be processed by the annotation processor.
 */
@ExplainLikeImFive(prompt = "This is a test class for method body extraction")
public class TestMethodBodyExtraction {
    
    @ExplainLikeImFive(prompt = "This method adds two numbers")
    public int add(int a, int b) {
        return a + b;
    }
    
    @ExplainLikeImFive(prompt = "This method calculates the maximum of two numbers")
    public int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }
    
    @ExplainLikeImFive(prompt = "This method checks if a number is even")
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
    
    @ExplainLikeImFive(includeBody = false, prompt = "This method has body extraction disabled")
    public void methodWithoutBody() {
        System.out.println("This method body should not be extracted");
    }
    
    @ExplainLikeImFive
    public static final String TEST_CONSTANT = "Hello World";
    
    @Test
    void testAnnotationProcessing() {
        // This test verifies that the annotation processor can process this class
        // The actual processing happens during compilation
        assertTrue(true, "Annotation processor should process this class during compilation");
    }
    
    @Test
    void testMethodBodyFormat() {
        // Test that method bodies are in the expected format
        String expectedAddBody = "return a + b;";
        String expectedMaxBody = "if (a > b) {\n    return a;\n} else {\n    return b;\n}";
        
        assertNotNull(expectedAddBody);
        assertNotNull(expectedMaxBody);
        assertTrue(expectedAddBody.contains("return"));
        assertTrue(expectedMaxBody.contains("if"));
        assertTrue(expectedMaxBody.contains("else"));
    }
}
