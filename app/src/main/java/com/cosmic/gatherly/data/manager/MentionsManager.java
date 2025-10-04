package com.cosmic.gatherly.data.manager;

import android.content.Context;
import android.util.Log;

import com.cosmic.gatherly.data.model.Mention;
import com.cosmic.gatherly.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for handling user mentions functionality
 */
public class MentionsManager {
    private static final String TAG = "MentionsManager";
    private static MentionsManager instance;
    private Context context;
    private List<Mention> mentions;
    private List<OnMentionsUpdateListener> listeners;

    public interface OnMentionsUpdateListener {
        void onMentionsUpdated(List<Mention> mentions);
        void onUnreadCountChanged(int unreadCount);
    }

    private MentionsManager(Context context) {
        this.context = context.getApplicationContext();
        this.mentions = new ArrayList<>();
        this.listeners = new ArrayList<>();
        initializeSampleMentions();
    }

    public static synchronized MentionsManager getInstance(Context context) {
        if (instance == null) {
            instance = new MentionsManager(context);
        }
        return instance;
    }

    /**
     * Add a listener for mentions updates
     */
    public void addListener(OnMentionsUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener for mentions updates
     */
    public void removeListener(OnMentionsUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of mentions updates
     */
    private void notifyListeners() {
        for (OnMentionsUpdateListener listener : listeners) {
            listener.onMentionsUpdated(new ArrayList<>(mentions));
            listener.onUnreadCountChanged(getUnreadMentionsCount());
        }
    }

    /**
     * Parse message content for mentions and create mention objects
     */
    public List<Mention> parseMentionsFromMessage(String messageContent, String messageId, 
                                                  String authorId, String channelId, String channelName) {
        List<Mention> foundMentions = new ArrayList<>();
        
        try {
            // Pattern to match @username mentions
            Pattern mentionPattern = Pattern.compile("@(\\w+)");
            Matcher matcher = mentionPattern.matcher(messageContent);
            
            while (matcher.find()) {
                String mentionedUsername = matcher.group(1);
                Log.d(TAG, "Found mention: @" + mentionedUsername);
                
                // In a real app, you would look up the user by username
                // For now, we'll create sample mentions
                String mentionedUserId = getMockUserIdByUsername(mentionedUsername);
                if (mentionedUserId != null && !mentionedUserId.equals(authorId)) {
                    Mention mention = new Mention(
                        UUID.randomUUID().toString(),
                        messageId,
                        mentionedUserId,
                        authorId,
                        channelId,
                        channelName,
                        messageContent
                    );
                    
                    foundMentions.add(mention);
                    Log.d(TAG, "Created mention: " + mention);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing mentions from message", e);
        }
        
        return foundMentions;
    }

    /**
     * Add a new mention to the list
     */
    public void addMention(Mention mention) {
        try {
            mentions.add(0, mention); // Add to beginning for chronological order
            Log.d(TAG, "Added mention: " + mention);
            notifyListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error adding mention", e);
        }
    }

    /**
     * Add multiple mentions
     */
    public void addMentions(List<Mention> newMentions) {
        try {
            for (Mention mention : newMentions) {
                mentions.add(0, mention);
            }
            Log.d(TAG, "Added " + newMentions.size() + " mentions");
            notifyListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error adding mentions", e);
        }
    }

    /**
     * Get all mentions for the current user
     */
    public List<Mention> getAllMentions() {
        return new ArrayList<>(mentions);
    }

    /**
     * Get unread mentions for the current user
     */
    public List<Mention> getUnreadMentions() {
        List<Mention> unreadMentions = new ArrayList<>();
        for (Mention mention : mentions) {
            if (!mention.isRead()) {
                unreadMentions.add(mention);
            }
        }
        return unreadMentions;
    }

    /**
     * Get count of unread mentions
     */
    public int getUnreadMentionsCount() {
        int count = 0;
        for (Mention mention : mentions) {
            if (!mention.isRead()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Mark a mention as read
     */
    public void markMentionAsRead(String mentionId) {
        try {
            for (Mention mention : mentions) {
                if (mention.getId().equals(mentionId)) {
                    mention.setRead(true);
                    Log.d(TAG, "Marked mention as read: " + mentionId);
                    notifyListeners();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking mention as read", e);
        }
    }

    /**
     * Mark all mentions as read
     */
    public void markAllMentionsAsRead() {
        try {
            boolean hasChanges = false;
            for (Mention mention : mentions) {
                if (!mention.isRead()) {
                    mention.setRead(true);
                    hasChanges = true;
                }
            }
            if (hasChanges) {
                Log.d(TAG, "Marked all mentions as read");
                notifyListeners();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking all mentions as read", e);
        }
    }

    /**
     * Clear all mentions
     */
    public void clearAllMentions() {
        try {
            mentions.clear();
            Log.d(TAG, "Cleared all mentions");
            notifyListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing mentions", e);
        }
    }

    /**
     * Mock method to get user ID by username (in real app, this would query the database)
     */
    private String getMockUserIdByUsername(String username) {
        // Mock user mapping for demonstration
        switch (username.toLowerCase()) {
            case "cosmicexplorer":
                return "user_cosmic_explorer";
            case "stardust":
                return "user_stardust";
            case "nebulaninja":
                return "user_nebula_ninja";
            case "galaxygamer":
                return "user_galaxy_gamer";
            case "you":
            case "me":
                return "current_user"; // Current user ID
            default:
                return "user_" + username.toLowerCase();
        }
    }

    /**
     * Initialize sample mentions for demonstration
     */
    private void initializeSampleMentions() {
        try {
            // Create sample mentions
            Mention mention1 = new Mention(
                UUID.randomUUID().toString(),
                "msg_001",
                "current_user", // Current user is mentioned
                "user_cosmic_explorer",
                "general",
                "general",
                "Hey @you, welcome to the cosmic gaming hub! 🚀"
            );
            mention1.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 3600000)); // 1 hour ago

            Mention mention2 = new Mention(
                UUID.randomUUID().toString(),
                "msg_002",
                "current_user",
                "user_stardust",
                "announcements",
                "announcements",
                "@you should check out the new features we just released! ✨"
            );
            mention2.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 7200000)); // 2 hours ago

            Mention mention3 = new Mention(
                UUID.randomUUID().toString(),
                "msg_003",
                "current_user",
                "user_galaxy_gamer",
                "random",
                "random",
                "Thanks @you for the help with the cosmic theme! 🌌"
            );
            mention3.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 86400000)); // 1 day ago
            mention3.setRead(true); // This one is already read

            mentions.add(mention1);
            mentions.add(mention2);
            mentions.add(mention3);

            Log.d(TAG, "Initialized " + mentions.size() + " sample mentions");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sample mentions", e);
        }
    }
}