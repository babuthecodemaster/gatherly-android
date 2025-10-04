package com.cosmic.gatherly.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

@Entity(
    tableName = "messages",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "channelId"),
        @Index(value = "userId"),
        @Index(value = "timestamp")
    }
)
public class MessageEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String content;
    private String channelId;
    private String userId;
    private String username;
    private String userAvatar;
    private long timestamp;
    private boolean isSent;
    private boolean isDelivered;
    private boolean isRead;
    private String messageType; // text, image, file, etc.
    private String attachmentUrl;
    private String attachmentData; // JSON string for file attachment details
    
    public MessageEntity() {}
    
    @Ignore
    public MessageEntity(@NonNull String id, String content, String channelId, String userId, 
                        String username, long timestamp) {
        this.id = id;
        this.content = content;
        this.channelId = channelId;
        this.userId = userId;
        this.username = username;
        this.timestamp = timestamp;
        this.isSent = false;
        this.isDelivered = false;
        this.isRead = false;
        this.messageType = "text";
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }
    
    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public String getAttachmentData() { return attachmentData; }
    public void setAttachmentData(String attachmentData) { this.attachmentData = attachmentData; }
}