package com.cosmic.gatherly;

import android.app.Application;
import android.util.Log;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.data.repository.AuthManagerImpl;
import com.google.firebase.FirebaseApp;

/**
 * MinimalApplication class with centralized authentication management
 * Initializes Firebase and provides app-wide access to AuthManager
 */
public class MinimalApplication extends Application {
    private static final String TAG = "MinimalApplication";
    
    private AuthManager authManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MinimalApplication started - initializing Firebase and AuthManager");
        
        // Initialize Firebase (already partially done check)
        initializeFirebase();
        
        // Create AuthManager singleton instance
        initializeAuthManager();
        
        // Set up global auth state checking
        setupGlobalAuthStateChecking();
        
        Log.d(TAG, "MinimalApplication initialization completed successfully");
    }
    
    /**
     * Initialize Firebase if not already initialized
     */
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "✅ Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize Firebase", e);
        }
    }
    
    /**
     * Initialize AuthManager singleton instance
     */
    private void initializeAuthManager() {
        try {
            authManager = AuthManagerImpl.getInstance(this);
            Log.d(TAG, "✅ AuthManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize AuthManager", e);
        }
    }
    
    /**
     * Set up global auth state checking
     * This will check the current authentication state on app startup
     */
    private void setupGlobalAuthStateChecking() {
        try {
            if (authManager != null) {
                // Trigger auth state check to restore authentication state
                authManager.checkAuthState();
                Log.d(TAG, "✅ Global auth state checking initiated");
            } else {
                Log.w(TAG, "Cannot setup auth state checking - AuthManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to setup global auth state checking", e);
        }
    }
    
    /**
     * Getter method for AuthManager access
     * Provides app-wide access to the centralized authentication manager
     * @return AuthManager instance for authentication operations
     */
    public AuthManager getAuthManager() {
        if (authManager == null) {
            Log.w(TAG, "AuthManager is null, attempting to reinitialize");
            initializeAuthManager();
        }
        return authManager;
    }
}