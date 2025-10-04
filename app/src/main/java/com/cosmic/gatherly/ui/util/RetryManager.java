package com.cosmic.gatherly.ui.util;

import android.os.Handler;
import android.os.Looper;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.util.ErrorHandler;
import com.cosmic.gatherly.data.util.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages retry mechanisms for network failures and other recoverable errors
 * Implements exponential backoff with jitter
 */
public class RetryManager {
    private static final String TAG = "RetryManager";
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_BASE_DELAY = 1000; // 1 second
    private static final long MAX_DELAY = 30000; // 30 seconds
    
    private final Handler handler;
    private final AtomicInteger attemptCount;
    private final int maxRetries;
    private final long baseDelay;
    private final String operationName;
    
    private RetryCallback callback;
    private boolean isRetrying = false;
    
    public interface RetryCallback {
        void onRetryAttempt(int attemptNumber);
        void onRetrySuccess();
        void onRetryFailed(AuthError error);
        void onMaxRetriesExceeded();
    }
    
    public RetryManager(String operationName) {
        this(operationName, DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY);
    }
    
    public RetryManager(String operationName, int maxRetries, long baseDelay) {
        this.operationName = operationName;
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.handler = new Handler(Looper.getMainLooper());
        this.attemptCount = new AtomicInteger(0);
    }
    
    /**
     * Set retry callback
     */
    public void setRetryCallback(RetryCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Execute operation with retry logic
     */
    public void executeWithRetry(RetryableOperation operation) {
        executeWithRetry(operation, null);
    }
    
    /**
     * Execute operation with retry logic and initial error
     */
    public void executeWithRetry(RetryableOperation operation, AuthError initialError) {
        if (isRetrying) {
            Logger.w(TAG, "Retry already in progress for operation: %s", operationName);
            return;
        }
        
        isRetrying = true;
        attemptCount.set(0);
        
        Logger.d(TAG, "Starting retry sequence for operation: %s", operationName);
        
        if (initialError != null && !ErrorHandler.isRecoverableError(initialError)) {
            Logger.d(TAG, "Initial error is not recoverable for operation: %s", operationName);
            isRetrying = false;
            if (callback != null) {
                callback.onRetryFailed(initialError);
            }
            return;
        }
        
        performRetry(operation);
    }
    
    private void performRetry(RetryableOperation operation) {
        int currentAttempt = attemptCount.incrementAndGet();
        
        Logger.d(TAG, "Retry attempt %d/%d for operation: %s", currentAttempt, maxRetries, operationName);
        
        if (callback != null) {
            callback.onRetryAttempt(currentAttempt);
        }
        
        try {
            operation.execute(new RetryableOperation.RetryCallback() {
                @Override
                public void onSuccess() {
                    Logger.i(TAG, "Retry successful on attempt %d for operation: %s", currentAttempt, operationName);
                    isRetrying = false;
                    if (callback != null) {
                        callback.onRetrySuccess();
                    }
                }
                
                @Override
                public void onError(AuthError error) {
                    Logger.w(TAG, "Retry attempt %d failed for operation: %s - %s", 
                        currentAttempt, operationName, error.getMessage());
                    
                    if (currentAttempt >= maxRetries) {
                        Logger.e(TAG, "Max retries exceeded for operation: %s", operationName);
                        isRetrying = false;
                        if (callback != null) {
                            callback.onMaxRetriesExceeded();
                        }
                        return;
                    }
                    
                    if (!ErrorHandler.isRecoverableError(error)) {
                        Logger.d(TAG, "Error is not recoverable for operation: %s", operationName);
                        isRetrying = false;
                        if (callback != null) {
                            callback.onRetryFailed(error);
                        }
                        return;
                    }
                    
                    // Schedule next retry with exponential backoff
                    long delay = ErrorHandler.getRetryDelay(error, currentAttempt);
                    Logger.d(TAG, "Scheduling next retry in %d ms for operation: %s", delay, operationName);
                    
                    handler.postDelayed(() -> performRetry(operation), delay);
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Exception during retry attempt %d for operation: %s", currentAttempt, operationName, e);
            
            AuthError error = ErrorHandler.parseNetworkError(e);
            
            if (currentAttempt >= maxRetries || !ErrorHandler.isRecoverableError(error)) {
                isRetrying = false;
                if (callback != null) {
                    if (currentAttempt >= maxRetries) {
                        callback.onMaxRetriesExceeded();
                    } else {
                        callback.onRetryFailed(error);
                    }
                }
                return;
            }
            
            // Schedule next retry
            long delay = ErrorHandler.getRetryDelay(error, currentAttempt);
            handler.postDelayed(() -> performRetry(operation), delay);
        }
    }
    
    /**
     * Cancel ongoing retry sequence
     */
    public void cancel() {
        Logger.d(TAG, "Cancelling retry sequence for operation: %s", operationName);
        isRetrying = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    /**
     * Check if retry is in progress
     */
    public boolean isRetrying() {
        return isRetrying;
    }
    
    /**
     * Get current attempt count
     */
    public int getCurrentAttempt() {
        return attemptCount.get();
    }
    
    /**
     * Get max retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
    
    /**
     * Reset retry state
     */
    public void reset() {
        Logger.d(TAG, "Resetting retry state for operation: %s", operationName);
        cancel();
        attemptCount.set(0);
    }
    
    /**
     * Interface for retryable operations
     */
    public interface RetryableOperation {
        void execute(RetryCallback callback) throws Exception;
        
        interface RetryCallback {
            void onSuccess();
            void onError(AuthError error);
        }
    }
    
    /**
     * Create a simple retry manager for network operations
     */
    public static RetryManager forNetworkOperation(String operationName) {
        return new RetryManager(operationName, 3, 2000); // 3 retries, 2 second base delay
    }
    
    /**
     * Create a retry manager for file operations
     */
    public static RetryManager forFileOperation(String operationName) {
        return new RetryManager(operationName, 2, 1000); // 2 retries, 1 second base delay
    }
    
    /**
     * Create a retry manager for search operations
     */
    public static RetryManager forSearchOperation(String operationName) {
        return new RetryManager(operationName, 2, 500); // 2 retries, 500ms base delay
    }
}