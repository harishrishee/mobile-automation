package com.automation.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static final String CONFIG_PATH = "/config/";
    private static String loadedEnvironment;

    public static void loadProperties(String environment) {
        if (environment == null || environment.trim().isEmpty()) {
            environment = "android"; // Default to android if environment is not specified
        }
        if (properties != null && environment.equalsIgnoreCase(loadedEnvironment)) {
            return;
        }
        properties = new Properties();
        String configFile = CONFIG_PATH + environment + ".properties";
        
        try (InputStream is = ConfigReader.class.getResourceAsStream(configFile)) {
            if (is == null) {
                throw new RuntimeException("Configuration file not found: " + configFile + 
                    ". Please ensure the file exists in src/main/resources/config/ or specify environment with -Denv=android|ios");
            }
            properties.load(is);
            loadedEnvironment = environment;
            System.out.println("Loaded configuration from: " + configFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration file: " + configFile, e);
        }
    }

    public static String getProperty(String key) {
        if (properties == null) {
            String env = System.getProperty("env");
            if (env == null || env.trim().isEmpty()) {
                env = "android"; // Default to android
            }
            loadProperties(env);
        }
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            return systemValue;
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in configuration");
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            String env = System.getProperty("env");
            if (env == null || env.trim().isEmpty()) {
                env = "android"; // Default to android
            }
            loadProperties(env);
        }
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            return systemValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static boolean isLoaded() {
        return properties != null;
    }
}
