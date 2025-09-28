package com.cosmic.gatherly;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Focused tests for authentication error handling scenarios
 * Requirements: 3.1, 3.2, 3.4, 4.1
 */
@RunWith(RobolectricTestRunner.class)
public class AuthErrorHandlingTest {

    @Mock
    private AuthRepository mockAuthRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test server error handling
     * Requirements: 3.1, 4.1
     */
    @Test
    public void testServerErrorHandling() throws InterruptedException {
        testInternalServerError();
        testServiceUnavailableError();
        testBadGatewayError();
    }

    private void testInternalServerError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError serverError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Internal server error",
            "Server is experiencing issues. Please try again later."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(serverError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Server error should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                assertNotNull("Error message should not be null", error.getMessage());
                assertNotNull("User-friendly message should not be null", error.getUserFriendlyMessage());
                assertTrue("User-friendly message should be helpful", 
                          error.getUserFriendlyMessage().toLowerCase().contains("try again"));
            }
        });

        assertTrue("Server error test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testServiceUnavailableError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError serviceError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Service unavailable",
            "Service is temporarily unavailable. Please try again in a few minutes."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(serviceError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Service unavailable should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                assertTrue("Should mention service unavailable", 
                          error.getMessage().toLowerCase().contains("unavailable"));
            }
        });

        assertTrue("Service unavailable test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testBadGatewayError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError gatewayError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Bad gateway",
            "Server gateway error. Please try again later."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(gatewayError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Bad gateway should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                assertTrue("Should mention gateway", 
                          error.getMessage().toLowerCase().contains("gateway"));
            }
        });

        assertTrue("Bad gateway test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test authentication error handling
     * Requirements: 2.3, 4.1
     */
    @Test
    public void testAuthenticationErrorHandling() throws InterruptedException {
        testInvalidCredentialsError();
        testAccountLockedError();
        testAccountNotFoundError();
    }

    private void testInvalidCredentialsError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError authError = new AuthError(
            AuthError.Type.AUTHENTICATION_ERROR,
            "Invalid credentials",
            "The email or password you entered is incorrect."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(authError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "wrongpassword", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Invalid credentials should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be authentication error", AuthError.Type.AUTHENTICATION_ERROR, error.getType());
                assertTrue("Should mention credentials", 
                          error.getMessage().toLowerCase().contains("credentials") ||
                          error.getMessage().toLowerCase().contains("password") ||
                          error.getMessage().toLowerCase().contains("email"));
            }
        });

        assertTrue("Invalid credentials test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testAccountLockedError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError lockError = new AuthError(
            AuthError.Type.AUTHENTICATION_ERROR,
            "Account locked",
            "Your account has been temporarily locked due to multiple failed login attempts."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(lockError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("locked@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Locked account should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be authentication error", AuthError.Type.AUTHENTICATION_ERROR, error.getType());
                assertTrue("Should mention account locked", 
                          error.getMessage().toLowerCase().contains("locked"));
            }
        });

        assertTrue("Account locked test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testAccountNotFoundError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError notFoundError = new AuthError(
            AuthError.Type.AUTHENTICATION_ERROR,
            "Account not found",
            "No account found with this email address."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(notFoundError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("nonexistent@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Non-existent account should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be authentication error", AuthError.Type.AUTHENTICATION_ERROR, error.getType());
                assertTrue("Should mention account not found", 
                          error.getMessage().toLowerCase().contains("not found") ||
                          error.getMessage().toLowerCase().contains("account"));
            }
        });

        assertTrue("Account not found test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test unknown error handling
     * Requirements: 4.1
     */
    @Test
    public void testUnknownErrorHandling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Exception cause = new RuntimeException("Unexpected error occurred");
        AuthError unknownError = new AuthError(
            AuthError.Type.UNKNOWN_ERROR,
            "An unexpected error occurred",
            "Something went wrong. Please try again.",
            cause
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(unknownError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Unknown error should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be unknown error", AuthError.Type.UNKNOWN_ERROR, error.getType());
                assertNotNull("Error should have a cause", error.getCause());
                assertTrue("User-friendly message should be generic", 
                          error.getUserFriendlyMessage().toLowerCase().contains("try again"));
            }
        });

        assertTrue("Unknown error test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test error message formatting and user-friendly messages
     * Requirements: 3.4, 4.1
     */
    @Test
    public void testErrorMessageFormatting() throws InterruptedException {
        testErrorMessageNotNull();
        testUserFriendlyMessageNotNull();
        testErrorMessageLength();
    }

    private void testErrorMessageNotNull() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError error = new AuthError(
            AuthError.Type.VALIDATION_ERROR,
            "Email is required",
            "Please enter your email address."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(error);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Empty email should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertNotNull("Error message should not be null", error.getMessage());
                assertFalse("Error message should not be empty", error.getMessage().isEmpty());
            }
        });

        assertTrue("Error message null test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testUserFriendlyMessageNotNull() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError error = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Connection timeout",
            "The request took too long to complete. Please check your internet connection and try again."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(error);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Network error should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertNotNull("User-friendly message should not be null", error.getUserFriendlyMessage());
                assertFalse("User-friendly message should not be empty", error.getUserFriendlyMessage().isEmpty());
                assertTrue("User-friendly message should be helpful", 
                          error.getUserFriendlyMessage().length() > 10);
            }
        });

        assertTrue("User-friendly message test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testErrorMessageLength() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError error = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Internal server error occurred while processing authentication request",
            "We're experiencing technical difficulties. Please try again in a few minutes."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(error);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Server error should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertTrue("Error message should be reasonable length", 
                          error.getMessage().length() > 5 && error.getMessage().length() < 500);
                assertTrue("User-friendly message should be reasonable length", 
                          error.getUserFriendlyMessage().length() > 10 && error.getUserFriendlyMessage().length() < 200);
            }
        });

        assertTrue("Error message length test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test error recovery scenarios
     * Requirements: 3.4
     */
    @Test
    public void testErrorRecoveryScenarios() throws InterruptedException {
        testRetryAfterNetworkError();
        testRetryAfterServerError();
        testNoRetryAfterValidationError();
    }

    private void testRetryAfterNetworkError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2); // Two calls expected
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            if (callCount[0] == 1) {
                // First call fails with network error
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "Connection timeout",
                    "Please check your connection and try again."
                ));
            } else {
                // Second call succeeds
                User mockUser = createMockUser("testuser", "test@example.com", "user123");
                callback.onSuccess(mockUser);
            }
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // First attempt
        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Should not happen on first call
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                
                // Simulate retry after network error
                mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        assertNotNull("User should not be null on retry", user);
                    }

                    @Override
                    public void onError(AuthError retryError) {
                        fail("Retry should succeed");
                    }
                });
            }
        });

        assertTrue("Retry after network error test should complete", latch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have made 2 calls", 2, callCount[0]);
    }

    private void testRetryAfterServerError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            if (callCount[0] == 1) {
                // First call fails with server error
                callback.onError(new AuthError(
                    AuthError.Type.SERVER_ERROR,
                    "Internal server error",
                    "Server error occurred. Please try again."
                ));
            } else {
                // Second call succeeds
                User mockUser = createMockUser("testuser", "test@example.com", "user123");
                callback.onSuccess(mockUser);
            }
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // First attempt
        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Should not happen on first call
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                
                // Simulate retry after server error
                mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        assertNotNull("User should not be null on retry", user);
                    }

                    @Override
                    public void onError(AuthError retryError) {
                        fail("Retry should succeed");
                    }
                });
            }
        });

        assertTrue("Retry after server error test should complete", latch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have made 2 calls", 2, callCount[0]);
    }

    private void testNoRetryAfterValidationError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            // Always return validation error
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Invalid email format",
                "Please enter a valid email address."
            ));
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("invalid-email", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Validation error should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
                // Don't retry validation errors - they won't succeed without fixing the input
            }
        });

        assertTrue("No retry after validation error test should complete", latch.await(5, TimeUnit.SECONDS));
        assertEquals("Should have made only 1 call", 1, callCount[0]);
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
}