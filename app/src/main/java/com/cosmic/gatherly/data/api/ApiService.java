package com.cosmic.gatherly.data.api;

import com.cosmic.gatherly.data.model.Channel;
import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.model.Server;
import com.cosmic.gatherly.data.model.ServerMember;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.request.LoginRequest;
import com.cosmic.gatherly.data.request.RegisterRequest;
import com.cosmic.gatherly.data.request.CreateServerRequest;
import com.cosmic.gatherly.data.request.CreateChannelRequest;
import com.cosmic.gatherly.data.request.SendMessageRequest;
import com.cosmic.gatherly.data.response.AuthResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    // Authentication endpoints
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
    
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @POST("api/auth/logout")
    Call<Void> logout();
    
    @GET("api/auth/me")
    Call<AuthResponse> getCurrentUser();
    
    // Server endpoints
    @GET("api/servers")
    Call<List<Server>> getUserServers();
    
    @GET("api/servers/{id}")
    Call<Server> getServerWithChannels(@Path("id") String serverId);
    
    @POST("api/servers")
    Call<Server> createServer(@Body CreateServerRequest request);
    
    @GET("api/servers/{id}/members")
    Call<List<ServerMember>> getServerMembers(@Path("id") String serverId);
    
    // Channel endpoints
    @GET("api/channels/{id}")
    Call<Channel> getChannel(@Path("id") String channelId);
    
    @POST("api/servers/{serverId}/channels")
    Call<Channel> createChannel(@Path("serverId") String serverId, @Body CreateChannelRequest request);
    
    // Message endpoints
    @GET("api/channels/{id}/messages")
    Call<List<Message>> getChannelMessages(@Path("id") String channelId, @Query("limit") Integer limit);
    
    @POST("api/channels/{id}/messages")
    Call<Message> sendMessage(@Path("id") String channelId, @Body SendMessageRequest request);
    
    // File upload endpoint
    @Multipart
    @POST("api/upload")
    Call<FileUploadResponse> uploadFile(
        @Part MultipartBody.Part file,
        @Part("channelId") RequestBody channelId
    );
    
    // File upload response class
    class FileUploadResponse {
        private String id;
        private String fileName;
        private String originalFileName;
        private String fileType;
        private String mimeType;
        private long fileSize;
        private String url;
        private String thumbnailUrl;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    }
}