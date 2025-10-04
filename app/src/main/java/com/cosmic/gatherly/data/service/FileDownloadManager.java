package com.cosmic.gatherly.data.service;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.URLUtil;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.util.Logger;

public class FileDownloadManager {
    private static final String TAG = "FileDownloadManager";
    
    private final Context context;
    private final DownloadManager downloadManager;
    
    public FileDownloadManager(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public long downloadFile(FileAttachment attachment) {
        try {
            String url = attachment.getUrl();
            String fileName = attachment.getOriginalFileName();
            
            if (!URLUtil.isValidUrl(url)) {
                Logger.e(TAG, "Invalid URL: " + url);
                return -1;
            }
            
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            
            // Set download destination
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            
            // Set notification settings
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("Downloading " + fileName);
            request.setDescription("Downloading file from Gatherly");
            
            // Set allowed network types
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            
            // Set MIME type if available
            if (attachment.getMimeType() != null) {
                request.setMimeType(attachment.getMimeType());
            }
            
            // Add request headers if needed
            request.addRequestHeader("User-Agent", "Gatherly Android App");
            
            // Enqueue the download
            long downloadId = downloadManager.enqueue(request);
            
            Logger.d(TAG, "Download started for file: " + fileName + " with ID: " + downloadId);
            return downloadId;
            
        } catch (Exception e) {
            Logger.e(TAG, "Error starting download", e);
            return -1;
        }
    }
    
    public void cancelDownload(long downloadId) {
        try {
            downloadManager.remove(downloadId);
            Logger.d(TAG, "Download cancelled: " + downloadId);
        } catch (Exception e) {
            Logger.e(TAG, "Error cancelling download", e);
        }
    }
    
    public static class DownloadInfo {
        private final long id;
        private final int status;
        private final int reason;
        private final long bytesDownloaded;
        private final long totalBytes;
        private final String localUri;
        
        public DownloadInfo(long id, int status, int reason, long bytesDownloaded, long totalBytes, String localUri) {
            this.id = id;
            this.status = status;
            this.reason = reason;
            this.bytesDownloaded = bytesDownloaded;
            this.totalBytes = totalBytes;
            this.localUri = localUri;
        }
        
        public long getId() { return id; }
        public int getStatus() { return status; }
        public int getReason() { return reason; }
        public long getBytesDownloaded() { return bytesDownloaded; }
        public long getTotalBytes() { return totalBytes; }
        public String getLocalUri() { return localUri; }
        
        public boolean isCompleted() {
            return status == DownloadManager.STATUS_SUCCESSFUL;
        }
        
        public boolean isFailed() {
            return status == DownloadManager.STATUS_FAILED;
        }
        
        public boolean isRunning() {
            return status == DownloadManager.STATUS_RUNNING;
        }
        
        public boolean isPending() {
            return status == DownloadManager.STATUS_PENDING;
        }
        
        public boolean isPaused() {
            return status == DownloadManager.STATUS_PAUSED;
        }
        
        public int getProgress() {
            if (totalBytes <= 0) return 0;
            return (int) ((bytesDownloaded * 100) / totalBytes);
        }
    }
    
    public DownloadInfo getDownloadInfo(long downloadId) {
        try {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            
            android.database.Cursor cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                
                int status = cursor.getInt(statusIndex);
                int reason = cursor.getInt(reasonIndex);
                long bytesDownloaded = cursor.getLong(bytesDownloadedIndex);
                long totalBytes = cursor.getLong(totalBytesIndex);
                String localUri = cursor.getString(localUriIndex);
                
                cursor.close();
                
                return new DownloadInfo(downloadId, status, reason, bytesDownloaded, totalBytes, localUri);
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
        } catch (Exception e) {
            Logger.e(TAG, "Error getting download info", e);
        }
        
        return null;
    }
}