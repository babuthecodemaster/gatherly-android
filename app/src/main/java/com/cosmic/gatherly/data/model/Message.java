package com.cosmic.gatherly.data.model;

import java.util.Date;
import java.util.List;

public class Message {
    private String id;
    private String content;
    private String authorId;
    private String channelId;
    private List<String> attachments;
    private List<String> reactions;
    private Date createdAt;
    private User author; // For MessageWithAuthor

    // Constructors
    public Message() {}

    public Message(String id, String content, String authorId, String channelId) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.channelId = channelId;
        this.createdAt = new Date();
    }

    public Message(String id, String content, String authorId, String channelId, 
                   List<String> attachments, List<String> reactions) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.channelId = channelId;
        this.attachments = attachments;
        this.reactions = reactions;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public List<String> getReactions() {
        return reactions;
    }

    public void setReactions(List<String> reactions) {
        this.reactions = reactions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", authorId='" + authorId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}