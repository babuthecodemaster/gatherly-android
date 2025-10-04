package com.cosmic.gatherly.data.model;

public class Member {
    private String id;
    private String name;
    private String status;
    private String role;
    private boolean isOnline;
    private boolean isSpeaking;
    private boolean isInVoiceChannel;
    private int avatarResource;
    private int statusColor;

    public enum Status {
        ONLINE, AWAY, BUSY, OFFLINE
    }

    public enum Role {
        OWNER, ADMIN, MODERATOR, MEMBER
    }

    // Constructors
    public Member() {}

    public Member(String id, String name, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.isOnline = isOnline;
        this.status = isOnline ? "Online" : "Offline";
    }

    public Member(String id, String name, String status, String role, boolean isOnline, 
                  int avatarResource, int statusColor) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.role = role;
        this.isOnline = isOnline;
        this.avatarResource = avatarResource;
        this.statusColor = statusColor;
        this.isSpeaking = false;
        this.isInVoiceChannel = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }

    public boolean isInVoiceChannel() {
        return isInVoiceChannel;
    }

    public void setInVoiceChannel(boolean inVoiceChannel) {
        isInVoiceChannel = inVoiceChannel;
    }

    public int getAvatarResource() {
        return avatarResource;
    }

    public void setAvatarResource(int avatarResource) {
        this.avatarResource = avatarResource;
    }

    public int getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(int statusColor) {
        this.statusColor = statusColor;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", isOnline=" + isOnline +
                ", isSpeaking=" + isSpeaking +
                ", isInVoiceChannel=" + isInVoiceChannel +
                '}';
    }
}