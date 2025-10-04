package com.cosmic.gatherly.data.model;

import java.util.Date;

public class SearchResult {
    private String messageId;
    private String content;
    private String authorName;
    private String channelName;
    private String channelId;
    private Date timestamp;
    private int avatarResource;
    private int authorColor;

    // Constructors
    public SearchResult() {}

    public SearchResult(String messageId, String content, String authorName, 
                       String channelName, String channelId, Date timestamp) {
        this.messageId = messageId;
        this.content = content;
        this.authorName = authorName;
        this.channelName = channelName;
        this.channelId = channelId;
        this.timestamp = timestamp;
    }

    public SearchResult(String messageId, String content, String authorName, 
                       String channelName, String channelId, Date timestamp,
                       int avatarResource, int authorColor) {
        this.messageId = messageId;
        this.content = content;
        this.authorName = authorName;
        this.channelName = channelName;
        this.channelId = channelId;
        this.timestamp = timestamp;
        this.avatarResource = avatarResource;
        this.authorColor = authorColor;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getAvatarResource() {
        return avatarResource;
    }

    public void setAvatarResource(int avatarResource) {
        this.avatarResource = avatarResource;
    }

    public int getAuthorColor() {
        return authorColor;
    }

    public void setAuthorColor(int authorColor) {
        this.authorColor = authorColor;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "messageId='" + messageId + '\'' +
                ", content='" + content + '\'' +
                ", authorName='" + authorName + '\'' +
                ", channelName='" + channelName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}