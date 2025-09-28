package com.cosmic.gatherly.data.api;

import com.cosmic.gatherly.data.model.Channel;
import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.model.Server;
import com.cosmic.gatherly.data.model.ServerMember;
import com.cosmic.gatherly.data.request.LoginRequest;
import com.cosmic.gatherly.data.request.RegisterRequest;
import com.cosmic.gatherly.data.request.CreateServerRequest;
import com.cosmic.gatherly.data.request.CreateChannelRequest;
import com.cosmic.gatherly.data.request.SendMessageRequest;
import com.cosmic.gatherly.data.response.AuthResponse;

import java.util.List;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Simplified Mock API service - Firebase Auth will handle authentication
 * This is a minimal stub to avoid compilation errors
 */
public class MockApiService implements ApiService {

    // Simple mock Call implementation
    private static class MockCall<T> implements Call<T> {
        private final T response;

        public MockCall(T response) {
            this.response = response;
        }

        @Override
        public Response<T> execute() {
            return Response.success(response);
        }

        @Override
        public void enqueue(Callback<T> callback) {
            callback.onResponse(this, Response.success(response));
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<T> clone() {
            return new MockCall<>(response);
        }

        @Override
        public okhttp3.Request request() {
            return null;
        }

        @Override
        public okio.Timeout timeout() {
            return okio.Timeout.NONE;
        }
    }

    @Override
    public Call<AuthResponse> login(LoginRequest request) {
        return new MockCall<>(new AuthResponse());
    }

    @Override
    public Call<AuthResponse> register(RegisterRequest request) {
        return new MockCall<>(new AuthResponse());
    }

    @Override
    public Call<Void> logout() {
        return new MockCall<>(null);
    }

    @Override
    public Call<AuthResponse> getCurrentUser() {
        return new MockCall<>(new AuthResponse());
    }

    @Override
    public Call<List<Server>> getUserServers() {
        return new MockCall<>(Collections.emptyList());
    }

    @Override
    public Call<Server> getServerWithChannels(String serverId) {
        return new MockCall<>(null);
    }

    @Override
    public Call<Server> createServer(CreateServerRequest request) {
        return new MockCall<>(null);
    }

    @Override
    public Call<List<ServerMember>> getServerMembers(String serverId) {
        return new MockCall<>(Collections.emptyList());
    }

    @Override
    public Call<Channel> getChannel(String channelId) {
        return new MockCall<>(null);
    }

    @Override
    public Call<Channel> createChannel(String serverId, CreateChannelRequest request) {
        return new MockCall<>(null);
    }

    @Override
    public Call<List<Message>> getChannelMessages(String channelId, Integer limit) {
        return new MockCall<>(Collections.emptyList());
    }

    @Override
    public Call<Message> sendMessage(String channelId, SendMessageRequest request) {
        return new MockCall<>(null);
    }
}