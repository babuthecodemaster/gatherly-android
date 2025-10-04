package com.cosmic.gatherly.data.repository;

import androidx.lifecycle.LiveData;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.model.UserProfile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

/**
 * AuthManager interface for centralized authentication management
 * Provides methods for authentication state, operations, and user data management
 * Supports reactive state management through LiveData
 */
public interface AuthManager {
    
    // Authentication state management
    
    /**
     * Gets the current authentication state as LiveData for reactive UI updates
     * @return LiveData<AuthState> that emits authentication state changes
     */
    LiveData<AuthState> getAuthState();
    
    /**
     * Checks if a user is currently logged in
     * @return true if user is authenticated, false otherwise
     */
    boolean isUserLoggedIn();
    
    /**
     * Gets the current Firebase user
     * @return FirebaseUser if authenticated, null otherwise
     */
    FirebaseUser getCurrentUser();
    
    // Authentication operations
    
    /**
     * Signs in a user with email and password
     * Creates or updates user profile in Firestore on successful authentication
     * @param email User's email address
     * @param password User's password
     * @return Task<AuthResult> representing the sign-in operation
     */
    Task<AuthResult> signIn(String email, String password);
    
    /**
     * Signs up a new user with email and password
     * Creates user profile in Firestore on successful registration
     * @param email User's email address
     * @param password User's password
     * @return Task<AuthResult> representing the sign-up operation
     */
    Task<AuthResult> signUp(String email, String password);
    
    /**
     * Signs out the current user
     * Clears authentication state and cached user data
     * @return Task<Void> representing the sign-out operation
     */
    Task<Void> signOut();
    
    // Auto-authentication and persistence
    
    /**
     * Checks current authentication state and performs auto-login if applicable
     * Should be called on app startup to restore authentication state
     */
    void checkAuthState();
    
    /**
     * Enables or disables auto-login functionality
     * @param enabled true to enable auto-login, false to disable
     */
    void enableAutoLogin(boolean enabled);
    
    /**
     * Checks if auto-login is currently enabled
     * @return true if auto-login is enabled, false otherwise
     */
    boolean isAutoLoginEnabled();
    
    // User data management
    
    /**
     * Gets the current user's profile from Firestore
     * @return Task<UserProfile> representing the profile retrieval operation
     */
    Task<UserProfile> getUserProfile();
    
    /**
     * Updates the current user's profile in Firestore
     * @param profile UserProfile object with updated data
     * @return Task<Void> representing the profile update operation
     */
    Task<Void> updateUserProfile(UserProfile profile);
    
    /**
     * Gets the cached user profile from local storage
     * @return UserProfile if cached, null otherwise
     */
    UserProfile getCachedUserProfile();
    
    /**
     * Refreshes user profile data from Firestore
     * Updates both remote and cached profile data
     * @return Task<UserProfile> representing the profile refresh operation
     */
    Task<UserProfile> refreshUserProfile();
    
    // Authentication state utilities
    
    /**
     * Gets the current user's UID
     * @return User UID if authenticated, null otherwise
     */
    String getCurrentUserId();
    
    /**
     * Gets the current user's email
     * @return User email if authenticated, null otherwise
     */
    String getCurrentUserEmail();
    
    /**
     * Checks if the current user's email is verified
     * @return true if email is verified, false otherwise
     */
    boolean isEmailVerified();
    
    /**
     * Sends email verification to the current user
     * @return Task<Void> representing the email verification operation
     */
    Task<Void> sendEmailVerification();
    
    // Offline and caching support
    
    /**
     * Gets user profile with offline support
     * Returns cached profile immediately if available
     * @return UserProfile from cache or null if not available
     */
    UserProfile getUserProfileOffline();
    
    /**
     * Forces a cache refresh by clearing cached data and reloading from Firestore
     * @return Task<UserProfile> representing the refresh operation
     */
    Task<UserProfile> forceCacheRefresh();
}