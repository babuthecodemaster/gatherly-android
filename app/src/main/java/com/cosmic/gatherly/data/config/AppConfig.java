package com.cosmic.gatherly.data.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.cosmic.gatherly.BuildConfig;

/**
 * Application configuration settings
 */
public class AppConfig {
    private static final String PREFS_NAME = "app_config";
    private static final String KEY_USE_MOCK_API = "use_mock_api";
    private static final String KEY_MOCK_MODE_ENABLED = "mock_mode_enabled";
    
    private final SharedPreferences prefs;
    
    public AppConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Check if mock API should be used
     */
    public boolean shouldUseMockApi() {
        // For now, always use mock API to enable testing without server
        return true;
        
        // In the future, you can make this configurable:
        // return BuildConfig.DEBUG || prefs.getBoolean(KEY_USE_MOCK_API, false);
    }
    
    /**
     * Enable or disable mock API
     */
    public void setUseMockApi(boolean useMock) {
        prefs.edit().putBoolean(KEY_USE_MOCK_API, useMock).apply();
    }
    
    /**
     * Check if mock mode is enabled (for UI indicators)
     */
    public boolean isMockModeEnabled() {
        return shouldUseMockApi();
    }
    
    /**
     * Get configuration summary for debugging
     */
    public String getConfigSummary() {
        return String.format("AppConfig{mockApi=%s, debug=%s}", 
            shouldUseMockApi(), BuildConfig.DEBUG);
    }
}