package com.cosmic.gatherly.data.response;

public class AuthResponse {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private String status;

    public AuthResponse() {}

    public AuthResponse(String id, String username, String email, String avatar, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
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
}