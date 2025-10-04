package com.cosmic.gatherly.data.service;

import android.content.Context;
import android.net.Uri;
import com.cosmic.gatherly.data.model.FileAttachment;
import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.network.ApiClient;
import com.cosmic.gatherly.data.request.SendMessageRequest;
import com.cosmic.gatherly.data.util.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private static final String TAG = "MessageService";

    private final Context context;
    private final MessageApi messageApi;
    private final FileUploadService fileUploadService;

    public interface MessageApi {
        @GET("/api/channels/{channelId}/messages")
        Call<List<Message>> getChannelMessages(
                @Path("channelId") String channelId,
                @Query("limit") int limit);

        @POST("/api/channels/{channelId}/messages")
        Call<Message> sendMessage(
                @Path("channelId") String channelId,
                @Body SendMessageRequest request);
    }

    public interface MessageCallback {
        void onSuccess(Message message);

        void onError(String error);
    }

    public interface MessagesCallback {
        void onSuccess(List<Message> messages);

        void onError(String error);
    }

    public interface MessageWithFileCallback {
        void onSuccess(Message message);

        void onError(String error);

        void onUploadProgress(int progress);
    }

    public MessageService(Context context) {
        this.context = context;
        this.messageApi = ApiClient.getInstance().create(MessageApi.class);
        this.fileUploadService = new FileUploadService(context);
    }

    public void getChannelMessages(String channelId, int limit, MessagesCallback callback) {
        Call<List<Message>> call = messageApi.getChannelMessages(channelId, limit);
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load messages: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Logger.e(TAG, "Error loading messages", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void sendTextMessage(String channelId, String content, MessageCallback callback) {
        SendMessageRequest request = new SendMessageRequest(content);
        sendMessage(channelId, request, callback);
    }

    public void sendMessageWithFiles(String channelId, String content, List<Uri> fileUris,
            MessageWithFileCallback callback) {
        if (fileUris == null || fileUris.isEmpty()) {
            // Send text-only message
            MessageCallback textCallback = new MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    callback.onSuccess(message);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            };
            sendTextMessage(channelId, content, textCallback);
            return;
        }

        // Upload files first, then send message with attachments
        uploadFilesSequentially(channelId, fileUris, content, callback);
    }

    private void sendMessage(String channelId, SendMessageRequest request, MessageCallback callback) {
        Call<Message> call = messageApi.sendMessage(channelId, request);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to send message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Logger.e(TAG, "Error sending message", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void uploadFilesSequentially(String channelId, List<Uri> fileUris, String content, MessageWithFileCallback callback) {
        uploadFileAtIndexSimple(channelId, fileUris, 0, new ArrayList<>(), content, callback);
    }

    private void uploadFileAtIndexSimple(String channelId, List<Uri> fileUris, int index,
            ArrayList<FileAttachment> uploadedAttachments, String content, MessageWithFileCallback callback) {
        if (index >= fileUris.size()) {
            // All files uploaded successfully - send message
            SendMessageRequest request = new SendMessageRequest(content);
            request.setFileAttachments(uploadedAttachments);
            sendMessage(channelId, request, new MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    callback.onSuccess(message);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
            return;
        }

        Uri fileUri = fileUris.get(index);
        fileUploadService.uploadFile(fileUri, channelId, new FileUploadService.FileUploadCallback() {
            @Override
            public void onSuccess(FileAttachment attachment) {
                uploadedAttachments.add(attachment);
                // Upload next file
                uploadFileAtIndexSimple(channelId, fileUris, index + 1, uploadedAttachments, content, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to upload file " + (index + 1) + ": " + error);
            }

            @Override
            public void onProgress(int progress) {
                int overallProgress = ((index * 100) + progress) / fileUris.size();
                callback.onUploadProgress(overallProgress);
            }

            @Override
            public void onStarted() {
                // File upload started
            }

            @Override
            public void onRetrying(int attemptNumber) {
                // File upload retrying
            }
        });
    }

    public void downloadFile(FileAttachment attachment, FileDownloadCallback callback) {
        FileDownloadManager downloadManager = new FileDownloadManager(context);
        long downloadId = downloadManager.downloadFile(attachment);

        if (downloadId != -1) {
            callback.onDownloadStarted(downloadId);
        } else {
            callback.onDownloadError("Failed to start download");
        }
    }

    public interface FileDownloadCallback {
        void onDownloadStarted(long downloadId);

        void onDownloadError(String error);
    }

}