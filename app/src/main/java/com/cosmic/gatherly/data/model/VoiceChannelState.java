package com.cosmic.gatherly.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of a voice channel connection
 */
public class VoiceChannelState {
    private String channelId;
    private boolean isConnected;
    private boolean isMuted;
    private boolean isDeafened;
    private boolean isSpeaking;
    private ConnectionQuality connectionQuality;
    private List<VoiceUser> connectedUsers;

    public enum ConnectionQuality {
        EXCELLENT("excellent"),
        GOOD("good"),
        POOR("poor"),
        DISCONNECTED("disconnected");

        private final String value;

        ConnectionQuality(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class VoiceUser {
        private String userId;
        private String username;
        private boolean isMuted;
        private boolean isDeafened;
        private boolean isSpeaking;

        public VoiceUser(String userId, String username) {
            this.userId = userId;
            this.username = username;
            this.isMuted = false;
            this.isDeafened = false;
            this.isSpeaking = false;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public boolean isMuted() { return isMuted; }
        public void setMuted(boolean muted) { isMuted = muted; }

        public boolean isDeafened() { return isDeafened; }
        public void setDeafened(boolean deafened) { isDeafened = deafened; }

        public boolean isSpeaking() { return isSpeaking; }
        public void setSpeaking(boolean speaking) { isSpeaking = speaking; }
    }

    // Constructors
    public VoiceChannelState() {
        this.connectedUsers = new ArrayList<>();
        this.connectionQuality = ConnectionQuality.DISCONNECTED;
    }

    public VoiceChannelState(String channelId) {
        this.channelId = channelId;
        this.isConnected = false;
        this.isMuted = false;
        this.isDeafened = false;
        this.isSpeaking = false;
        this.connectionQuality = ConnectionQuality.DISCONNECTED;
        this.connectedUsers = new ArrayList<>();
    }

    // Getters and setters
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public boolean isDeafened() { return isDeafened; }
    public void setDeafened(boolean deafened) { isDeafened = deafened; }

    public boolean isSpeaking() { return isSpeaking; }
    public void setSpeaking(boolean speaking) { isSpeaking = speaking; }

    public ConnectionQuality getConnectionQuality() { return connectionQuality; }
    public void setConnectionQuality(ConnectionQuality connectionQuality) { this.connectionQuality = connectionQuality; }

    public List<VoiceUser> getConnectedUsers() { return connectedUsers; }
    public void setConnectedUsers(List<VoiceUser> connectedUsers) { this.connectedUsers = connectedUsers; }

    // Helper methods
    public void addUser(VoiceUser user) {
        if (!connectedUsers.contains(user)) {
            connectedUsers.add(user);
        }
    }

    public void removeUser(String userId) {
        connectedUsers.removeIf(user -> user.getUserId().equals(userId));
    }

    public VoiceUser getUser(String userId) {
        return connectedUsers.stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public int getUserCount() {
        return connectedUsers.size();
    }

    @Override
    public String toString() {
        return "VoiceChannelState{" +
                "channelId='" + channelId + '\'' +
                ", isConnected=" + isConnected +
                ", isMuted=" + isMuted +
                ", isDeafened=" + isDeafened +
                ", isSpeaking=" + isSpeaking +
                ", connectionQuality=" + connectionQuality +
                ", connectedUsers=" + connectedUsers.size() +
                '}';
    }
}