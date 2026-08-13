package com.tfs.config;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input =
                     ConfigManager.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "config/framework.properties"
                             )) {

            if (input == null) {
                throw new RuntimeException(
                        "framework.properties not found."
                );
            }

            PROPERTIES.load(input);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load framework.properties",
                    e
            );
        }
    }

    private ConfigManager() {
    }

    /*
     * Get property.
     * Returns empty string if property does not exist.
     */
    public static String get(String key) {

        // First allow Maven/System property override
        String systemValue = System.getProperty(key);

        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }

        String value = PROPERTIES.getProperty(key);

        if (value == null) {
            return "";
        }

        return value.trim();
    }


    /*
     * Required property.
     * Framework fails clearly when required value is missing.
     */
    public static String require(String key) {

        String value = get(key);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Required configuration is missing: " + key
            );
        }

        return value;
    }


    /*
     * Return default value if configuration is empty.
     */
    public static String getOrDefault(
            String key,
            String defaultValue
    ) {

        String value = get(key);

        return value.isEmpty()
                ? defaultValue
                : value;
    }


    /*
     * Boolean property
     */
    public static boolean getBoolean(String key) {

        return Boolean.parseBoolean(
                get(key)
        );
    }


    /*
     * Boolean property with default
     */
    public static boolean getBoolean(
            String key,
            boolean defaultValue
    ) {

        String value = get(key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }


    /*
     * Integer property
     */
    public static int getInt(
            String key,
            int defaultValue
    ) {

        String value = get(key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Configuration value must be an integer: "
                            + key
                            + "="
                            + value
            );
        }
    }


    /*
     * Long property
     */
    public static long getLong(
            String key,
            long defaultValue
    ) {

        String value = get(key);

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Configuration value must be a number: "
                            + key
                            + "="
                            + value
            );
        }
    }
}