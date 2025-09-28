package com.cosmic.gatherly.data.model;

/**
 * Represents different types of authentication errors with user-friendly messages
 * Updated for incremental build testing
 */
public class AuthError {
    
    public enum Type {
        NETWORK_ERROR,
        TIMEOUT_ERROR,
        SERVER_ERROR,
        VALIDATION_ERROR,
        AUTHENTICATION_ERROR,
        UNKNOWN_ERROR
    }
    
    private Type type;
    private String message;
    private String userFriendlyMessage;
    private int httpCode;
    private Throwable cause;
    
    public AuthError(Type type, String message, String userFriendlyMessage) {
        this.type = type;
        this.message = message;
        this.userFriendlyMessage = userFriendlyMessage;
    }
    
    public AuthError(Type type, String message, String userFriendlyMessage, int httpCode) {
        this.type = type;
        this.message = message;
        this.userFriendlyMessage = userFriendlyMessage;
        this.httpCode = httpCode;
    }
    
    public AuthError(Type type, String message, String userFriendlyMessage, Throwable cause) {
        this.type = type;
        this.message = message;
        this.userFriendlyMessage = userFriendlyMessage;
        this.cause = cause;
    }
    
    public AuthError(Type type, String message, String userFriendlyMessage, int httpCode, Throwable cause) {
        this.type = type;
        this.message = message;
        this.userFriendlyMessage = userFriendlyMessage;
        this.httpCode = httpCode;
        this.cause = cause;
    }
    
    // Getters
    public Type getType() { return type; }
    public String getMessage() { return message; }
    public String getUserFriendlyMessage() { return userFriendlyMessage; }
    public int getHttpCode() { return httpCode; }
    public Throwable getCause() { return cause; }
    
    // Setters
    public void setType(Type type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setUserFriendlyMessage(String userFriendlyMessage) { this.userFriendlyMessage = userFriendlyMessage; }
    public void setHttpCode(int httpCode) { this.httpCode = httpCode; }
    public void setCause(Throwable cause) { this.cause = cause; }
    
    /**
     * Checks if this error is a network-related error
     * @return true if the error is network-related
     */
    public boolean isNetworkError() {
        return type == Type.NETWORK_ERROR || type == Type.TIMEOUT_ERROR;
    }
    
    /**
     * Checks if this error is a server-related error
     * @return true if the error is server-related
     */
    public boolean isServerError() {
        return type == Type.SERVER_ERROR;
    }
    
    @Override
    public String toString() {
        return "AuthError{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", userFriendlyMessage='" + userFriendlyMessage + '\'' +
                ", httpCode=" + httpCode +
                ", cause=" + (cause != null ? cause.getClass().getSimpleName() : "null") +
                '}';
    }
}