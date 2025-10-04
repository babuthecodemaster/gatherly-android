package com.cosmic.gatherly.data.model;

import java.util.Date;

/**
 * Data model representing a user mention in a message
 */
public class Mention {
    private String id;
    private String messageId;
    private String mentionedUserId;
    private String mentionedByUserId;
    private String channelId;
    private String channelName;
    private String messageContent;
    private Date createdAt;
    private boolean isRead;
    private User mentionedUser;
    private User mentionedByUser;

    // Constructors
    public Mention() {}

    public Mention(String id, String messageId, String mentionedUserId, String mentionedByUserId, 
                   String channelId, String channelName, String messageContent) {
        this.id = id;
        this.messageId = messageId;
        this.mentionedUserId = mentionedUserId;
        this.mentionedByUserId = mentionedByUserId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.messageContent = messageContent;
        this.createdAt = new Date();
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMentionedUserId() {
        return mentionedUserId;
    }

    public void setMentionedUserId(String mentionedUserId) {
        this.mentionedUserId = mentionedUserId;
    }

    public String getMentionedByUserId() {
        return mentionedByUserId;
    }

    public void setMentionedByUserId(String mentionedByUserId) {
        this.mentionedByUserId = mentionedByUserId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public User getMentionedUser() {
        return mentionedUser;
    }

    public void setMentionedUser(User mentionedUser) {
        this.mentionedUser = mentionedUser;
    }

    public User getMentionedByUser() {
        return mentionedByUser;
    }

    public void setMentionedByUser(User mentionedByUser) {
        this.mentionedByUser = mentionedByUser;
    }

    /**
     * Get formatted time string for display
     */
    public String getFormattedTime() {
        if (createdAt == null) return "now";
        
        long timeDiff = System.currentTimeMillis() - createdAt.getTime();
        long seconds = timeDiff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (seconds < 60) {
            return "now";
        } else if (minutes < 60) {
            return minutes + "m ago";
        } else if (hours < 24) {
            return hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else {
            return "over a week ago";
        }
    }

    @Override
    public String toString() {
        return "Mention{" +
                "id='" + id + '\'' +
                ", messageId='" + messageId + '\'' +
                ", mentionedUserId='" + mentionedUserId + '\'' +
                ", mentionedByUserId='" + mentionedByUserId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}