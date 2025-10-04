package com.cosmic.gatherly.ui.util;

import android.content.Context;
import android.widget.FrameLayout;

import com.cosmic.gatherly.data.manager.SearchManager;
import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.SearchResult;
import com.cosmic.gatherly.data.util.Logger;
import com.cosmic.gatherly.ui.components.SearchDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced search handler with comprehensive error handling and loading states
 * Demonstrates integration of all error handling components for search
 * functionality
 */
public class EnhancedSearchHandler {
    private static final String TAG = "EnhancedSearchHandler";

    private final Context context;
    private final SearchManager searchManager;
    private ComponentErrorHandler errorHandler;
    private SearchDialog searchDialog;

    private String currentChannelId;
    private String currentChannelName;
    private SearchCallback callback;

    public interface SearchCallback {
        void onSearchResults(List<SearchResult> results, String query);

        void onSearchFailed(AuthError error);

        void onSearchCancelled();
    }

    public EnhancedSearchHandler(Context context, FrameLayout containerView) {
        this.context = context;
        this.searchManager = SearchManager.getInstance(context);

        // Initialize comprehensive error handler
        this.errorHandler = new ComponentErrorHandler(context, containerView, "Search");
        setupErrorHandlerCallbacks();
    }

    private void setupErrorHandlerCallbacks() {
        errorHandler.setComponentErrorCallback(new ComponentErrorHandler.ComponentErrorCallback() {
            @Override
            public void onComponentRecovered() {
                Logger.i(TAG, "Search component recovered");
                // Could retry last search or just notify recovery
            }

            @Override
            public void onComponentFailed(AuthError error) {
                Logger.e(TAG, "Search component failed: %s", error.getMessage());
                if (callback != null) {
                    callback.onSearchFailed(error);
                }
            }

            @Override
            public void onFallbackDataUsed() {
                Logger.d(TAG, "Using fallback mode for search");
                // In fallback mode, we might search cached data or show offline message
                showFallbackSearchResults();
            }
        });
    }

    /**
     * Show search dialog with enhanced error handling
     */
    public void showSearchDialog(String channelId, String channelName, SearchCallback callback) {
        this.currentChannelId = channelId;
        this.currentChannelName = channelName;
        this.callback = callback;

        Logger.d(TAG, "Showing enhanced search dialog for channel: %s", channelName);

        if (searchDialog == null) {
            searchDialog = new SearchDialog(context);
            setupSearchDialogCallbacks();
        }

        searchDialog.setCurrentChannel(channelId, channelName);
        searchDialog.show();
    }

    private void setupSearchDialogCallbacks() {
        searchDialog.setOnSearchResultSelectedListener(result -> {
            Logger.d(TAG, "Search result selected: %s", result.getContent());
            if (callback != null) {
                // Convert single result to list for consistency
                List<SearchResult> results = new ArrayList<>();
                results.add(result);
                callback.onSearchResults(results, "");
            }
        });
    }

    /**
     * Perform search with comprehensive error handling
     */
    public void performSearch(String query, SearchCallback callback) {
        this.callback = callback;

        Logger.d(TAG, "Starting enhanced search for query: %s", query);

        // Execute search with error handling
        errorHandler.executeWithErrorHandling(() -> {
            performSearchOperation(query);
        }, "Search");
    }

    private void performSearchOperation(String query) {
        searchManager.searchMessages(query, currentChannelId, new SearchManager.SearchCallback() {
            @Override
            public void onSearchStarted() {
                Logger.d(TAG, "Search started for query: %s", query);
                errorHandler.showLoading("Searching for \"" + query + "\"...");
            }

            @Override
            public void onSearchRetrying(int attemptNumber) {
                Logger.d(TAG, "Search retrying: attempt %d", attemptNumber);
                errorHandler.showLoading("Retrying search... (Attempt " + attemptNumber + ")");
            }

            @Override
            public void onSearchResults(List<SearchResult> results) {
                Logger.i(TAG, "Search completed with %d results", results.size());

                errorHandler.hideLoading();
                errorHandler.hideError();

                if (callback != null) {
                    callback.onSearchResults(results, query);
                }
            }

            @Override
            public void onSearchError(String error) {
                Logger.e(TAG, "Search error: %s", error);

                // Parse error and handle appropriately
                AuthError authError = new AuthError(
                        AuthError.Type.UNKNOWN_ERROR,
                        error,
                        error);

                handleSearchError(authError, query);
            }
        });
    }

    private void handleSearchError(AuthError error, String query) {
        // Use error handler for comprehensive error handling
        errorHandler.handleError(error, () -> {
            performSearchOperation(query);
        }, "Search");
    }

    private void showFallbackSearchResults() {
        Logger.d(TAG, "Showing fallback search results");

        FallbackDataProvider fallbackProvider = errorHandler.getFallbackDataProvider();
        List<SearchResult> fallbackResults = fallbackProvider.getFallbackSearchResults("");

        if (callback != null) {
            callback.onSearchResults(fallbackResults, "");
        }
    }

    /**
     * Cancel ongoing search
     */
    public void cancelSearch() {
        Logger.d(TAG, "Cancelling search");

        errorHandler.hideLoading();

        if (searchDialog != null && searchDialog.isShowing()) {
            searchDialog.dismiss();
        }

        if (callback != null) {
            callback.onSearchCancelled();
        }
    }

    /**
     * Hide search dialog
     */
    public void hideSearchDialog() {
        if (searchDialog != null && searchDialog.isShowing()) {
            searchDialog.dismiss();
        }
    }

    /**
     * Check if search is in progress
     */
    public boolean isSearching() {
        return errorHandler.isLoading();
    }

    /**
     * Check if search dialog is showing
     */
    public boolean isSearchDialogShowing() {
        return searchDialog != null && searchDialog.isShowing();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        hideSearchDialog();
        errorHandler.cleanup();

        if (searchManager != null) {
            searchManager.shutdown();
        }
    }

    /**
     * Get error handler for advanced usage
     */
    public ComponentErrorHandler getErrorHandler() {
        return errorHandler;
    }

    // Example usage is documented in ERROR_HANDLING_IMPLEMENTATION_GUIDE.md
}