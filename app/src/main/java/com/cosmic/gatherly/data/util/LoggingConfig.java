package com.cosmic.gatherly.data.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Configuration class for managing logging settings and behavior
 */
public class LoggingConfig {
    private static final String TAG = "LoggingConfig";
    private static final String PREFS_NAME = "logging_config";
    
    // Preference keys
    private static final String KEY_DETAILED_LOGGING_ENABLED = "detailed_logging_enabled";
    private static final String KEY_PERFORMANCE_LOGGING_ENABLED = "performance_logging_enabled";
    private static final String KEY_NETWORK_LOGGING_ENABLED = "network_logging_enabled";
    private static final String KEY_AUTH_LOGGING_ENABLED = "auth_logging_enabled";
    private static final String KEY_DATABASE_LOGGING_ENABLED = "database_logging_enabled";
    private static final String KEY_CRASH_PREVENTION_ENABLED = "crash_prevention_enabled";
    private static final String KEY_LOG_LEVEL = "log_level";
    
    // Log levels
    public enum LogLevel {
        VERBOSE(2),
        DEBUG(3),
        INFO(4),
        WARN(5),
        ERROR(6);
        
        private final int priority;
        
        LogLevel(int priority) {
            this.priority = priority;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    private static LoggingConfig instance;
    private SharedPreferences prefs;
    
    private LoggingConfig(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeDefaults();
    }
    
    public static synchronized LoggingConfig getInstance(Context context) {
        if (instance == null) {
            instance = new LoggingConfig(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initializeDefaults() {
        // Set default values if not already set
        if (!prefs.contains(KEY_DETAILED_LOGGING_ENABLED)) {
            setDetailedLoggingEnabled(true); // Enable by default for debugging
        }
        if (!prefs.contains(KEY_PERFORMANCE_LOGGING_ENABLED)) {
            setPerformanceLoggingEnabled(true);
        }
        if (!prefs.contains(KEY_NETWORK_LOGGING_ENABLED)) {
            setNetworkLoggingEnabled(true);
        }
        if (!prefs.contains(KEY_AUTH_LOGGING_ENABLED)) {
            setAuthLoggingEnabled(true);
        }
        if (!prefs.contains(KEY_DATABASE_LOGGING_ENABLED)) {
            setDatabaseLoggingEnabled(true);
        }
        if (!prefs.contains(KEY_CRASH_PREVENTION_ENABLED)) {
            setCrashPreventionEnabled(true);
        }
        if (!prefs.contains(KEY_LOG_LEVEL)) {
            setLogLevel(LogLevel.DEBUG);
        }
    }
    
    // Getters
    public boolean isDetailedLoggingEnabled() {
        return prefs.getBoolean(KEY_DETAILED_LOGGING_ENABLED, true);
    }
    
    public boolean isPerformanceLoggingEnabled() {
        return prefs.getBoolean(KEY_PERFORMANCE_LOGGING_ENABLED, true);
    }
    
    public boolean isNetworkLoggingEnabled() {
        return prefs.getBoolean(KEY_NETWORK_LOGGING_ENABLED, true);
    }
    
    public boolean isAuthLoggingEnabled() {
        return prefs.getBoolean(KEY_AUTH_LOGGING_ENABLED, true);
    }
    
    public boolean isDatabaseLoggingEnabled() {
        return prefs.getBoolean(KEY_DATABASE_LOGGING_ENABLED, true);
    }
    
    public boolean isCrashPreventionEnabled() {
        return prefs.getBoolean(KEY_CRASH_PREVENTION_ENABLED, true);
    }
    
    public LogLevel getLogLevel() {
        String levelName = prefs.getString(KEY_LOG_LEVEL, LogLevel.DEBUG.name());
        try {
            return LogLevel.valueOf(levelName);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid log level: " + levelName + ", using DEBUG");
            return LogLevel.DEBUG;
        }
    }
    
    // Setters
    public void setDetailedLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DETAILED_LOGGING_ENABLED, enabled).apply();
        Log.d(TAG, "Detailed logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setPerformanceLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PERFORMANCE_LOGGING_ENABLED, enabled).apply();
        Log.d(TAG, "Performance logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setNetworkLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NETWORK_LOGGING_ENABLED, enabled).apply();
        Log.d(TAG, "Network logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setAuthLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTH_LOGGING_ENABLED, enabled).apply();
        Log.d(TAG, "Auth logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setDatabaseLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DATABASE_LOGGING_ENABLED, enabled).apply();
        Log.d(TAG, "Database logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setCrashPreventionEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_CRASH_PREVENTION_ENABLED, enabled).apply();
        Log.d(TAG, "Crash prevention " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setLogLevel(LogLevel level) {
        prefs.edit().putString(KEY_LOG_LEVEL, level.name()).apply();
        Log.d(TAG, "Log level set to: " + level.name());
    }
    
    /**
     * Check if a log should be written based on current log level
     */
    public boolean shouldLog(LogLevel messageLevel) {
        return messageLevel.getPriority() >= getLogLevel().getPriority();
    }
    
    /**
     * Get current configuration as a string for debugging
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder("Logging Configuration:\n");
        summary.append("  Detailed Logging: ").append(isDetailedLoggingEnabled()).append("\n");
        summary.append("  Performance Logging: ").append(isPerformanceLoggingEnabled()).append("\n");
        summary.append("  Network Logging: ").append(isNetworkLoggingEnabled()).append("\n");
        summary.append("  Auth Logging: ").append(isAuthLoggingEnabled()).append("\n");
        summary.append("  Database Logging: ").append(isDatabaseLoggingEnabled()).append("\n");
        summary.append("  Crash Prevention: ").append(isCrashPreventionEnabled()).append("\n");
        summary.append("  Log Level: ").append(getLogLevel().name()).append("\n");
        return summary.toString();
    }
    
    /**
     * Reset all settings to defaults
     */
    public void resetToDefaults() {
        prefs.edit().clear().apply();
        initializeDefaults();
        Log.i(TAG, "Logging configuration reset to defaults");
    }
    
    /**
     * Enable debug mode (all logging enabled, DEBUG level)
     */
    public void enableDebugMode() {
        setDetailedLoggingEnabled(true);
        setPerformanceLoggingEnabled(true);
        setNetworkLoggingEnabled(true);
        setAuthLoggingEnabled(true);
        setDatabaseLoggingEnabled(true);
        setCrashPreventionEnabled(true);
        setLogLevel(LogLevel.DEBUG);
        Log.i(TAG, "Debug mode enabled - all logging features activated");
    }
    
    /**
     * Enable production mode (minimal logging, ERROR level only)
     */
    public void enableProductionMode() {
        setDetailedLoggingEnabled(false);
        setPerformanceLoggingEnabled(false);
        setNetworkLoggingEnabled(false);
        setAuthLoggingEnabled(false);
        setDatabaseLoggingEnabled(false);
        setCrashPreventionEnabled(true); // Keep crash prevention in production
        setLogLevel(LogLevel.ERROR);
        Log.i(TAG, "Production mode enabled - minimal logging activated");
    }
}