package com.cosmic.gatherly;

import android.app.Application;
import android.util.Log;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.data.repository.AuthManagerImpl;
import com.cosmic.gatherly.data.storage.SecurePreferences;
import com.google.firebase.FirebaseApp;
import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * MinimalApplication class with centralized authentication management
 * Initializes Firebase and provides app-wide access to AuthManager and other essential services
 */
public class MinimalApplication extends Application {
    private static final String TAG = "MinimalApplication";
    
    private static MinimalApplication instance;
    private AuthManager authManager;
    private SecurePreferences securePreferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "MinimalApplication started - initializing all essential components");
        
        try {
            // Initialize ThreeTenABP for better date/time handling
            AndroidThreeTen.init(this);
            Log.d(TAG, "ThreeTenABP initialized");
            
            // Initialize Firebase (already partially done check)
            initializeFirebase();
            
            // Initialize essential components
            initializeEssentialComponents();
            
            // Create AuthManager singleton instance
            initializeAuthManager();
            
            // Set up global auth state checking
            setupGlobalAuthStateChecking();
            
            Log.d(TAG, "✅ MinimalApplication initialization completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Critical error in MinimalApplication onCreate", e);
            // Continue anyway - don't crash the app
        }
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
     * Initialize essential components needed by the UI
     */
    private void initializeEssentialComponents() {
        try {
            Log.d(TAG, "Initializing essential components");
            
            // Initialize secure preferences (needed for auth and other features)
            try {
                securePreferences = new SecurePreferences(this);
                Log.d(TAG, "✅ Secure preferences initialized");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize secure preferences", e);
            }
            
            Log.d(TAG, "✅ Essential components initialized");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing essential components", e);
        }
    }

    /**
     * Get singleton instance
     */
    public static MinimalApplication getInstance() {
        return instance;
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

    /**
     * Getter for SecurePreferences
     */
    public SecurePreferences getSecurePreferences() {
        return securePreferences;
    }
}