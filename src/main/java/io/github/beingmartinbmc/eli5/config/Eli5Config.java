package io.github.beingmartinbmc.eli5.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Optional;

/**
 * Configuration utility for ELI5 annotations.
 * Handles loading configuration from properties file and environment variables.
 */
public class Eli5Config {
    
    private static final Logger logger = LoggerFactory.getLogger(Eli5Config.class);
    private static final Properties config = new Properties();
    private static volatile boolean initialized = false;
    
    private static void initialize() {
        if (initialized) {
            return;
        }
        
        synchronized (Eli5Config.class) {
            if (initialized) {
                return;
            }
            
            // Load from properties file
            try (InputStream input = Eli5Config.class.getClassLoader().getResourceAsStream("eli5.properties")) {
                if (input != null) {
                    config.load(input);
                    logger.debug("Loaded configuration from eli5.properties");
                } else {
                    logger.debug("No eli5.properties file found, using defaults");
                }
            } catch (IOException e) {
                logger.warn("Failed to load eli5.properties: {}", e.getMessage());
            }
            
            initialized = true;
        }
    }
    
    /**
     * Gets a configuration value, checking properties file first, then environment variables.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if not found
     * @return The configuration value
     */
    public static String get(String key, String defaultValue) {
        initialize();
        
        // Check properties file first
        String value = config.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        // Check environment variable
        String envKey = key.replace('.', '_').toUpperCase();
        value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        return defaultValue;
    }
    
    /**
     * Gets a configuration value as integer.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if not found
     * @return The configuration value as integer
     */
    public static int getInt(String key, int defaultValue) {
        return Optional.ofNullable(get(key, null))
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid integer value for key '{}': {}", key, value);
                        return null;
                    }
                })
                .orElse(defaultValue);
    }
    
    // Configuration keys
    public static class Keys {
        // General configuration
        public static final String OUTPUT_MODE = "eli5.outputMode";
        
        // OpenAI configuration
        public static final String OPENAI_API_KEY = "eli5.openai.apiKey";
        public static final String OPENAI_MODEL = "eli5.openai.model";
        public static final String OPENAI_MAX_TOKENS = "eli5.openai.maxTokens";
        public static final String OPENAI_TEMPERATURE = "eli5.openai.temperature";
    }
    
    // Default values
    public static class Defaults {
        public static final String OPENAI_MODEL = "gpt-4.1-nano";
        public static final int OPENAI_MAX_TOKENS = 500;
        public static final double OPENAI_TEMPERATURE = 0.7;
    }
}
