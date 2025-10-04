package com.cosmic.gatherly.ui.main;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.service.MessageService;
import com.cosmic.gatherly.ui.adapters.MessageAdapter;
import com.cosmic.gatherly.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to integrate file upload functionality with the message system
 */
public class FileUploadIntegration {
    private static final String TAG = "FileUploadIntegration";
    
    private final Context context;
    private final MessageService messageService;
    private final MessageAdapter messageAdapter;
    private final String currentChannelId;
    
    public FileUploadIntegration(Context context, MessageAdapter messageAdapter, String channelId) {
        this.context = context;
        this.messageService = new MessageService(context);
        this.messageAdapter = messageAdapter;
        this.currentChannelId = channelId;
    }
    
    /**
     * Upload file and send as message
     */
    public void uploadFileAndSendMessage(Uri fileUri, String messageContent, FileUploadCallback callback) {
        List<Uri> fileUris = new ArrayList<>();
        fileUris.add(fileUri);
        
        messageService.sendMessageWithFiles(currentChannelId, messageContent, fileUris, new MessageService.MessageWithFileCallback() {
            @Override
            public void onSuccess(Message message) {
                // Add message to chat UI
                addMessageToChat(message);
                callback.onSuccess("File uploaded and message sent successfully");
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to upload file: " + error);
            }
            
            @Override
            public void onUploadProgress(int progress) {
                callback.onProgress(progress);
            }
        });
    }
    
    /**
     * Add message with file attachments to chat UI
     */
    private void addMessageToChat(Message message) {
        try {
            // Convert Message to MessageAdapter.MessageItem
            MessageAdapter.MessageItem messageItem = new MessageAdapter.MessageItem(
                message.getAuthor() != null ? message.getAuthor().getUsername() : "Unknown User",
                message.getContent() != null ? message.getContent() : "",
                "now",
                R.drawable.ic_person,
                0xFF4ECDC4, // Cosmic blue color
                message.getFileAttachments()
            );
            
            // Add to adapter on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                messageAdapter.addMessage(messageItem);
            });
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error adding message to chat", e);
        }
    }
    
    /**
     * Send text-only message
     */
    public void sendTextMessage(String content, MessageCallback callback) {
        messageService.sendTextMessage(currentChannelId, content, new MessageService.MessageCallback() {
            @Override
            public void onSuccess(Message message) {
                addMessageToChat(message);
                callback.onSuccess("Message sent successfully");
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to send message: " + error);
            }
        });
    }
    
    /**
     * Load messages for current channel
     */
    public void loadChannelMessages(MessagesLoadCallback callback) {
        messageService.getChannelMessages(currentChannelId, 50, new MessageService.MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                // Clear existing messages
                messageAdapter.clearMessages();
                
                // Add loaded messages to chat
                for (Message message : messages) {
                    addMessageToChat(message);
                }
                
                callback.onSuccess(messages.size() + " messages loaded");
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to load messages: " + error);
            }
        });
    }
    
    /**
     * Download file attachment
     */
    public void downloadFile(FileAttachment attachment, FileDownloadCallback callback) {
        messageService.downloadFile(attachment, new MessageService.FileDownloadCallback() {
            @Override
            public void onDownloadStarted(long downloadId) {
                callback.onDownloadStarted(downloadId, "Download started for " + attachment.getOriginalFileName());
            }
            
            @Override
            public void onDownloadError(String error) {
                callback.onDownloadError("Download failed: " + error);
            }
        });
    }
    
    // Callback interfaces
    public interface FileUploadCallback {
        void onSuccess(String message);
        void onError(String error);
        void onProgress(int progress);
    }
    
    public interface MessageCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface MessagesLoadCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface FileDownloadCallback {
        void onDownloadStarted(long downloadId, String message);
        void onDownloadError(String error);
    }
}