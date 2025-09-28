package com.cosmic.gatherly.data.util;

import android.util.Log;
import timber.log.Timber;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized logging utility for the Gatherly app
 * Provides structured logging with different levels and crash prevention
 */
public class Logger {
    
    // Log tags for different components
    public static final String TAG_AUTH = "Auth";
    public static final String TAG_API = "API";
    public static final String TAG_NETWORK = "Network";
    public static final String TAG_DATABASE = "Database";
    public static final String TAG_UI = "UI";
    public static final String TAG_WEBSOCKET = "WebSocket";
    public static final String TAG_SYNC = "Sync";
    public static final String TAG_SECURITY = "Security";
    public static final String TAG_PERFORMANCE = "Performance";
    public static final String TAG_FIREBASE = "Firebase";
    
    // Performance tracking
    private static final Map<String, Long> performanceTimers = new HashMap<>();
    
    /**
     * Log debug information
     */
    public static void d(String tag, String message) {
        try {
            Timber.tag(tag).d(message);
        } catch (Exception e) {
            // Fallback to Android Log if Timber fails
            Log.d(tag, message);
        }
    }
    
    /**
     * Log debug information with formatted arguments
     */
    public static void d(String tag, String message, Object... args) {
        try {
            Timber.tag(tag).d(message, args);
        } catch (Exception e) {
            // Fallback to Android Log if Timber fails
            Log.d(tag, String.format(message, args));
        }
    }
    
    /**
     * Log informational messages
     */
    public static void i(String tag, String message) {
        try {
            Timber.tag(tag).i(message);
        } catch (Exception e) {
            Log.i(tag, message);
        }
    }
    
    /**
     * Log informational messages with formatted arguments
     */
    public static void i(String tag, String message, Object... args) {
        try {
            Timber.tag(tag).i(message, args);
        } catch (Exception e) {
            Log.i(tag, String.format(message, args));
        }
    }
    
    /**
     * Log warning messages
     */
    public static void w(String tag, String message) {
        try {
            Timber.tag(tag).w(message);
        } catch (Exception e) {
            Log.w(tag, message);
        }
    }
    
    /**
     * Log warning messages with formatted arguments
     */
    public static void w(String tag, String message, Object... args) {
        try {
            Timber.tag(tag).w(message, args);
        } catch (Exception e) {
            Log.w(tag, String.format(message, args));
        }
    }
    
    /**
     * Log warning messages with throwable
     */
    public static void w(String tag, String message, Throwable throwable) {
        try {
            Timber.tag(tag).w(throwable, message);
        } catch (Exception e) {
            Log.w(tag, message, throwable);
        }
    }
    
    /**
     * Log error messages
     */
    public static void e(String tag, String message) {
        try {
            Timber.tag(tag).e(message);
        } catch (Exception e) {
            Log.e(tag, message);
        }
    }
    
    /**
     * Log error messages with formatted arguments
     */
    public static void e(String tag, String message, Object... args) {
        try {
            Timber.tag(tag).e(message, args);
        } catch (Exception e) {
            Log.e(tag, String.format(message, args));
        }
    }
    
    /**
     * Log error messages with throwable and full stack trace
     */
    public static void e(String tag, String message, Throwable throwable) {
        try {
            // Log with Timber
            Timber.tag(tag).e(throwable, message);
            
            // Also log detailed stack trace for debugging
            logDetailedError(tag, message, throwable);
        } catch (Exception e) {
            // Fallback to Android Log
            Log.e(tag, message, throwable);
        }
    }
    
    /**
     * Log detailed error information with full stack trace
     */
    private static void logDetailedError(String tag, String message, Throwable throwable) {
        try {
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("ERROR DETAILS:\n");
            errorDetails.append("Message: ").append(message).append("\n");
            errorDetails.append("Exception: ").append(throwable.getClass().getSimpleName()).append("\n");
            errorDetails.append("Exception Message: ").append(throwable.getMessage()).append("\n");
            
            // Add stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            errorDetails.append("Stack Trace:\n").append(sw.toString());
            
            // Add cause chain if present
            Throwable cause = throwable.getCause();
            int causeLevel = 1;
            while (cause != null && causeLevel <= 5) { // Limit to 5 levels to prevent infinite loops
                errorDetails.append("Caused by (Level ").append(causeLevel).append("): ")
                           .append(cause.getClass().getSimpleName())
                           .append(" - ").append(cause.getMessage()).append("\n");
                cause = cause.getCause();
                causeLevel++;
            }
            
            Timber.tag(tag + "_DETAILED").e(errorDetails.toString());
        } catch (Exception e) {
            // If detailed logging fails, just log the basic error
            Log.e(tag, "Failed to log detailed error: " + e.getMessage());
        }
    }
    
    /**
     * Log method entry for debugging
     */
    public static void methodEntry(String tag, String methodName) {
        d(tag, "→ Entering method: %s", methodName);
    }
    
    /**
     * Log method entry with parameters
     */
    public static void methodEntry(String tag, String methodName, Object... params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("→ Entering method: ").append(methodName);
            if (params != null && params.length > 0) {
                sb.append(" with params: ");
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(sanitizeParam(params[i]));
                }
            }
            d(tag, sb.toString());
        } catch (Exception e) {
            d(tag, "→ Entering method: %s (param logging failed)", methodName);
        }
    }
    
    /**
     * Log method exit for debugging
     */
    public static void methodExit(String tag, String methodName) {
        d(tag, "← Exiting method: %s", methodName);
    }
    
    /**
     * Log method exit with return value
     */
    public static void methodExit(String tag, String methodName, Object returnValue) {
        try {
            d(tag, "← Exiting method: %s, returning: %s", methodName, sanitizeParam(returnValue));
        } catch (Exception e) {
            d(tag, "← Exiting method: %s (return value logging failed)", methodName);
        }
    }
    
    /**
     * Start performance timing for a operation
     */
    public static void startTiming(String operationName) {
        try {
            performanceTimers.put(operationName, System.currentTimeMillis());
            d(TAG_PERFORMANCE, "⏱️ Started timing: %s", operationName);
        } catch (Exception e) {
            Log.w(TAG_PERFORMANCE, "Failed to start timing for: " + operationName);
        }
    }
    
    /**
     * End performance timing and log duration
     */
    public static void endTiming(String operationName) {
        try {
            Long startTime = performanceTimers.remove(operationName);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                i(TAG_PERFORMANCE, "⏱️ %s completed in %dms", operationName, duration);
                
                // Warn about slow operations
                if (duration > 5000) { // 5 seconds
                    w(TAG_PERFORMANCE, "⚠️ Slow operation detected: %s took %dms", operationName, duration);
                }
            } else {
                w(TAG_PERFORMANCE, "No start time found for operation: %s", operationName);
            }
        } catch (Exception e) {
            Log.w(TAG_PERFORMANCE, "Failed to end timing for: " + operationName);
        }
    }
    
    /**
     * Log API request details
     */
    public static void logApiRequest(String method, String url, Object requestBody) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🌐 API Request: ").append(method).append(" ").append(url);
            if (requestBody != null) {
                sb.append("\nRequest Body: ").append(sanitizeApiData(requestBody.toString()));
            }
            d(TAG_API, sb.toString());
        } catch (Exception e) {
            d(TAG_API, "🌐 API Request: %s %s (body logging failed)", method, url);
        }
    }
    
    /**
     * Log API response details
     */
    public static void logApiResponse(String method, String url, int statusCode, String responseBody, long durationMs) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🌐 API Response: ").append(method).append(" ").append(url);
            sb.append(" → ").append(statusCode).append(" (").append(durationMs).append("ms)");
            
            if (responseBody != null && !responseBody.isEmpty()) {
                // Limit response body size for logging
                String sanitizedBody = sanitizeApiData(responseBody);
                if (sanitizedBody.length() > 1000) {
                    sanitizedBody = sanitizedBody.substring(0, 1000) + "... (truncated)";
                }
                sb.append("\nResponse Body: ").append(sanitizedBody);
            }
            
            if (statusCode >= 200 && statusCode < 300) {
                d(TAG_API, sb.toString());
            } else if (statusCode >= 400 && statusCode < 500) {
                w(TAG_API, sb.toString());
            } else {
                e(TAG_API, sb.toString());
            }
        } catch (Exception e) {
            d(TAG_API, "🌐 API Response: %s %s → %d (%dms) (response logging failed)", 
                method, url, statusCode, durationMs);
        }
    }
    
    /**
     * Log network error with detailed information
     */
    public static void logNetworkError(String operation, Throwable error, String url) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🚫 Network Error in ").append(operation);
            if (url != null) {
                sb.append(" for URL: ").append(url);
            }
            sb.append("\nError Type: ").append(error.getClass().getSimpleName());
            sb.append("\nError Message: ").append(error.getMessage());
            
            e(TAG_NETWORK, sb.toString(), error);
        } catch (Exception e) {
            Log.e(TAG_NETWORK, "Network error in " + operation + ": " + error.getMessage(), error);
        }
    }
    
    /**
     * Log authentication events
     */
    public static void logAuthEvent(String event, String userId, boolean success) {
        try {
            String status = success ? "✅ SUCCESS" : "❌ FAILED";
            String sanitizedUserId = userId != null ? sanitizeUserId(userId) : "unknown";
            i(TAG_AUTH, "🔐 Auth Event: %s for user %s - %s", event, sanitizedUserId, status);
        } catch (Exception e) {
            Log.i(TAG_AUTH, "Auth event: " + event + " - " + (success ? "SUCCESS" : "FAILED"));
        }
    }
    
    /**
     * Log security-related events
     */
    public static void logSecurityEvent(String event, String details) {
        try {
            w(TAG_SECURITY, "🔒 Security Event: %s - %s", event, details);
        } catch (Exception e) {
            Log.w(TAG_SECURITY, "Security event: " + event);
        }
    }
    
    /**
     * Sanitize parameter for logging (remove sensitive data)
     */
    private static String sanitizeParam(Object param) {
        if (param == null) return "null";
        
        String paramStr = param.toString();
        
        // Don't log sensitive information
        if (paramStr.toLowerCase().contains("password") || 
            paramStr.toLowerCase().contains("token") ||
            paramStr.toLowerCase().contains("secret")) {
            return "[SENSITIVE_DATA_HIDDEN]";
        }
        
        // Limit length
        if (paramStr.length() > 200) {
            return paramStr.substring(0, 200) + "... (truncated)";
        }
        
        return paramStr;
    }
    
    /**
     * Sanitize API data for logging
     */
    private static String sanitizeApiData(String data) {
        if (data == null) return "null";
        
        // Remove sensitive fields from JSON-like strings
        String sanitized = data
            .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[HIDDEN]\"")
            .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"[HIDDEN]\"")
            .replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"[HIDDEN]\"")
            .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"[HIDDEN]\"")
            .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"[HIDDEN]\"");
        
        return sanitized;
    }
    
    /**
     * Sanitize user ID for logging (show only first few characters)
     */
    private static String sanitizeUserId(String userId) {
        if (userId == null || userId.length() <= 4) return userId;
        return userId.substring(0, 4) + "***";
    }
    
    /**
     * Safe execution wrapper that prevents crashes
     */
    public static <T> T safeExecute(String tag, String operation, SafeOperation<T> operation_func, T fallbackValue) {
        try {
            d(tag, "🛡️ Safe execution: %s", operation);
            T result = operation_func.execute();
            d(tag, "🛡️ Safe execution completed: %s", operation);
            return result;
        } catch (Exception e) {
            e(tag, "🛡️ Safe execution failed: " + operation, e);
            return fallbackValue;
        }
    }
    
    /**
     * Safe execution wrapper for void operations
     */
    public static void safeExecute(String tag, String operation, SafeVoidOperation operation_func) {
        try {
            d(tag, "🛡️ Safe execution: %s", operation);
            operation_func.execute();
            d(tag, "🛡️ Safe execution completed: %s", operation);
        } catch (Exception e) {
            e(tag, "🛡️ Safe execution failed: " + operation, e);
        }
    }
    
    /**
     * Interface for safe operations that return a value
     */
    public interface SafeOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Interface for safe void operations
     */
    public interface SafeVoidOperation {
        void execute() throws Exception;
    }
}