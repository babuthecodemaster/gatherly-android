package com.cosmic.gatherly.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cosmic.gatherly.BuildConfig;
import com.cosmic.gatherly.data.util.Logger;

/**
 * Manages server configuration and endpoint URLs for different environments
 */
public class ServerConfig {
    private static final String TAG = "ServerConfig";
    private static final String PREFS_NAME = "server_config";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_ENVIRONMENT = "environment";

    // Default URLs for different environments
    private static final String DEVELOPMENT_URL = "http://10.0.2.2:3000/";
    private static final String LOCALHOST_URL = "http://localhost:3000/";
    private static final String USB_DEBUG_URL = "http://10.0.2.2:3000/";
    private static final String PRODUCTION_URL = "https://your-production-server.com/";

    public enum Environment {
        DEVELOPMENT("development", DEVELOPMENT_URL),
        LOCALHOST("localhost", LOCALHOST_URL),
        USB_DEBUG("usb_debug", USB_DEBUG_URL),
        PRODUCTION("production", PRODUCTION_URL),
        CUSTOM("custom", "");

        private final String name;
        private final String defaultUrl;

        Environment(String name, String defaultUrl) {
            this.name = name;
            this.defaultUrl = defaultUrl;
        }

        public String getName() {
            return name;
        }

        public String getDefaultUrl() {
            return defaultUrl;
        }

        public static Environment fromString(String name) {
            for (Environment env : values()) {
                if (env.name.equalsIgnoreCase(name)) {
                    return env;
                }
            }
            return DEVELOPMENT; // Default fallback
        }
    }

    private final SharedPreferences prefs;
    private Environment currentEnvironment;
    private String baseUrl;

    public ServerConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadConfiguration();
        Logger.i(Logger.TAG_NETWORK, "ServerConfig initialized - Environment: %s, URL: %s",
                currentEnvironment.getName(), baseUrl);
    }

    /**
     * Load configuration from preferences or set defaults
     */
    private void loadConfiguration() {
        String envName = prefs.getString(KEY_ENVIRONMENT, null);
        String savedUrl = prefs.getString(KEY_BASE_URL, null);

        if (envName != null) {
            currentEnvironment = Environment.fromString(envName);
        } else {
            // Determine default environment based on build type
            currentEnvironment = BuildConfig.DEBUG ? Environment.DEVELOPMENT : Environment.PRODUCTION;
        }

        if (savedUrl != null && !savedUrl.isEmpty()) {
            baseUrl = savedUrl;
            if (!baseUrl.equals(currentEnvironment.getDefaultUrl())) {
                currentEnvironment = Environment.CUSTOM;
            }
        } else {
            baseUrl = currentEnvironment.getDefaultUrl();
        }

        // Ensure URL ends with slash
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
    }

    /**
     * Get current base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get current environment
     */
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }

    /**
     * Set environment and update base URL
     */
    public void setEnvironment(Environment environment) {
        if (environment != Environment.CUSTOM) {
            this.currentEnvironment = environment;
            this.baseUrl = environment.getDefaultUrl();
            saveConfiguration();
            Logger.i(Logger.TAG_NETWORK, "Environment changed to: %s with URL: %s",
                    environment.getName(), baseUrl);
        }
    }

    /**
     * Set custom base URL
     */
    public void setCustomBaseUrl(String url) {
        if (url != null && !url.isEmpty()) {
            this.baseUrl = url.endsWith("/") ? url : url + "/";
            this.currentEnvironment = Environment.CUSTOM;
            saveConfiguration();
            Logger.i(Logger.TAG_NETWORK, "Custom base URL set: %s", baseUrl);
        }
    }

    /**
     * Reset to default configuration
     */
    public void resetToDefault() {
        currentEnvironment = BuildConfig.DEBUG ? Environment.DEVELOPMENT : Environment.PRODUCTION;
        baseUrl = currentEnvironment.getDefaultUrl();
        saveConfiguration();
        Logger.i(Logger.TAG_NETWORK, "Configuration reset to default: %s", baseUrl);
    }

    /**
     * Save current configuration to preferences
     */
    private void saveConfiguration() {
        prefs.edit()
                .putString(KEY_ENVIRONMENT, currentEnvironment.getName())
                .putString(KEY_BASE_URL, baseUrl)
                .apply();
    }

    /**
     * Get API endpoint URL
     */
    public String getApiUrl() {
        return baseUrl + "api/";
    }

    /**
     * Get health check URL
     */
    public String getHealthUrl() {
        return baseUrl + "health";
    }

    /**
     * Get API health check URL
     */
    public String getApiHealthUrl() {
        return baseUrl + "api/health";
    }

    /**
     * Check if current configuration is for development
     */
    public boolean isDevelopment() {
        return currentEnvironment == Environment.DEVELOPMENT ||
                currentEnvironment == Environment.LOCALHOST ||
                (currentEnvironment == Environment.CUSTOM && baseUrl.contains("localhost"));
    }

    /**
     * Check if current configuration is for production
     */
    public boolean isProduction() {
        return currentEnvironment == Environment.PRODUCTION ||
                (currentEnvironment == Environment.CUSTOM && !baseUrl.contains("localhost"));
    }

    /**
     * Get configuration summary for debugging
     */
    public String getConfigurationSummary() {
        return String.format("ServerConfig{environment=%s, baseUrl='%s', isDev=%s}",
                currentEnvironment.getName(), baseUrl, isDevelopment());
    }

    /**
     * Validate current URL format
     */
    public boolean isValidUrl() {
        try {
            if (baseUrl == null || baseUrl.isEmpty()) {
                return false;
            }

            // Basic URL validation
            return baseUrl.startsWith("http://") || baseUrl.startsWith("https://");
        } catch (Exception e) {
            Logger.e(TAG, "Error validating URL: " + baseUrl, e);
            return false;
        }
    }
}