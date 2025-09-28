package com.cosmic.gatherly.data.response;

import com.google.gson.annotations.SerializedName;

/**
 * Standard error response model for API errors
 */
public class ErrorResponse {
    
    @SerializedName("error")
    private String error;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("details")
    private String details;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }
    
    // Getters
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getCode() { return code; }
    public String getDetails() { return details; }
    public String getTimestamp() { return timestamp; }
    
    // Setters
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setCode(String code) { this.code = code; }
    public void setDetails(String details) { this.details = details; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", details='" + details + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}