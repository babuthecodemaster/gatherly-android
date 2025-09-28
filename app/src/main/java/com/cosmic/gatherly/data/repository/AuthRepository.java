package com.cosmic.gatherly.data.repository;

import android.content.Context;
import android.util.Log;

import com.cosmic.gatherly.data.api.ApiClient;
import com.cosmic.gatherly.data.api.ApiService;
import com.cosmic.gatherly.data.network.ServerConfig;
import com.cosmic.gatherly.data.network.ServerHealthChecker;
import com.cosmic.gatherly.data.database.GatherlyDatabase;
import com.cosmic.gatherly.data.database.dao.UserDao;
import com.cosmic.gatherly.data.database.entity.UserEntity;
import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.request.LoginRequest;
import com.cosmic.gatherly.data.request.RegisterRequest;
import com.cosmic.gatherly.data.response.AuthResponse;
import com.cosmic.gatherly.data.storage.SecurePreferences;
import com.cosmic.gatherly.data.util.ErrorHandler;
import com.cosmic.gatherly.data.util.Logger;
import com.cosmic.gatherly.data.util.NetworkUtils;
import com.cosmic.gatherly.data.websocket.WebSocketManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    
    private ApiService apiService;
    private SecurePreferences securePrefs;
    private UserDao userDao;
    private WebSocketManager webSocketManager;
    private CompositeDisposable disposables;
    private Context context;
    private ServerHealthChecker serverHealthChecker;
    private ServerConfig serverConfig;

    public AuthRepository(Context context) {
        Logger.methodEntry(Logger.TAG_AUTH, "AuthRepository.<init>");
        
        try {
            this.context = context;
            this.apiService = ApiClient.getApiService(context);
            this.securePrefs = new SecurePreferences(context);
            this.userDao = GatherlyDatabase.getInstance(context).userDao();
            this.webSocketManager = WebSocketManager.getInstance();
            this.disposables = new CompositeDisposable();
            this.serverHealthChecker = new ServerHealthChecker(context);
            this.serverConfig = ApiClient.getServerConfig(context);
            
            Logger.i(Logger.TAG_AUTH, "AuthRepository initialized successfully");
            Logger.d(Logger.TAG_AUTH, "Secure storage encryption enabled: %s", securePrefs.isUsingEncryption());
            Logger.d(Logger.TAG_AUTH, "Database instance: %s", userDao != null ? "OK" : "NULL");
            Logger.d(Logger.TAG_AUTH, "WebSocket manager: %s", webSocketManager != null ? "OK" : "NULL");
            
        } catch (Exception e) {
            Logger.e(Logger.TAG_AUTH, "Failed to initialize AuthRepository", e);
            throw new RuntimeException("AuthRepository initialization failed", e);
        }
        
        Logger.methodExit(Logger.TAG_AUTH, "AuthRepository.<init>");
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(AuthError error);
    }

    public void login(String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "login", email != null ? email.replaceAll("@.*", "@***") : "null");
        Logger.startTiming("AUTH_LOGIN");
        
        // Safe execution wrapper to prevent crashes
        Logger.safeExecute(Logger.TAG_AUTH, "login_validation_and_request", () -> {
            // Input validation with detailed logging
            if (email == null || email.trim().isEmpty()) {
                Logger.w(Logger.TAG_AUTH, "Login failed: Email is null or empty");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Email is required",
                    "Please enter your email address."
                ));
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                Logger.w(Logger.TAG_AUTH, "Login failed: Password is null or empty");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Password is required",
                    "Please enter your password."
                ));
                return;
            }
            
            // Additional validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                Logger.w(Logger.TAG_AUTH, "Login failed: Invalid email format");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Invalid email format",
                    "Please enter a valid email address."
                ));
                return;
            }
            
            // Enhanced network connectivity checks before making request
            if (!NetworkUtils.isNetworkAvailable(context)) {
                String networkType = NetworkUtils.getNetworkType(context);
                Logger.w(Logger.TAG_AUTH, "Login failed: No network connectivity (Network type: %s)", networkType);
                
                // Provide more specific error message based on network type
                String errorMessage = "No internet connection detected";
                if (!"No Connection".equals(networkType) && !"Unknown".equals(networkType)) {
                    errorMessage += " via " + networkType.toLowerCase();
                }
                errorMessage += ". Please check your network settings and try again.";
                
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "No internet connection",
                    errorMessage
                ));
                return;
            }
            
            // Check server health before attempting authentication
            Logger.d(Logger.TAG_AUTH, "Checking server health before login attempt");
            serverHealthChecker.checkServerHealth().thenAccept(healthStatus -> {
                if (!healthStatus.isHealthy()) {
                    Logger.w(Logger.TAG_AUTH, "Server health check failed: %s", healthStatus.getStatusMessage());
                    callback.onError(new AuthError(
                        AuthError.Type.NETWORK_ERROR,
                        "Server unavailable: " + healthStatus.getStatusMessage(),
                        healthStatus.getUserMessage()
                    ));
                    return;
                }
                
                Logger.d(Logger.TAG_AUTH, "Server health check passed, proceeding with login");
                performLoginRequest(email, password, callback);
            }).exceptionally(throwable -> {
                Logger.w(Logger.TAG_AUTH, "Server health check failed with exception, proceeding anyway", throwable);
                // If health check fails, still try the login (fallback behavior)
                performLoginRequest(email, password, callback);
                return null;
            });
        });
        
        Logger.methodExit(Logger.TAG_AUTH, "login");
    }
    
    /**
     * Perform the actual login request after validation and health checks
     */
    private void performLoginRequest(String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "performLoginRequest");
        
        // Perform network diagnostics for debugging
        Logger.d(Logger.TAG_AUTH, "Network diagnostics before login:\n%s", 
            NetworkUtils.performNetworkDiagnostics(context));
        
        try {
            LoginRequest request = new LoginRequest(email.trim(), password);
            Logger.logAuthEvent("LOGIN_ATTEMPT", email.trim(), false);
            Logger.i(Logger.TAG_AUTH, "Initiating login request for user: %s", 
                email.trim().replaceAll("@.*", "@***"));
            
            apiService.login(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    Logger.safeExecute(Logger.TAG_AUTH, "login_response_processing", () -> {
                        Logger.d(Logger.TAG_AUTH, "Login response received: HTTP %d", response.code());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            Logger.d(Logger.TAG_AUTH, "Login response body received successfully");
                            
                            // Validate response data with detailed logging
                            if (authResponse.getId() == null || authResponse.getId().isEmpty()) {
                                Logger.e(Logger.TAG_AUTH, "Login response validation failed: missing user ID");
                                Logger.logSecurityEvent("INVALID_LOGIN_RESPONSE", "Missing user ID in response");
                                callback.onError(new AuthError(
                                    AuthError.Type.SERVER_ERROR,
                                    "Invalid server response - missing user ID",
                                    "Server error occurred. Please try again."
                                ));
                                return;
                            }
                            
                            if (authResponse.getAccessToken() == null || authResponse.getAccessToken().isEmpty()) {
                                Logger.e(Logger.TAG_AUTH, "Login response validation failed: missing access token");
                                Logger.logSecurityEvent("INVALID_LOGIN_RESPONSE", "Missing access token in response");
                                callback.onError(new AuthError(
                                    AuthError.Type.SERVER_ERROR,
                                    "Invalid server response - missing access token",
                                    "Server error occurred. Please try again."
                                ));
                                return;
                            }
                            
                            Logger.d(Logger.TAG_AUTH, "Login response validation passed");
                            
                            User user = createUserFromResponse(authResponse);
                            Logger.d(Logger.TAG_AUTH, "User object created from response: %s", 
                                user.getUsername());
                            
                            // Save to secure storage with error handling
                            Logger.safeExecute(Logger.TAG_AUTH, "save_user_session", () -> {
                                saveUserSession(user, authResponse.getAccessToken(), authResponse.getRefreshToken());
                                Logger.d(Logger.TAG_AUTH, "User session saved to secure storage");
                            });
                            
                            // Save to local database with error handling
                            Logger.safeExecute(Logger.TAG_AUTH, "save_user_database", () -> {
                                saveUserToDatabase(user);
                                Logger.d(Logger.TAG_AUTH, "User saved to local database");
                            });
                            
                            // Initialize WebSocket connection with error handling
                            Logger.safeExecute(Logger.TAG_AUTH, "initialize_websocket", () -> {
                                initializeWebSocket(authResponse.getAccessToken());
                                Logger.d(Logger.TAG_AUTH, "WebSocket connection initialized");
                            });
                            
                            Logger.logAuthEvent("LOGIN_SUCCESS", user.getId(), true);
                            Logger.endTiming("AUTH_LOGIN");
                            Logger.i(Logger.TAG_AUTH, "✅ Login successful for user: %s", user.getUsername());
                            callback.onSuccess(user);
                            
                        } else {
                            // Parse error response with detailed logging
                            AuthError error = ErrorHandler.parseError(response);
                            Logger.w(Logger.TAG_AUTH, "Login failed with HTTP %d: %s", 
                                response.code(), error.getMessage());
                            Logger.logAuthEvent("LOGIN_FAILED", email.trim(), false);
                            
                            // Try offline login as fallback with detailed logging
                            Logger.d(Logger.TAG_AUTH, "Attempting offline login fallback");
                            User cachedUser = getCachedUser();
                            if (cachedUser != null && isValidCredentials(email, password)) {
                                Logger.i(Logger.TAG_AUTH, "✅ Offline login successful for cached user: %s", 
                                    cachedUser.getUsername());
                                Logger.logAuthEvent("OFFLINE_LOGIN_SUCCESS", cachedUser.getId(), true);
                                Logger.endTiming("AUTH_LOGIN");
                                callback.onSuccess(cachedUser);
                            } else {
                                Logger.w(Logger.TAG_AUTH, "❌ Offline login failed - no valid cached credentials");
                                Logger.endTiming("AUTH_LOGIN");
                                callback.onError(error);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Logger.safeExecute(Logger.TAG_AUTH, "login_failure_handling", () -> {
                        Logger.logNetworkError("Login Request", t, call.request().url().toString());
                        Logger.e(Logger.TAG_AUTH, "❌ Login network error occurred", t);
                        Logger.logAuthEvent("LOGIN_NETWORK_ERROR", email.trim(), false);
                        
                        // Check if this is a server connectivity issue
                        AuthError networkError;
                        if (NetworkUtils.isServerError(t)) {
                            Logger.w(Logger.TAG_AUTH, "Server appears to be unreachable");
                            networkError = new AuthError(
                                AuthError.Type.NETWORK_ERROR,
                                "Server unreachable: " + t.getMessage(),
                                "Unable to connect to server. Please check if the server is running and try again."
                            );
                        } else {
                            // Parse network error with detailed logging
                            networkError = ErrorHandler.parseNetworkError(t);
                            // Enhance error message with network-specific guidance
                            String userMessage = NetworkUtils.getNetworkErrorMessage(context);
                            networkError = new AuthError(
                                networkError.getType(),
                                networkError.getMessage(),
                                userMessage,
                                t
                            );
                        }
                        Logger.d(Logger.TAG_AUTH, "Network error parsed: %s", networkError.getType());
                        
                        // Try offline login as fallback with comprehensive logging
                        Logger.d(Logger.TAG_AUTH, "Attempting offline login fallback due to network error");
                        User cachedUser = getCachedUser();
                        if (cachedUser != null && isValidCredentials(email, password)) {
                            Logger.i(Logger.TAG_AUTH, "✅ Offline login successful due to network error for user: %s", 
                                cachedUser.getUsername());
                            Logger.logAuthEvent("OFFLINE_LOGIN_SUCCESS_NETWORK_ERROR", cachedUser.getId(), true);
                            Logger.endTiming("AUTH_LOGIN");
                            callback.onSuccess(cachedUser);
                        } else {
                            Logger.w(Logger.TAG_AUTH, "❌ Offline login failed - no valid cached credentials available");
                            Logger.endTiming("AUTH_LOGIN");
                            callback.onError(networkError);
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            Logger.e(Logger.TAG_AUTH, "Critical error initiating login request", e);
            Logger.endTiming("AUTH_LOGIN");
            callback.onError(new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Error initiating login request: " + e.getMessage(),
                "An unexpected error occurred. Please try again.",
                e
            ));
        }
        
        Logger.methodExit(Logger.TAG_AUTH, "performLoginRequest");
    }

    public void register(String username, String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "register", username, 
            email != null ? email.replaceAll("@.*", "@***") : "null");
        Logger.startTiming("AUTH_REGISTER");
        
        // Safe execution wrapper to prevent crashes
        Logger.safeExecute(Logger.TAG_AUTH, "register_validation_and_request", () -> {
            // Comprehensive input validation with detailed logging
            if (username == null || username.trim().isEmpty()) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Username is null or empty");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Username is required",
                    "Please enter a username."
                ));
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Email is null or empty");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Email is required",
                    "Please enter your email address."
                ));
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Password is null or empty");
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Password is required",
                    "Please enter a password."
                ));
                return;
            }
            
            // Enhanced validation with logging
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Invalid email format for %s", 
                    email.trim().replaceAll("@.*", "@***"));
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Invalid email format",
                    "Please enter a valid email address."
                ));
                return;
            }
            
            if (password.length() < 6) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Password too short (%d characters)", 
                    password.length());
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Password too short",
                    "Password must be at least 6 characters long."
                ));
                return;
            }
            
            // Additional username validation
            if (username.trim().length() < 3) {
                Logger.w(Logger.TAG_AUTH, "Registration failed: Username too short (%d characters)", 
                    username.trim().length());
                callback.onError(new AuthError(
                    AuthError.Type.VALIDATION_ERROR,
                    "Username too short",
                    "Username must be at least 3 characters long."
                ));
                return;
            }
            
            // Enhanced network connectivity checks before making request
            if (!NetworkUtils.isNetworkAvailable(context)) {
                String networkType = NetworkUtils.getNetworkType(context);
                Logger.w(Logger.TAG_AUTH, "Registration failed: No network connectivity (Network type: %s)", networkType);
                
                // Provide more specific error message based on network type
                String errorMessage = "No internet connection detected";
                if (!"No Connection".equals(networkType) && !"Unknown".equals(networkType)) {
                    errorMessage += " via " + networkType.toLowerCase();
                }
                errorMessage += ". Please check your network settings and try again.";
                
                callback.onError(new AuthError(
                    AuthError.Type.NETWORK_ERROR,
                    "No internet connection",
                    errorMessage
                ));
                return;
            }
            
            // Check server health before attempting registration
            Logger.d(Logger.TAG_AUTH, "Checking server health before registration attempt");
            serverHealthChecker.checkServerHealth().thenAccept(healthStatus -> {
                if (!healthStatus.isHealthy()) {
                    Logger.w(Logger.TAG_AUTH, "Server health check failed: %s", healthStatus.getStatusMessage());
                    callback.onError(new AuthError(
                        AuthError.Type.NETWORK_ERROR,
                        "Server unavailable: " + healthStatus.getStatusMessage(),
                        healthStatus.getUserMessage()
                    ));
                    return;
                }
                
                Logger.d(Logger.TAG_AUTH, "Server health check passed, proceeding with registration");
                performRegistrationRequest(username, email, password, callback);
            }).exceptionally(throwable -> {
                Logger.w(Logger.TAG_AUTH, "Server health check failed with exception, proceeding anyway", throwable);
                // If health check fails, still try the registration (fallback behavior)
                performRegistrationRequest(username, email, password, callback);
                return null;
            });
        });
        
        Logger.methodExit(Logger.TAG_AUTH, "register");
    }
    
    /**
     * Perform the actual registration request after validation and health checks
     */
    private void performRegistrationRequest(String username, String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "performRegistrationRequest");
        
        // Perform network diagnostics for debugging
        Logger.d(Logger.TAG_AUTH, "Network diagnostics before registration:\n%s", 
            NetworkUtils.performNetworkDiagnostics(context));
        
        try {
            RegisterRequest request = new RegisterRequest(username.trim(), email.trim(), password);
            Logger.logAuthEvent("REGISTER_ATTEMPT", email.trim(), false);
            Logger.i(Logger.TAG_AUTH, "Initiating registration for user: %s with email: %s", 
                username.trim(), email.trim().replaceAll("@.*", "@***"));
            
            apiService.register(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            
                            // Validate response data
                            if (authResponse.getId() == null || authResponse.getId().isEmpty()) {
                                Log.e(TAG, "Registration response missing user ID");
                                callback.onError(new AuthError(
                                    AuthError.Type.SERVER_ERROR,
                                    "Invalid server response - missing user ID",
                                    "Server error occurred. Please try again."
                                ));
                                return;
                            }
                            
                            if (authResponse.getAccessToken() == null || authResponse.getAccessToken().isEmpty()) {
                                Log.e(TAG, "Registration response missing access token");
                                callback.onError(new AuthError(
                                    AuthError.Type.SERVER_ERROR,
                                    "Invalid server response - missing access token",
                                    "Server error occurred. Please try again."
                                ));
                                return;
                            }
                            
                            User user = createUserFromResponse(authResponse);
                            
                            // Save to secure storage
                            saveUserSession(user, authResponse.getAccessToken(), authResponse.getRefreshToken());
                            
                            // Save to local database
                            saveUserToDatabase(user);
                            
                            // Initialize WebSocket connection
                            initializeWebSocket(authResponse.getAccessToken());
                            
                            Log.d(TAG, "Registration successful for user: " + user.getUsername());
                            callback.onSuccess(user);
                            
                        } else {
                            // Parse error response
                            AuthError error = ErrorHandler.parseError(response);
                            Log.w(TAG, "Registration failed with error: " + error);
                            callback.onError(error);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing registration response", e);
                        callback.onError(new AuthError(
                            AuthError.Type.UNKNOWN_ERROR,
                            "Error processing registration response: " + e.getMessage(),
                            "An unexpected error occurred. Please try again.",
                            e
                        ));
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    try {
                        Log.e(TAG, "Registration network error", t);
                        
                        // Check if this is a server connectivity issue
                        AuthError networkError;
                        if (NetworkUtils.isServerError(t)) {
                            Log.w(TAG, "Server appears to be unreachable during registration");
                            networkError = new AuthError(
                                AuthError.Type.NETWORK_ERROR,
                                "Server unreachable: " + t.getMessage(),
                                "Unable to connect to server. Please check if the server is running and try again."
                            );
                        } else {
                            // Parse network error with enhanced messaging
                            networkError = ErrorHandler.parseNetworkError(t);
                            String userMessage = NetworkUtils.getNetworkErrorMessage(context);
                            networkError = new AuthError(
                                networkError.getType(),
                                networkError.getMessage(),
                                userMessage,
                                t
                            );
                        }
                        callback.onError(networkError);
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling registration failure", e);
                        callback.onError(new AuthError(
                            AuthError.Type.UNKNOWN_ERROR,
                            "Error handling registration failure: " + e.getMessage(),
                            "An unexpected error occurred. Please try again.",
                            e
                        ));
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error initiating registration request", e);
            callback.onError(new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Error initiating registration request: " + e.getMessage(),
                "An unexpected error occurred. Please try again.",
                e
            ));
        }
        
        Logger.methodExit(Logger.TAG_AUTH, "performRegistrationRequest");
    }

    public void logout(AuthCallback callback) {
        try {
            Log.d(TAG, "Attempting logout");
            
            apiService.logout().enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    try {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Logout API call successful");
                        } else {
                            Log.w(TAG, "Logout API call returned error code: " + response.code());
                        }
                        
                        performLogout();
                        callback.onSuccess(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing logout response", e);
                        // Still perform logout locally even if there's an error
                        performLogout();
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    try {
                        Log.w(TAG, "Logout API call failed, clearing local session anyway", t);
                        performLogout(); // Clear session even if API call fails
                        callback.onSuccess(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling logout failure", e);
                        // Still try to clear session
                        try {
                            performLogout();
                            callback.onSuccess(null);
                        } catch (Exception clearError) {
                            Log.e(TAG, "Failed to clear session during logout", clearError);
                            callback.onError(new AuthError(
                                AuthError.Type.UNKNOWN_ERROR,
                                "Failed to clear session: " + clearError.getMessage(),
                                "Logout may not have completed properly. Please restart the app.",
                                clearError
                            ));
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error initiating logout request", e);
            // Still try to perform local logout
            try {
                performLogout();
                callback.onSuccess(null);
            } catch (Exception clearError) {
                Log.e(TAG, "Failed to clear session during logout", clearError);
                callback.onError(new AuthError(
                    AuthError.Type.UNKNOWN_ERROR,
                    "Failed to clear session: " + clearError.getMessage(),
                    "Logout may not have completed properly. Please restart the app.",
                    clearError
                ));
            }
        }
    }
    
    private void performLogout() {
        // Disconnect WebSocket
        webSocketManager.disconnect();
        
        // Clear secure storage
        clearUserSession();
        
        // Clear local database (optional - you might want to keep some data)
        // clearLocalDatabase();
        
        Log.d(TAG, "User logged out successfully");
    }

    public void getCurrentUser(AuthCallback callback) {
        try {
            if (!isLoggedIn()) {
                callback.onError(new AuthError(
                    AuthError.Type.AUTHENTICATION_ERROR,
                    "User not logged in",
                    "Please log in to continue."
                ));
                return;
            }

            Log.d(TAG, "Fetching current user information");
            
            apiService.getCurrentUser().enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            
                            // Validate response data
                            if (authResponse.getId() == null || authResponse.getId().isEmpty()) {
                                Log.e(TAG, "getCurrentUser response missing user ID");
                                callback.onError(new AuthError(
                                    AuthError.Type.SERVER_ERROR,
                                    "Invalid server response - missing user ID",
                                    "Server error occurred. Please try again."
                                ));
                                return;
                            }
                            
                            User user = createUserFromResponse(authResponse);
                            
                            // Update session if tokens are provided
                            if (authResponse.getAccessToken() != null && !authResponse.getAccessToken().isEmpty()) {
                                saveUserSession(user, authResponse.getAccessToken(), authResponse.getRefreshToken());
                            }
                            
                            saveUserToDatabase(user);
                            Log.d(TAG, "Current user information updated successfully");
                            callback.onSuccess(user);
                            
                        } else {
                            // Parse error response
                            AuthError error = ErrorHandler.parseError(response);
                            Log.w(TAG, "getCurrentUser failed with error: " + error);
                            
                            // If unauthorized, clear session
                            if (response.code() == 401 || response.code() == 403) {
                                Log.d(TAG, "Session expired, clearing user session");
                                clearUserSession();
                            }
                            
                            callback.onError(error);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing getCurrentUser response", e);
                        callback.onError(new AuthError(
                            AuthError.Type.UNKNOWN_ERROR,
                            "Error processing user information: " + e.getMessage(),
                            "An unexpected error occurred. Please try again.",
                            e
                        ));
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    try {
                        Log.e(TAG, "getCurrentUser network error", t);
                        
                        // Parse network error
                        AuthError networkError = ErrorHandler.parseNetworkError(t);
                        callback.onError(networkError);
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling getCurrentUser failure", e);
                        callback.onError(new AuthError(
                            AuthError.Type.UNKNOWN_ERROR,
                            "Error handling user information request: " + e.getMessage(),
                            "An unexpected error occurred. Please try again.",
                            e
                        ));
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error initiating getCurrentUser request", e);
            callback.onError(new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Error initiating user information request: " + e.getMessage(),
                "An unexpected error occurred. Please try again.",
                e
            ));
        }
    }

    public User getCachedUser() {
        if (!isLoggedIn()) {
            return null;
        }

        String id = securePrefs.getString(SecurePreferences.KEY_USER_ID, null);
        String username = securePrefs.getString(SecurePreferences.KEY_USERNAME, null);
        String email = securePrefs.getString(SecurePreferences.KEY_EMAIL, null);
        String avatar = securePrefs.getString(SecurePreferences.KEY_AVATAR, null);
        String status = securePrefs.getString(SecurePreferences.KEY_STATUS, "offline");

        if (id != null && username != null && email != null) {
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatar(avatar);
            user.setStatus(User.UserStatus.fromString(status));
            return user;
        }

        return null;
    }

    public boolean isLoggedIn() {
        try {
            boolean isLoggedIn = securePrefs.getBoolean(SecurePreferences.KEY_IS_LOGGED_IN, false);
            
            if (isLoggedIn) {
                // Validate session integrity
                String userId = securePrefs.getString(SecurePreferences.KEY_USER_ID, null);
                String accessToken = securePrefs.getString(SecurePreferences.KEY_ACCESS_TOKEN, null);
                
                if (userId == null || userId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
                    Log.w(TAG, "Session marked as logged in but missing critical data, clearing session");
                    clearUserSession();
                    return false;
                }
            }
            
            return isLoggedIn;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            return false;
        }
    }
    
    /**
     * Validates the integrity of the current user session
     * @return true if session is valid and complete, false otherwise
     */
    public boolean isSessionValid() {
        try {
            if (!isLoggedIn()) {
                return false;
            }
            
            // Check all required session data
            String userId = securePrefs.getString(SecurePreferences.KEY_USER_ID, null);
            String username = securePrefs.getString(SecurePreferences.KEY_USERNAME, null);
            String email = securePrefs.getString(SecurePreferences.KEY_EMAIL, null);
            String accessToken = securePrefs.getString(SecurePreferences.KEY_ACCESS_TOKEN, null);
            
            boolean isValid = userId != null && !userId.isEmpty() &&
                             username != null && !username.isEmpty() &&
                             email != null && !email.isEmpty() &&
                             accessToken != null && !accessToken.isEmpty();
            
            if (!isValid) {
                Log.w(TAG, "Session validation failed - missing required data");
            }
            
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error validating session", e);
            return false;
        }
    }
    
    public String getAccessToken() {
        return securePrefs.getString(SecurePreferences.KEY_ACCESS_TOKEN, null);
    }
    
    public String getRefreshToken() {
        return securePrefs.getString(SecurePreferences.KEY_REFRESH_TOKEN, null);
    }
    
    public Single<User> getCachedUserRx() {
        return Single.fromCallable(this::getCachedUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private User createUserFromResponse(AuthResponse response) {
        try {
            if (response == null) {
                throw new IllegalArgumentException("AuthResponse cannot be null");
            }
            
            User user = new User();
            user.setId(response.getId());
            user.setUsername(response.getUsername());
            user.setEmail(response.getEmail());
            user.setAvatar(response.getAvatar());
            
            // Handle status safely
            String statusString = response.getStatus();
            if (statusString != null && !statusString.isEmpty()) {
                user.setStatus(User.UserStatus.fromString(statusString));
            } else {
                user.setStatus(User.UserStatus.ONLINE); // Default status
            }
            
            Log.d(TAG, "Created user from response: " + user.getUsername() + " (ID: " + user.getId() + ")");
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error creating user from response", e);
            throw new RuntimeException("Failed to create user from server response", e);
        }
    }

    private void saveUserSession(User user, String accessToken, String refreshToken) {
        try {
            // Validate input parameters
            if (user == null) {
                Log.e(TAG, "Cannot save session: user is null");
                return;
            }
            
            if (user.getId() == null || user.getId().isEmpty()) {
                Log.e(TAG, "Cannot save session: user ID is null or empty");
                return;
            }
            
            if (accessToken == null || accessToken.isEmpty()) {
                Log.e(TAG, "Cannot save session: access token is null or empty");
                return;
            }
            
            // Save user session data
            securePrefs.putString(SecurePreferences.KEY_USER_ID, user.getId());
            securePrefs.putString(SecurePreferences.KEY_USERNAME, user.getUsername());
            securePrefs.putString(SecurePreferences.KEY_EMAIL, user.getEmail());
            securePrefs.putString(SecurePreferences.KEY_AVATAR, user.getAvatar());
            securePrefs.putString(SecurePreferences.KEY_STATUS, user.getStatus().getValue());
            securePrefs.putString(SecurePreferences.KEY_ACCESS_TOKEN, accessToken);
            securePrefs.putString(SecurePreferences.KEY_REFRESH_TOKEN, refreshToken);
            securePrefs.putBoolean(SecurePreferences.KEY_IS_LOGGED_IN, true);
            securePrefs.putLong(SecurePreferences.KEY_LAST_SYNC, System.currentTimeMillis());
            
            // Verify the session was saved correctly
            boolean isLoggedIn = securePrefs.getBoolean(SecurePreferences.KEY_IS_LOGGED_IN, false);
            String savedUserId = securePrefs.getString(SecurePreferences.KEY_USER_ID, null);
            
            if (isLoggedIn && savedUserId != null && savedUserId.equals(user.getId())) {
                Log.d(TAG, "User session saved and verified successfully for user: " + user.getUsername());
            } else {
                Log.e(TAG, "User session save verification failed");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving user session", e);
        }
    }
    
    private void saveUserToDatabase(User user) {
        UserEntity userEntity = new UserEntity(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar(),
            user.getStatus().getValue()
        );
        userEntity.setOnline(true);
        
        disposables.add(
            userDao.insertUser(userEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "User saved to database"),
                    error -> Log.e(TAG, "Error saving user to database", error)
                )
        );
    }
    
    private void initializeWebSocket(String accessToken) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                Log.w(TAG, "Cannot initialize WebSocket - access token is null or empty");
                return;
            }
            
            // Replace with your WebSocket server URL
            String wsUrl = "wss://your-websocket-server.com/ws";
            webSocketManager.initialize(wsUrl, accessToken);
            webSocketManager.connect();
            Log.d(TAG, "WebSocket connection initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing WebSocket - continuing without real-time features", e);
            // Don't fail the authentication process if WebSocket fails
        }
    }
    
    private boolean isValidCredentials(String email, String password) {
        try {
            if (email == null || password == null) {
                return false;
            }
            
            // Simple validation - in production, you might want to hash and compare
            String cachedEmail = securePrefs.getString(SecurePreferences.KEY_EMAIL, null);
            boolean emailMatches = email.trim().equalsIgnoreCase(cachedEmail);
            
            // For security, we don't store passwords, so we just check if email matches
            // and assume password is correct if user has a valid session
            boolean hasValidSession = isSessionValid();
            
            Log.d(TAG, "Credential validation - Email matches: " + emailMatches + ", Valid session: " + hasValidSession);
            return emailMatches && hasValidSession;
        } catch (Exception e) {
            Log.e(TAG, "Error validating credentials", e);
            return false;
        }
    }

    private void clearUserSession() {
        try {
            Log.d(TAG, "Clearing user session");
            
            // Disconnect WebSocket first
            try {
                if (webSocketManager != null) {
                    webSocketManager.disconnect();
                }
            } catch (Exception e) {
                Log.w(TAG, "Error disconnecting WebSocket during session clear", e);
            }
            
            // Clear secure preferences
            try {
                securePrefs.clear();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing secure preferences", e);
                // Try to clear individual keys as fallback
                try {
                    securePrefs.putBoolean(SecurePreferences.KEY_IS_LOGGED_IN, false);
                    securePrefs.putString(SecurePreferences.KEY_ACCESS_TOKEN, null);
                    securePrefs.putString(SecurePreferences.KEY_REFRESH_TOKEN, null);
                    securePrefs.putString(SecurePreferences.KEY_USER_ID, null);
                } catch (Exception fallbackError) {
                    Log.e(TAG, "Fallback session clear also failed", fallbackError);
                }
            }
            
            Log.d(TAG, "User session cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user session", e);
        }
    }
    
    private void clearLocalDatabase() {
        disposables.add(
            Completable.fromAction(() -> {
                userDao.deleteAllUsers();
                // Add other DAOs as needed
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> Log.d(TAG, "Local database cleared"),
                error -> Log.e(TAG, "Error clearing local database", error)
            )
        );
    }
    
    /**
     * Check if network is available (basic connectivity check)
     */
    public boolean isNetworkAvailable(Context context) {
        try {
            android.net.ConnectivityManager connectivityManager = 
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking network availability", e);
            return true; // Assume network is available if we can't check
        }
    }
    
    public void dispose() {
        try {
            if (disposables != null && !disposables.isDisposed()) {
                disposables.dispose();
                Log.d(TAG, "Disposed RxJava disposables");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disposing resources", e);
        }
    }
    
    /**
     * Performs a health check on the AuthRepository
     * @return true if the repository is healthy, false otherwise
     */
    public boolean performHealthCheck() {
        try {
            Logger.methodEntry(Logger.TAG_AUTH, "performHealthCheck");
            
            boolean isHealthy = true;
            StringBuilder healthReport = new StringBuilder();
            healthReport.append("AuthRepository Health Check:\n");
            
            // Check API service
            if (apiService == null) {
                healthReport.append("  ❌ API Service: NULL\n");
                isHealthy = false;
            } else {
                healthReport.append("  ✅ API Service: OK\n");
            }
            
            // Check secure preferences
            if (securePrefs == null) {
                healthReport.append("  ❌ Secure Preferences: NULL\n");
                isHealthy = false;
            } else {
                healthReport.append("  ✅ Secure Preferences: OK\n");
            }
            
            // Check user DAO
            if (userDao == null) {
                healthReport.append("  ❌ User DAO: NULL\n");
                isHealthy = false;
            } else {
                healthReport.append("  ✅ User DAO: OK\n");
            }
            
            // Check WebSocket manager
            if (webSocketManager == null) {
                healthReport.append("  ❌ WebSocket Manager: NULL\n");
                isHealthy = false;
            } else {
                healthReport.append("  ✅ WebSocket Manager: OK\n");
            }
            
            // Check disposables
            if (disposables == null) {
                healthReport.append("  ❌ Disposables: NULL\n");
                isHealthy = false;
            } else if (disposables.isDisposed()) {
                healthReport.append("  ⚠️ Disposables: DISPOSED\n");
                // This is not necessarily unhealthy, just a warning
            } else {
                healthReport.append("  ✅ Disposables: OK\n");
            }
            
            // Check if user is logged in
            boolean loggedIn = isLoggedIn();
            healthReport.append("  ").append(loggedIn ? "✅" : "ℹ️").append(" User Logged In: ").append(loggedIn).append("\n");
            
            // Check session validity if logged in
            if (loggedIn) {
                boolean sessionValid = isSessionValid();
                healthReport.append("  ").append(sessionValid ? "✅" : "⚠️").append(" Session Valid: ").append(sessionValid).append("\n");
                if (!sessionValid) {
                    // Session invalid is a warning, not a critical failure
                    healthReport.append("  ℹ️ Note: Invalid session can be refreshed\n");
                }
            }
            
            Logger.i(Logger.TAG_AUTH, healthReport.toString());
            Logger.methodExit(Logger.TAG_AUTH, "performHealthCheck", isHealthy);
            
            return isHealthy;
            
        } catch (Exception e) {
            Logger.e(Logger.TAG_AUTH, "Error during health check", e);
            return false;
        }
    }
    
    /**
     * Get comprehensive server diagnostics including connectivity status
     */
    public void getServerDiagnostics(ServerDiagnosticsCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "getServerDiagnostics");
        
        if (serverHealthChecker == null) {
            callback.onResult("Server health checker not initialized");
            return;
        }
        
        serverHealthChecker.getServerDiagnostics().thenAccept(diagnostics -> {
            StringBuilder fullDiagnostics = new StringBuilder();
            fullDiagnostics.append("=== GATHERLY SERVER DIAGNOSTICS ===\n\n");
            
            // Add server configuration info
            if (serverConfig != null) {
                fullDiagnostics.append("Server Configuration:\n");
                fullDiagnostics.append("• Environment: ").append(serverConfig.getCurrentEnvironment().getName()).append("\n");
                fullDiagnostics.append("• Base URL: ").append(serverConfig.getBaseUrl()).append("\n");
                fullDiagnostics.append("• Is Development: ").append(serverConfig.isDevelopment() ? "Yes" : "No").append("\n");
                fullDiagnostics.append("• URL Valid: ").append(serverConfig.isValidUrl() ? "✅ Yes" : "❌ No").append("\n\n");
            }
            
            // Add detailed diagnostics from health checker
            fullDiagnostics.append(diagnostics);
            
            // Add authentication repository status
            fullDiagnostics.append("\nAuthentication Repository Status:\n");
            fullDiagnostics.append("• Health Check: ").append(performHealthCheck() ? "✅ Healthy" : "❌ Unhealthy").append("\n");
            fullDiagnostics.append("• User Logged In: ").append(isLoggedIn() ? "✅ Yes" : "ℹ️ No").append("\n");
            if (isLoggedIn()) {
                fullDiagnostics.append("• Session Valid: ").append(isSessionValid() ? "✅ Yes" : "⚠️ No").append("\n");
            }
            
            Logger.i(Logger.TAG_AUTH, "Server diagnostics completed");
            callback.onResult(fullDiagnostics.toString());
        }).exceptionally(throwable -> {
            Logger.e(Logger.TAG_AUTH, "Error getting server diagnostics", throwable);
            callback.onResult("Error getting server diagnostics: " + throwable.getMessage());
            return null;
        });
        
        Logger.methodExit(Logger.TAG_AUTH, "getServerDiagnostics");
    }
    
    /**
     * Check server connectivity and return status
     */
    public void checkServerConnectivity(ServerConnectivityCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "checkServerConnectivity");
        
        if (serverHealthChecker == null) {
            callback.onResult(false, "Server health checker not initialized");
            return;
        }
        
        serverHealthChecker.checkServerHealth().thenAccept(healthStatus -> {
            Logger.i(Logger.TAG_AUTH, "Server connectivity check result: %s", healthStatus.toString());
            callback.onResult(healthStatus.isHealthy(), healthStatus.getUserMessage());
        }).exceptionally(throwable -> {
            Logger.e(Logger.TAG_AUTH, "Server connectivity check failed", throwable);
            callback.onResult(false, "Server connectivity check failed: " + throwable.getMessage());
            return null;
        });
        
        Logger.methodExit(Logger.TAG_AUTH, "checkServerConnectivity");
    }
    
    /**
     * Update server configuration
     */
    public void updateServerConfiguration(String baseUrl) {
        Logger.methodEntry(Logger.TAG_AUTH, "updateServerConfiguration", baseUrl);
        
        try {
            if (serverConfig != null) {
                serverConfig.setCustomBaseUrl(baseUrl);
                
                // Update health checker with new URL
                if (serverHealthChecker != null) {
                    serverHealthChecker.setBaseUrl(baseUrl);
                }
                
                // Reset API client to use new configuration
                ApiClient.resetClient();
                this.apiService = ApiClient.getApiService(context);
                
                Logger.i(Logger.TAG_AUTH, "Server configuration updated successfully to: %s", baseUrl);
            } else {
                Logger.e(Logger.TAG_AUTH, "Cannot update server configuration - serverConfig is null");
            }
        } catch (Exception e) {
            Logger.e(Logger.TAG_AUTH, "Error updating server configuration", e);
        }
        
        Logger.methodExit(Logger.TAG_AUTH, "updateServerConfiguration");
    }
    
    /**
     * Callback interface for server diagnostics
     */
    public interface ServerDiagnosticsCallback {
        void onResult(String diagnostics);
    }
    
    /**
     * Callback interface for server connectivity checks
     */
    public interface ServerConnectivityCallback {
        void onResult(boolean isConnected, String message);
    }

}
                