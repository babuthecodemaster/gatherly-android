package com.cosmic.gatherly.data.request;

import com.cosmic.gatherly.data.model.FileAttachment;
import java.util.List;

public class SendMessageRequest {
    private String content;
    private List<String> attachments; // Legacy attachment URLs
    private List<FileAttachment> fileAttachments; // New structured file attachments

    public SendMessageRequest(String content) {
        this.content = content;
    }

    public SendMessageRequest(String content, List<String> attachments) {
        this.content = content;
        this.attachments = attachments;
    }
    
    public SendMessageRequest(String content, List<FileAttachment> fileAttachments, boolean isFileAttachment) {
        this.content = content;
        this.fileAttachments = fileAttachments;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
    
    public List<FileAttachment> getFileAttachments() {
        return fileAttachments;
    }
    
    public void setFileAttachments(List<FileAttachment> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }
    
    public boolean hasFileAttachments() {
        return fileAttachments != null && !fileAttachments.isEmpty();
    }
}