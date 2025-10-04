package com.cosmic.gatherly.ui.util;

import android.content.Context;
import android.net.Uri;
import android.widget.FrameLayout;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.service.FileUploadService;
import com.cosmic.gatherly.data.util.ErrorHandler;
import com.cosmic.gatherly.data.util.Logger;
import com.cosmic.gatherly.ui.components.FileUploadProgressDialog;

/**
 * Enhanced file upload handler with comprehensive error handling and loading states
 * Demonstrates integration of all error handling components
 */
public class EnhancedFileUploadHandler {
    private static final String TAG = "EnhancedFileUploadHandler";
    
    private final Context context;
    private final FileUploadService fileUploadService;
    private ComponentErrorHandler errorHandler;
    private FileUploadProgressDialog progressDialog;
    
    private FileUploadCallback callback;
    
    public interface FileUploadCallback {
        void onUploadSuccess(FileAttachment attachment);
        void onUploadFailed(AuthError error);
        void onUploadCancelled();
    }
    
    public EnhancedFileUploadHandler(Context context, FrameLayout containerView) {
        this.context = context;
        this.fileUploadService = new FileUploadService(context);
        
        // Initialize comprehensive error handler
        this.errorHandler = new ComponentErrorHandler(context, containerView, "FileUpload");
        setupErrorHandlerCallbacks();
    }
    
    private void setupErrorHandlerCallbacks() {
        errorHandler.setComponentErrorCallback(new ComponentErrorHandler.ComponentErrorCallback() {
            @Override
            public void onComponentRecovered() {
                Logger.i(TAG, "File upload component recovered");
                if (callback != null) {
                    // Could retry last upload or just notify recovery
                }
            }
            
            @Override
            public void onComponentFailed(AuthError error) {
                Logger.e(TAG, "File upload component failed: %s", error.getMessage());
                if (callback != null) {
                    callback.onUploadFailed(error);
                }
            }
            
            @Override
            public void onFallbackDataUsed() {
                Logger.d(TAG, "Using fallback mode for file upload");
                // In fallback mode, we might queue uploads for later or show offline message
            }
        });
    }
    
    /**
     * Upload file with comprehensive error handling
     */
    public void uploadFile(Uri fileUri, String channelId, FileUploadCallback callback) {
        this.callback = callback;
        
        Logger.d(TAG, "Starting enhanced file upload for channel: %s", channelId);
        
        // Show progress dialog
        showProgressDialog();
        
        // Execute upload with error handling
        errorHandler.executeWithErrorHandling(() -> {
            performFileUpload(fileUri, channelId);
        }, "File Upload");
    }
    
    private void performFileUpload(Uri fileUri, String channelId) {
        fileUploadService.uploadFile(fileUri, channelId, new FileUploadService.FileUploadCallback() {
            @Override
            public void onStarted() {
                Logger.d(TAG, "File upload started");
                if (progressDialog != null) {
                    progressDialog.showUploading();
                }
            }
            
            @Override
            public void onProgress(int progress) {
                Logger.d(TAG, "File upload progress: %d%%", progress);
                if (progressDialog != null) {
                    progressDialog.setProgress(progress);
                }
            }
            
            @Override
            public void onRetrying(int attemptNumber) {
                Logger.d(TAG, "File upload retrying: attempt %d", attemptNumber);
                if (progressDialog != null) {
                    progressDialog.showRetrying(attemptNumber);
                }
            }
            
            @Override
            public void onSuccess(FileAttachment attachment) {
                Logger.i(TAG, "File upload successful: %s", attachment.getFileName());
                
                hideProgressDialog();
                errorHandler.hideLoading();
                errorHandler.hideError();
                
                if (callback != null) {
                    callback.onUploadSuccess(attachment);
                }
            }
            
            @Override
            public void onError(String error) {
                Logger.e(TAG, "File upload error: %s", error);
                
                // Parse error and handle appropriately
                AuthError authError = new AuthError(
                    AuthError.Type.UNKNOWN_ERROR,
                    error,
                    error
                );
                
                handleUploadError(authError, fileUri, channelId);
            }
        });
    }
    
    private void handleUploadError(AuthError error, Uri fileUri, String channelId) {
        if (progressDialog != null) {
            progressDialog.showError(error.getUserFriendlyMessage());
            
            // Set up retry functionality in dialog
            progressDialog.setOnRetryListener(() -> {
                Logger.d(TAG, "User requested upload retry");
                performFileUpload(fileUri, channelId);
            });
        }
        
        // Use error handler for comprehensive error handling
        errorHandler.handleError(error, () -> {
            performFileUpload(fileUri, channelId);
        }, "File Upload");
    }
    
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new FileUploadProgressDialog(context);
            
            progressDialog.setOnCancelListener(() -> {
                Logger.d(TAG, "User cancelled file upload");
                cancelUpload();
            });
        }
        
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }
    
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    /**
     * Cancel ongoing upload
     */
    public void cancelUpload() {
        Logger.d(TAG, "Cancelling file upload");
        
        hideProgressDialog();
        errorHandler.hideLoading();
        
        if (callback != null) {
            callback.onUploadCancelled();
        }
    }
    
    /**
     * Set file information in progress dialog
     */
    public void setFileInfo(String fileName, String fileSize) {
        if (progressDialog != null) {
            progressDialog.setFileName(fileName);
            progressDialog.setFileSize(fileSize);
        }
    }
    
    /**
     * Check if upload is in progress
     */
    public boolean isUploading() {
        return errorHandler.isLoading() || (progressDialog != null && progressDialog.isShowing());
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        hideProgressDialog();
        errorHandler.cleanup();
    }
    
    /**
     * Get error handler for advanced usage
     */
    public ComponentErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    /**
     * Example usage method showing how to integrate with UI components
     */
    public static void demonstrateUsage(Context context, FrameLayout containerView, Uri fileUri, String channelId) {
        EnhancedFileUploadHandler uploadHandler = new EnhancedFileUploadHandler(context, containerView);
        
        // Set file info (you would get this from the URI)
        uploadHandler.setFileInfo("example.pdf", "2.5 MB");
        
        // Start upload with callback
        uploadHandler.uploadFile(fileUri, channelId, new FileUploadCallback() {
            @Override
            public void onUploadSuccess(FileAttachment attachment) {
                Logger.i(TAG, "Upload completed successfully: %s", attachment.getFileName());
                // Handle successful upload (e.g., add to message list)
            }
            
            @Override
            public void onUploadFailed(AuthError error) {
                Logger.e(TAG, "Upload failed: %s", error.getUserFriendlyMessage());
                // Handle upload failure (e.g., show error message)
            }
            
            @Override
            public void onUploadCancelled() {
                Logger.d(TAG, "Upload was cancelled by user");
                // Handle upload cancellation
            }
        });
    }
}