package com.cosmic.gatherly.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.auth.FirebaseAuthActivity;
import com.cosmic.gatherly.ui.main.SimpleMainActivity;

public class SplashActivity extends AppCompatActivity {
    
    private static final String TAG = "SplashActivity";
    private static final int MIN_SPLASH_DELAY = 1500; // Minimum 1.5 seconds for branding
    
    private AuthManager authManager;
    private ProgressBar loadingIndicator;
    private TextView loadingText;
    private long splashStartTime;
    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "SplashActivity created successfully");
            
            // Record splash start time for minimum display duration
            splashStartTime = System.currentTimeMillis();
            
            // Initialize UI components
            initializeUI();
            
            // Get AuthManager instance from Application class
            initializeAuthManager();
            
            // Set up authentication state observation
            setupAuthStateObserver();
            
            // Trigger auto-authentication check
            triggerAuthStateCheck();
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in SplashActivity onCreate", e);
            handleCriticalError("Failed to initialize splash screen");
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        try {
            loadingIndicator = findViewById(R.id.loadingIndicator);
            if (loadingIndicator == null) {
                // Find the ProgressBar in the layout (it doesn't have an ID in the current layout)
                loadingIndicator = findViewById(android.R.id.progress);
            }
            
            // Find loading text (the "Loading Cosmic" text)
            loadingText = findViewById(R.id.loadingText);
            
            Log.d(TAG, "UI components initialized");
        } catch (Exception e) {
            Log.w(TAG, "Error initializing UI components", e);
            // Continue without UI references - they're optional
        }
    }

    /**
     * Get AuthManager instance from Application class
     */
    private void initializeAuthManager() {
        try {
            MinimalApplication app = (MinimalApplication) getApplication();
            authManager = app.getAuthManager();
            
            if (authManager == null) {
                throw new IllegalStateException("AuthManager is null from Application");
            }
            
            Log.d(TAG, "✅ AuthManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize AuthManager", e);
            handleCriticalError("Authentication system unavailable");
        }
    }

    /**
     * Set up AuthState LiveData observer for reactive navigation
     */
    private void setupAuthStateObserver() {
        if (authManager == null) {
            Log.e(TAG, "Cannot setup auth state observer - AuthManager is null");
            return;
        }
        
        try {
            authManager.getAuthState().observe(this, new Observer<AuthState>() {
                @Override
                public void onChanged(AuthState authState) {
                    handleAuthStateChange(authState);
                }
            });
            
            Log.d(TAG, "✅ Auth state observer set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to setup auth state observer", e);
            handleCriticalError("Failed to setup authentication monitoring");
        }
    }

    /**
     * Handle authentication state changes with navigation logic
     */
    private void handleAuthStateChange(AuthState authState) {
        if (navigationHandled) {
            Log.d(TAG, "Navigation already handled, ignoring auth state change");
            return;
        }
        
        try {
            Log.d(TAG, "Auth state changed: " + authState.toString());
            
            switch (authState.getStatus()) {
                case LOADING:
                    handleLoadingState();
                    break;
                    
                case AUTHENTICATED:
                    handleAuthenticatedState(authState);
                    break;
                    
                case UNAUTHENTICATED:
                    handleUnauthenticatedState();
                    break;
                    
                case ERROR:
                    handleAuthErrorState(authState);
                    break;
                    
                default:
                    Log.w(TAG, "Unknown auth state: " + authState.getStatus());
                    handleUnauthenticatedState();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling auth state change", e);
            handleUnauthenticatedState();
        }
    }

    /**
     * Handle loading state - show loading indicator
     */
    private void handleLoadingState() {
        Log.d(TAG, "Handling loading state");
        
        // Show loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }
        
        // Update loading text if available
        if (loadingText != null) {
            loadingText.setText("Checking authentication...");
        }
    }

    /**
     * Handle authenticated state - navigate to main activity
     */
    private void handleAuthenticatedState(AuthState authState) {
        Log.d(TAG, "Handling authenticated state for user: " + authState.getUserId());
        
        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
        
        // Update loading text
        if (loadingText != null) {
            loadingText.setText("Welcome back!");
        }
        
        // Navigate to main activity with minimum splash delay
        navigateWithDelay(() -> navigateToMain(), "main activity");
    }

    /**
     * Handle unauthenticated state - navigate to auth activity
     */
    private void handleUnauthenticatedState() {
        Log.d(TAG, "Handling unauthenticated state");
        
        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
        
        // Update loading text
        if (loadingText != null) {
            loadingText.setText("Please sign in");
        }
        
        // Navigate to auth activity with minimum splash delay
        navigateWithDelay(() -> navigateToAuth(), "auth activity");
    }

    /**
     * Handle authentication error state with proper user feedback
     */
    private void handleAuthErrorState(AuthState authState) {
        Log.e(TAG, "Handling auth error state: " + authState.getErrorMessage());
        
        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
        
        // Update loading text
        if (loadingText != null) {
            loadingText.setText("Authentication error");
        }
        
        // Show error message to user
        String errorMessage = authState.getErrorMessage();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Authentication failed. Please try again.";
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        // Navigate to auth activity after showing error
        navigateWithDelay(() -> navigateToAuth(), "auth activity (after error)");
    }

    /**
     * Navigate with minimum splash delay for branding
     */
    private void navigateWithDelay(Runnable navigationAction, String destination) {
        long elapsedTime = System.currentTimeMillis() - splashStartTime;
        long remainingDelay = Math.max(0, MIN_SPLASH_DELAY - elapsedTime);
        
        Log.d(TAG, "Navigating to " + destination + " in " + remainingDelay + " ms");
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!navigationHandled) {
                navigationAction.run();
            }
        }, remainingDelay);
    }

    /**
     * Call authManager.checkAuthState() to trigger auto-login
     */
    private void triggerAuthStateCheck() {
        if (authManager == null) {
            Log.e(TAG, "Cannot trigger auth state check - AuthManager is null");
            handleCriticalError("Authentication system unavailable");
            return;
        }
        
        try {
            Log.d(TAG, "Triggering authentication state check for auto-login");
            authManager.checkAuthState();
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to trigger auth state check", e);
            handleAuthErrorState(AuthState.error("Failed to check authentication state"));
        }
    }

    /**
     * Navigate to authentication activity
     */
    private void navigateToAuth() {
        if (navigationHandled) {
            Log.d(TAG, "Navigation already handled, skipping auth navigation");
            return;
        }
        
        try {
            navigationHandled = true;
            Log.d(TAG, "Navigating to authentication activity");
            
            Intent intent = new Intent(this, FirebaseAuthActivity.class);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to auth activity", e);
            handleCriticalError("Failed to open authentication screen");
        }
    }
    
    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        if (navigationHandled) {
            Log.d(TAG, "Navigation already handled, skipping main navigation");
            return;
        }
        
        try {
            navigationHandled = true;
            Log.d(TAG, "Navigating to main activity");
            
            Intent intent = new Intent(this, SimpleMainActivity.class);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to main activity", e);
            // Fallback to auth activity
            navigationHandled = false;
            navigateToAuth();
        }
    }

    /**
     * Handle critical errors that prevent normal operation
     */
    private void handleCriticalError(String message) {
        Log.e(TAG, "Critical error: " + message);
        
        // Show error to user
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Try to navigate to auth activity as fallback
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Intent intent = new Intent(this, FirebaseAuthActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to navigate to fallback activity", e);
                // Last resort - close the app
                finish();
            }
        }, 2000); // Give user time to read the error message
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity destroyed");
    }
}