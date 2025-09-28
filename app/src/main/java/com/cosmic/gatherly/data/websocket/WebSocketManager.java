package com.cosmic.gatherly.data.websocket;

import android.util.Log;

import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    
    private WebSocketClient webSocketClient;
    private String serverUrl;
    private String authToken;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    
    // Event subjects for reactive programming
    private final PublishSubject<WebSocketEvent> eventSubject = PublishSubject.create();
    private final PublishSubject<Message> messageSubject = PublishSubject.create();
    private final PublishSubject<User> userStatusSubject = PublishSubject.create();
    private final PublishSubject<ConnectionState> connectionSubject = PublishSubject.create();
    
    // Listeners
    private final CopyOnWriteArrayList<WebSocketListener> listeners = new CopyOnWriteArrayList<>();
    
    private final Gson gson = new Gson();
    
    public enum ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED, ERROR
    }
    
    public interface WebSocketListener {
        void onMessageReceived(Message message);
        void onUserStatusChanged(User user);
        void onConnectionStateChanged(ConnectionState state);
        void onError(String error);
    }
    
    public static class WebSocketEvent {
        public String type;
        public Object data;
        
        public WebSocketEvent(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }
    
    private WebSocketManager() {}
    
    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }
    
    public void initialize(String serverUrl, String authToken) {
        this.serverUrl = serverUrl;
        this.authToken = authToken;
        Log.d(TAG, "WebSocket initialized with server: " + serverUrl);
    }
    
    public void connect() {
        if (isConnected || isConnecting) {
            Log.d(TAG, "Already connected or connecting");
            return;
        }
        
        if (serverUrl == null || authToken == null) {
            Log.e(TAG, "Server URL or auth token not set");
            notifyConnectionState(ConnectionState.ERROR);
            return;
        }
        
        try {
            isConnecting = true;
            notifyConnectionState(ConnectionState.CONNECTING);
            
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket connected");
                    isConnected = true;
                    isConnecting = false;
                    notifyConnectionState(ConnectionState.CONNECTED);
                    
                    // Send authentication
                    sendAuth();
                }
                
                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);
                    handleIncomingMessage(message);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason);
                    isConnected = false;
                    isConnecting = false;
                    notifyConnectionState(ConnectionState.DISCONNECTED);
                }
                
                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    isConnected = false;
                    isConnecting = false;
                    notifyConnectionState(ConnectionState.ERROR);
                    notifyError("Connection error: " + ex.getMessage());
                }
            };
            
            // Add auth header
            webSocketClient.addHeader("Authorization", "Bearer " + authToken);
            webSocketClient.connect();
            
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to WebSocket", e);
            isConnecting = false;
            notifyConnectionState(ConnectionState.ERROR);
            notifyError("Failed to connect: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        isConnected = false;
        isConnecting = false;
        notifyConnectionState(ConnectionState.DISCONNECTED);
        Log.d(TAG, "WebSocket disconnected");
    }
    
    public void sendMessage(String channelId, String content) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send message - not connected");
            return;
        }
        
        try {
            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("type", "message");
            messageObj.addProperty("channelId", channelId);
            messageObj.addProperty("content", content);
            messageObj.addProperty("timestamp", System.currentTimeMillis());
            
            String jsonMessage = gson.toJson(messageObj);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Message sent: " + jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            notifyError("Failed to send message: " + e.getMessage());
        }
    }
    
    public void sendUserStatus(String status) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send status - not connected");
            return;
        }
        
        try {
            JsonObject statusObj = new JsonObject();
            statusObj.addProperty("type", "status");
            statusObj.addProperty("status", status);
            
            String jsonMessage = gson.toJson(statusObj);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Status sent: " + jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending status", e);
        }
    }
    
    public void joinChannel(String channelId) {
        if (!isConnected) {
            Log.w(TAG, "Cannot join channel - not connected");
            return;
        }
        
        try {
            JsonObject joinObj = new JsonObject();
            joinObj.addProperty("type", "join");
            joinObj.addProperty("channelId", channelId);
            
            String jsonMessage = gson.toJson(joinObj);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Joined channel: " + channelId);
        } catch (Exception e) {
            Log.e(TAG, "Error joining channel", e);
        }
    }
    
    public void leaveChannel(String channelId) {
        if (!isConnected) {
            return;
        }
        
        try {
            JsonObject leaveObj = new JsonObject();
            leaveObj.addProperty("type", "leave");
            leaveObj.addProperty("channelId", channelId);
            
            String jsonMessage = gson.toJson(leaveObj);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Left channel: " + channelId);
        } catch (Exception e) {
            Log.e(TAG, "Error leaving channel", e);
        }
    }
    
    private void sendAuth() {
        try {
            JsonObject authObj = new JsonObject();
            authObj.addProperty("type", "auth");
            authObj.addProperty("token", authToken);
            
            String jsonMessage = gson.toJson(authObj);
            webSocketClient.send(jsonMessage);
            Log.d(TAG, "Authentication sent");
        } catch (Exception e) {
            Log.e(TAG, "Error sending authentication", e);
        }
    }
    
    private void handleIncomingMessage(String jsonMessage) {
        try {
            JsonObject messageObj = JsonParser.parseString(jsonMessage).getAsJsonObject();
            String type = messageObj.get("type").getAsString();
            
            switch (type) {
                case "message":
                    handleNewMessage(messageObj);
                    break;
                case "user_status":
                    handleUserStatus(messageObj);
                    break;
                case "auth_success":
                    Log.d(TAG, "Authentication successful");
                    break;
                case "auth_error":
                    Log.e(TAG, "Authentication failed");
                    notifyError("Authentication failed");
                    break;
                case "error":
                    String error = messageObj.get("message").getAsString();
                    Log.e(TAG, "Server error: " + error);
                    notifyError(error);
                    break;
                default:
                    Log.d(TAG, "Unknown message type: " + type);
                    break;
            }
            
            // Emit generic event
            eventSubject.onNext(new WebSocketEvent(type, messageObj));
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling incoming message", e);
        }
    }
    
    private void handleNewMessage(JsonObject messageObj) {
        try {
            Message message = gson.fromJson(messageObj, Message.class);
            messageSubject.onNext(message);
            
            for (WebSocketListener listener : listeners) {
                listener.onMessageReceived(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling new message", e);
        }
    }
    
    private void handleUserStatus(JsonObject statusObj) {
        try {
            User user = gson.fromJson(statusObj.get("user"), User.class);
            userStatusSubject.onNext(user);
            
            for (WebSocketListener listener : listeners) {
                listener.onUserStatusChanged(user);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling user status", e);
        }
    }
    
    private void notifyConnectionState(ConnectionState state) {
        connectionSubject.onNext(state);
        for (WebSocketListener listener : listeners) {
            listener.onConnectionStateChanged(state);
        }
    }
    
    private void notifyError(String error) {
        for (WebSocketListener listener : listeners) {
            listener.onError(error);
        }
    }
    
    // Public methods for reactive programming
    public Observable<WebSocketEvent> getEventObservable() {
        return eventSubject;
    }
    
    public Observable<Message> getMessageObservable() {
        return messageSubject;
    }
    
    public Observable<User> getUserStatusObservable() {
        return userStatusSubject;
    }
    
    public Observable<ConnectionState> getConnectionObservable() {
        return connectionSubject;
    }
    
    // Listener management
    public void addListener(WebSocketListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }
    
    public void clearListeners() {
        listeners.clear();
    }
    
    // Getters
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isConnecting() {
        return isConnecting;
    }
}