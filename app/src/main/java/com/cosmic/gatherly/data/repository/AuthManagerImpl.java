package com.cosmic.gatherly.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.model.UserProfile;
import com.cosmic.gatherly.data.util.Logger;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

/**
 * AuthManagerImpl provides centralized authentication management for the application
 * Implements singleton pattern for app-wide access to authentication state and operations
 * Handles Firebase Auth integration, Firestore user profiles, and persistent authentication
 */
public class AuthManagerImpl implements AuthManager {
    private static final String TAG = "AuthManagerImpl";
    
    // SharedPreferences constants
    private static final String PREF_NAME = "gatherly_auth";
    private static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    private static final String KEY_LAST_LOGIN_EMAIL = "last_login_email";
    private static final String KEY_USER_PROFILE_CACHE = "user_profile_cache";
    private static final String KEY_LAST_AUTH_CHECK = "last_auth_check";
    
    // Singleton instance
    private static AuthManagerImpl instance;
    
    // Dependencies
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;
    private final SharedPreferences mPrefs;
    private final FirestoreUserService mUserService;
    private final Gson mGson;
    
    // State management
    private final MutableLiveData<AuthState> authStateLiveData;
    private UserProfile cachedUserProfile;
    
    /**
     * Private constructor to enforce singleton pattern
     * @param context Application context for SharedPreferences
     */
    private AuthManagerImpl(Context context) {
        Logger.methodEntry(TAG, "AuthManagerImpl constructor");
        
        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mUserService = new FirestoreUserService(mFirestore);
        
        // Initialize SharedPreferences
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Initialize utilities
        mGson = new Gson();
        
        // Initialize state management
        authStateLiveData = new MutableLiveData<>(AuthState.loading());
        
        // Load cached user profile
        loadCachedUserProfile();
        
        // Set up Firebase Auth state listener
        setupAuthStateListener();
        
        Logger.i(TAG, "✅ AuthManagerImpl initialized successfully");
    }
    
    /**
     * Gets the singleton instance of AuthManagerImpl
     * @param context Application context
     * @return AuthManagerImpl singleton instance
     */
    public static synchronized AuthManagerImpl getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManagerImpl(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Sets up Firebase Auth state listener for automatic state updates
     */
    private void setupAuthStateListener() {
        Logger.d(TAG, "Setting up Firebase Auth state listener");
        
        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            Logger.d(TAG, "Firebase Auth state changed. User: %s", 
                firebaseUser != null ? firebaseUser.getUid() : "null");
            
            if (firebaseUser != null) {
                // User is signed in, load or create profile
                handleUserSignedIn(firebaseUser);
            } else {
                // User is signed out
                handleUserSignedOut();
            }
        });
    }
    
    /**
     * Handles user signed in state
     * @param firebaseUser The authenticated Firebase user
     */
    private void handleUserSignedIn(FirebaseUser firebaseUser) {
        Logger.d(TAG, "Handling user signed in: %s", firebaseUser.getUid());
        
        // Update auth state to loading while we fetch profile
        authStateLiveData.setValue(AuthState.loading());
        
        // Use comprehensive profile loading with fallback
        loadUserProfileWithFallback(firebaseUser)
            .addOnSuccessListener(userProfile -> {
                Logger.i(TAG, "✅ User profile loaded successfully with fallback handling");
                authStateLiveData.setValue(AuthState.authenticated(firebaseUser, userProfile));
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to load user profile even with fallback handling", exception);
                // Still authenticate but without profile as last resort
                authStateLiveData.setValue(AuthState.authenticated(firebaseUser));
            });
    }
    
    /**
     * Handles user signed out state
     */
    private void handleUserSignedOut() {
        Logger.d(TAG, "Handling user signed out");
        cachedUserProfile = null;
        clearCachedUserProfile();
        authStateLiveData.setValue(AuthState.unauthenticated());
    }
    
    /**
     * Loads cached user profile from SharedPreferences
     */
    private void loadCachedUserProfile() {
        try {
            String cachedProfileJson = mPrefs.getString(KEY_USER_PROFILE_CACHE, null);
            if (cachedProfileJson != null) {
                cachedUserProfile = mGson.fromJson(cachedProfileJson, UserProfile.class);
                Logger.d(TAG, "Loaded cached user profile for UID: %s", 
                    cachedUserProfile != null ? cachedUserProfile.getUid() : "null");
            }
        } catch (Exception e) {
            Logger.w(TAG, "Failed to load cached user profile", e);
            cachedUserProfile = null;
        }
    }
    
    /**
     * Caches user profile in SharedPreferences
     * @param userProfile The user profile to cache
     */
    private void cacheUserProfile(UserProfile userProfile) {
        try {
            String profileJson = mGson.toJson(userProfile);
            mPrefs.edit()
                .putString(KEY_USER_PROFILE_CACHE, profileJson)
                .apply();
            Logger.d(TAG, "Cached user profile for UID: %s", userProfile.getUid());
        } catch (Exception e) {
            Logger.w(TAG, "Failed to cache user profile", e);
        }
    }
    
    /**
     * Clears cached user profile from SharedPreferences
     */
    private void clearCachedUserProfile() {
        mPrefs.edit()
            .remove(KEY_USER_PROFILE_CACHE)
            .apply();
        Logger.d(TAG, "Cleared cached user profile");
    }
    
    // AuthManager interface implementation
    
    @Override
    public LiveData<AuthState> getAuthState() {
        return authStateLiveData;
    }
    
    @Override
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = currentUser != null;
        Logger.d(TAG, "isUserLoggedIn: %s", isLoggedIn);
        return isLoggedIn;
    }
    
    @Override
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    @Override
    public Task<AuthResult> signIn(String email, String password) {
        Logger.methodEntry(TAG, "signIn");
        Logger.startTiming("AUTH_SIGN_IN");
        
        if (email == null || email.trim().isEmpty()) {
            Logger.w(TAG, "Sign in failed: email is null or empty");
            return Tasks.forException(new IllegalArgumentException("Email cannot be null or empty"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            Logger.w(TAG, "Sign in failed: password is null or empty");
            return Tasks.forException(new IllegalArgumentException("Password cannot be null or empty"));
        }
        
        Logger.d(TAG, "Signing in user with email: %s", email);
        authStateLiveData.setValue(AuthState.loading());
        
        return mAuth.signInWithEmailAndPassword(email.trim(), password)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    AuthResult authResult = task.getResult();
                    FirebaseUser firebaseUser = authResult.getUser();
                    
                    if (firebaseUser != null) {
                        Logger.i(TAG, "✅ User signed in successfully, updating last login timestamp");
                        
                        // Update last login timestamp in Firestore
                        return mUserService.updateLastLogin(firebaseUser.getUid())
                            .continueWith(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Logger.i(TAG, "✅ Last login timestamp updated successfully");
                                } else {
                                    Logger.w(TAG, "Failed to update last login timestamp, but sign in succeeded", updateTask.getException());
                                }
                                
                                // Save last login email if auto-login is enabled
                                if (isAutoLoginEnabled()) {
                                    mPrefs.edit()
                                        .putString(KEY_LAST_LOGIN_EMAIL, email.trim())
                                        .apply();
                                }
                                
                                Logger.endTiming("AUTH_SIGN_IN");
                                return authResult;
                            });
                    } else {
                        Logger.e(TAG, "❌ Sign in succeeded but FirebaseUser is null");
                        Logger.endTiming("AUTH_SIGN_IN");
                        authStateLiveData.setValue(AuthState.error("Authentication succeeded but user data is unavailable"));
                        return Tasks.forException(new RuntimeException("FirebaseUser is null after successful sign in"));
                    }
                } else {
                    Logger.e(TAG, "❌ Sign in failed", task.getException());
                    Logger.endTiming("AUTH_SIGN_IN");
                    authStateLiveData.setValue(AuthState.error(task.getException().getMessage()));
                    return task;
                }
            });
    }
    
    @Override
    public Task<AuthResult> signUp(String email, String password) {
        Logger.methodEntry(TAG, "signUp");
        Logger.startTiming("AUTH_SIGN_UP");
        
        if (email == null || email.trim().isEmpty()) {
            Logger.w(TAG, "Sign up failed: email is null or empty");
            return Tasks.forException(new IllegalArgumentException("Email cannot be null or empty"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            Logger.w(TAG, "Sign up failed: password is null or empty");
            return Tasks.forException(new IllegalArgumentException("Password cannot be null or empty"));
        }
        
        Logger.d(TAG, "Signing up user with email: %s", email);
        authStateLiveData.setValue(AuthState.loading());
        
        return mAuth.createUserWithEmailAndPassword(email.trim(), password)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    AuthResult authResult = task.getResult();
                    FirebaseUser firebaseUser = authResult.getUser();
                    
                    if (firebaseUser != null) {
                        Logger.i(TAG, "✅ User signed up successfully, creating user profile");
                        
                        // Create user profile in Firestore immediately after successful registration
                        UserProfile newProfile = new UserProfile(firebaseUser);
                        return mUserService.createUserProfile(newProfile)
                            .continueWith(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Logger.i(TAG, "✅ User profile created successfully during registration");
                                    cachedUserProfile = newProfile;
                                    cacheUserProfile(newProfile);
                                    authStateLiveData.setValue(AuthState.authenticated(firebaseUser, newProfile));
                                } else {
                                    Logger.w(TAG, "Failed to create user profile during registration, but authentication succeeded", profileTask.getException());
                                    // Still proceed with authentication even if profile creation fails
                                    authStateLiveData.setValue(AuthState.authenticated(firebaseUser));
                                }
                                
                                // Save last login email if auto-login is enabled
                                if (isAutoLoginEnabled()) {
                                    mPrefs.edit()
                                        .putString(KEY_LAST_LOGIN_EMAIL, email.trim())
                                        .apply();
                                }
                                
                                Logger.endTiming("AUTH_SIGN_UP");
                                return authResult;
                            });
                    } else {
                        Logger.e(TAG, "❌ Sign up succeeded but FirebaseUser is null");
                        Logger.endTiming("AUTH_SIGN_UP");
                        authStateLiveData.setValue(AuthState.error("Authentication succeeded but user data is unavailable"));
                        return Tasks.forException(new RuntimeException("FirebaseUser is null after successful registration"));
                    }
                } else {
                    Logger.e(TAG, "❌ Sign up failed", task.getException());
                    Logger.endTiming("AUTH_SIGN_UP");
                    authStateLiveData.setValue(AuthState.error(task.getException().getMessage()));
                    return task;
                }
            });
    }
    
    @Override
    public Task<Void> signOut() {
        Logger.methodEntry(TAG, "signOut");
        Logger.startTiming("AUTH_SIGN_OUT");
        
        Logger.d(TAG, "Signing out current user");
        authStateLiveData.setValue(AuthState.loading());
        
        // Clear cached data
        cachedUserProfile = null;
        clearCachedUserProfile();
        
        // Clear last login email
        mPrefs.edit()
            .remove(KEY_LAST_LOGIN_EMAIL)
            .apply();
        
        // Sign out from Firebase
        mAuth.signOut();
        
        Logger.i(TAG, "✅ User signed out successfully");
        Logger.endTiming("AUTH_SIGN_OUT");
        
        // Return completed task
        return Tasks.forResult(null);
    }
    
    @Override
    public void checkAuthState() {
        Logger.methodEntry(TAG, "checkAuthState");
        Logger.startTiming("AUTH_CHECK_STATE");
        
        Logger.d(TAG, "Checking current authentication state");
        
        // Update last auth check timestamp
        mPrefs.edit()
            .putLong(KEY_LAST_AUTH_CHECK, System.currentTimeMillis())
            .apply();
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Logger.d(TAG, "Current user found: %s", currentUser.getUid());
            handleUserSignedIn(currentUser);
        } else {
            Logger.d(TAG, "No current user found");
            handleUserSignedOut();
        }
        
        Logger.endTiming("AUTH_CHECK_STATE");
    }
    
    @Override
    public void enableAutoLogin(boolean enabled) {
        Logger.d(TAG, "Setting auto-login enabled: %s", enabled);
        mPrefs.edit()
            .putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled)
            .apply();
        
        if (!enabled) {
            // Clear last login email when disabling auto-login
            mPrefs.edit()
                .remove(KEY_LAST_LOGIN_EMAIL)
                .apply();
        }
    }
    
    @Override
    public boolean isAutoLoginEnabled() {
        boolean enabled = mPrefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, true); // Default to enabled
        Logger.d(TAG, "Auto-login enabled: %s", enabled);
        return enabled;
    }
    
    @Override
    public Task<UserProfile> getUserProfile() {
        Logger.methodEntry(TAG, "getUserProfile");
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot get user profile: no authenticated user");
            return Tasks.forException(new IllegalStateException("No authenticated user"));
        }
        
        Logger.d(TAG, "Getting user profile for UID: %s", currentUser.getUid());
        
        // Use fallback mechanism for profile retrieval
        return mUserService.getUserProfile(currentUser.getUid())
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    UserProfile profile = task.getResult();
                    cachedUserProfile = profile;
                    cacheUserProfile(profile);
                    return Tasks.forResult(profile);
                } else {
                    Logger.w(TAG, "Failed to get user profile from Firestore, trying cached profile", task.getException());
                    
                    // Try to use cached profile as fallback
                    if (cachedUserProfile != null && currentUser.getUid().equals(cachedUserProfile.getUid())) {
                        Logger.i(TAG, "✅ Using cached user profile as fallback for getUserProfile");
                        return Tasks.forResult(cachedUserProfile);
                    } else {
                        Logger.w(TAG, "No valid cached profile available for getUserProfile");
                        return Tasks.forException(task.getException());
                    }
                }
            });
    }
    
    @Override
    public Task<Void> updateUserProfile(UserProfile profile) {
        Logger.methodEntry(TAG, "updateUserProfile");
        
        if (profile == null) {
            Logger.w(TAG, "Cannot update user profile: profile is null");
            return Tasks.forException(new IllegalArgumentException("UserProfile cannot be null"));
        }
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot update user profile: no authenticated user");
            return Tasks.forException(new IllegalStateException("No authenticated user"));
        }
        
        Logger.d(TAG, "Updating user profile for UID: %s", profile.getUid());
        return mUserService.updateUserProfile(profile)
            .addOnSuccessListener(aVoid -> {
                cachedUserProfile = profile;
                cacheUserProfile(profile);
                
                // Update auth state with new profile
                authStateLiveData.setValue(AuthState.authenticated(currentUser, profile));
            });
    }
    
    @Override
    public UserProfile getCachedUserProfile() {
        Logger.d(TAG, "Getting cached user profile: %s", 
            cachedUserProfile != null ? cachedUserProfile.getUid() : "null");
        return cachedUserProfile;
    }
    
    @Override
    public Task<UserProfile> refreshUserProfile() {
        Logger.methodEntry(TAG, "refreshUserProfile");
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot refresh user profile: no authenticated user");
            return Tasks.forException(new IllegalStateException("No authenticated user"));
        }
        
        Logger.d(TAG, "Refreshing user profile for UID: %s", currentUser.getUid());
        return mUserService.getUserProfile(currentUser.getUid())
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    UserProfile userProfile = task.getResult();
                    cachedUserProfile = userProfile;
                    cacheUserProfile(userProfile);
                    
                    // Update auth state with refreshed profile
                    authStateLiveData.setValue(AuthState.authenticated(currentUser, userProfile));
                    Logger.i(TAG, "✅ User profile refreshed successfully");
                    return Tasks.forResult(userProfile);
                } else {
                    Logger.w(TAG, "Failed to refresh user profile from Firestore, using cached profile if available", task.getException());
                    
                    // If refresh fails, return cached profile if available
                    if (cachedUserProfile != null && currentUser.getUid().equals(cachedUserProfile.getUid())) {
                        Logger.i(TAG, "✅ Using cached user profile for refresh fallback");
                        // Update auth state with cached profile
                        authStateLiveData.setValue(AuthState.authenticated(currentUser, cachedUserProfile));
                        return Tasks.forResult(cachedUserProfile);
                    } else {
                        Logger.e(TAG, "❌ Failed to refresh user profile and no cached profile available");
                        return Tasks.forException(task.getException());
                    }
                }
            });
    }
    
    @Override
    public String getCurrentUserId() {
        FirebaseUser currentUser = getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : null;
        Logger.d(TAG, "getCurrentUserId: %s", uid);
        return uid;
    }
    
    @Override
    public String getCurrentUserEmail() {
        FirebaseUser currentUser = getCurrentUser();
        String email = currentUser != null ? currentUser.getEmail() : null;
        Logger.d(TAG, "getCurrentUserEmail: %s", email);
        return email;
    }
    
    @Override
    public boolean isEmailVerified() {
        FirebaseUser currentUser = getCurrentUser();
        boolean verified = currentUser != null && currentUser.isEmailVerified();
        Logger.d(TAG, "isEmailVerified: %s", verified);
        return verified;
    }
    
    @Override
    public Task<Void> sendEmailVerification() {
        Logger.methodEntry(TAG, "sendEmailVerification");
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot send email verification: no authenticated user");
            return Tasks.forException(new IllegalStateException("No authenticated user"));
        }
        
        Logger.d(TAG, "Sending email verification to: %s", currentUser.getEmail());
        return currentUser.sendEmailVerification()
            .addOnSuccessListener(aVoid -> {
                Logger.i(TAG, "✅ Email verification sent successfully");
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to send email verification", exception);
            });
    }
    
    /**
     * Gets the last login email from SharedPreferences
     * @return Last login email or null if not available
     */
    public String getLastLoginEmail() {
        String email = mPrefs.getString(KEY_LAST_LOGIN_EMAIL, null);
        Logger.d(TAG, "getLastLoginEmail: %s", email);
        return email;
    }
    
    /**
     * Gets the timestamp of the last auth state check
     * @return Timestamp of last auth check or 0 if never checked
     */
    public long getLastAuthCheckTimestamp() {
        long timestamp = mPrefs.getLong(KEY_LAST_AUTH_CHECK, 0);
        Logger.d(TAG, "getLastAuthCheckTimestamp: %s", timestamp);
        return timestamp;
    }
    
    /**
     * Loads user profile with comprehensive fallback handling
     * Tries Firestore first, then cached profile, then creates new profile
     * @param firebaseUser The authenticated Firebase user
     * @return Task<UserProfile> representing the async operation
     */
    private Task<UserProfile> loadUserProfileWithFallback(FirebaseUser firebaseUser) {
        Logger.methodEntry(TAG, "loadUserProfileWithFallback");
        
        if (firebaseUser == null) {
            Logger.w(TAG, "Cannot load user profile: FirebaseUser is null");
            return Tasks.forException(new IllegalArgumentException("FirebaseUser cannot be null"));
        }
        
        String uid = firebaseUser.getUid();
        Logger.d(TAG, "Loading user profile with fallback for UID: %s", uid);
        
        // First, try to load from Firestore
        return mUserService.getUserProfile(uid)
            .continueWithTask(task -> {
                if (task.isSuccessful()) {
                    UserProfile profile = task.getResult();
                    Logger.i(TAG, "✅ User profile loaded from Firestore successfully");
                    
                    // Update last login timestamp
                    profile.updateLastLogin();
                    
                    // Cache the profile
                    cachedUserProfile = profile;
                    cacheUserProfile(profile);
                    
                    // Update profile in Firestore with new last login timestamp
                    mUserService.updateUserProfile(profile)
                        .addOnFailureListener(updateException -> {
                            Logger.w(TAG, "Failed to update last login timestamp in Firestore", updateException);
                        });
                    
                    return Tasks.forResult(profile);
                } else {
                    Logger.w(TAG, "Failed to load user profile from Firestore, trying fallback options", task.getException());
                    
                    // Second, try to use cached profile if available and valid
                    if (cachedUserProfile != null && uid.equals(cachedUserProfile.getUid())) {
                        Logger.i(TAG, "✅ Using cached user profile as fallback");
                        
                        // Update last login timestamp in cached profile
                        cachedUserProfile.updateLastLogin();
                        cacheUserProfile(cachedUserProfile);
                        
                        // Try to create/update profile in Firestore in background
                        mUserService.createUserProfile(cachedUserProfile)
                            .addOnSuccessListener(aVoid -> {
                                Logger.i(TAG, "✅ Cached profile successfully synced to Firestore");
                            })
                            .addOnFailureListener(syncException -> {
                                Logger.w(TAG, "Failed to sync cached profile to Firestore", syncException);
                            });
                        
                        return Tasks.forResult(cachedUserProfile);
                    } else {
                        Logger.d(TAG, "No valid cached profile available, creating new profile");
                        
                        // Third, create new profile from FirebaseUser
                        UserProfile newProfile = new UserProfile(firebaseUser);
                        
                        // Cache the new profile immediately
                        cachedUserProfile = newProfile;
                        cacheUserProfile(newProfile);
                        
                        // Try to save to Firestore
                        mUserService.createUserProfile(newProfile)
                            .addOnSuccessListener(aVoid -> {
                                Logger.i(TAG, "✅ New user profile created and saved to Firestore");
                            })
                            .addOnFailureListener(createException -> {
                                Logger.w(TAG, "Failed to save new profile to Firestore, but profile is cached locally", createException);
                            });
                        
                        return Tasks.forResult(newProfile);
                    }
                }
            });
    }
    
    /**
     * Validates if cached user profile is still valid
     * @param uid The user ID to validate against
     * @return true if cached profile is valid, false otherwise
     */
    private boolean isCachedProfileValid(String uid) {
        if (cachedUserProfile == null) {
            Logger.d(TAG, "Cached profile is null");
            return false;
        }
        
        if (!uid.equals(cachedUserProfile.getUid())) {
            Logger.d(TAG, "Cached profile UID mismatch: expected %s, got %s", uid, cachedUserProfile.getUid());
            return false;
        }
        
        // Check if cached profile is not too old (24 hours)
        long cacheAge = System.currentTimeMillis() - cachedUserProfile.getLastLoginAt();
        long maxCacheAge = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
        
        if (cacheAge > maxCacheAge) {
            Logger.d(TAG, "Cached profile is too old: %d ms", cacheAge);
            return false;
        }
        
        Logger.d(TAG, "Cached profile is valid for UID: %s", uid);
        return true;
    }
    
    /**
     * Gets user profile with offline support
     * Returns cached profile immediately if available, then tries to refresh from Firestore
     * @return UserProfile from cache or null if not available
     */
    public UserProfile getUserProfileOffline() {
        Logger.methodEntry(TAG, "getUserProfileOffline");
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot get offline user profile: no authenticated user");
            return null;
        }
        
        if (isCachedProfileValid(currentUser.getUid())) {
            Logger.i(TAG, "✅ Returning valid cached user profile for offline access");
            return cachedUserProfile;
        } else {
            Logger.w(TAG, "No valid cached profile available for offline access");
            return null;
        }
    }
    
    /**
     * Forces a cache refresh by clearing cached data and reloading from Firestore
     * @return Task<UserProfile> representing the refresh operation
     */
    public Task<UserProfile> forceCacheRefresh() {
        Logger.methodEntry(TAG, "forceCacheRefresh");
        
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Logger.w(TAG, "Cannot force cache refresh: no authenticated user");
            return Tasks.forException(new IllegalStateException("No authenticated user"));
        }
        
        Logger.d(TAG, "Forcing cache refresh for UID: %s", currentUser.getUid());
        
        // Clear cached data
        cachedUserProfile = null;
        clearCachedUserProfile();
        
        // Reload profile with fallback
        return loadUserProfileWithFallback(currentUser);
    }
    
    /**
     * Clears all authentication data and preferences
     * Used for complete logout or account deletion
     */
    public void clearAllAuthData() {
        Logger.methodEntry(TAG, "clearAllAuthData");
        
        Logger.d(TAG, "Clearing all authentication data");
        
        // Clear cached data
        cachedUserProfile = null;
        
        // Clear all SharedPreferences
        mPrefs.edit().clear().apply();
        
        // Sign out from Firebase
        mAuth.signOut();
        
        // Update auth state
        authStateLiveData.setValue(AuthState.unauthenticated());
        
        Logger.i(TAG, "✅ All authentication data cleared");
    }
}