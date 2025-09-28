package com.cosmic.gatherly.data.util;

import android.util.Log;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.response.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

/**
 * Utility class for handling and parsing API errors
 */
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    private static final Gson gson = new Gson();
    
    /**
     * Parse error from Retrofit response with comprehensive logging
     */
    public static AuthError parseError(Response<?> response) {
        Logger.methodEntry(Logger.TAG_API, "parseError");
        
        if (response == null) {
            Logger.e(Logger.TAG_API, "Cannot parse error: response is null");
            return new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Response is null",
                "An unexpected error occurred. Please try again."
            );
        }
        
        int httpCode = response.code();
        String errorMessage = "HTTP " + httpCode;
        String userFriendlyMessage;
        AuthError.Type errorType;
        
        Logger.d(Logger.TAG_API, "Parsing error response: HTTP %d from %s", 
            httpCode, response.raw().request().url());
        
        // Determine error type based on HTTP status code
        switch (httpCode) {
            case 400:
                errorType = AuthError.Type.VALIDATION_ERROR;
                userFriendlyMessage = "Please check your input and try again.";
                break;
            case 401:
                errorType = AuthError.Type.AUTHENTICATION_ERROR;
                userFriendlyMessage = "Invalid credentials. Please check your email and password.";
                break;
            case 403:
                errorType = AuthError.Type.AUTHENTICATION_ERROR;
                userFriendlyMessage = "Access denied. Please check your credentials.";
                break;
            case 404:
                errorType = AuthError.Type.SERVER_ERROR;
                userFriendlyMessage = "Service not found. Please try again later.";
                break;
            case 408:
                errorType = AuthError.Type.TIMEOUT_ERROR;
                userFriendlyMessage = "Request timed out. Please check your connection and try again.";
                break;
            case 429:
                errorType = AuthError.Type.SERVER_ERROR;
                userFriendlyMessage = "Too many requests. Please wait a moment and try again.";
                break;
            case 500:
            case 502:
            case 503:
            case 504:
                errorType = AuthError.Type.SERVER_ERROR;
                userFriendlyMessage = "Server error. Please try again later.";
                break;
            default:
                errorType = AuthError.Type.UNKNOWN_ERROR;
                userFriendlyMessage = "An unexpected error occurred. Please try again.";
                break;
        }
        
        // Try to parse error body for more specific message with comprehensive logging
        try {
            if (response.errorBody() != null) {
                String errorBodyString = response.errorBody().string();
                Logger.d(Logger.TAG_API, "Error response body received (length: %d)", errorBodyString.length());
                
                // Try to parse as ErrorResponse
                try {
                    ErrorResponse errorResponse = gson.fromJson(errorBodyString, ErrorResponse.class);
                    if (errorResponse != null && errorResponse.getMessage() != null) {
                        errorMessage = errorResponse.getMessage();
                        Logger.d(Logger.TAG_API, "Parsed structured error message: %s", errorMessage);
                        
                        // Use server message if it's user-friendly, otherwise keep our default
                        if (isUserFriendlyMessage(errorResponse.getMessage())) {
                            userFriendlyMessage = errorResponse.getMessage();
                            Logger.d(Logger.TAG_API, "Using server error message as user-friendly message");
                        } else {
                            Logger.d(Logger.TAG_API, "Server error message not user-friendly, using default");
                        }
                    }
                } catch (JsonSyntaxException e) {
                    Logger.w(Logger.TAG_API, "Error body is not valid JSON, trying raw string", e);
                    // If not JSON, use the raw string if it's reasonable length
                    if (errorBodyString.length() < 200 && isUserFriendlyMessage(errorBodyString)) {
                        errorMessage = errorBodyString;
                        userFriendlyMessage = errorBodyString;
                        Logger.d(Logger.TAG_API, "Using raw error body as message");
                    } else {
                        Logger.d(Logger.TAG_API, "Raw error body not suitable for user display");
                    }
                }
            } else {
                Logger.d(Logger.TAG_API, "No error body in response");
            }
        } catch (IOException e) {
            Logger.e(Logger.TAG_API, "Error reading error response body", e);
        }
        
        AuthError authError = new AuthError(errorType, errorMessage, userFriendlyMessage, httpCode);
        Logger.d(Logger.TAG_API, "Created AuthError: type=%s, message=%s", errorType, errorMessage);
        Logger.methodExit(Logger.TAG_API, "parseError", authError.getType());
        
        return authError;
    }
    
    /**
     * Parse error from network exception with comprehensive logging
     */
    public static AuthError parseNetworkError(Throwable throwable) {
        Logger.methodEntry(Logger.TAG_NETWORK, "parseNetworkError");
        
        if (throwable == null) {
            Logger.e(Logger.TAG_NETWORK, "Cannot parse network error: throwable is null");
            return new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Unknown error",
                "An unexpected error occurred. Please try again."
            );
        }
        
        Logger.e(Logger.TAG_NETWORK, "Parsing network error: %s - %s", 
            throwable.getClass().getSimpleName(), throwable.getMessage(), throwable);
        
        AuthError.Type errorType;
        String userFriendlyMessage;
        String technicalMessage = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
        
        if (throwable instanceof SocketTimeoutException) {
            errorType = AuthError.Type.TIMEOUT_ERROR;
            userFriendlyMessage = "Connection timed out. Please check your internet connection and try again.";
        } else if (throwable instanceof ConnectException) {
            errorType = AuthError.Type.NETWORK_ERROR;
            userFriendlyMessage = "Unable to connect to server. Please check your internet connection and try again.";
        } else if (throwable instanceof UnknownHostException) {
            errorType = AuthError.Type.NETWORK_ERROR;
            userFriendlyMessage = "Unable to reach server. Please check your internet connection and try again.";
        } else if (throwable instanceof IOException) {
            errorType = AuthError.Type.NETWORK_ERROR;
            userFriendlyMessage = "Network error occurred. Please check your connection and try again.";
        } else {
            errorType = AuthError.Type.UNKNOWN_ERROR;
            userFriendlyMessage = "An unexpected error occurred. Please try again.";
        }
        
        AuthError networkError = new AuthError(errorType, technicalMessage, userFriendlyMessage, throwable);
        Logger.d(Logger.TAG_NETWORK, "Created network AuthError: type=%s, message=%s", 
            errorType, userFriendlyMessage);
        Logger.methodExit(Logger.TAG_NETWORK, "parseNetworkError", networkError.getType());
        
        return networkError;
    }
    
    /**
     * Check if a message is user-friendly (not too technical)
     */
    private static boolean isUserFriendlyMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        // Check for technical terms that shouldn't be shown to users
        String lowerMessage = message.toLowerCase();
        String[] technicalTerms = {
            "null pointer", "exception", "stack trace", "error code", 
            "internal server", "database", "sql", "connection refused",
            "timeout", "http", "json", "xml", "api"
        };
        
        for (String term : technicalTerms) {
            if (lowerMessage.contains(term)) {
                return false;
            }
        }
        
        // Check reasonable length
        return message.length() <= 150;
    }
    
    /**
     * Get a generic error for unexpected situations
     */
    public static AuthError getGenericError() {
        return new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Generic error",
            "An unexpected error occurred. Please try again."
        );
    }
    
    /**
     * Get a network unavailable error
     */
    public static AuthError getNetworkUnavailableError() {
        return new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Network unavailable",
            "Please check your internet connection and try again."
        );
    }

    /**
     * Log detailed error information for debugging
     */
    public static void logDetailedError(String context, AuthError error) {
        Logger.safeExecute(Logger.TAG_API, "log_detailed_error", () -> {
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("DETAILED ERROR REPORT - Context: ").append(context).append("\n");
            errorDetails.append("Error Type: ").append(error.getType()).append("\n");
            errorDetails.append("Technical Message: ").append(error.getMessage()).append("\n");
            errorDetails.append("User Message: ").append(error.getUserFriendlyMessage()).append("\n");
            errorDetails.append("HTTP Code: ").append(error.getHttpCode()).append("\n");
            
            if (error.getCause() != null) {
                errorDetails.append("Root Cause: ").append(error.getCause().getClass().getSimpleName()).append("\n");
                errorDetails.append("Root Cause Message: ").append(error.getCause().getMessage()).append("\n");
            }
            
            Logger.e(Logger.TAG_API, errorDetails.toString());
        });
    }
    
    /**
     * Create a fallback error when error parsing fails
     */
    public static AuthError createFallbackError(String context, Throwable cause) {
        Logger.w(Logger.TAG_API, "Creating fallback error for context: %s", context);
        
        return new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Fallback error for " + context + ": " + (cause != null ? cause.getMessage() : "unknown"),
            "An unexpected error occurred. Please try again.",
            cause
        );
    }
    
    /**
     * Check if an error is recoverable (user can retry)
     */
    public static boolean isRecoverableError(AuthError error) {
        if (error == null) return false;
        
        switch (error.getType()) {
            case NETWORK_ERROR:
            case TIMEOUT_ERROR:
            case SERVER_ERROR:
                return true;
            case VALIDATION_ERROR:
            case AUTHENTICATION_ERROR:
                return false;
            case UNKNOWN_ERROR:
            default:
                return true; // Allow retry for unknown errors
        }
    }
    
    /**
     * Get retry delay based on error type
     */
    public static long getRetryDelay(AuthError error, int attemptNumber) {
        if (error == null) return 1000;
        
        long baseDelay;
        switch (error.getType()) {
            case NETWORK_ERROR:
                baseDelay = 2000; // 2 seconds for network errors
                break;
            case TIMEOUT_ERROR:
                baseDelay = 5000; // 5 seconds for timeout errors
                break;
            case SERVER_ERROR:
                baseDelay = 10000; // 10 seconds for server errors
                break;
            default:
                baseDelay = 1000; // 1 second default
                break;
        }
        
        // Exponential backoff with jitter
        long delay = baseDelay * (long) Math.pow(2, attemptNumber - 1);
        long jitter = (long) (Math.random() * 1000); // Add up to 1 second jitter
        
        return Math.min(delay + jitter, 30000); // Cap at 30 seconds
    }
    
    /**
     * Enhanced method to check if error should trigger security logging
     */
    public static boolean isSecurityRelevantError(AuthError error) {
        if (error == null) return false;
        
        return error.getType() == AuthError.Type.AUTHENTICATION_ERROR ||
               error.getHttpCode() == 401 ||
               error.getHttpCode() == 403 ||
               (error.getMessage() != null && 
                (error.getMessage().toLowerCase().contains("unauthorized") ||
                 error.getMessage().toLowerCase().contains("forbidden") ||
                 error.getMessage().toLowerCase().contains("invalid token")));
    }
}