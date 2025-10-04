package com.cosmic.gatherly.ui.main;

import android.net.Uri;

/**
 * Simple data class to hold file information
 */
public class FileInfo {
    public String name;
    public long size;
    public String mimeType;
    public Uri uri;
    
    public FileInfo() {}
    
    public FileInfo(String name, long size, String mimeType, Uri uri) {
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.uri = uri;
    }
    
    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                ", uri=" + uri +
                '}';
    }
}