package com.cosmic.gatherly.ui.util;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.util.Logger;

/**
 * Manages loading states for UI components
 * Provides consistent loading indicators across the app
 */
public class LoadingStateManager {
    private static final String TAG = "LoadingStateManager";
    
    private final Context context;
    private final FrameLayout containerView;
    private View loadingOverlay;
    private TextView loadingText;
    private ProgressBar loadingProgressBar;
    
    private boolean isLoading = false;
    private String componentName;
    
    public LoadingStateManager(@NonNull Context context, @NonNull FrameLayout containerView) {
        this.context = context;
        this.containerView = containerView;
        this.componentName = "Unknown";
        init();
    }
    
    public LoadingStateManager(@NonNull Context context, @NonNull FrameLayout containerView, String componentName) {
        this.context = context;
        this.containerView = containerView;
        this.componentName = componentName;
        init();
    }
    
    private void init() {
        // Find or create loading overlay
        loadingOverlay = containerView.findViewById(R.id.loadingOverlayInclude);
        
        if (loadingOverlay != null) {
            loadingText = loadingOverlay.findViewById(R.id.loadingOverlayText);
            loadingProgressBar = loadingOverlay.findViewById(R.id.loadingProgressBar);
        } else {
            Logger.w(TAG, "Loading overlay not found in container for component: %s", componentName);
        }
    }
    
    /**
     * Show loading state with default message
     */
    public void showLoading() {
        showLoading(context.getString(R.string.loading));
    }
    
    /**
     * Show loading state with custom message
     */
    public void showLoading(String message) {
        Logger.d(TAG, "Showing loading state for component: %s - %s", componentName, message);
        
        isLoading = true;
        
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            
            if (loadingText != null) {
                loadingText.setText(message);
            }
            
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            Logger.w(TAG, "Cannot show loading - overlay not available for component: %s", componentName);
        }
    }
    
    /**
     * Show loading state with string resource
     */
    public void showLoading(@StringRes int messageResId) {
        showLoading(context.getString(messageResId));
    }
    
    /**
     * Hide loading state
     */
    public void hideLoading() {
        Logger.d(TAG, "Hiding loading state for component: %s", componentName);
        
        isLoading = false;
        
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
    
    /**
     * Check if currently loading
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Show loading for file upload with progress
     */
    public void showFileUploadLoading(String fileName) {
        String message = context.getString(R.string.uploading_file, fileName);
        showLoading(message);
    }
    
    /**
     * Show loading for search operation
     */
    public void showSearchLoading(String query) {
        String message = context.getString(R.string.searching_for, query);
        showLoading(message);
    }
    
    /**
     * Show loading for network operation
     */
    public void showNetworkLoading() {
        showLoading(context.getString(R.string.loading_network));
    }
    
    /**
     * Show loading for data sync
     */
    public void showSyncLoading() {
        showLoading(context.getString(R.string.syncing_data));
    }
    
    /**
     * Execute operation with loading state
     */
    public void executeWithLoading(Runnable operation) {
        executeWithLoading(operation, context.getString(R.string.loading));
    }
    
    /**
     * Execute operation with loading state and custom message
     */
    public void executeWithLoading(Runnable operation, String loadingMessage) {
        try {
            showLoading(loadingMessage);
            operation.run();
        } catch (Exception e) {
            Logger.e(TAG, "Error during loading operation for component: %s", componentName, e);
            hideLoading();
            throw e; // Re-throw to let error boundary handle it
        }
    }
    
    /**
     * Set component name for logging
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    /**
     * Get component name
     */
    public String getComponentName() {
        return componentName;
    }
}