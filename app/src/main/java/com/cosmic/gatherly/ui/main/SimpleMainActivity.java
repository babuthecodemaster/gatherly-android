package com.cosmic.gatherly.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.auth.FirebaseAuthActivity;
import com.google.firebase.auth.FirebaseUser;

/**
 * Simple MainActivity for Firebase Auth testing
 * This is a minimal implementation to avoid crashes
 */
public class SimpleMainActivity extends AppCompatActivity {
    
    private static final String TAG = "SimpleMainActivity";
    private AuthManager authManager;
    private TextView welcomeText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SimpleMainActivity onCreate started");
        
        try {
            // Get AuthManager from Application
            authManager = ((MinimalApplication) getApplication()).getAuthManager();
            if (authManager == null) {
                Log.e(TAG, "AuthManager is null, redirecting to auth");
                redirectToAuth();
                return;
            }
            
            // Create a simple layout programmatically to avoid layout issues
            createSimpleLayout();
            
            // Check if user is still authenticated
            FirebaseUser currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated, redirecting to auth");
                redirectToAuth();
                return;
            }
            
            // Display user info
            displayUserInfo(currentUser);
            
            // Setup AuthState observer for automatic auth state handling
            setupAuthStateObserver();
            
            Log.d(TAG, "✅ SimpleMainActivity initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in SimpleMainActivity onCreate", e);
            Toast.makeText(this, "Error loading main screen", Toast.LENGTH_SHORT).show();
            redirectToAuth();
        }
    }
    
    private void createSimpleLayout() {
        // Create a simple linear layout programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        layout.setBackgroundColor(0xFF0A0A0F); // cosmic_black
        
        // Welcome text
        welcomeText = new TextView(this);
        welcomeText.setText("Welcome to Gatherly!");
        welcomeText.setTextColor(0xFFFFFFFF); // white
        welcomeText.setTextSize(24);
        welcomeText.setPadding(0, 0, 0, 32);
        layout.addView(welcomeText);
        
        // User info text
        TextView userInfoText = new TextView(this);
        userInfoText.setText("You are successfully logged in with Firebase Auth!");
        userInfoText.setTextColor(0xFFB8B8CC); // cosmic_text_secondary
        userInfoText.setTextSize(16);
        userInfoText.setPadding(0, 0, 0, 48);
        layout.addView(userInfoText);
        
        // Logout button
        logoutButton = new Button(this);
        logoutButton.setText("Logout");
        logoutButton.setBackgroundColor(0xFF6C63FF); // cosmic_accent
        logoutButton.setTextColor(0xFFFFFFFF); // white
        logoutButton.setOnClickListener(v -> performLogout());
        layout.addView(logoutButton);
        
        setContentView(layout);
    }
    
    /**
     * Sets up AuthState observer for automatic navigation to auth screen on logout
     */
    private void setupAuthStateObserver() {
        if (authManager != null) {
            authManager.getAuthState().observe(this, authState -> {
                Log.d(TAG, "AuthState changed: " + authState.getStatus());
                
                switch (authState.getStatus()) {
                    case UNAUTHENTICATED:
                        Log.d(TAG, "User unauthenticated, navigating to auth");
                        redirectToAuth();
                        break;
                    case ERROR:
                        Log.e(TAG, "Auth error: " + authState.getErrorMessage());
                        Toast.makeText(this, "Authentication error: " + authState.getErrorMessage(), 
                            Toast.LENGTH_LONG).show();
                        break;
                    case AUTHENTICATED:
                        Log.d(TAG, "User authenticated: " + (authState.getUser() != null ? authState.getUser().getEmail() : "null"));
                        break;
                    case LOADING:
                        Log.d(TAG, "Authentication loading...");
                        break;
                }
            });
        }
    }
    
    private void displayUserInfo(FirebaseUser user) {
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        
        String welcomeMessage = "Welcome";
        if (displayName != null && !displayName.isEmpty()) {
            welcomeMessage += ", " + displayName + "!";
        } else if (email != null) {
            welcomeMessage += ", " + email + "!";
        } else {
            welcomeMessage += "!";
        }
        
        welcomeText.setText(welcomeMessage);
        Log.d(TAG, "Displaying info for user: " + email);
    }
    
    /**
     * Performs logout with confirmation dialog
     * Uses centralized AuthManager for logout operation
     */
    private void performLogout() {
        try {
            Log.d(TAG, "Logout requested by user");
            
            // Show confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🚪 Logout");
            builder.setMessage("Are you sure you want to logout?");
            
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Log.d(TAG, "User confirmed logout");
                
                if (authManager != null) {
                    // Use AuthManager for logout
                    authManager.signOut()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "✅ Logout successful");
                                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                // Navigation handled by AuthState observer
                            } else {
                                Log.e(TAG, "❌ Logout failed", task.getException());
                                Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
                                // Still navigate to auth for safety
                                redirectToAuth();
                            }
                        })
                        .addOnFailureListener(exception -> {
                            Log.e(TAG, "❌ Logout operation failed", exception);
                            Toast.makeText(this, "Error during logout: " + exception.getMessage(), 
                                Toast.LENGTH_LONG).show();
                            // Still navigate to auth for safety
                            redirectToAuth();
                        });
                } else {
                    Log.e(TAG, "AuthManager is null during logout");
                    Toast.makeText(this, "Error: Authentication manager not available", Toast.LENGTH_SHORT).show();
                    redirectToAuth();
                }
            });
            
            builder.setNegativeButton("No", (dialog, which) -> {
                Log.d(TAG, "User cancelled logout");
                dialog.dismiss();
            });
            
            builder.show();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing logout confirmation", e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
            redirectToAuth();
        }
    }
    
    private void redirectToAuth() {
        try {
            Intent intent = new Intent(this, FirebaseAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to auth", e);
            finish();
        }
    }
}