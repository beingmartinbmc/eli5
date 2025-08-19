package io.github.beingmartinbmc.eli5.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
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
            
            // Try multiple locations for the properties file
            boolean loaded = false;
            
            // 1. Try classpath resource (for annotation processor)
            try (InputStream input = Eli5Config.class.getClassLoader().getResourceAsStream("eli5.properties")) {
                if (input != null) {
                    config.load(input);
                    logger.debug("Loaded configuration from classpath eli5.properties");
                    loaded = true;
                }
            } catch (IOException e) {
                logger.debug("Failed to load classpath eli5.properties: {}", e.getMessage());
            }
            
            // 2. Try project src/main/resources (for Maven plugin)
            if (!loaded) {
                try {
                    File projectProps = new File("src/main/resources/eli5.properties");
                    if (projectProps.exists()) {
                        try (FileInputStream input = new FileInputStream(projectProps)) {
                            config.load(input);
                            logger.debug("Loaded configuration from src/main/resources/eli5.properties");
                            loaded = true;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Failed to load src/main/resources/eli5.properties: {}", e.getMessage());
                }
            }
            
            // 3. Try current working directory
            if (!loaded) {
                try {
                    File cwdProps = new File("eli5.properties");
                    if (cwdProps.exists()) {
                        try (FileInputStream input = new FileInputStream(cwdProps)) {
                            config.load(input);
                            logger.debug("Loaded configuration from current directory eli5.properties");
                            loaded = true;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Failed to load current directory eli5.properties: {}", e.getMessage());
                }
            }
            
            // 4. Try target/classes (for compiled projects)
            if (!loaded) {
                try {
                    File targetProps = new File("target/classes/eli5.properties");
                    if (targetProps.exists()) {
                        try (FileInputStream input = new FileInputStream(targetProps)) {
                            config.load(input);
                            logger.debug("Loaded configuration from target/classes/eli5.properties");
                            loaded = true;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Failed to load target/classes/eli5.properties: {}", e.getMessage());
                }
            }
            
            if (!loaded) {
                logger.debug("No eli5.properties file found, using defaults");
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
