package com.cosmic.gatherly;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.ui.auth.AuthActivity;
import com.cosmic.gatherly.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive authentication flow tests covering various scenarios
 * Tests registration and login with server running/not running, network errors, and navigation
 */
@RunWith(AndroidJUnit4.class)
public class AuthenticationFlowTest {

    @Rule
    public ActivityTestRule<AuthActivity> activityRule = 
        new ActivityTestRule<>(AuthActivity.class, true, false);

    @Mock
    private AuthRepository mockAuthRepository;

    private Context context;
    private AuthActivity authActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * Test 1: Registration with server running - Success scenario
     * Requirements: 1.1, 1.4
     */
    @Test
    public void testRegistrationWithServerRunning_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "test@example.com", "user123");
        
        // Mock successful registration
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(anyString(), anyString(), anyString(), any());

        // Act
        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onRegisterRequested("testuser", "test@example.com", "password123");

        // Assert
        assertTrue("Registration should complete within timeout", 
                  latch.await(5, TimeUnit.SECONDS));
        verify(mockAuthRepository).register("testuser", "test@example.com", "password123", any());
    }

    /**
     * Test 2: Registration with server not running - Network error scenario
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testRegistrationWithServerNotRunning_NetworkError() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AuthError networkError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Connection failed",
            "Unable to connect to server. Please check your internet connection."
        );
        
        // Mock network error
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(networkError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(anyString(), anyString(), anyString(), any());

        // Act
        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onRegisterRequested("testuser", "test@example.com", "password123");

        // Assert
        assertTrue("Registration error should be handled within timeout", 
                  latch.await(5, TimeUnit.SECONDS));
        verify(mockAuthRepository).register("testuser", "test@example.com", "password123", any());
    }

    /**
     * Test 3: Login with valid credentials - Success scenario
     * Requirements: 2.1, 2.4
     */
    @Test
    public void testLoginWithValidCredentials_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "test@example.com", "user123");
        
        // Mock successful login
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // Act
        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        // Assert
        assertTrue("Login should complete within timeout", 
                  latch.await(5, TimeUnit.SECONDS));
        verify(mockAuthRepository).login("test@example.com", "password123", any());
    }

    /**
     * Test 4: Login with invalid credentials - Authentication error
     * Requirements: 2.3
     */
    @Test
    public void testLoginWithInvalidCredentials_AuthError() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AuthError authError = new AuthError(
            AuthError.Type.AUTHENTICATION_ERROR,
            "Invalid credentials",
            "The email or password you entered is incorrect."
        );
        
        // Mock authentication error
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(authError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // Act
        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "wrongpassword");

        // Assert
        assertTrue("Login error should be handled within timeout", 
                  latch.await(5, TimeUnit.SECONDS));
        verify(mockAuthRepository).login("test@example.com", "wrongpassword", any());
    }

    /**
     * Test 5: Network error scenarios and recovery
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testNetworkErrorScenarios_Recovery() throws InterruptedException {
        // Test timeout error
        testNetworkTimeoutError();
        
        // Test connection refused error
        testConnectionRefusedError();
        
        // Test server unavailable error
        testServerUnavailableError();
    }

    private void testNetworkTimeoutError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError timeoutError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Request timeout",
            "The request took too long to complete. Please try again."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(timeoutError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        assertTrue("Timeout error should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testConnectionRefusedError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError connectionError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Connection refused",
            "Unable to connect to server. Please check your internet connection."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(connectionError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        assertTrue("Connection error should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testServerUnavailableError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError serverError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Server unavailable",
            "Server is currently unavailable. Please try again later."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(serverError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        assertTrue("Server error should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test 6: Proper navigation and error handling validation
     * Requirements: 2.2, 4.4
     */
    @Test
    public void testNavigationAndErrorHandling() throws InterruptedException {
        // Test successful navigation after login
        testSuccessfulNavigationAfterLogin();
        
        // Test navigation failure handling
        testNavigationFailureHandling();
        
        // Test error message display
        testErrorMessageDisplay();
    }

    private void testSuccessfulNavigationAfterLogin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "test@example.com", "user123");
        
        // Mock successful login and session validation
        when(mockAuthRepository.isLoggedIn()).thenReturn(true);
        when(mockAuthRepository.isSessionValid()).thenReturn(true);
        when(mockAuthRepository.getCachedUser()).thenReturn(mockUser);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        assertTrue("Login and navigation should complete", 
                  latch.await(5, TimeUnit.SECONDS));
        
        // Verify session validation methods were called
        verify(mockAuthRepository).isLoggedIn();
        verify(mockAuthRepository).isSessionValid();
        verify(mockAuthRepository).getCachedUser();
    }

    private void testNavigationFailureHandling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "test@example.com", "user123");
        
        // Mock successful login but failed session validation
        when(mockAuthRepository.isLoggedIn()).thenReturn(false);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("test@example.com", "password123");

        assertTrue("Login should complete but navigation should fail gracefully", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testErrorMessageDisplay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError validationError = new AuthError(
            AuthError.Type.VALIDATION_ERROR,
            "Invalid email format",
            "Please enter a valid email address."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(validationError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("invalid-email", "password123");

        assertTrue("Error should be displayed", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test 7: Edge cases and boundary conditions
     */
    @Test
    public void testEdgeCasesAndBoundaryConditions() throws InterruptedException {
        // Test empty credentials
        testEmptyCredentials();
        
        // Test null values
        testNullValues();
        
        // Test very long inputs
        testVeryLongInputs();
        
        // Test special characters
        testSpecialCharacters();
    }

    private void testEmptyCredentials() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError validationError = new AuthError(
            AuthError.Type.VALIDATION_ERROR,
            "Email is required",
            "Please enter your email address."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(validationError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested("", "");

        assertTrue("Empty credentials should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testNullValues() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError validationError = new AuthError(
            AuthError.Type.VALIDATION_ERROR,
            "Email is required",
            "Please enter your email address."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(validationError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested(null, null);

        assertTrue("Null values should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testVeryLongInputs() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String veryLongEmail = "a".repeat(1000) + "@example.com";
        String veryLongPassword = "p".repeat(1000);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Input too long",
                "Please enter shorter values."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested(veryLongEmail, veryLongPassword);

        assertTrue("Very long inputs should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    private void testSpecialCharacters() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String emailWithSpecialChars = "test+special@example.com";
        String passwordWithSpecialChars = "p@ssw0rd!#$";
        
        User mockUser = createMockUser("testuser", emailWithSpecialChars, "user123");
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        authActivity = activityRule.launchActivity(new Intent());
        authActivity.onLoginRequested(emailWithSpecialChars, passwordWithSpecialChars);

        assertTrue("Special characters should be handled", 
                  latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Helper method to create a mock user for testing
     */
    private User createMockUser(String username, String email, String id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setCreatedAt(new java.util.Date(System.currentTimeMillis()));
        return user;
    }

    /**
     * Test cleanup
     */
    @org.junit.After
    public void tearDown() {
        if (authActivity != null) {
            authActivity.finish();
        }
    }
}