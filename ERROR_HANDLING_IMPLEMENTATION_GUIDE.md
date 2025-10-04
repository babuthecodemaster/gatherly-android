# Error Handling and Loading States Implementation Guide

This document provides a comprehensive guide to the enhanced error handling and loading states implementation for the Gatherly Android application.

## Overview

The implementation includes the following components:

1. **ErrorBoundary** - Handles component failures gracefully
2. **LoadingStateManager** - Manages loading states consistently
3. **RetryManager** - Implements retry mechanisms with exponential backoff
4. **FallbackDataProvider** - Provides fallback data when services are unavailable
5. **ComponentErrorHandler** - Integrates all components for comprehensive error handling
6. **Enhanced Service Handlers** - Demonstrates integration with file upload and search

## Components

### 1. ErrorBoundary

Provides fallback UI when components crash or fail to load.

```java
// Basic usage
ErrorBoundary errorBoundary = new ErrorBoundary(context);
errorBoundary.setComponentName("MyComponent");
errorBoundary.setMainContent(myView);

// Set up listeners
errorBoundary.setOnRetryListener(() -> {
    // Handle retry
});

errorBoundary.setOnReportListener((componentName, error) -> {
    // Handle error reporting
});

// Show error
errorBoundary.showError("Error Title", "Error message");

// Hide error
errorBoundary.hideError();
```

### 2. LoadingStateManager

Manages loading states for UI components.

```java
// Initialize
LoadingStateManager loadingManager = new LoadingStateManager(context, containerView, "ComponentName");

// Show loading
loadingManager.showLoading();
loadingManager.showLoading("Custom loading message");

// Hide loading
loadingManager.hideLoading();

// Execute with loading
loadingManager.executeWithLoading(() -> {
    // Your operation here
}, "Loading message");
```

### 3. RetryManager

Implements retry mechanisms for network failures.

```java
// Create retry manager
RetryManager retryManager = RetryManager.forNetworkOperation("MyOperation");

// Set callback
retryManager.setRetryCallback(new RetryManager.RetryCallback() {
    @Override
    public void onRetryAttempt(int attemptNumber) {
        // Handle retry attempt
    }
    
    @Override
    public void onRetrySuccess() {
        // Handle success
    }
    
    @Override
    public void onRetryFailed(AuthError error) {
        // Handle failure
    }
    
    @Override
    public void onMaxRetriesExceeded() {
        // Handle max retries exceeded
    }
});

// Execute with retry
retryManager.executeWithRetry(new RetryManager.RetryableOperation() {
    @Override
    public void execute(RetryCallback callback) throws Exception {
        // Your operation here
        // Call callback.onSuccess() or callback.onError(error)
    }
});
```

### 4. FallbackDataProvider

Provides fallback data when network requests fail.

```java
FallbackDataProvider fallbackProvider = FallbackDataProvider.getInstance(context);

// Get fallback data
List<Server> fallbackServers = fallbackProvider.getFallbackServers();
List<Channel> fallbackChannels = fallbackProvider.getFallbackChannels(serverId);
List<Message> fallbackMessages = fallbackProvider.getFallbackMessages(channelId);

// Check if should use fallback
boolean shouldUseFallback = fallbackProvider.shouldUseFallbackData(error);
```

### 5. ComponentErrorHandler

Comprehensive error handler that integrates all components.

```java
// Initialize
ComponentErrorHandler errorHandler = new ComponentErrorHandler(context, containerView, "ComponentName");

// Set callback
errorHandler.setComponentErrorCallback(new ComponentErrorHandler.ComponentErrorCallback() {
    @Override
    public void onComponentRecovered() {
        // Handle recovery
    }
    
    @Override
    public void onComponentFailed(AuthError error) {
        // Handle failure
    }
    
    @Override
    public void onFallbackDataUsed() {
        // Handle fallback data usage
    }
});

// Execute with error handling
errorHandler.executeWithErrorHandling(() -> {
    // Your operation here
}, "Operation Name");

// Handle specific errors
errorHandler.handleError(authError, retryOperation, "Operation Name");
```

## Enhanced Service Implementations

### File Upload with Error Handling

```java
// Initialize enhanced file upload handler
EnhancedFileUploadHandler uploadHandler = new EnhancedFileUploadHandler(context, containerView);

// Set file info
uploadHandler.setFileInfo("document.pdf", "2.5 MB");

// Upload with comprehensive error handling
uploadHandler.uploadFile(fileUri, channelId, new EnhancedFileUploadHandler.FileUploadCallback() {
    @Override
    public void onUploadSuccess(FileAttachment attachment) {
        // Handle success
    }
    
    @Override
    public void onUploadFailed(AuthError error) {
        // Handle failure
    }
    
    @Override
    public void onUploadCancelled() {
        // Handle cancellation
    }
});
```

### Search with Error Handling

```java
// Initialize enhanced search handler
EnhancedSearchHandler searchHandler = new EnhancedSearchHandler(context, containerView);

// Show search dialog
searchHandler.showSearchDialog(channelId, channelName, new EnhancedSearchHandler.SearchCallback() {
    @Override
    public void onSearchResults(List<SearchResult> results, String query) {
        // Handle results
    }
    
    @Override
    public void onSearchFailed(AuthError error) {
        // Handle failure
    }
    
    @Override
    public void onSearchCancelled() {
        // Handle cancellation
    }
});
```

## Integration Examples

### Basic Component Integration

```java
public class MyFragment extends Fragment {
    private ComponentErrorHandler errorHandler;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        
        // Initialize error handler
        FrameLayout containerView = view.findViewById(R.id.container);
        errorHandler = new ComponentErrorHandler(getContext(), containerView, "MyFragment");
        
        // Set up error handling
        setupErrorHandling();
        
        return view;
    }
    
    private void setupErrorHandling() {
        errorHandler.setComponentErrorCallback(new ComponentErrorHandler.ComponentErrorCallback() {
            @Override
            public void onComponentRecovered() {
                // Reload data or refresh UI
                loadData();
            }
            
            @Override
            public void onComponentFailed(AuthError error) {
                // Show error message or navigate away
                Toast.makeText(getContext(), error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onFallbackDataUsed() {
                // Show offline indicator
                showOfflineIndicator();
            }
        });
    }
    
    private void loadData() {
        errorHandler.executeWithErrorHandling(() -> {
            // Your data loading logic here
            performNetworkRequest();
        }, "Data Loading");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (errorHandler != null) {
            errorHandler.cleanup();
        }
    }
}
```

### Activity Integration

```java
public class MyActivity extends AppCompatActivity {
    private ComponentErrorHandler errorHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Initialize error handler
        FrameLayout containerView = findViewById(R.id.main_container);
        errorHandler = new ComponentErrorHandler(this, containerView, "MyActivity");
        
        // Set main content
        View mainContent = getLayoutInflater().inflate(R.layout.main_content, null);
        errorHandler.setMainContent(mainContent);
        
        // Load initial data
        loadInitialData();
    }
    
    private void loadInitialData() {
        errorHandler.executeWithErrorHandling(() -> {
            // Load data with automatic error handling
            fetchDataFromServer();
        }, "Initial Data Loading");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (errorHandler != null) {
            errorHandler.cleanup();
        }
    }
}
```

## Best Practices

### 1. Component Naming
Always provide meaningful component names for better logging and debugging:
```java
ComponentErrorHandler errorHandler = new ComponentErrorHandler(context, containerView, "UserProfileFragment");
```

### 2. Error Categorization
Use appropriate error types for better handling:
```java
AuthError networkError = new AuthError(AuthError.Type.NETWORK_ERROR, "Connection failed", "Please check your internet connection");
AuthError validationError = new AuthError(AuthError.Type.VALIDATION_ERROR, "Invalid input", "Please check your input and try again");
```

### 3. Fallback Data
Provide meaningful fallback data:
```java
// Good - provides useful fallback
List<Message> fallbackMessages = fallbackProvider.getFallbackMessages(channelId);

// Better - customize fallback for specific scenarios
if (isOfflineMode()) {
    fallbackMessages = getCachedMessages(channelId);
} else {
    fallbackMessages = fallbackProvider.getFallbackMessages(channelId);
}
```

### 4. Resource Cleanup
Always clean up resources in lifecycle methods:
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (errorHandler != null) {
        errorHandler.cleanup();
    }
    if (uploadHandler != null) {
        uploadHandler.cleanup();
    }
    if (searchHandler != null) {
        searchHandler.cleanup();
    }
}
```

### 5. User Feedback
Provide clear, actionable feedback to users:
```java
// Good
errorHandler.showError("Upload Failed", "File upload failed. Please check your connection and try again.");

// Better - with retry option
errorHandler.showError("Upload Failed", "File upload failed. Please check your connection and try again.");
errorHandler.getErrorBoundary().setOnRetryListener(() -> {
    retryUpload();
});
```

## Testing

### Unit Testing Error Handling

```java
@Test
public void testErrorHandling() {
    ComponentErrorHandler errorHandler = new ComponentErrorHandler(context, containerView, "TestComponent");
    
    // Test error display
    AuthError testError = new AuthError(AuthError.Type.NETWORK_ERROR, "Test error", "Test message");
    errorHandler.showError(testError);
    
    assertTrue(errorHandler.isInErrorState());
    
    // Test recovery
    errorHandler.hideError();
    assertFalse(errorHandler.isInErrorState());
}
```

### Integration Testing

```java
@Test
public void testFileUploadWithErrorHandling() {
    EnhancedFileUploadHandler uploadHandler = new EnhancedFileUploadHandler(context, containerView);
    
    // Mock file upload failure
    when(fileUploadService.uploadFile(any(), any(), any())).thenThrow(new IOException("Network error"));
    
    // Test error handling
    uploadHandler.uploadFile(mockUri, "channelId", mockCallback);
    
    // Verify error handling was triggered
    verify(mockCallback).onUploadFailed(any(AuthError.class));
}
```

## Conclusion

This comprehensive error handling implementation provides:

1. **Graceful Degradation** - Components continue to function even when errors occur
2. **User-Friendly Feedback** - Clear, actionable error messages for users
3. **Automatic Recovery** - Retry mechanisms for transient failures
4. **Offline Support** - Fallback data when network is unavailable
5. **Consistent Experience** - Unified error handling across the application

The implementation ensures that the Gatherly app remains functional and user-friendly even when network conditions are poor or services are temporarily unavailable.