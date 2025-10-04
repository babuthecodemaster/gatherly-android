package com.cosmic.gatherly.data.util;

import java.text.DecimalFormat;

/**
 * Utility class for file operations
 */
public class FileUtils {
    
    /**
     * Format file size in bytes to human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return new DecimalFormat("#.#").format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }
    
    /**
     * Get file type from MIME type
     */
    public static String getFileTypeFromMimeType(String mimeType) {
        if (mimeType == null) return "unknown";
        
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.equals("application/pdf")) return "pdf";
        if (mimeType.startsWith("text/")) return "text";
        if (mimeType.startsWith("application/msword") || 
            mimeType.startsWith("application/vnd.openxmlformats-officedocument")) return "document";
        
        return "file";
    }
    
    /**
     * Check if file type is allowed for upload
     */
    public static boolean isAllowedFileType(String mimeType) {
        if (mimeType == null) return false;
        
        return mimeType.startsWith("image/") ||
               mimeType.startsWith("video/") ||
               mimeType.startsWith("audio/") ||
               mimeType.equals("application/pdf") ||
               mimeType.equals("text/plain") ||
               mimeType.startsWith("application/msword") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument");
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}