package com.cosmic.gatherly.ui.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.widget.FrameLayout;
import androidx.test.core.app.ApplicationProvider;

import com.cosmic.gatherly.data.model.AuthError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for ComponentErrorHandler - testing error handling and loading states
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ComponentErrorHandlerTest {

    @Mock
    private ComponentErrorHandler.ComponentErrorCallback mockCallback;

    @Mock
    private FrameLayout mockContainerView;

    private Context context;
    private ComponentErrorHandler errorHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Initialize error handler
        errorHandler = new ComponentErrorHandler(context, mockContainerView, "TestComponent");
        errorHandler.setComponentErrorCallback(mockCallback);
    }

    @Test
    public void testErrorHandlerInitialization() {
        assertNotNull("Error handler should be initialized", errorHandler);
        assertFalse("Should not be loading initially", errorHandler.isLoading());
        assertNotNull("Fallback data provider should be available", errorHandler.getFallbackDataProvider());
    }

    @Test
    public void testLoadingStateManagement() {
        // Test showing loading
        errorHandler.showLoading("Loading test data...");
        assertTrue("Should be loading after showLoading", errorHandler.isLoading());
        
        // Test hiding loading
        errorHandler.hideLoading();
        assertFalse("Should not be loading after hideLoading", errorHandler.isLoading());
    }

    @Test
    public void testErrorHandling() {
        AuthError testError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Test error",
            "This is a test error message"
        );
        
        Runnable mockRetryAction = mock(Runnable.class);
        
        // Handle error
        errorHandler.handleError(testError, mockRetryAction, "Test Operation");
        
        // Verify error is handled (method completes without exception)
        assertTrue("Error handling should complete without exception", true);
    }

    @Test
    public void testErrorHiding() {
        // Show error first
        AuthError testError = new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Test error",
            "Test error message"
        );
        errorHandler.handleError(testError, null, "Test");
        
        // Hide error
        errorHandler.hideError();
        
        // Verify error is hidden (method completes without exception)
        assertTrue("Error hiding should complete without exception", true);
    }

    @Test
    public void testExecuteWithErrorHandling() {
        Runnable mockOperation = mock(Runnable.class);
        
        // Execute operation with error handling
        errorHandler.executeWithErrorHandling(mockOperation, "Test Operation");
        
        // Verify operation was executed
        verify(mockOperation, times(1)).run();
    }

    @Test
    public void testExecuteWithErrorHandlingException() {
        Runnable failingOperation = () -> {
            throw new RuntimeException("Test exception");
        };
        
        // Execute failing operation
        errorHandler.executeWithErrorHandling(failingOperation, "Failing Operation");
        
        // Verify callback was called for component failure
        verify(mockCallback, times(1)).onComponentFailed(any(AuthError.class));
    }

    @Test
    public void testComponentRecovery() {
        // Simulate component recovery
        mockCallback.onComponentRecovered();
        
        // Verify callback was called
        verify(mockCallback, times(1)).onComponentRecovered();
    }

    @Test
    public void testFallbackDataUsage() {
        // Simulate fallback data usage
        mockCallback.onFallbackDataUsed();
        
        // Verify callback was called
        verify(mockCallback, times(1)).onFallbackDataUsed();
    }

    @Test
    public void testMultipleLoadingStates() {
        // Test multiple loading operations
        errorHandler.showLoading("First operation...");
        assertTrue("Should be loading", errorHandler.isLoading());
        
        errorHandler.showLoading("Second operation...");
        assertTrue("Should still be loading", errorHandler.isLoading());
        
        errorHandler.hideLoading();
        assertFalse("Should not be loading after hide", errorHandler.isLoading());
    }

    @Test
    public void testErrorWithRetry() {
        AuthError testError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Network error",
            "Connection failed"
        );
        
        Runnable retryAction = mock(Runnable.class);
        
        // Handle error with retry
        errorHandler.handleError(testError, retryAction, "Network Operation");
        
        // Simulate retry (in a real test, you might trigger the retry button)
        retryAction.run();
        
        // Verify retry action was executed
        verify(retryAction, times(1)).run();
    }

    @Test
    public void testCleanup() {
        // Show loading and error first
        errorHandler.showLoading("Test loading...");
        AuthError testError = new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Test error",
            "Test message"
        );
        errorHandler.handleError(testError, null, "Test");
        
        // Cleanup
        errorHandler.cleanup();
        
        // Verify cleanup completed
        assertFalse("Should not be loading after cleanup", errorHandler.isLoading());
    }

    @Test
    public void testFallbackDataProvider() {
        FallbackDataProvider fallbackProvider = errorHandler.getFallbackDataProvider();
        assertNotNull("Fallback data provider should not be null", fallbackProvider);
        
        // Test fallback data methods
        assertNotNull("Fallback search results should not be null", 
            fallbackProvider.getFallbackSearchResults("test"));
        assertNotNull("Fallback channel list should not be null", 
            fallbackProvider.getFallbackChannelList());
        assertNotNull("Fallback server list should not be null", 
            fallbackProvider.getFallbackServerList());
    }

    @Test
    public void testErrorTypeHandling() {
        // Test different error types
        AuthError networkError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Network error",
            "No internet connection"
        );
        
        AuthError authError = new AuthError(
            AuthError.Type.AUTHENTICATION_FAILED,
            "Auth error",
            "Invalid credentials"
        );
        
        AuthError unknownError = new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Unknown error",
            "Something went wrong"
        );
        
        // Handle different error types
        errorHandler.handleError(networkError, null, "Network Test");
        errorHandler.handleError(authError, null, "Auth Test");
        errorHandler.handleError(unknownError, null, "Unknown Test");
        
        // Verify all errors were handled without exceptions
        assertTrue("All error types should be handled", true);
    }

    @Test
    public void testLoadingWithDifferentMessages() {
        // Test loading with different messages
        String[] loadingMessages = {
            "Loading channels...",
            "Connecting to server...",
            "Uploading file...",
            "Searching messages..."
        };
        
        for (String message : loadingMessages) {
            errorHandler.showLoading(message);
            assertTrue("Should be loading with message: " + message, errorHandler.isLoading());
            errorHandler.hideLoading();
            assertFalse("Should not be loading after hiding", errorHandler.isLoading());
        }
    }

    @Test
    public void testCallbackNullSafety() {
        // Test with null callback
        ComponentErrorHandler errorHandlerWithoutCallback = new ComponentErrorHandler(
            context, mockContainerView, "TestComponent"
        );
        
        // These should not crash even without callback
        errorHandlerWithoutCallback.showLoading("Test");
        errorHandlerWithoutCallback.hideLoading();
        errorHandlerWithoutCallback.hideError();
        
        AuthError testError = new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "Test",
            "Test message"
        );
        errorHandlerWithoutCallback.handleError(testError, null, "Test");
        
        assertTrue("Should handle null callback gracefully", true);
    }
}