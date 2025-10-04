package com.cosmic.gatherly.data.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * AuthPreferences handles authentication-related preferences and settings
 * for persistent login functionality and user profile caching.
 */
public class AuthPreferences {
    private static final String TAG = "AuthPreferences";
    private static final String PREF_NAME = "gatherly_auth";
    
    // Preference keys for authentication settings
    public static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    public static final String KEY_LAST_LOGIN_EMAIL = "last_login_email";
    public static final String KEY_USER_PROFILE_CACHE = "user_profile_cache";
    public static final String KEY_LAST_AUTH_CHECK = "last_auth_check";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String KEY_SESSION_TIMEOUT = "session_timeout";
    public static final String KEY_LAST_SUCCESSFUL_LOGIN = "last_successful_login";
    public static final String KEY_LOGIN_ATTEMPTS = "login_attempts";
    public static final String KEY_LAST_FAILED_LOGIN = "last_failed_login";
    
    // Default values
    private static final boolean DEFAULT_AUTO_LOGIN_ENABLED = true;
    private static final boolean DEFAULT_REMEMBER_ME = true;
    private static final boolean DEFAULT_BIOMETRIC_ENABLED = false;
    private static final long DEFAULT_SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private static final int DEFAULT_LOGIN_ATTEMPTS = 0;
    
    private SharedPreferences preferences;
    private Gson gson;
    
    public AuthPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        Log.d(TAG, "AuthPreferences initialized");
    }
    
    // Auto-login settings
    public void setAutoLoginEnabled(boolean enabled) {
        try {
            preferences.edit().putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled).apply();
            Log.d(TAG, "Auto-login enabled set to: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting auto-login enabled", e);
        }
    }
    
    public boolean isAutoLoginEnabled() {
        try {
            return preferences.getBoolean(KEY_AUTO_LOGIN_ENABLED, DEFAULT_AUTO_LOGIN_ENABLED);
        } catch (Exception e) {
            Log.e(TAG, "Error getting auto-login enabled", e);
            return DEFAULT_AUTO_LOGIN_ENABLED;
        }
    }
    
    // Last login email
    public void setLastLoginEmail(String email) {
        try {
            preferences.edit().putString(KEY_LAST_LOGIN_EMAIL, email).apply();
            Log.d(TAG, "Last login email saved");
        } catch (Exception e) {
            Log.e(TAG, "Error setting last login email", e);
        }
    }
    
    public String getLastLoginEmail() {
        try {
            return preferences.getString(KEY_LAST_LOGIN_EMAIL, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting last login email", e);
            return null;
        }
    }
    
    // User profile caching
    public void cacheUserProfile(Object userProfile) {
        try {
            String profileJson = gson.toJson(userProfile);
            preferences.edit().putString(KEY_USER_PROFILE_CACHE, profileJson).apply();
            Log.d(TAG, "User profile cached successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error caching user profile", e);
        }
    }
    
    public <T> T getCachedUserProfile(Class<T> profileClass) {
        try {
            String profileJson = preferences.getString(KEY_USER_PROFILE_CACHE, null);
            if (profileJson != null) {
                return gson.fromJson(profileJson, profileClass);
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing cached user profile", e);
            clearCachedUserProfile();
        } catch (Exception e) {
            Log.e(TAG, "Error getting cached user profile", e);
        }
        return null;
    }
    
    public void clearCachedUserProfile() {
        try {
            preferences.edit().remove(KEY_USER_PROFILE_CACHE).apply();
            Log.d(TAG, "Cached user profile cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cached user profile", e);
        }
    }
    
    public boolean hasCachedUserProfile() {
        try {
            return preferences.contains(KEY_USER_PROFILE_CACHE) && 
                   preferences.getString(KEY_USER_PROFILE_CACHE, null) != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking cached user profile", e);
            return false;
        }
    }
    
    // Authentication state tracking
    public void setLastAuthCheck(long timestamp) {
        try {
            preferences.edit().putLong(KEY_LAST_AUTH_CHECK, timestamp).apply();
            Log.d(TAG, "Last auth check timestamp updated");
        } catch (Exception e) {
            Log.e(TAG, "Error setting last auth check", e);
        }
    }
    
    public long getLastAuthCheck() {
        try {
            return preferences.getLong(KEY_LAST_AUTH_CHECK, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting last auth check", e);
            return 0;
        }
    }
    
    // Remember me functionality
    public void setRememberMe(boolean remember) {
        try {
            preferences.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
            Log.d(TAG, "Remember me set to: " + remember);
        } catch (Exception e) {
            Log.e(TAG, "Error setting remember me", e);
        }
    }
    
    public boolean isRememberMeEnabled() {
        try {
            return preferences.getBoolean(KEY_REMEMBER_ME, DEFAULT_REMEMBER_ME);
        } catch (Exception e) {
            Log.e(TAG, "Error getting remember me", e);
            return DEFAULT_REMEMBER_ME;
        }
    }
    
    // Biometric authentication
    public void setBiometricEnabled(boolean enabled) {
        try {
            preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
            Log.d(TAG, "Biometric enabled set to: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting biometric enabled", e);
        }
    }
    
    public boolean isBiometricEnabled() {
        try {
            return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, DEFAULT_BIOMETRIC_ENABLED);
        } catch (Exception e) {
            Log.e(TAG, "Error getting biometric enabled", e);
            return DEFAULT_BIOMETRIC_ENABLED;
        }
    }  
  
    // Session management
    public void setSessionTimeout(long timeoutMillis) {
        try {
            preferences.edit().putLong(KEY_SESSION_TIMEOUT, timeoutMillis).apply();
            Log.d(TAG, "Session timeout set to: " + timeoutMillis + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Error setting session timeout", e);
        }
    }
    
    public long getSessionTimeout() {
        try {
            return preferences.getLong(KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT);
        } catch (Exception e) {
            Log.e(TAG, "Error getting session timeout", e);
            return DEFAULT_SESSION_TIMEOUT;
        }
    }
    
    public void setLastSuccessfulLogin(long timestamp) {
        try {
            preferences.edit().putLong(KEY_LAST_SUCCESSFUL_LOGIN, timestamp).apply();
            Log.d(TAG, "Last successful login timestamp updated");
        } catch (Exception e) {
            Log.e(TAG, "Error setting last successful login", e);
        }
    }
    
    public long getLastSuccessfulLogin() {
        try {
            return preferences.getLong(KEY_LAST_SUCCESSFUL_LOGIN, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting last successful login", e);
            return 0;
        }
    }
    
    // Login attempt tracking for security
    public void incrementLoginAttempts() {
        try {
            int currentAttempts = getLoginAttempts();
            preferences.edit().putInt(KEY_LOGIN_ATTEMPTS, currentAttempts + 1).apply();
            Log.d(TAG, "Login attempts incremented to: " + (currentAttempts + 1));
        } catch (Exception e) {
            Log.e(TAG, "Error incrementing login attempts", e);
        }
    }
    
    public void resetLoginAttempts() {
        try {
            preferences.edit().putInt(KEY_LOGIN_ATTEMPTS, 0).apply();
            Log.d(TAG, "Login attempts reset");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting login attempts", e);
        }
    }
    
    public int getLoginAttempts() {
        try {
            return preferences.getInt(KEY_LOGIN_ATTEMPTS, DEFAULT_LOGIN_ATTEMPTS);
        } catch (Exception e) {
            Log.e(TAG, "Error getting login attempts", e);
            return DEFAULT_LOGIN_ATTEMPTS;
        }
    }
    
    public void setLastFailedLogin(long timestamp) {
        try {
            preferences.edit().putLong(KEY_LAST_FAILED_LOGIN, timestamp).apply();
            Log.d(TAG, "Last failed login timestamp updated");
        } catch (Exception e) {
            Log.e(TAG, "Error setting last failed login", e);
        }
    }
    
    public long getLastFailedLogin() {
        try {
            return preferences.getLong(KEY_LAST_FAILED_LOGIN, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting last failed login", e);
            return 0;
        }
    }
    
    // Session validation
    public boolean isSessionValid() {
        try {
            long lastLogin = getLastSuccessfulLogin();
            long sessionTimeout = getSessionTimeout();
            long currentTime = System.currentTimeMillis();
            
            if (lastLogin == 0) {
                return false; // No previous login
            }
            
            boolean isValid = (currentTime - lastLogin) < sessionTimeout;
            Log.d(TAG, "Session validation: " + isValid + 
                  " (last login: " + lastLogin + 
                  ", timeout: " + sessionTimeout + 
                  ", current: " + currentTime + ")");
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error validating session", e);
            return false;
        }
    }
    
    // Utility methods
    public void clearAllAuthData() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(KEY_LAST_LOGIN_EMAIL);
            editor.remove(KEY_USER_PROFILE_CACHE);
            editor.remove(KEY_LAST_AUTH_CHECK);
            editor.remove(KEY_LAST_SUCCESSFUL_LOGIN);
            editor.remove(KEY_LAST_FAILED_LOGIN);
            editor.putInt(KEY_LOGIN_ATTEMPTS, 0);
            editor.apply();
            Log.d(TAG, "All authentication data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing authentication data", e);
        }
    }
    
    public void clearAllPreferences() {
        try {
            preferences.edit().clear().apply();
            Log.d(TAG, "All authentication preferences cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all preferences", e);
        }
    }
    
    // Convenience method to check if auto-login should be performed
    public boolean shouldPerformAutoLogin() {
        try {
            return isAutoLoginEnabled() && 
                   isRememberMeEnabled() && 
                   isSessionValid() && 
                   getLastLoginEmail() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if auto-login should be performed", e);
            return false;
        }
    }
    
    // Method to update login success
    public void onLoginSuccess(String email) {
        try {
            long currentTime = System.currentTimeMillis();
            setLastLoginEmail(email);
            setLastSuccessfulLogin(currentTime);
            setLastAuthCheck(currentTime);
            resetLoginAttempts();
            Log.d(TAG, "Login success recorded for email: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Error recording login success", e);
        }
    }
    
    // Method to update login failure
    public void onLoginFailure() {
        try {
            long currentTime = System.currentTimeMillis();
            setLastFailedLogin(currentTime);
            incrementLoginAttempts();
            Log.d(TAG, "Login failure recorded");
        } catch (Exception e) {
            Log.e(TAG, "Error recording login failure", e);
        }
    }
    
    // Method to check if account is temporarily locked due to failed attempts
    public boolean isAccountTemporarilyLocked() {
        try {
            int maxAttempts = 5; // Maximum allowed login attempts
            long lockoutDuration = 15 * 60 * 1000; // 15 minutes in milliseconds
            
            int attempts = getLoginAttempts();
            long lastFailedLogin = getLastFailedLogin();
            long currentTime = System.currentTimeMillis();
            
            if (attempts >= maxAttempts && lastFailedLogin > 0) {
                boolean isLocked = (currentTime - lastFailedLogin) < lockoutDuration;
                Log.d(TAG, "Account lockout check: " + isLocked + 
                      " (attempts: " + attempts + 
                      ", last failed: " + lastFailedLogin + 
                      ", current: " + currentTime + ")");
                return isLocked;
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking account lockout", e);
            return false;
        }
    }
    
    // Method to get remaining lockout time in milliseconds
    public long getRemainingLockoutTime() {
        try {
            if (!isAccountTemporarilyLocked()) {
                return 0;
            }
            
            long lockoutDuration = 15 * 60 * 1000; // 15 minutes
            long lastFailedLogin = getLastFailedLogin();
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastFailedLogin;
            
            return Math.max(0, lockoutDuration - elapsed);
        } catch (Exception e) {
            Log.e(TAG, "Error getting remaining lockout time", e);
            return 0;
        }
    }
}