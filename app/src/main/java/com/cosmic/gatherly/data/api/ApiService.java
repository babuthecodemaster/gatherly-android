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

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
}