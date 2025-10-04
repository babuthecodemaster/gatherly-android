package com.cosmic.gatherly.ui.main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.auth.UltraMinimalAuthActivity;
import com.google.firebase.auth.FirebaseUser;

/**
 * Working Main Activity - Shows after successful Firebase Auth
 * This is the main app screen that users see after logging in
 */
public class WorkingMainActivity extends AppCompatActivity {
    
    private static final String TAG = "WorkingMainActivity";
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "WorkingMainActivity started");
        
        try {
            // Get AuthManager from Application
            authManager = ((MinimalApplication) getApplication()).getAuthManager();
            if (authManager == null) {
                Log.e(TAG, "AuthManager is null, redirecting to auth");
                redirectToAuth();
                return;
            }
            
            // Check if user is still authenticated
            FirebaseUser currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated, redirecting to auth");
                redirectToAuth();
                return;
            }
            
            createMainUI(currentUser);
            
            // Setup AuthState observer for automatic auth state handling
            setupAuthStateObserver();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in WorkingMainActivity", e);
            Toast.makeText(this, "Error loading main screen", Toast.LENGTH_SHORT).show();
            redirectToAuth();
        }
    }
    
    private void createMainUI(FirebaseUser user) {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 50, 50, 50);
        mainLayout.setBackgroundColor(0xFF0A0A0F); // Dark background
        mainLayout.setGravity(android.view.Gravity.CENTER);
        
        // Welcome header
        TextView welcomeText = new TextView(this);
        welcomeText.setText("🎉 Welcome to Gatherly!");
        welcomeText.setTextColor(0xFFFFFFFF);
        welcomeText.setTextSize(28);
        welcomeText.setPadding(0, 0, 0, 20);
        welcomeText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(welcomeText);
        
        // User info
        TextView userInfoText = new TextView(this);
        userInfoText.setText("✅ Successfully signed in as:\n" + user.getEmail() + "\n\nFirebase Authentication Working!");
        userInfoText.setTextColor(0xFF4ECDC4);
        userInfoText.setTextSize(16);
        userInfoText.setPadding(0, 0, 0, 40);
        userInfoText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(userInfoText);
        
        // App features placeholder
        TextView featuresText = new TextView(this);
        featuresText.setText("🚀 Main App Features:\n\n• Real-time Chat\n• Server Management\n• User Profiles\n• Voice Channels\n• Message History\n\n(Ready for implementation)");
        featuresText.setTextColor(0xFFB8B8CC);
        featuresText.setTextSize(14);
        featuresText.setPadding(0, 0, 0, 40);
        featuresText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(featuresText);
        
        // Logout button
        Button logoutButton = new Button(this);
        logoutButton.setText("Sign Out");
        logoutButton.setBackgroundColor(0xFFFF6B6B);
        logoutButton.setTextColor(0xFFFFFFFF);
        logoutButton.setOnClickListener(v -> performLogout());
        LinearLayout.LayoutParams logoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        logoutParams.setMargins(0, 20, 0, 0);
        logoutButton.setLayoutParams(logoutParams);
        mainLayout.addView(logoutButton);
        
        // App info
        TextView appInfoText = new TextView(this);
        appInfoText.setText("\n🔥 Gatherly v1.0\nPowered by Firebase Authentication\nNo crashes, fully functional!");
        appInfoText.setTextColor(0xFF888888);
        appInfoText.setTextSize(12);
        appInfoText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(appInfoText);
        
        setContentView(mainLayout);
        
        Log.d(TAG, "Main UI created successfully for user: " + user.getEmail());
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
            Intent intent = new Intent(this, UltraMinimalAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to auth", e);
            finish();
        }
    }
}