package com.cosmic.gatherly.data.util;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

/**
 * Utility class for handling Firebase exceptions and converting them to user-friendly error messages.
 * This class provides static methods to convert various Firebase exceptions into readable messages
 * that can be displayed to users.
 */
public class FirebaseErrorHandler {

    /**
     * Converts Firebase exceptions to user-friendly error messages.
     * 
     * @param exception The exception to convert
     * @return A user-friendly error message string
     */
    public static String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "An unknown error occurred. Please try again.";
        }

        // Handle Firebase Authentication exceptions
        if (exception instanceof FirebaseAuthException) {
            return handleFirebaseAuthException((FirebaseAuthException) exception);
        }
        
        // Handle Firebase Firestore exceptions
        if (exception instanceof FirebaseFirestoreException) {
            return handleFirebaseFirestoreException((FirebaseFirestoreException) exception);
        }
        
        // Handle Firebase Network exceptions
        if (exception instanceof FirebaseNetworkException) {
            return "Network connection failed. Please check your internet connection and try again.";
        }
        
        // Handle Firebase Too Many Requests exceptions
        if (exception instanceof FirebaseTooManyRequestsException) {
            return "Too many requests. Please wait a moment and try again.";
        }
        
        // Handle generic exceptions
        return handleGenericException(exception);
    }

    /**
     * Handles Firebase Authentication specific exceptions.
     * 
     * @param authException The FirebaseAuthException to handle
     * @return A user-friendly error message
     */
    private static String handleFirebaseAuthException(FirebaseAuthException authException) {
        String errorCode = authException.getErrorCode();
        
        switch (errorCode) {
            // Network and connectivity errors
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "Network connection failed. Please check your internet connection and try again.";
            
            // User authentication errors
            case "ERROR_USER_NOT_FOUND":
                return "No account found with this email address. Please check your email or create a new account.";
            
            case "ERROR_WRONG_PASSWORD":
                return "Incorrect password. Please try again or reset your password.";
            
            case "ERROR_INVALID_EMAIL":
                return "Please enter a valid email address.";
            
            case "ERROR_USER_DISABLED":
                return "This account has been disabled. Please contact support for assistance.";
            
            // Registration errors
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "An account with this email already exists. Please sign in or use a different email.";
            
            case "ERROR_WEAK_PASSWORD":
                return "Password is too weak. Please choose a stronger password with at least 6 characters.";
            
            case "ERROR_INVALID_CREDENTIAL":
                return "Invalid credentials. Please check your email and password.";
            
            // Token and session errors
            case "ERROR_USER_TOKEN_EXPIRED":
                return "Your session has expired. Please sign in again.";
            
            case "ERROR_INVALID_USER_TOKEN":
                return "Invalid authentication token. Please sign in again.";
            
            case "ERROR_REQUIRES_RECENT_LOGIN":
                return "This operation requires recent authentication. Please sign in again.";
            
            // Account linking errors
            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                return "An account already exists with the same email but different sign-in credentials.";
            
            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                return "This credential is already associated with a different user account.";
            
            // Operation errors
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "This sign-in method is not enabled. Please contact support.";
            
            case "ERROR_TOO_MANY_REQUESTS":
                return "Too many unsuccessful attempts. Please try again later.";
            
            // Generic auth errors
            default:
                return "Authentication failed: " + getReadableErrorMessage(authException.getMessage());
        }
    }

    /**
     * Handles Firebase Firestore specific exceptions.
     * 
     * @param firestoreException The FirebaseFirestoreException to handle
     * @return A user-friendly error message
     */
    private static String handleFirebaseFirestoreException(FirebaseFirestoreException firestoreException) {
        FirebaseFirestoreException.Code errorCode = firestoreException.getCode();
        
        switch (errorCode) {
            case PERMISSION_DENIED:
                return "Access denied. You don't have permission to perform this operation.";
            
            case NOT_FOUND:
                return "The requested data was not found.";
            
            case ALREADY_EXISTS:
                return "The data you're trying to create already exists.";
            
            case RESOURCE_EXHAUSTED:
                return "Service is temporarily overloaded. Please try again later.";
            
            case FAILED_PRECONDITION:
                return "Operation failed due to a conflict. Please refresh and try again.";
            
            case ABORTED:
                return "Operation was aborted due to a conflict. Please try again.";
            
            case OUT_OF_RANGE:
                return "Invalid data range. Please check your input.";
            
            case UNIMPLEMENTED:
                return "This feature is not yet available.";
            
            case INTERNAL:
                return "Internal server error. Please try again later.";
            
            case UNAVAILABLE:
                return "Service is temporarily unavailable. Please try again later.";
            
            case DATA_LOSS:
                return "Data corruption detected. Please contact support.";
            
            case UNAUTHENTICATED:
                return "Authentication required. Please sign in and try again.";
            
            case INVALID_ARGUMENT:
                return "Invalid data provided. Please check your input.";
            
            case DEADLINE_EXCEEDED:
                return "Operation timed out. Please check your connection and try again.";
            
            case CANCELLED:
                return "Operation was cancelled.";
            
            default:
                return "Database error: " + getReadableErrorMessage(firestoreException.getMessage());
        }
    }

    /**
     * Handles generic exceptions that are not Firebase-specific.
     * 
     * @param exception The generic exception to handle
     * @return A user-friendly error message
     */
    private static String handleGenericException(Exception exception) {
        String message = exception.getMessage();
        
        // Check for common network-related errors
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("network") || lowerMessage.contains("connection")) {
                return "Network connection failed. Please check your internet connection and try again.";
            }
            
            if (lowerMessage.contains("timeout")) {
                return "Operation timed out. Please check your connection and try again.";
            }
            
            if (lowerMessage.contains("host") || lowerMessage.contains("dns")) {
                return "Unable to connect to server. Please check your internet connection.";
            }
        }
        
        return "An unexpected error occurred: " + getReadableErrorMessage(message);
    }

    /**
     * Converts technical error messages to more readable format.
     * 
     * @param message The technical error message
     * @return A more readable error message
     */
    private static String getReadableErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Please try again.";
        }
        
        // Remove technical prefixes and make message more user-friendly
        String cleanMessage = message
            .replaceAll("com\\.google\\.firebase\\.[\\w\\.]+:\\s*", "")
            .replaceAll("java\\.lang\\.[\\w\\.]+:\\s*", "")
            .replaceAll("\\[\\w+\\]\\s*", "")
            .trim();
        
        // Capitalize first letter if not already capitalized
        if (!cleanMessage.isEmpty() && Character.isLowerCase(cleanMessage.charAt(0))) {
            cleanMessage = Character.toUpperCase(cleanMessage.charAt(0)) + cleanMessage.substring(1);
        }
        
        // Ensure message ends with proper punctuation
        if (!cleanMessage.endsWith(".") && !cleanMessage.endsWith("!") && !cleanMessage.endsWith("?")) {
            cleanMessage += ".";
        }
        
        return cleanMessage;
    }

    /**
     * Checks if an exception is related to network connectivity issues.
     * 
     * @param exception The exception to check
     * @return true if the exception is network-related, false otherwise
     */
    public static boolean isNetworkError(Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            return true;
        }
        
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            return "ERROR_NETWORK_REQUEST_FAILED".equals(errorCode);
        }
        
        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) exception).getCode();
            return code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                   code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED;
        }
        
        String message = exception.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("network") || 
                   lowerMessage.contains("connection") ||
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("host") ||
                   lowerMessage.contains("dns");
        }
        
        return false;
    }

    /**
     * Checks if an exception requires user re-authentication.
     * 
     * @param exception The exception to check
     * @return true if re-authentication is required, false otherwise
     */
    public static boolean requiresReAuthentication(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            return "ERROR_USER_TOKEN_EXPIRED".equals(errorCode) ||
                   "ERROR_INVALID_USER_TOKEN".equals(errorCode) ||
                   "ERROR_REQUIRES_RECENT_LOGIN".equals(errorCode);
        }
        
        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) exception).getCode();
            return code == FirebaseFirestoreException.Code.UNAUTHENTICATED;
        }
        
        return false;
    }

    /**
     * Checks if an exception is retryable (temporary error that might succeed on retry).
     * 
     * @param exception The exception to check
     * @return true if the operation can be retried, false otherwise
     */
    public static boolean isRetryable(Exception exception) {
        if (exception instanceof FirebaseTooManyRequestsException) {
            return true;
        }
        
        if (exception instanceof FirebaseNetworkException) {
            return true;
        }
        
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            return "ERROR_NETWORK_REQUEST_FAILED".equals(errorCode) ||
                   "ERROR_TOO_MANY_REQUESTS".equals(errorCode);
        }
        
        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) exception).getCode();
            return code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                   code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                   code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ||
                   code == FirebaseFirestoreException.Code.ABORTED;
        }
        
        return isNetworkError(exception);
    }
}