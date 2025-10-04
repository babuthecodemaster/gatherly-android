package com.cosmic.gatherly.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.util.Logger;
import com.cosmic.gatherly.data.util.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Repository for handling authentication operations with Firebase Auth
 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static final String PREFS_NAME = "auth_prefs";
    
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final SharedPreferences prefs;
    
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(AuthError error);
    }
    
    public AuthRepository(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Logger.d(TAG, "AuthRepository initialized with Firebase Auth");
    }
    
    /**
     * Login with email and password using Firebase Auth
     */
    public void login(String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "login");
        Logger.startTiming("AUTH_LOGIN");
        
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Email is required",
                "Please enter your email address"
            ));
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password is required",
                "Please enter your password"
            ));
            return;
        }
        
        performLoginRequest(email.trim(), password, callback);
    }
    
    /**
     * Register new user with Firebase Auth
     */
    public void register(String username, String email, String password, AuthCallback callback) {
        Logger.methodEntry(Logger.TAG_AUTH, "register");
        Logger.startTiming("AUTH_REGISTER");
        
        if (username == null || username.trim().isEmpty()) {
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Username is required",
                "Please enter a username"
            ));
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Email is required",
                "Please enter your email address"
            ));
            return;
        }
        
        if (password == null || password.length() < 6) {
            callback.onError(new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password must be at least 6 characters",
                "Please enter a stronger password"
            ));
            return;
        }
        
        performRegistrationRequest(username.trim(), email.trim(), password, callback);
    }   
 
    /**
     * Perform Firebase login request
     */
    private void performLoginRequest(String email, String password, AuthCallback callback) {
        Logger.d(TAG, "Performing Firebase login for: %s", email.replaceAll("@.*", "@***"));
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        User user = createUserFromFirebaseUser(firebaseUser);
                        saveUserSession(user, firebaseUser);
                        
                        Logger.i(TAG, "✅ Login successful for user: %s", user.getUsername());
                        Logger.logAuthEvent("LOGIN_SUCCESS", user.getId(), true);
                        Logger.endTiming("AUTH_LOGIN");
                        callback.onSuccess(user);
                    } else {
                        AuthError error = new AuthError(
                            AuthError.Type.AUTHENTICATION_ERROR,
                            "Firebase user is null",
                            "Authentication failed. Please try again."
                        );
                        Logger.w(TAG, "Login failed: Firebase user is null");
                        Logger.endTiming("AUTH_LOGIN");
                        callback.onError(error);
                    }
                } else {
                    Exception exception = task.getException();
                    AuthError error = parseFirebaseError(exception);
                    Logger.w(TAG, "Login failed: %s", error.getMessage());
                    Logger.logAuthEvent("LOGIN_FAILED", email, false);
                    Logger.endTiming("AUTH_LOGIN");
                    callback.onError(error);
                }
            });
    }
    
    /**
     * Perform Firebase registration request
     */
    private void performRegistrationRequest(String username, String email, String password, AuthCallback callback) {
        Logger.d(TAG, "Performing Firebase registration for: %s", email.replaceAll("@.*", "@***"));
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Update display name
                        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = 
                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        
                        firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(profileTask -> {
                                User user = createUserFromFirebaseUser(firebaseUser, username);
                                saveUserSession(user, firebaseUser);
                                
                                Logger.i(TAG, "✅ Registration successful for user: %s", user.getUsername());
                                Logger.logAuthEvent("REGISTER_SUCCESS", user.getId(), true);
                                Logger.endTiming("AUTH_REGISTER");
                                callback.onSuccess(user);
                            });
                    } else {
                        AuthError error = new AuthError(
                            AuthError.Type.AUTHENTICATION_ERROR,
                            "Firebase user is null after registration",
                            "Registration failed. Please try again."
                        );
                        Logger.w(TAG, "Registration failed: Firebase user is null");
                        Logger.endTiming("AUTH_REGISTER");
                        callback.onError(error);
                    }
                } else {
                    Exception exception = task.getException();
                    AuthError error = parseFirebaseError(exception);
                    Logger.w(TAG, "Registration failed: %s", error.getMessage());
                    Logger.logAuthEvent("REGISTER_FAILED", email, false);
                    Logger.endTiming("AUTH_REGISTER");
                    callback.onError(error);
                }
            });
    }    
  
  /**
     * Create User object from Firebase user
     */
    private User createUserFromFirebaseUser(FirebaseUser firebaseUser) {
        String username = firebaseUser.getDisplayName();
        if (username == null || username.isEmpty()) {
            username = firebaseUser.getEmail().split("@")[0];
        }
        return createUserFromFirebaseUser(firebaseUser, username);
    }
    
    /**
     * Create User object from Firebase user with custom username
     */
    private User createUserFromFirebaseUser(FirebaseUser firebaseUser, String username) {
        return new User(
            firebaseUser.getUid(),
            username,
            firebaseUser.getEmail(),
            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
            User.UserStatus.ONLINE
        );
    }
    
    /**
     * Save user session to secure storage
     */
    private void saveUserSession(User user, FirebaseUser firebaseUser) {
        try {
            prefs.edit()
                .putString("user_id", user.getId())
                .putString("username", user.getUsername())
                .putString("email", user.getEmail())
                .putString("firebase_uid", firebaseUser.getUid())
                .putBoolean("is_logged_in", true)
                .putLong("login_timestamp", System.currentTimeMillis())
                .apply();
            
            Logger.d(TAG, "User session saved to secure storage");
        } catch (Exception e) {
            Logger.e(TAG, "Error saving user session", e);
        }
    }    

    /**
     * Parse Firebase authentication errors
     */
    private AuthError parseFirebaseError(Exception exception) {
        if (exception == null) {
            return new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                "Unknown error occurred",
                "An unexpected error occurred. Please try again."
            );
        }
        
        String message = exception.getMessage();
        if (message == null) {
            message = "Unknown error";
        }
        
        if (message.contains("password is invalid") || message.contains("wrong-password")) {
            return new AuthError(
                AuthError.Type.AUTHENTICATION_ERROR,
                "Invalid password",
                "The password you entered is incorrect. Please try again."
            );
        } else if (message.contains("no user record") || message.contains("user-not-found")) {
            return new AuthError(
                AuthError.Type.AUTHENTICATION_ERROR,
                "User not found",
                "No account found with this email address. Please check your email or register a new account."
            );
        } else if (message.contains("email address is already") || message.contains("email-already-in-use")) {
            return new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Email already in use",
                "An account with this email address already exists. Please use a different email or try logging in."
            );
        } else if (message.contains("network error") || message.contains("network")) {
            return new AuthError(
                AuthError.Type.NETWORK_ERROR,
                "Network error",
                "Please check your internet connection and try again."
            );
        } else if (message.contains("too many requests")) {
            return new AuthError(
                AuthError.Type.SERVER_ERROR,
                "Too many requests",
                "Too many failed attempts. Please wait a moment and try again."
            );
        } else if (message.contains("weak-password")) {
            return new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Password too weak",
                "Please choose a stronger password with at least 6 characters."
            );
        } else if (message.contains("invalid-email")) {
            return new AuthError(
                AuthError.Type.VALIDATION_ERROR,
                "Invalid email format",
                "Please enter a valid email address."
            );
        } else {
            return new AuthError(
                AuthError.Type.UNKNOWN_ERROR,
                message,
                "An error occurred during authentication. Please try again."
            );
        }
    }
    
    /**
     * Get currently logged in user
     */
    public User getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            return createUserFromFirebaseUser(firebaseUser);
        }
        return null;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        try {
            firebaseAuth.signOut();
            prefs.edit().clear().apply();
            Logger.i(TAG, "User logged out successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Error during logout", e);
        }
    }
    
    /**
     * Perform health check (for compatibility with existing code)
     */
    public boolean performHealthCheck() {
        try {
            return firebaseAuth.getCurrentUser() != null || NetworkUtils.isNetworkAvailable(context);
        } catch (Exception e) {
            Logger.e(TAG, "Error performing health check", e);
            return false;
        }
    }
    
    /**
     * Check server connectivity (for compatibility with existing code)
     */
    public void checkServerConnectivity(ServerConnectivityCallback callback) {
        try {
            boolean isConnected = NetworkUtils.isNetworkAvailable(context);
            String message = isConnected ? "Network connection available" : "No network connection";
            callback.onResult(isConnected, message);
        } catch (Exception e) {
            Logger.e(TAG, "Error checking server connectivity", e);
            callback.onResult(false, "Error checking connectivity: " + e.getMessage());
        }
    }
    
    /**
     * Get server diagnostics (for compatibility with existing code)
     */
    public void getServerDiagnostics(DiagnosticsCallback callback) {
        try {
            String diagnostics = NetworkUtils.performNetworkDiagnostics(context);
            callback.onResult(diagnostics);
        } catch (Exception e) {
            Logger.e(TAG, "Error getting server diagnostics", e);
            callback.onResult("Error getting diagnostics: " + e.getMessage());
        }
    }
    
    /**
     * Check if current session is valid
     */
    public boolean isSessionValid() {
        try {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                return false;
            }
            
            // Check if session timestamp is not too old (24 hours)
            long loginTimestamp = prefs.getLong("login_timestamp", 0);
            long currentTime = System.currentTimeMillis();
            long sessionAge = currentTime - loginTimestamp;
            long maxSessionAge = 24 * 60 * 60 * 1000; // 24 hours
            
            return sessionAge < maxSessionAge;
        } catch (Exception e) {
            Logger.e(TAG, "Error checking session validity", e);
            return false;
        }
    }
    
    /**
     * Get cached user from local storage
     */
    public User getCachedUser() {
        try {
            if (!prefs.getBoolean("is_logged_in", false)) {
                return null;
            }
            
            String userId = prefs.getString("user_id", null);
            String username = prefs.getString("username", null);
            String email = prefs.getString("email", null);
            
            if (userId != null && username != null && email != null) {
                return new User(userId, username, email, null, User.UserStatus.ONLINE);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error getting cached user", e);
        }
        return null;
    }
    
    // Callback interfaces for compatibility
    public interface ServerConnectivityCallback {
        void onResult(boolean isConnected, String message);
    }
    
    public interface DiagnosticsCallback {
        void onResult(String diagnostics);
    }
}