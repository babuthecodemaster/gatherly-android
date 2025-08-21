package com.cosmic.gatherly.data.model;

import java.util.Date;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private UserStatus status;
    private Date createdAt;

    public enum UserStatus {
        ONLINE("online"),
        OFFLINE("offline"),
        AWAY("away"),
        BUSY("busy");

        private final String value;

        UserStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserStatus fromString(String status) {
            for (UserStatus s : UserStatus.values()) {
                if (s.value.equals(status)) {
                    return s;
                }
            }
            return OFFLINE;
        }
    }

    // Constructors
    public User() {}

    public User(String id, String username, String email, String avatar, UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status != null ? status : UserStatus.OFFLINE;
        this.createdAt = new Date();
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}