package com.cosmic.gatherly.data.model;

import java.util.Date;

public class FileAttachment {
    private String id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private String mimeType;
    private long fileSize;
    private String url;
    private String thumbnailUrl;
    private Date uploadedAt;
    private String uploadedBy;
    
    // Constructors
    public FileAttachment() {}
    
    public FileAttachment(String id, String fileName, String originalFileName, 
                         String fileType, String mimeType, long fileSize, String url) {
        this.id = id;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.url = url;
        this.uploadedAt = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public Date getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public String getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    // Utility methods
    public boolean isImage() {
        return fileType != null && fileType.equals("image");
    }
    
    public boolean isDocument() {
        return fileType != null && (fileType.equals("document") || fileType.equals("pdf"));
    }
    
    public boolean isVideo() {
        return fileType != null && fileType.equals("video");
    }
    
    public boolean isAudio() {
        return fileType != null && fileType.equals("audio");
    }
    
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return "FileAttachment{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", url='" + url + '\'' +
                '}';
    }
}