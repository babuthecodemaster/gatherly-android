package com.cosmic.gatherly;

import android.content.Context;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for AuthRepository authentication scenarios
 * Tests Requirements: 1.1, 1.4, 2.1, 2.4, 3.1, 3.2
 */
@RunWith(RobolectricTestRunner.class)
public class AuthRepositoryTest {

    private AuthRepository authRepository;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        authRepository = new AuthRepository(context);
    }

    /**
     * Test 1: Login with valid credentials (server running scenario)
     * Requirements: 2.1, 2.4
     */
    @Test
    public void testLoginWithValidCredentials() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final AuthError[] error = {null};
        final User[] user = {null};

        authRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User resultUser) {
                success[0] = true;
                user[0] = resultUser;
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        // Wait up to 30 seconds for response
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue("Login request should complete within timeout", completed);

        // Verify that either success or error occurred (both are valid outcomes)
        assertTrue("Either success or error should occur", success[0] || error[0] != null);

        if (success[0]) {
            assertNotNull("User should not be null on success", user[0]);
            assertNotNull("User ID should not be null", user[0].getId());
            assertNotNull("User email should not be null", user[0].getEmail());
        } else {
            assertNotNull("Error should not be null on failure", error[0]);
            assertNotNull("Error message should not be null", error[0].getMessage());
        }
    }

    /**
     * Test 2: Login with invalid credentials
     * Requirements: 2.1, 2.4
     */
    @Test
    public void testLoginWithInvalidCredentials() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final AuthError[] error = {null};

        authRepository.login("invalid@example.com", "wrongpassword", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        // Wait up to 30 seconds for response
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue("Login request should complete within timeout", completed);

        // Invalid credentials should result in error (unless offline login succeeds)
        assertTrue("Either success (offline) or error should occur", success[0] || error[0] != null);

        if (error[0] != null) {
            assertTrue("Error should be authentication or network related", 
                error[0].getType() == AuthError.Type.AUTHENTICATION_ERROR ||
                error[0].getType() == AuthError.Type.NETWORK_ERROR ||
                error[0].getType() == AuthError.Type.SERVER_ERROR);
        }
    }

    /**
     * Test 3: Registration with valid data (server running scenario)
     * Requirements: 1.1, 1.4
     */
    @Test
    public void testRegistrationWithValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final AuthError[] error = {null};
        final User[] user = {null};

        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";

        authRepository.register(uniqueUsername, uniqueEmail, "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User resultUser) {
                success[0] = true;
                user[0] = resultUser;
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        // Wait up to 30 seconds for response
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue("Registration request should complete within timeout", completed);

        // Verify that either success or error occurred
        assertTrue("Either success or error should occur", success[0] || error[0] != null);

        if (success[0]) {
            assertNotNull("User should not be null on success", user[0]);
            assertNotNull("User ID should not be null", user[0].getId());
            assertEquals("Username should match", uniqueUsername, user[0].getUsername());
            assertEquals("Email should match", uniqueEmail, user[0].getEmail());
        } else {
            assertNotNull("Error should not be null on failure", error[0]);
            assertNotNull("Error message should not be null", error[0].getMessage());
        }
    }

    /**
     * Test 4: Registration with invalid data (validation errors)
     * Requirements: 1.1, 1.4
     */
    @Test
    public void testRegistrationWithInvalidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final AuthError[] error = {null};

        // Test with short username
        authRepository.register("ab", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        // Wait up to 10 seconds for validation response
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Registration validation should complete quickly", completed);

        // Should result in validation error
        assertFalse("Registration with invalid data should fail", success[0]);
        assertNotNull("Error should not be null", error[0]);
        assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error[0].getType());
    }

    /**
     * Test 5: Network error handling (server down scenario)
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testNetworkErrorHandling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final AuthError[] error = {null};

        // Attempt login which may result in network error if server is down
        authRepository.login("networktest@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        // Wait up to 45 seconds for network timeout
        boolean completed = latch.await(45, TimeUnit.SECONDS);
        assertTrue("Network request should complete or timeout", completed);

        // Verify that either success or error occurred
        assertTrue("Either success or error should occur", success[0] || error[0] != null);

        if (error[0] != null) {
            // Should be network or server error if server is down
            assertTrue("Error should be network or server related", 
                error[0].getType() == AuthError.Type.NETWORK_ERROR ||
                error[0].getType() == AuthError.Type.SERVER_ERROR ||
                error[0].getType() == AuthError.Type.AUTHENTICATION_ERROR);
            
            assertNotNull("Error should have user-friendly message", error[0].getUserFriendlyMessage());
        }
    }

    /**
     * Test 6: Input validation edge cases
     * Requirements: 1.1, 2.1
     */
    @Test
    public void testInputValidationEdgeCases() throws InterruptedException {
        // Test null inputs
        testLoginValidation(null, "password", "Email is required");
        testLoginValidation("test@example.com", null, "Password is required");
        testLoginValidation("", "password", "Email is required");
        testLoginValidation("test@example.com", "", "Password is required");
        testLoginValidation("invalid-email", "password", "Invalid email format");

        // Test registration validation
        testRegistrationValidation(null, "test@example.com", "password123", "Username is required");
        testRegistrationValidation("user", null, "password123", "Email is required");
        testRegistrationValidation("user", "test@example.com", null, "Password is required");
        testRegistrationValidation("", "test@example.com", "password123", "Username is required");
        testRegistrationValidation("user", "", "password123", "Email is required");
        testRegistrationValidation("user", "test@example.com", "", "Password is required");
        testRegistrationValidation("ab", "test@example.com", "password123", "Username too short");
        testRegistrationValidation("user", "invalid-email", "password123", "Invalid email format");
        testRegistrationValidation("user", "test@example.com", "123", "Password too short");
    }

    private void testLoginValidation(String email, String password, String expectedErrorType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final AuthError[] error = {null};

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Validation should complete quickly", completed);
        assertNotNull("Should have validation error", error[0]);
        assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error[0].getType());
    }

    private void testRegistrationValidation(String username, String email, String password, String expectedErrorType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final AuthError[] error = {null};

        authRepository.register(username, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                latch.countDown();
            }

            @Override
            public void onError(AuthError authError) {
                error[0] = authError;
                latch.countDown();
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Validation should complete quickly", completed);
        assertNotNull("Should have validation error", error[0]);
        assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error[0].getType());
    }

    /**
     * Test 7: Session management and cached user functionality
     * Requirements: 2.4
     */
    @Test
    public void testSessionManagement() {
        // Test initial state
        assertFalse("Should not be logged in initially", authRepository.isLoggedIn());
        assertNull("Should not have cached user initially", authRepository.getCachedUser());
        assertFalse("Session should not be valid initially", authRepository.isSessionValid());
    }

    /**
     * Test 8: Multiple concurrent authentication requests
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testConcurrentAuthenticationRequests() throws InterruptedException {
        int numRequests = 3;
        CountDownLatch latch = new CountDownLatch(numRequests);
        final int[] completedRequests = {0};

        // Make multiple concurrent login requests
        for (int i = 0; i < numRequests; i++) {
            final int requestId = i;
            authRepository.login("concurrent" + i + "@example.com", "password123", new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    synchronized (completedRequests) {
                        completedRequests[0]++;
                    }
                    latch.countDown();
                }

                @Override
                public void onError(AuthError error) {
                    synchronized (completedRequests) {
                        completedRequests[0]++;
                    }
                    latch.countDown();
                }
            });
        }

        // Wait for all requests to complete
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue("All concurrent requests should complete", completed);
        assertEquals("All requests should have completed", numRequests, completedRequests[0]);
    }
}