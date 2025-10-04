package com.cosmic.gatherly;

import android.app.Application;
import android.util.Log;

import com.cosmic.gatherly.data.storage.SecurePreferences;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class GatherlyApplication extends Application {
    private static final String TAG = "GatherlyApplication";
    
    private static GatherlyApplication instance;
    
    // Only essential components for Firebase Auth
    private SecurePreferences securePreferences;
    private com.cosmic.gatherly.data.repository.AuthManager authManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        Log.d(TAG, "Starting GatherlyApplication initialization");
        
        try {
            // Simple logging setup - no Timber to avoid dependency issues
            Log.d(TAG, "Using Android Log instead of Timber");
            
            // Initialize ThreeTenABP for better date/time handling
            AndroidThreeTen.init(this);
            Log.d(TAG, "ThreeTenABP initialized");
            
            // Initialize Firebase with error handling
            initializeFirebaseSimple();
            
            // Initialize only essential components
            initializeEssentialComponents();
            
            // Initialize AuthManager
            initializeAuthManager();
            
            Log.d(TAG, "✅ Gatherly Application initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Critical error in Application onCreate", e);
            // Continue anyway - don't crash the app
        }
    }
    
    private void initializeFirebaseSimple() {
        try {
            boolean firebaseInitialized = com.cosmic.gatherly.data.util.FirebaseUtils.initializeFirebase(this);
            
            if (firebaseInitialized) {
                Log.d(TAG, "✅ Firebase initialized successfully");
            } else {
                Log.e(TAG, "❌ Firebase initialization failed");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during Firebase initialization", e);
        }
    }
    
    private void initializeEssentialComponents() {
        try {
            // Only initialize what's absolutely necessary
            Log.d(TAG, "Initializing essential components");
            
            // Initialize secure preferences (needed for auth)
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
    
    public static GatherlyApplication getInstance() {
        return instance;
    }
    
    /**
     * Initialize AuthManager singleton instance
     */
    private void initializeAuthManager() {
        try {
            authManager = com.cosmic.gatherly.data.repository.AuthManagerImpl.getInstance(this);
            Log.d(TAG, "✅ AuthManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize AuthManager", e);
        }
    }
    
    // Getters for essential components only
    public SecurePreferences getSecurePreferences() {
        return securePreferences;
    }
    
    /**
     * Getter method for AuthManager access
     */
    public com.cosmic.gatherly.data.repository.AuthManager getAuthManager() {
        if (authManager == null) {
            Log.w(TAG, "AuthManager is null, attempting to reinitialize");
            initializeAuthManager();
        }
        return authManager;
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }
}