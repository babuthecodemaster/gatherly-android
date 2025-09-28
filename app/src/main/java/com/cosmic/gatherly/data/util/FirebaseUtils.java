package com.cosmic.gatherly.data.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

/**
 * Utility class for Firebase initialization and health checks
 */
public class FirebaseUtils {
    private static final String TAG = "FirebaseUtils";
    
    private static boolean isInitialized = false;
    private static boolean initializationFailed = false;
    private static String initializationError = null;
    
    /**
     * Initialize Firebase with comprehensive error handling
     */
    public static boolean initializeFirebase(Context context) {
        if (isInitialized) {
            Log.d(TAG, "Firebase already initialized successfully");
            return true;
        }
        
        if (initializationFailed) {
            Log.w(TAG, String.format("Firebase initialization previously failed: %s", initializationError));
            return false;
        }
        
        try {
            Logger.methodEntry(Logger.TAG_FIREBASE, "initializeFirebase");
            Logger.startTiming("FIREBASE_INITIALIZATION");
            
            // Check if Firebase is already initialized by another component
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.d(TAG, "No Firebase apps found, initializing default app");
                
                // Initialize default Firebase app
                FirebaseApp.initializeApp(context);
                Log.d(TAG, "Firebase default app initialized successfully");
            } else {
                Log.d(TAG, "Firebase apps already exist, using existing configuration");
            }
            
            // Verify Firebase app is available
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            if (defaultApp == null) {
                throw new RuntimeException("Firebase default app is null after initialization");
            }
            
            Log.d(TAG, String.format("Firebase app name: %s", defaultApp.getName()));
            Log.d(TAG, String.format("Firebase project ID: %s", defaultApp.getOptions().getProjectId()));
            
            // Initialize Firebase services with error handling
            initializeFirebaseServices(context);
            
            isInitialized = true;
            Logger.endTiming("FIREBASE_INITIALIZATION");
            Logger.i(Logger.TAG_FIREBASE, "✅ Firebase initialized successfully");
            
            return true;
            
        } catch (Exception e) {
            initializationFailed = true;
            initializationError = e.getMessage();
            
            Logger.endTiming("FIREBASE_INITIALIZATION");
            Logger.e(Logger.TAG_FIREBASE, "❌ Firebase initialization failed", e);
            Log.e(TAG, "Firebase initialization failed", e);
            
            // Log to Crashlytics if available
            try {
                FirebaseCrashlytics.getInstance().recordException(e);
            } catch (Exception crashlyticsError) {
                Log.w(TAG, "Could not log to Crashlytics", crashlyticsError);
            }
            
            return false;
        } finally {
            Logger.methodExit(Logger.TAG_FIREBASE, "initializeFirebase");
        }
    }
    
    /**
     * Initialize individual Firebase services
     */
    private static void initializeFirebaseServices(Context context) {
        Logger.safeExecute(Logger.TAG_FIREBASE, "initialize_firebase_auth", () -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth != null) {
                Log.d(TAG, "Firebase Auth initialized successfully");
                Logger.d(Logger.TAG_FIREBASE, "Firebase Auth current user: %s", 
                    auth.getCurrentUser() != null ? "Signed in" : "Not signed in");
            } else {
                Log.w(TAG, "Firebase Auth initialization returned null");
            }
        });
        
        Logger.safeExecute(Logger.TAG_FIREBASE, "initialize_firebase_crashlytics", () -> {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCrashlyticsCollectionEnabled(true);
            Log.d(TAG, "Firebase Crashlytics initialized successfully");
        });
        
        Logger.safeExecute(Logger.TAG_FIREBASE, "initialize_firebase_remote_config", () -> {
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour
                .build();
            remoteConfig.setConfigSettingsAsync(configSettings);
            Log.d(TAG, "Firebase Remote Config initialized successfully");
        });
    }
    
    /**
     * Check if Firebase is properly initialized and healthy
     */
    public static boolean isFirebaseHealthy() {
        try {
            if (!isInitialized) {
                Log.d(TAG, "Firebase health check: Not initialized");
                return false;
            }
            
            // Check if Firebase app is still available
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            if (defaultApp == null) {
                Log.w(TAG, "Firebase health check: Default app is null");
                return false;
            }
            
            // Check Firebase Auth
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth == null) {
                Log.w(TAG, "Firebase health check: Auth instance is null");
                return false;
            }
            
            // Check Firebase Crashlytics
            try {
                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                if (crashlytics == null) {
                    Log.w(TAG, "Firebase health check: Crashlytics instance is null");
                    return false;
                }
            } catch (Exception e) {
                Log.w(TAG, "Firebase health check: Crashlytics not available", e);
                // Crashlytics not being available shouldn't fail the health check
            }
            
            Log.d(TAG, "✅ Firebase health check passed");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase health check failed", e);
            return false;
        }
    }
    
    /**
     * Get Firebase initialization status and diagnostics
     */
    public static String getFirebaseDiagnostics(Context context) {
        StringBuilder diagnostics = new StringBuilder("Firebase Diagnostics:\n");
        
        try {
            diagnostics.append("• Initialization Status: ");
            if (isInitialized) {
                diagnostics.append("✅ Initialized");
            } else if (initializationFailed) {
                diagnostics.append("❌ Failed (").append(initializationError).append(")");
            } else {
                diagnostics.append("⏳ Not Attempted");
            }
            diagnostics.append("\n");
            
            // Check Firebase apps
            int appCount = FirebaseApp.getApps(context).size();
            diagnostics.append("• Firebase Apps Count: ").append(appCount).append("\n");
            
            if (appCount > 0) {
                try {
                    FirebaseApp defaultApp = FirebaseApp.getInstance();
                    diagnostics.append("• Default App Name: ").append(defaultApp.getName()).append("\n");
                    diagnostics.append("• Project ID: ").append(defaultApp.getOptions().getProjectId()).append("\n");
                    diagnostics.append("• Application ID: ").append(defaultApp.getOptions().getApplicationId()).append("\n");
                } catch (Exception e) {
                    diagnostics.append("• Default App Error: ").append(e.getMessage()).append("\n");
                }
            }
            
            // Check individual services
            try {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                diagnostics.append("• Firebase Auth: ").append(auth != null ? "✅ Available" : "❌ Null").append("\n");
                if (auth != null) {
                    diagnostics.append("• Auth User: ").append(auth.getCurrentUser() != null ? "Signed In" : "Not Signed In").append("\n");
                }
            } catch (Exception e) {
                diagnostics.append("• Firebase Auth Error: ").append(e.getMessage()).append("\n");
            }
            
            try {
                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                diagnostics.append("• Firebase Crashlytics: ").append(crashlytics != null ? "✅ Available" : "❌ Null").append("\n");
            } catch (Exception e) {
                diagnostics.append("• Firebase Crashlytics: ❌ Not Available (").append(e.getMessage()).append(")\n");
            }
            
            // Overall health
            boolean healthy = isFirebaseHealthy();
            diagnostics.append("• Overall Health: ").append(healthy ? "✅ Healthy" : "❌ Unhealthy").append("\n");
            
        } catch (Exception e) {
            diagnostics.append("• Diagnostics Error: ").append(e.getMessage()).append("\n");
            Log.e(TAG, "Error generating Firebase diagnostics", e);
        }
        
        return diagnostics.toString();
    }
    
    /**
     * Reset Firebase initialization state (for testing or recovery)
     */
    public static void resetInitializationState() {
        Log.w(TAG, "Resetting Firebase initialization state");
        isInitialized = false;
        initializationFailed = false;
        initializationError = null;
    }
    
    /**
     * Get Firebase initialization error if any
     */
    public static String getInitializationError() {
        return initializationError;
    }
    
    /**
     * Check if Firebase initialization failed
     */
    public static boolean hasInitializationFailed() {
        return initializationFailed;
    }
}