package io.github.beingmartinbmc.eli5.output;

import java.util.List;

/**
 * Interface for writing ELI5 explanations to different output formats.
 */
public interface OutputWriter {
    
    /**
     * Writes explanations to the output format.
     * 
     * @param explanations List of explanations to write
     * @param outputPath The path where to write the output
     * @throws Exception if writing fails
     */
    void writeExplanations(List<Explanation> explanations, String outputPath) throws Exception;
    
    /**
     * Gets the file extension for this output format.
     * 
     * @return The file extension (e.g., ".md", ".json")
     */
    String getFileExtension();
    
    /**
     * Gets the default output filename for this format.
     * 
     * @return The default filename
     */
    String getDefaultFilename();
    
    /**
     * Represents a single ELI5 explanation.
     */
    class Explanation {
        private final String elementName;
        private final String elementType;
        private final String codeSignature;
        private final String codeBody;
        private final String explanation;
        private final String customPrompt;
        
        public Explanation(String elementName, String elementType, String codeSignature, 
                          String codeBody, String explanation, String customPrompt) {
            this.elementName = elementName;
            this.elementType = elementType;
            this.codeSignature = codeSignature;
            this.codeBody = codeBody;
            this.explanation = explanation;
            this.customPrompt = customPrompt;
        }
        
        public String getElementName() { return elementName; }
        public String getElementType() { return elementType; }
        public String getCodeSignature() { return codeSignature; }
        public String getCodeBody() { return codeBody; }
        public String getExplanation() { return explanation; }
        public String getCustomPrompt() { return customPrompt; }
    }
}
