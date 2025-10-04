package com.cosmic.gatherly.ui.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.util.ErrorHandler;
import com.cosmic.gatherly.data.util.Logger;

/**
 * Error boundary component for handling component failures gracefully
 * Provides fallback UI when components crash or fail to load
 */
public class ErrorBoundary extends FrameLayout {
    private static final String TAG = "ErrorBoundary";
    
    private View mainContentContainer;
    private View errorBoundaryLayout;
    private TextView errorTitle;
    private TextView errorMessage;
    private Button retryButton;
    private Button reportButton;
    
    private OnRetryListener retryListener;
    private OnReportListener reportListener;
    private String componentName;
    private boolean isErrorState = false;
    
    public interface OnRetryListener {
        void onRetry();
    }
    
    public interface OnReportListener {
        void onReport(String componentName, Throwable error);
    }
    
    public ErrorBoundary(@NonNull Context context) {
        super(context);
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.include_error_handler, this);
        
        mainContentContainer = findViewById(R.id.mainContentContainer);
        errorBoundaryLayout = findViewById(R.id.genericErrorInclude);
        
        if (errorBoundaryLayout != null) {
            errorTitle = errorBoundaryLayout.findViewById(R.id.errorTitle);
            errorMessage = errorBoundaryLayout.findViewById(R.id.errorMessage);
            retryButton = errorBoundaryLayout.findViewById(R.id.retryButton);
            reportButton = errorBoundaryLayout.findViewById(R.id.reportButton);
            
            setupClickListeners();
        }
    }
    
    private void setupClickListeners() {
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                Logger.d(TAG, "Retry button clicked for component: %s", componentName);
                if (retryListener != null) {
                    hideError();
                    retryListener.onRetry();
                } else {
                    Logger.w(TAG, "No retry listener set for component: %s", componentName);
                }
            });
        }
        
        if (reportButton != null) {
            reportButton.setOnClickListener(v -> {
                Logger.d(TAG, "Report button clicked for component: %s", componentName);
                if (reportListener != null) {
                    reportListener.onReport(componentName, null);
                } else {
                    Logger.w(TAG, "No report listener set for component: %s", componentName);
                }
            });
        }
    }
    
    /**
     * Set the main content view that will be wrapped by this error boundary
     */
    public void setMainContent(View content) {
        if (mainContentContainer != null) {
            ((FrameLayout) mainContentContainer).removeAllViews();
            ((FrameLayout) mainContentContainer).addView(content);
        }
    }
    
    /**
     * Set the component name for error reporting
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    /**
     * Set retry listener
     */
    public void setOnRetryListener(OnRetryListener listener) {
        this.retryListener = listener;
    }
    
    /**
     * Set report listener
     */
    public void setOnReportListener(OnReportListener listener) {
        this.reportListener = listener;
    }
    
    /**
     * Show error state with custom message
     */
    public void showError(String title, String message) {
        showError(title, message, null);
    }
    
    /**
     * Show error state with throwable
     */
    public void showError(String title, String message, @Nullable Throwable throwable) {
        Logger.e(TAG, "Showing error boundary for component: %s - %s", componentName, title);
        
        if (throwable != null) {
            Logger.e(TAG, "Error details", throwable);
        }
        
        isErrorState = true;
        
        // Hide main content
        if (mainContentContainer != null) {
            mainContentContainer.setVisibility(View.GONE);
        }
        
        // Show error layout
        if (errorBoundaryLayout != null) {
            errorBoundaryLayout.setVisibility(View.VISIBLE);
            
            if (errorTitle != null) {
                errorTitle.setText(title != null ? title : getContext().getString(R.string.error_component_crashed));
            }
            
            if (errorMessage != null) {
                errorMessage.setText(message != null ? message : getContext().getString(R.string.error_component_crashed_description));
            }
        }
        
        // Log detailed error for debugging
        if (throwable != null) {
            AuthError authError = ErrorHandler.createFallbackError(componentName, throwable);
            ErrorHandler.logDetailedError("ErrorBoundary:" + componentName, authError);
        }
    }
    
    /**
     * Show error from AuthError
     */
    public void showError(AuthError error) {
        String title = getContext().getString(R.string.error_component_crashed);
        String message = error.getUserFriendlyMessage();
        
        showError(title, message, error.getCause());
    }
    
    /**
     * Hide error state and show main content
     */
    public void hideError() {
        Logger.d(TAG, "Hiding error boundary for component: %s", componentName);
        
        isErrorState = false;
        
        // Show main content
        if (mainContentContainer != null) {
            mainContentContainer.setVisibility(View.VISIBLE);
        }
        
        // Hide error layout
        if (errorBoundaryLayout != null) {
            errorBoundaryLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * Check if currently in error state
     */
    public boolean isInErrorState() {
        return isErrorState;
    }
    
    /**
     * Catch and handle exceptions from child components
     */
    public void executeWithErrorHandling(Runnable operation) {
        executeWithErrorHandling(operation, "Unknown operation");
    }
    
    /**
     * Catch and handle exceptions from child components with operation name
     */
    public void executeWithErrorHandling(Runnable operation, String operationName) {
        try {
            operation.run();
        } catch (Exception e) {
            Logger.e(TAG, "Error in component %s during operation: %s", componentName, operationName, e);
            
            String title = getContext().getString(R.string.error_operation_failed);
            String message = String.format("Failed to %s. Please try again.", operationName);
            
            showError(title, message, e);
        }
    }
    
    /**
     * Static method to wrap any view with error boundary
     */
    public static ErrorBoundary wrap(Context context, View content, String componentName) {
        ErrorBoundary errorBoundary = new ErrorBoundary(context);
        errorBoundary.setComponentName(componentName);
        errorBoundary.setMainContent(content);
        return errorBoundary;
    }
}