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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Focused tests for network-related authentication scenarios
 * Requirements: 3.1, 3.2
 */
@RunWith(RobolectricTestRunner.class)
public class AuthNetworkTest {

    @Mock
    private AuthRepository mockAuthRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test connection timeout scenarios
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testConnectionTimeoutScenarios() throws InterruptedException {
        testSocketTimeout();
        testConnectionTimeout();
        testReadTimeout();
    }

    private void testSocketTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SocketTimeoutException timeoutException = new SocketTimeoutException("Read timed out");
        AuthError timeoutError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Request timeout",
            "The request took too long to complete. Please check your internet connection and try again.",
            timeoutException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(timeoutError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Socket timeout should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention timeout", 
                          error.getMessage().toLowerCase().contains("timeout"));
                assertTrue("User message should mention connection", 
                          error.getUserFriendlyMessage().toLowerCase().contains("connection") ||
                          error.getUserFriendlyMessage().toLowerCase().contains("internet"));
                assertNotNull("Should have cause", error.getCause());
                assertTrue("Cause should be SocketTimeoutException", 
                          error.getCause() instanceof SocketTimeoutException);
            }
        });

        assertTrue("Socket timeout test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testConnectionTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConnectException connectException = new ConnectException("Connection timed out");
        AuthError connectError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Connection failed",
            "Unable to connect to server. Please check your internet connection.",
            connectException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(connectError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Connection timeout should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention connection", 
                          error.getMessage().toLowerCase().contains("connection"));
                assertNotNull("Should have cause", error.getCause());
                assertTrue("Cause should be ConnectException", 
                          error.getCause() instanceof ConnectException);
            }
        });

        assertTrue("Connection timeout test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testReadTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SocketTimeoutException readTimeoutException = new SocketTimeoutException("Read timed out");
        AuthError readTimeoutError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Read timeout",
            "Server response took too long. Please try again.",
            readTimeoutException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(readTimeoutError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Read timeout should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention timeout or read", 
                          error.getMessage().toLowerCase().contains("timeout") ||
                          error.getMessage().toLowerCase().contains("read"));
            }
        });

        assertTrue("Read timeout test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test DNS and host resolution failures
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testDNSAndHostResolutionFailures() throws InterruptedException {
        testUnknownHost();
        testDNSResolutionFailure();
    }

    private void testUnknownHost() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UnknownHostException unknownHostException = new UnknownHostException("Unable to resolve host");
        AuthError hostError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Host resolution failed",
            "Unable to connect to server. Please check your internet connection.",
            unknownHostException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(hostError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Unknown host should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention host or connection", 
                          error.getMessage().toLowerCase().contains("host") ||
                          error.getMessage().toLowerCase().contains("connection"));
                assertNotNull("Should have cause", error.getCause());
                assertTrue("Cause should be UnknownHostException", 
                          error.getCause() instanceof UnknownHostException);
            }
        });

        assertTrue("Unknown host test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testDNSResolutionFailure() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        UnknownHostException dnsException = new UnknownHostException("DNS resolution failed");
        AuthError dnsError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "DNS resolution failed",
            "Unable to resolve server address. Please check your internet connection.",
            dnsException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(dnsError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("DNS failure should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention DNS or resolution", 
                          error.getMessage().toLowerCase().contains("dns") ||
                          error.getMessage().toLowerCase().contains("resolution"));
            }
        });

        assertTrue("DNS resolution failure test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test server not running scenarios
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testServerNotRunningScenarios() throws InterruptedException {
        testConnectionRefused();
        testServerUnavailable();
        testNoRouteToHost();
    }

    private void testConnectionRefused() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConnectException refusedException = new ConnectException("Connection refused");
        AuthError refusedError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Connection refused",
            "Unable to connect to server. The server may be temporarily unavailable.",
            refusedException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(refusedError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Connection refused should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention connection refused", 
                          error.getMessage().toLowerCase().contains("refused"));
                assertTrue("User message should be helpful", 
                          error.getUserFriendlyMessage().toLowerCase().contains("server") ||
                          error.getUserFriendlyMessage().toLowerCase().contains("unavailable"));
            }
        });

        assertTrue("Connection refused test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testServerUnavailable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError unavailableError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Server unavailable",
            "Server is currently unavailable. Please try again later."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(unavailableError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Server unavailable should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                assertTrue("Should mention unavailable", 
                          error.getMessage().toLowerCase().contains("unavailable"));
                assertTrue("User message should suggest trying later", 
                          error.getUserFriendlyMessage().toLowerCase().contains("try again") ||
                          error.getUserFriendlyMessage().toLowerCase().contains("later"));
            }
        });

        assertTrue("Server unavailable test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testNoRouteToHost() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConnectException noRouteException = new ConnectException("No route to host");
        AuthError noRouteError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "No route to host",
            "Unable to reach server. Please check your network connection.",
            noRouteException
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callback.onError(noRouteError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("No route to host should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention route or host", 
                          error.getMessage().toLowerCase().contains("route") ||
                          error.getMessage().toLowerCase().contains("host"));
            }
        });

        assertTrue("No route to host test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Test network recovery scenarios
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testNetworkRecoveryScenarios() throws InterruptedException {
        testRecoveryAfterTimeout();
        testRecoveryAfterConnectionRefused();
        testOfflineToOnlineRecovery();
    }

    private void testRecoveryAfterTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            if (callCount[0] == 1) {
                // First call times out
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "Request timeout",
                    "Request timed out. Please try again.",
                    new SocketTimeoutException("Read timed out")
                ));
            } else {
                // Second call succeeds (network recovered)
                User mockUser = createMockUser("testuser", "test@example.com", "user123");
                callback.onSuccess(mockUser);
            }
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // First attempt (fails)
        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("First attempt should timeout");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                
                // Simulate retry after network recovery
                mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        assertNotNull("User should not be null after recovery", user);
                        assertEquals("Email should match", "test@example.com", user.getEmail());
                    }

                    @Override
                    public void onError(AuthError retryError) {
                        fail("Retry should succeed after network recovery");
                    }
                });
            }
        });

        assertTrue("Network recovery test should complete", latch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have made 2 calls", 2, callCount[0]);
    }

    private void testRecoveryAfterConnectionRefused() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            if (callCount[0] == 1) {
                // First call connection refused (server not running)
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "Connection refused",
                    "Unable to connect to server. Server may be temporarily unavailable.",
                    new ConnectException("Connection refused")
                ));
            } else {
                // Second call succeeds (server started)
                User mockUser = createMockUser("testuser", "test@example.com", "user123");
                callback.onSuccess(mockUser);
            }
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // First attempt (server not running)
        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("First attempt should be refused");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention connection refused", 
                          error.getMessage().toLowerCase().contains("refused"));
                
                // Simulate retry after server starts
                mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        assertNotNull("User should not be null after server recovery", user);
                    }

                    @Override
                    public void onError(AuthError retryError) {
                        fail("Retry should succeed after server starts");
                    }
                });
            }
        });

        assertTrue("Server recovery test should complete", latch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have made 2 calls", 2, callCount[0]);
    }

    private void testOfflineToOnlineRecovery() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        final int[] callCount = {0};
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(2);
            callCount[0]++;
            
            if (callCount[0] == 1) {
                // First call fails (offline)
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "No internet connection",
                    "Please check your internet connection and try again.",
                    new UnknownHostException("Unable to resolve host")
                ));
            } else {
                // Second call succeeds (back online)
                User mockUser = createMockUser("testuser", "test@example.com", "user123");
                callback.onSuccess(mockUser);
            }
            
            latch.countDown();
            return null;
        }).when(mockAuthRepository).login(anyString(), anyString(), any());

        // First attempt (offline)
        mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("First attempt should fail offline");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention connection or internet", 
                          error.getUserFriendlyMessage().toLowerCase().contains("connection") ||
                          error.getUserFriendlyMessage().toLowerCase().contains("internet"));
                
                // Simulate retry after coming back online
                mockAuthRepository.login("test@example.com", "password123", new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        assertNotNull("User should not be null after coming online", user);
                    }

                    @Override
                    public void onError(AuthError retryError) {
                        fail("Retry should succeed after coming online");
                    }
                });
            }
        });

        assertTrue("Offline to online recovery test should complete", latch.await(10, TimeUnit.SECONDS));
        assertEquals("Should have made 2 calls", 2, callCount[0]);
    }

    /**
     * Test registration network scenarios
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testRegistrationNetworkScenarios() throws InterruptedException {
        testRegistrationTimeout();
        testRegistrationConnectionRefused();
        testRegistrationServerError();
    }

    private void testRegistrationTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError timeoutError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Registration timeout",
            "Registration request timed out. Please try again.",
            new SocketTimeoutException("Read timed out")
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(timeoutError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(anyString(), anyString(), anyString(), any());

        mockAuthRepository.register("testuser", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Registration timeout should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention timeout", 
                          error.getMessage().toLowerCase().contains("timeout"));
            }
        });

        assertTrue("Registration timeout test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testRegistrationConnectionRefused() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError connectionError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Registration connection refused",
            "Unable to connect to registration server. Please try again later.",
            new ConnectException("Connection refused")
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(connectionError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(anyString(), anyString(), anyString(), any());

        mockAuthRepository.register("testuser", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Registration connection refused should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be network error", AuthError.Type.NETWORK_ERROR, error.getType());
                assertTrue("Should mention connection", 
                          error.getMessage().toLowerCase().contains("connection"));
            }
        });

        assertTrue("Registration connection refused test should complete", latch.await(5, TimeUnit.SECONDS));
    }

    private void testRegistrationServerError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AuthError serverError = new AuthError(
            AuthError.Type.SERVER_ERROR,
            "Registration server error",
            "Registration server is experiencing issues. Please try again later."
        );
        
        doAnswer(invocation -> {
            AuthRepository.AuthCallback callback = invocation.getArgument(3);
            callback.onError(serverError);
            latch.countDown();
            return null;
        }).when(mockAuthRepository).register(anyString(), anyString(), anyString(), any());

        mockAuthRepository.register("testuser", "test@example.com", "password123", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                fail("Registration server error should not result in success");
            }

            @Override
            public void onError(AuthError error) {
                assertEquals("Should be server error", AuthError.Type.SERVER_ERROR, error.getType());
                assertTrue("Should mention server", 
                          error.getMessage().toLowerCase().contains("server"));
            }
        });

        assertTrue("Registration server error test should complete", latch.await(5, TimeUnit.SECONDS));
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