package com.cosmic.gatherly.ui.util;

import android.content.Context;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.Channel;
import com.cosmic.gatherly.data.model.Message;
import com.cosmic.gatherly.data.model.SearchResult;
import com.cosmic.gatherly.data.model.Server;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.util.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides fallback data when network requests fail or data is missing
 * Ensures the app remains functional even when backend services are unavailable
 */
public class FallbackDataProvider {
    private static final String TAG = "FallbackDataProvider";
    
    private final Context context;
    
    public FallbackDataProvider(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Get fallback servers when server list fails to load
     */
    public List<Server> getFallbackServers() {
        Logger.d(TAG, "Providing fallback servers");
        
        List<Server> servers = new ArrayList<>();
        
        // Default server
        Server defaultServer = new Server();
        defaultServer.setId("fallback-server");
        defaultServer.setName("Cosmic Hub");
        defaultServer.setDescription("Default server (offline mode)");
        defaultServer.setIconUrl(null);
        defaultServer.setMemberCount(1);
        defaultServer.setOnline(false);
        servers.add(defaultServer);
        
        return servers;
    }
    
    /**
     * Get fallback channels when channel list fails to load
     */
    public List<Channel> getFallbackChannels(String serverId) {
        Logger.d(TAG, "Providing fallback channels for server: %s", serverId);
        
        List<Channel> channels = new ArrayList<>();
        
        // General text channel
        Channel generalChannel = new Channel();
        generalChannel.setId("fallback-general");
        generalChannel.setName("general");
        generalChannel.setType(Channel.ChannelType.TEXT);
        generalChannel.setServerId(serverId);
        generalChannel.setDescription("General discussion (offline mode)");
        channels.add(generalChannel);
        
        // Announcements channel
        Channel announcementsChannel = new Channel();
        announcementsChannel.setId("fallback-announcements");
        announcementsChannel.setName("announcements");
        announcementsChannel.setType(Channel.ChannelType.TEXT);
        announcementsChannel.setServerId(serverId);
        announcementsChannel.setDescription("Server announcements (offline mode)");
        channels.add(announcementsChannel);
        
        return channels;
    }
    
    /**
     * Get fallback messages when message loading fails
     */
    public List<Message> getFallbackMessages(String channelId) {
        Logger.d(TAG, "Providing fallback messages for channel: %s", channelId);
        
        List<Message> messages = new ArrayList<>();
        
        // Welcome message
        Message welcomeMessage = new Message();
        welcomeMessage.setId("fallback-welcome");
        welcomeMessage.setContent("Welcome to Cosmic Gatherly! 🚀\n\nYou're currently in offline mode. Some features may be limited until connection is restored.");
        welcomeMessage.setAuthorId("system");
        welcomeMessage.setAuthorName("System");
        welcomeMessage.setChannelId(channelId);
        welcomeMessage.setTimestamp(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        messages.add(welcomeMessage);
        
        // Status message
        Message statusMessage = new Message();
        statusMessage.setId("fallback-status");
        statusMessage.setContent("Connection to server is currently unavailable. Your messages will be synced when connection is restored.");
        statusMessage.setAuthorId("system");
        statusMessage.setAuthorName("System");
        statusMessage.setChannelId(channelId);
        statusMessage.setTimestamp(new Date(System.currentTimeMillis() - 1800000)); // 30 minutes ago
        messages.add(statusMessage);
        
        return messages;
    }
    
    /**
     * Get fallback search results when search fails
     */
    public List<SearchResult> getFallbackSearchResults(String query) {
        Logger.d(TAG, "Providing fallback search results for query: %s", query);
        
        List<SearchResult> results = new ArrayList<>();
        
        // No results message
        SearchResult noResultsMessage = new SearchResult(
            "fallback-no-results",
            "Search is currently unavailable in offline mode. Please try again when connection is restored.",
            "System",
            "system",
            "system",
            new Date(),
            R.drawable.ic_search,
            0xFF5865F2
        );
        results.add(noResultsMessage);
        
        return results;
    }
    
    /**
     * Get fallback user data when user profile fails to load
     */
    public User getFallbackUser(String userId) {
        Logger.d(TAG, "Providing fallback user data for: %s", userId);
        
        User user = new User();
        user.setId(userId);
        user.setUsername("Unknown User");
        user.setEmail("offline@cosmic.app");
        user.setDisplayName("Offline User");
        user.setAvatarUrl(null);
        user.setOnline(false);
        user.setLastSeen(new Date());
        
        return user;
    }
    
    /**
     * Get fallback error message for display
     */
    public String getFallbackErrorMessage(String operation) {
        Logger.d(TAG, "Providing fallback error message for operation: %s", operation);
        
        switch (operation.toLowerCase()) {
            case "network":
                return context.getString(R.string.error_network_fallback);
            case "search":
                return context.getString(R.string.error_search_fallback);
            case "upload":
                return context.getString(R.string.error_upload_fallback);
            case "load":
                return context.getString(R.string.error_load_fallback);
            default:
                return context.getString(R.string.error_generic_fallback);
        }
    }
    
    /**
     * Check if we should use fallback data based on error type
     */
    public boolean shouldUseFallbackData(Throwable error) {
        if (error == null) return false;
        
        String errorMessage = error.getMessage();
        if (errorMessage == null) return true;
        
        String lowerMessage = errorMessage.toLowerCase();
        
        // Use fallback for network-related errors
        return lowerMessage.contains("network") ||
               lowerMessage.contains("connection") ||
               lowerMessage.contains("timeout") ||
               lowerMessage.contains("unreachable") ||
               lowerMessage.contains("offline");
    }
    
    /**
     * Get offline mode indicator message
     */
    public String getOfflineModeMessage() {
        return context.getString(R.string.offline_mode_message);
    }
    
    /**
     * Get connection retry message
     */
    public String getConnectionRetryMessage() {
        return context.getString(R.string.connection_retry_message);
    }
    
    /**
     * Get data sync pending message
     */
    public String getDataSyncPendingMessage() {
        return context.getString(R.string.data_sync_pending_message);
    }
    
    /**
     * Create a singleton instance
     */
    private static FallbackDataProvider instance;
    
    public static synchronized FallbackDataProvider getInstance(Context context) {
        if (instance == null) {
            instance = new FallbackDataProvider(context);
        }
        return instance;
    }
}