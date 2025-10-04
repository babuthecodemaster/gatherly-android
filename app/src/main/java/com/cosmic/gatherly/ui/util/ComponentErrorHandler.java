package com.cosmic.gatherly.ui.util;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.util.ErrorHandler;
import com.cosmic.gatherly.data.util.Logger;

/**
 * Comprehensive error handler that integrates ErrorBoundary, LoadingStateManager, 
 * RetryManager, and FallbackDataProvider for complete error handling coverage
 */
public class ComponentErrorHandler {
    private static final String TAG = "ComponentErrorHandler";
    
    private final Context context;
    private final String componentName;
    private final FrameLayout containerView;
    
    private ErrorBoundary errorBoundary;
    private LoadingStateManager loadingStateManager;
    private RetryManager retryManager;
    private FallbackDataProvider fallbackDataProvider;
    
    private ComponentErrorCallback callback;
    
    public interface ComponentErrorCallback {
        void onComponentRecovered();
        void onComponentFailed(AuthError error);
        void onFallbackDataUsed();
    }
    
    public ComponentErrorHandler(Context context, FrameLayout containerView, String componentName) {
        this.context = context;
        this.containerView = containerView;
        this.componentName = componentName;
        
        init();
    }
    
    private void init() {
        // Initialize error boundary
        errorBoundary = new ErrorBoundary(context);
        errorBoundary.setComponentName(componentName);
        
        // Initialize loading state manager
        loadingStateManager = new LoadingStateManager(context, containerView, componentName);
        
        // Initialize retry manager
        retryManager = RetryManager.forNetworkOperation(componentName);
        
        // Initialize fallback data provider
        fallbackDataProvider = FallbackDataProvider.getInstance(context);
        
        setupErrorBoundaryListeners();
        setupRetryManagerCallbacks();
    }
    
    private void setupErrorBoundaryListeners() {
        errorBoundary.setOnRetryListener(() -> {
            Logger.d(TAG, "Error boundary retry requested for component: %s", componentName);
            if (callback != null) {
                callback.onComponentRecovered();
            }
        });
        
        errorBoundary.setOnReportListener((componentName, error) -> {
            Logger.e(TAG, "Error reported for component: %s", componentName, error);
            // In a real app, this would send error reports to crash reporting service
        });
    }
    
    private void setupRetryManagerCallbacks() {
        retryManager.setRetryCallback(new RetryManager.RetryCallback() {
            @Override
            public void onRetryAttempt(int attemptNumber) {
                Logger.d(TAG, "Retry attempt %d for component: %s", attemptNumber, componentName);
                loadingStateManager.showLoading("Retrying... (Attempt " + attemptNumber + ")");
            }
            
            @Override
            public void onRetrySuccess() {
                Logger.i(TAG, "Retry successful for component: %s", componentName);
                loadingStateManager.hideLoading();
                errorBoundary.hideError();
                if (callback != null) {
                    callback.onComponentRecovered();
                }
            }
            
            @Override
            public void onRetryFailed(AuthError error) {
                Logger.e(TAG, "Retry failed for component: %s - %s", componentName, error.getMessage());
                loadingStateManager.hideLoading();
                showErrorWithFallback(error);
            }
            
            @Override
            public void onMaxRetriesExceeded() {
                Logger.e(TAG, "Max retries exceeded for component: %s", componentName);
                loadingStateManager.hideLoading();
                
                AuthError error = new AuthError(
                    AuthError.Type.UNKNOWN_ERROR,
                    "Max retries exceeded",
                    "Unable to complete operation after multiple attempts. Please try again later."
                );
                showErrorWithFallback(error);
            }
        });
    }
    
    /**
     * Set the main content view for the component
     */
    public void setMainContent(View content) {
        errorBoundary.setMainContent(content);
        
        // Replace container content with error boundary
        containerView.removeAllViews();
        containerView.addView(errorBoundary);
    }
    
    /**
     * Execute operation with comprehensive error handling
     */
    public void executeWithErrorHandling(Runnable operation) {
        executeWithErrorHandling(operation, "Operation");
    }
    
    /**
     * Execute operation with comprehensive error handling and custom loading message
     */
    public void executeWithErrorHandling(Runnable operation, String operationName) {
        loadingStateManager.showLoading("Loading " + operationName.toLowerCase() + "...");
        
        errorBoundary.executeWithErrorHandling(() -> {
            try {
                operation.run();
                loadingStateManager.hideLoading();
            } catch (Exception e) {
                loadingStateManager.hideLoading();
                
                AuthError error = ErrorHandler.parseNetworkError(e);
                handleError(error, operation, operationName);
            }
        }, operationName);
    }
    
    /**
     * Execute retryable operation
     */
    public void executeWithRetry(RetryManager.RetryableOperation operation) {
        retryManager.executeWithRetry(operation);
    }
    
    /**
     * Handle error with automatic retry and fallback logic
     */
    public void handleError(AuthError error, Runnable retryOperation, String operationName) {
        Logger.w(TAG, "Handling error for component %s: %s", componentName, error.getMessage());
        
        if (ErrorHandler.isRecoverableError(error)) {
            // Try to recover with retry
            retryManager.executeWithRetry(new RetryManager.RetryableOperation() {
                @Override
                public void execute(RetryManager.RetryableOperation.RetryCallback callback) throws Exception {
                    try {
                        retryOperation.run();
                        callback.onSuccess();
                    } catch (Exception e) {
                        AuthError retryError = ErrorHandler.parseNetworkError(e);
                        callback.onError(retryError);
                    }
                }
            }, error);
        } else {
            // Show error immediately for non-recoverable errors
            showErrorWithFallback(error);
        }
    }
    
    /**
     * Show error with fallback data if available
     */
    private void showErrorWithFallback(AuthError error) {
        if (fallbackDataProvider.shouldUseFallbackData(error.getCause())) {
            Logger.d(TAG, "Using fallback data for component: %s", componentName);
            
            // Show error but continue with fallback data
            String fallbackMessage = fallbackDataProvider.getFallbackErrorMessage(componentName);
            errorBoundary.showError("Using Offline Mode", fallbackMessage, error.getCause());
            
            if (callback != null) {
                callback.onFallbackDataUsed();
            }
        } else {
            // Show full error state
            errorBoundary.showError(error);
            
            if (callback != null) {
                callback.onComponentFailed(error);
            }
        }
    }
    
    /**
     * Show loading state
     */
    public void showLoading() {
        loadingStateManager.showLoading();
    }
    
    /**
     * Show loading state with message
     */
    public void showLoading(String message) {
        loadingStateManager.showLoading(message);
    }
    
    /**
     * Hide loading state
     */
    public void hideLoading() {
        loadingStateManager.hideLoading();
    }
    
    /**
     * Show error state
     */
    public void showError(String title, String message) {
        loadingStateManager.hideLoading();
        errorBoundary.showError(title, message);
    }
    
    /**
     * Show error state from AuthError
     */
    public void showError(AuthError error) {
        loadingStateManager.hideLoading();
        showErrorWithFallback(error);
    }
    
    /**
     * Hide error state
     */
    public void hideError() {
        errorBoundary.hideError();
    }
    
    /**
     * Check if in error state
     */
    public boolean isInErrorState() {
        return errorBoundary.isInErrorState();
    }
    
    /**
     * Check if loading
     */
    public boolean isLoading() {
        return loadingStateManager.isLoading();
    }
    
    /**
     * Set component error callback
     */
    public void setComponentErrorCallback(ComponentErrorCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (retryManager != null) {
            retryManager.cancel();
        }
    }
    
    /**
     * Get fallback data provider
     */
    public FallbackDataProvider getFallbackDataProvider() {
        return fallbackDataProvider;
    }
    
    /**
     * Get retry manager
     */
    public RetryManager getRetryManager() {
        return retryManager;
    }
    
    /**
     * Get error boundary
     */
    public ErrorBoundary getErrorBoundary() {
        return errorBoundary;
    }
    
    /**
     * Get loading state manager
     */
    public LoadingStateManager getLoadingStateManager() {
        return loadingStateManager;
    }
}