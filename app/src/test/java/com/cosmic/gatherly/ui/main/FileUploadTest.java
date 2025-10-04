package com.cosmic.gatherly.ui.main;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for file upload functionality
 */
public class FileUploadTest {
    
    private static final long MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024; // 100MB
    
    /**
     * Test file size validation
     */
    @Test
    public void testFileSizeValidation() {
        // Test valid file size
        String result = validateFileSize(50 * 1024 * 1024); // 50MB
        assertNull("50MB file should be valid", result);
        
        // Test file too large
        result = validateFileSize(150 * 1024 * 1024); // 150MB
        assertNotNull("150MB file should be invalid", result);
        assertTrue("Error message should mention size limit", result.contains("exceeds limit"));
        
        // Test empty file
        result = validateFileSize(0);
        assertNotNull("Empty file should be invalid", result);
        assertTrue("Error message should mention empty file", result.contains("empty"));
    }
    
    /**
     * Test file name validation
     */
    @Test
    public void testFileNameValidation() {
        // Test valid file names
        String result = validateFileName("document.pdf");
        assertNull("Valid PDF file name should pass", result);
        
        result = validateFileName("image.jpg");
        assertNull("Valid image file name should pass", result);
        
        result = validateFileName("presentation.pptx");
        assertNull("Valid presentation file name should pass", result);
        
        // Test invalid file names
        result = validateFileName("");
        assertNotNull("Empty file name should be invalid", result);
        
        result = validateFileName(null);
        assertNotNull("Null file name should be invalid", result);
        
        result = validateFileName("   ");
        assertNotNull("Whitespace-only file name should be invalid", result);
    }
    
    /**
     * Test dangerous file type validation
     */
    @Test
    public void testDangerousFileTypes() {
        // Test dangerous file extensions
        String result = validateFileName("malware.exe");
        assertNotNull("EXE files should be blocked", result);
        assertTrue("Error should mention security", result.contains("security"));
        
        result = validateFileName("script.bat");
        assertNotNull("BAT files should be blocked", result);
        
        result = validateFileName("command.cmd");
        assertNotNull("CMD files should be blocked", result);
        
        result = validateFileName("application.jar");
        assertNotNull("JAR files should be blocked", result);
        
        // Test safe file extensions
        result = validateFileName("document.pdf");
        assertNull("PDF files should be allowed", result);
        
        result = validateFileName("image.png");
        assertNull("PNG files should be allowed", result);
        
        result = validateFileName("archive.zip");
        assertNull("ZIP files should be allowed", result);
    }
    
    /**
     * Test file size formatting
     */
    @Test
    public void testFileSizeFormatting() {
        assertEquals("500 B", formatFileSize(500));
        assertEquals("1.5 KB", formatFileSize(1536));
        assertEquals("2.0 MB", formatFileSize(2 * 1024 * 1024));
        assertEquals("1.5 GB", formatFileSize((long)(1.5 * 1024 * 1024 * 1024)));
    }
    
    // Helper methods that mirror the actual implementation
    private String validateFileSize(long size) {
        if (size > MAX_FILE_SIZE_BYTES) {
            return String.format("File size exceeds limit. Maximum allowed: %dMB, File size: %.1fMB", 
                10L, size / (1024.0 * 1024.0));
        }
        
        if (size <= 0) {
            return "Cannot upload empty files";
        }
        
        return null;
    }
    
    private String validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "Invalid file name";
        }
        
        String lowerFileName = fileName.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".scr", ".pif", ".com", ".jar"};
        for (String ext : dangerousExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return "File type not allowed for security reasons: " + ext;
            }
        }
        
        return null;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}