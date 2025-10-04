package com.cosmic.gatherly.data.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import com.cosmic.gatherly.data.api.ApiClient;
import com.cosmic.gatherly.data.api.ApiService;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.util.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FileUploadService {
    private static final String TAG = "FileUploadService";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    
    private final Context context;
    private final ApiService apiService;
    
    public interface FileUploadCallback {
        void onSuccess(FileAttachment attachment);
        void onError(String error);
        void onProgress(int progress);
        void onStarted();
        void onRetrying(int attemptNumber);
    }
    
    public FileUploadService(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }
    
    public void uploadFile(Uri fileUri, String channelId, FileUploadCallback callback) {
        uploadFileWithRetry(fileUri, channelId, callback, 1, 3);
    }
    
    private void uploadFileWithRetry(Uri fileUri, String channelId, FileUploadCallback callback, int attemptNumber, int maxRetries) {
        try {
            Logger.d(TAG, "Starting file upload attempt %d/%d", attemptNumber, maxRetries);
            
            if (attemptNumber == 1) {
                callback.onStarted();
            } else {
                callback.onRetrying(attemptNumber);
            }
            
            // Validate file
            String error = validateFile(fileUri);
            if (error != null) {
                callback.onError(error);
                return;
            }
            
            // Create temporary file
            File tempFile = createTempFile(fileUri);
            if (tempFile == null) {
                callback.onError("Failed to create temporary file");
                return;
            }
            
            // Get file info
            String mimeType = getMimeType(fileUri);
            String fileName = getFileName(fileUri);
            
            Logger.d(TAG, "Uploading file: %s, size: %d bytes, type: %s", fileName, tempFile.length(), mimeType);
            
            // Create request body with progress tracking
            RequestBody requestFile = new ProgressRequestBody(MediaType.parse(mimeType), tempFile, callback);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);
            RequestBody channelIdBody = RequestBody.create(MediaType.parse("text/plain"), channelId);
            
            // Upload file
            Call<ApiService.FileUploadResponse> call = apiService.uploadFile(filePart, channelIdBody);
            call.enqueue(new Callback<ApiService.FileUploadResponse>() {
                @Override
                public void onResponse(Call<ApiService.FileUploadResponse> call, Response<ApiService.FileUploadResponse> response) {
                    // Clean up temp file
                    tempFile.delete();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Logger.i(TAG, "File upload successful on attempt %d", attemptNumber);
                        FileAttachment attachment = convertToFileAttachment(response.body());
                        callback.onSuccess(attachment);
                    } else {
                        Logger.w(TAG, "File upload failed on attempt %d: HTTP %d - %s", 
                            attemptNumber, response.code(), response.message());
                        
                        if (shouldRetryUpload(response.code()) && attemptNumber < maxRetries) {
                            // Retry after delay
                            long delay = calculateRetryDelay(attemptNumber);
                            Logger.d(TAG, "Retrying file upload in %d ms", delay);
                            
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                uploadFileWithRetry(fileUri, channelId, callback, attemptNumber + 1, maxRetries);
                            }, delay);
                        } else {
                            String errorMessage = parseUploadErrorMessage(response);
                            callback.onError(errorMessage);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<ApiService.FileUploadResponse> call, Throwable t) {
                    // Clean up temp file
                    tempFile.delete();
                    
                    Logger.e(TAG, "File upload failed on attempt %d", attemptNumber, t);
                    
                    if (shouldRetryUpload(t) && attemptNumber < maxRetries) {
                        // Retry after delay
                        long delay = calculateRetryDelay(attemptNumber);
                        Logger.d(TAG, "Retrying file upload in %d ms due to: %s", delay, t.getMessage());
                        
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            uploadFileWithRetry(fileUri, channelId, callback, attemptNumber + 1, maxRetries);
                        }, delay);
                    } else {
                        String errorMessage = parseUploadErrorMessage(t);
                        callback.onError(errorMessage);
                    }
                }
            });
            
        } catch (Exception e) {
            Logger.e(TAG, "Error during file upload attempt %d", attemptNumber, e);
            
            if (attemptNumber < maxRetries) {
                long delay = calculateRetryDelay(attemptNumber);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    uploadFileWithRetry(fileUri, channelId, callback, attemptNumber + 1, maxRetries);
                }, delay);
            } else {
                callback.onError("Error uploading file: " + e.getMessage());
            }
        }
    }
    
    private String validateFile(Uri fileUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return "Cannot access file";
            }
            
            // Check file size
            long fileSize = inputStream.available();
            inputStream.close();
            
            if (fileSize > MAX_FILE_SIZE) {
                return "File size exceeds 100MB limit";
            }
            
            // Check file type
            String mimeType = getMimeType(fileUri);
            if (!isAllowedFileType(mimeType)) {
                return "File type not supported";
            }
            
            return null; // No error
        } catch (IOException e) {
            return "Error reading file";
        }
    }
    
    private boolean isAllowedFileType(String mimeType) {
        if (mimeType == null) return false;
        
        return mimeType.startsWith("image/") ||
               mimeType.startsWith("video/") ||
               mimeType.startsWith("audio/") ||
               mimeType.equals("application/pdf") ||
               mimeType.equals("text/plain") ||
               mimeType.startsWith("application/msword") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument");
    }
    
    private File createTempFile(Uri fileUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;
            
            String fileName = "temp_" + UUID.randomUUID().toString();
            File tempFile = new File(context.getCacheDir(), fileName);
            
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            return tempFile;
        } catch (IOException e) {
            Logger.e(TAG, "Error creating temp file", e);
            return null;
        }
    }
    
    private String getMimeType(Uri fileUri) {
        String mimeType = context.getContentResolver().getType(fileUri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }
    
    private String getFileName(Uri fileUri) {
        String fileName = null;
        
        // Try to get the actual file name from content resolver
        try (Cursor cursor = context.getContentResolver().query(fileUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Error getting file name from cursor", e);
        }
        
        // Fallback to last path segment
        if (fileName == null) {
            fileName = fileUri.getLastPathSegment();
        }
        
        // Final fallback
        if (fileName == null) {
            fileName = "unknown_file";
        }
        
        return fileName;
    }
    
    private String getFileTypeFromMimeType(String mimeType) {
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
    
    private FileAttachment convertToFileAttachment(ApiService.FileUploadResponse response) {
        FileAttachment attachment = new FileAttachment();
        attachment.setId(response.getId());
        attachment.setFileName(response.getFileName());
        attachment.setOriginalFileName(response.getOriginalFileName());
        attachment.setFileType(response.getFileType());
        attachment.setMimeType(response.getMimeType());
        attachment.setFileSize(response.getFileSize());
        attachment.setUrl(response.getUrl());
        attachment.setThumbnailUrl(response.getThumbnailUrl());
        return attachment;
    }
    
    /**
     * Check if upload should be retried based on HTTP status code
     */
    private boolean shouldRetryUpload(int httpCode) {
        return httpCode == 408 || // Request Timeout
               httpCode == 429 || // Too Many Requests
               httpCode == 500 || // Internal Server Error
               httpCode == 502 || // Bad Gateway
               httpCode == 503 || // Service Unavailable
               httpCode == 504;   // Gateway Timeout
    }
    
    /**
     * Check if upload should be retried based on exception
     */
    private boolean shouldRetryUpload(Throwable t) {
        return t instanceof java.net.SocketTimeoutException ||
               t instanceof java.net.ConnectException ||
               t instanceof java.net.UnknownHostException ||
               t instanceof java.io.IOException;
    }
    
    /**
     * Calculate retry delay with exponential backoff
     */
    private long calculateRetryDelay(int attemptNumber) {
        long baseDelay = 2000; // 2 seconds
        long delay = baseDelay * (long) Math.pow(2, attemptNumber - 1);
        long jitter = (long) (Math.random() * 1000); // Add up to 1 second jitter
        return Math.min(delay + jitter, 15000); // Cap at 15 seconds
    }
    
    /**
     * Parse error message from HTTP response
     */
    private String parseUploadErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                if (errorBody.length() < 200) {
                    return "Upload failed: " + errorBody;
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Error parsing error response", e);
        }
        
        switch (response.code()) {
            case 400:
                return "Invalid file or request. Please check the file and try again.";
            case 401:
                return "Authentication required. Please log in and try again.";
            case 403:
                return "Permission denied. You don't have access to upload files here.";
            case 413:
                return "File too large. Please choose a smaller file.";
            case 415:
                return "File type not supported. Please choose a different file.";
            case 429:
                return "Too many uploads. Please wait a moment and try again.";
            case 500:
            case 502:
            case 503:
            case 504:
                return "Server error. Please try again later.";
            default:
                return "Upload failed with error code " + response.code() + ". Please try again.";
        }
    }
    
    /**
     * Parse error message from exception
     */
    private String parseUploadErrorMessage(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Upload timed out. Please check your connection and try again.";
        } else if (t instanceof java.net.ConnectException) {
            return "Cannot connect to server. Please check your connection and try again.";
        } else if (t instanceof java.net.UnknownHostException) {
            return "Cannot reach server. Please check your internet connection.";
        } else if (t instanceof java.io.IOException) {
            return "Network error during upload. Please try again.";
        } else {
            return "Upload failed: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
        }
    }
    
    /**
     * Progress tracking request body
     */
    private static class ProgressRequestBody extends RequestBody {
        private final MediaType mediaType;
        private final File file;
        private final FileUploadCallback callback;
        
        public ProgressRequestBody(MediaType mediaType, File file, FileUploadCallback callback) {
            this.mediaType = mediaType;
            this.file = file;
            this.callback = callback;
        }
        
        @Override
        public MediaType contentType() {
            return mediaType;
        }
        
        @Override
        public long contentLength() {
            return file.length();
        }
        
        @Override
        public void writeTo(okio.BufferedSink sink) throws IOException {
            long fileLength = file.length();
            byte[] buffer = new byte[8192];
            long uploaded = 0;
            
            try (java.io.FileInputStream inputStream = new java.io.FileInputStream(file)) {
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                    uploaded += read;
                    
                    // Update progress
                    int progress = (int) ((uploaded * 100) / fileLength);
                    if (callback != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            callback.onProgress(progress);
                        });
                    }
                }
            }
        }
    }
}
