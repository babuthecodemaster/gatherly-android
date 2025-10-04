package com.cosmic.gatherly.data.util;

import com.cosmic.gatherly.data.model.FileAttachment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileAttachmentConverter {
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<FileAttachment>>(){}.getType();
    
    public static String toJson(List<FileAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        return gson.toJson(attachments);
    }
    
    public static List<FileAttachment> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<FileAttachment> attachments = gson.fromJson(json, listType);
            return attachments != null ? attachments : new ArrayList<>();
        } catch (Exception e) {
            Logger.e("FileAttachmentConverter", "Error parsing JSON: " + json, e);
            return new ArrayList<>();
        }
    }
    
    public static String toJson(FileAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        return gson.toJson(attachment);
    }
    
    public static FileAttachment fromJsonSingle(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return gson.fromJson(json, FileAttachment.class);
        } catch (Exception e) {
            Logger.e("FileAttachmentConverter", "Error parsing single attachment JSON: " + json, e);
            return null;
        }
    }
}