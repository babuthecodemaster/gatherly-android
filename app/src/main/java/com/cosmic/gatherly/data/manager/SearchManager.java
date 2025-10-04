package com.cosmic.gatherly.data.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.SearchResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchManager {
    private static final String TAG = "SearchManager";
    
    private static SearchManager instance;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Context context;
    
    // Sample messages for search (in a real app, this would come from database/API)
    private List<SearchResult> allMessages;

    public interface SearchCallback {
        void onSearchResults(List<SearchResult> results);
        void onSearchError(String error);
        void onSearchStarted();
        void onSearchRetrying(int attemptNumber);
    }

    private SearchManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        initializeSampleMessages();
    }

    public static synchronized SearchManager getInstance(Context context) {
        if (instance == null) {
            instance = new SearchManager(context);
        }
        return instance;
    }

    private void initializeSampleMessages() {
        allMessages = new ArrayList<>();
        
        // Sample messages for different channels
        long now = System.currentTimeMillis();
        
        // General channel messages
        allMessages.add(new SearchResult(
            "1", "Welcome to the Cosmic Gaming Hub! 🚀",
            "CosmicExplorer", "general", "general",
            new Date(now - 86400000), // 1 day ago
            R.drawable.ic_person, 0xFF00D166
        ));
        
        allMessages.add(new SearchResult(
            "2", "This place looks amazing! The cosmic theme is perfect ✨",
            "StarDust", "general", "general",
            new Date(now - 82800000), // 23 hours ago
            R.drawable.ic_star, 0xFF5865F2
        ));
        
        allMessages.add(new SearchResult(
            "3", "Anyone up for some cosmic gaming tonight? 🎮",
            "NebulaNinja", "general", "general",
            new Date(now - 7200000), // 2 hours ago
            R.drawable.ic_person, 0xFFEB459E
        ));
        
        allMessages.add(new SearchResult(
            "4", "I'm in! What game are we playing? 🎯",
            "GalaxyGamer", "general", "general",
            new Date(now - 3600000), // 1 hour ago
            R.drawable.ic_person, 0xFFFEE75C
        ));
        
        // Announcements channel messages
        allMessages.add(new SearchResult(
            "5", "📢 Welcome to the announcements channel!",
            "Admin", "announcements", "announcements",
            new Date(now - 172800000), // 2 days ago
            R.drawable.ic_person, 0xFFFF6B6B
        ));
        
        allMessages.add(new SearchResult(
            "6", "🎉 New features coming soon! Stay tuned for updates.",
            "Admin", "announcements", "announcements",
            new Date(now - 86400000), // 1 day ago
            R.drawable.ic_person, 0xFFFF6B6B
        ));
        
        // Random channel messages
        allMessages.add(new SearchResult(
            "7", "Just sharing some random thoughts here! 🤔",
            "RandomUser", "random", "random",
            new Date(now - 10800000), // 3 hours ago
            R.drawable.ic_person, 0xFFFEE75C
        ));
        
        allMessages.add(new SearchResult(
            "8", "Anyone else love the cosmic vibes of this app? 🌌",
            "ChillGamer", "random", "random",
            new Date(now - 7200000), // 2 hours ago
            R.drawable.ic_person, 0xFF9B59B6
        ));
        
        // Add more sample messages with various content
        allMessages.add(new SearchResult(
            "9", "The search functionality is working great!",
            "TestUser", "general", "general",
            new Date(now - 1800000), // 30 minutes ago
            R.drawable.ic_person, 0xFF4ECDC4
        ));
        
        allMessages.add(new SearchResult(
            "10", "I love how the search highlights the matching text",
            "SearchFan", "general", "general",
            new Date(now - 900000), // 15 minutes ago
            R.drawable.ic_person, 0xFF95A5A6
        ));
    }

    public void searchMessages(String query, SearchCallback callback) {
        searchMessages(query, null, callback);
    }

    public void searchMessages(String query, String channelId, SearchCallback callback) {
        searchMessagesWithRetry(query, channelId, callback, 1, 2);
    }
    
    private void searchMessagesWithRetry(String query, String channelId, SearchCallback callback, int attemptNumber, int maxRetries) {
        if (query == null || query.trim().isEmpty()) {
            mainHandler.post(() -> callback.onSearchResults(new ArrayList<>()));
            return;
        }

        if (attemptNumber == 1) {
            mainHandler.post(() -> callback.onSearchStarted());
        } else {
            mainHandler.post(() -> callback.onSearchRetrying(attemptNumber));
        }

        executorService.execute(() -> {
            try {
                // Simulate network delay for realistic behavior
                Thread.sleep(attemptNumber == 1 ? 300 : 500);
                
                List<SearchResult> results = performSearch(query.trim(), channelId);
                mainHandler.post(() -> callback.onSearchResults(results));
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Search failed on attempt " + attemptNumber, e);
                
                if (shouldRetrySearch(e) && attemptNumber < maxRetries) {
                    // Retry after delay
                    long delay = calculateSearchRetryDelay(attemptNumber);
                    mainHandler.postDelayed(() -> {
                        searchMessagesWithRetry(query, channelId, callback, attemptNumber + 1, maxRetries);
                    }, delay);
                } else {
                    String errorMessage = parseSearchErrorMessage(e);
                    mainHandler.post(() -> callback.onSearchError(errorMessage));
                }
            }
        });
    }

    private List<SearchResult> performSearch(String query, String channelId) {
        List<SearchResult> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (SearchResult message : allMessages) {
            // Filter by channel if specified
            if (channelId != null && !channelId.equals(message.getChannelId())) {
                continue;
            }

            // Check if message content contains the search query
            if (message.getContent().toLowerCase().contains(lowerQuery) ||
                message.getAuthorName().toLowerCase().contains(lowerQuery)) {
                results.add(message);
            }
        }

        // Sort results by timestamp (newest first)
        results.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return results;
    }

    public void addMessage(SearchResult message) {
        if (message != null) {
            allMessages.add(0, message); // Add to beginning for newest first
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Check if search should be retried based on exception
     */
    private boolean shouldRetrySearch(Exception e) {
        return e instanceof java.net.SocketTimeoutException ||
               e instanceof java.net.ConnectException ||
               e instanceof java.net.UnknownHostException ||
               e instanceof java.io.IOException ||
               e instanceof java.util.concurrent.TimeoutException;
    }
    
    /**
     * Calculate retry delay for search operations
     */
    private long calculateSearchRetryDelay(int attemptNumber) {
        long baseDelay = 1000; // 1 second
        return baseDelay * attemptNumber; // Linear backoff for search
    }
    
    /**
     * Parse error message from search exception
     */
    private String parseSearchErrorMessage(Exception e) {
        if (e instanceof java.net.SocketTimeoutException) {
            return "Search timed out. Please try again.";
        } else if (e instanceof java.net.ConnectException) {
            return "Cannot connect to search service. Please check your connection.";
        } else if (e instanceof java.net.UnknownHostException) {
            return "Cannot reach search service. Please check your internet connection.";
        } else if (e instanceof java.io.IOException) {
            return "Network error during search. Please try again.";
        } else {
            return "Search failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }
    
    /**
     * Get fallback search results when search fails
     */
    public List<SearchResult> getFallbackSearchResults(String query) {
        List<SearchResult> fallbackResults = new ArrayList<>();
        
        // Add a message indicating search is unavailable
        fallbackResults.add(new SearchResult(
            "fallback-search-unavailable",
            "Search is currently unavailable. Please try again later.",
            "System",
            "system",
            "system",
            new Date(),
            R.drawable.ic_search,
            0xFF5865F2
        ));
        
        return fallbackResults;
    }
}
