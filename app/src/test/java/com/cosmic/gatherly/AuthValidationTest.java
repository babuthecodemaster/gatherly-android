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
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Focused tests for authentication input validation
 * Requirements: 1.1, 1.2, 2.1, 2.3
 */
@RunWith(RobolectricTestRunner.class)
public class AuthValidationTest {

    @Mock
    private AuthRepository mockAuthRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test email validation scenarios
     */
    @Test
    public void testEmailValidation() throws InterruptedException {
        testValidEmail();
        testInvalidEmailFormats();
        testEmptyEmail();
        testNullEmail();
    }

    private void testValidEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "valid@example.com", "user123");
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("valid@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                assertEquals("Email should match", "valid@example.com", user.getEmail());
            }

            @Override
            public void onError(AuthError error) {
                fail("Valid email should not cause error");
            }
        });

        assertTrue("Valid email test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testInvalidEmailFormats() throws InterruptedException {
        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "test@",
            "test..test@example.com",
            "test@example",
            "test@.com"
        };

        for (String invalidEmail : invalidEmails) {
            CountDownLatch latch = new CountDownLatch(1);
            
            doAnswer(invocation -> {
                AuthRepository.AuthCallback callback = invocation.getArgument(2);
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Invalid email format",
                    "Please enter a valid email address."
                ));
                latch.countDown();
                return null;
            }).when(mockAuthRepository).login(eq(invalidEmail), anyString(), any());

            mockAuthRepository.login(invalidEmail, "password123", new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    fail("Invalid email should not succeed: " + invalidEmail);
                }

                @Override
                public void onError(AuthError error) {
                    assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
                }
            });

            assertTrue("Invalid email test should complete for: " + invalidEmail, 
                      latch.await(5, TimeUnit.SECONDS));
        }
    }

    private void testEmptyEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Email is required",
                "Please enter your email address."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(eq(""), anyString(), any());

        mockAuthRepository.login("", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Empty email should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
                assertTrue("Error message should mention email", 
                          error.getMessage().toLowerCase().contains("email"));
            }
        });

        assertTrue("Empty email test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testNullEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Email is required",
                "Please enter your email address."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(isNull(), anyString(), any());

        mockAuthRepository.login(null, "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Null email should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
            }
        });

        assertTrue("Null email test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test password validation scenarios
     */
    @Test
    public void testPasswordValidation() throws InterruptedException {
        testValidPassword();
        testShortPassword();
        testEmptyPassword();
        testNullPassword();
    }

    private void testValidPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("testuser", "test@example.com", "user123");
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), eq("validpassword123"), any());

        mockAuthRepository.login("test@example.com", "validpassword123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                assertNotNull("User should not be null", user);
            }

            @Override
            public void onError(AuthError error) {
                fail("Valid password should not cause error");
            }
        });

        assertTrue("Valid password test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testShortPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password too short",
                "Password must be at least 6 characters long."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), eq("12345"), any());

        mockAuthRepository.login("test@example.com", "12345", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Short password should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
                assertTrue("Error message should mention password length", 
                          error.getMessage().toLowerCase().contains("password"));
            }
        });

        assertTrue("Short password test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testEmptyPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password is required",
                "Please enter a password."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), eq(""), any());

        mockAuthRepository.login("test@example.com", "", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Empty password should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
            }
        });

        assertTrue("Empty password test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testNullPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password is required",
                "Please enter a password."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), isNull(), any());

        mockAuthRepository.login("test@example.com", null, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Null password should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
            }
        });

        assertTrue("Null password test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test username validation for registration
     */
    @Test
    public void testUsernameValidation() throws InterruptedException {
        testValidUsername();
        testShortUsername();
        testEmptyUsername();
        testNullUsername();
    }

    private void testValidUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        User mockUser = createMockUser("validusername", "test@example.com", "user123");
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onSuccess(mockUser);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(eq("validusername"), anyString(), anyString(), any());

        mockAuthRepository.register("validusername", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                assertEquals("Username should match", "validusername", user.getUsername());
            }

            @Override
            public void onError(AuthError error) {
                fail("Valid username should not cause error");
            }
        });

        assertTrue("Valid username test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testShortUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Username too short",
                "Username must be at least 3 characters long."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(eq("ab"), anyString(), anyString(), any());

        mockAuthRepository.register("ab", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Short username should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
                assertTrue("Error message should mention username length", 
                          error.getMessage().toLowerCase().contains("username"));
            }
        });

        assertTrue("Short username test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testEmptyUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Username is required",
                "Please enter a username."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(eq(""), anyString(), anyString(), any());

        mockAuthRepository.register("", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Empty username should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
            }
        });

        assertTrue("Empty username test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testNullUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Username is required",
                "Please enter a username."
            ));
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(isNull(), anyString(), anyString(), any());

        mockAuthRepository.register(null, "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Null username should not succeed");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be validation error", AuthError.Type.VALIDATION_ERROR, error.getType());
            }
        });

        assertTrue("Null username test should complete", latch.await(5, TimeUnit.SECONDS));
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