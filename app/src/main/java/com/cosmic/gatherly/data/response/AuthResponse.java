package com.cosmic.gatherly.data.response;

public class AuthResponse {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private String status;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public AuthResponse() {}

    public AuthResponse(String id, String username, String email, String avatar, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
    }
    
    public AuthResponse(String id, String username, String email, String avatar, String status, 
                       String accessToken, String refreshToken, long expiresIn) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}