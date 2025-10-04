package com.cosmic.gatherly.data.model;

/**
 * Mock FileAttachment class for testing purposes
 */
public class FileAttachment {
    private String fileName;
    private String mimeType;
    private long size;
    private String url;
    private String id;

    public FileAttachment(String id, String fileName, String mimeType, long size, String url) {
        this.id = id;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
        this.url = url;
    }

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public long getSize() { return size; }
    public String getUrl() { return url; }
}