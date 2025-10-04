package com.cosmic.gatherly.data.model;

/**
 * Mock SearchResult class for testing purposes
 */
public class SearchResult {
    private String content;
    private String author;
    private long timestamp;
    private String channelId;
    private String messageId;

    public SearchResult(String messageId, String content, String author, long timestamp, String channelId) {
        this.messageId = messageId;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.channelId = channelId;
    }

    public String getMessageId() { return messageId; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public long getTimestamp() { return timestamp; }
    public String getChannelId() { return channelId; }
}