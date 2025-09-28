package com.cosmic.gatherly.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String username;
    private String email;
    private String avatar;
    private String status;
    private long lastUpdated;
    private boolean isOnline;
    
    public UserEntity() {}
    
    @Ignore
    public UserEntity(@NonNull String id, String username, String email, String avatar, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.lastUpdated = System.currentTimeMillis();
        this.isOnline = false;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
}