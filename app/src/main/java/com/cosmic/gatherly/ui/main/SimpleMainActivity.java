package com.cosmic.gatherly.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.ui.auth.FirebaseAuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Simple MainActivity for Firebase Auth testing
 * This is a minimal implementation to avoid crashes
 */
public class SimpleMainActivity extends AppCompatActivity {
    
    private static final String TAG = "SimpleMainActivity";
    private FirebaseAuth mAuth;
    private TextView welcomeText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SimpleMainActivity onCreate started");
        
        try {
            // Create a simple layout programmatically to avoid layout issues
            createSimpleLayout();
            
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is still authenticated
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated, redirecting to auth");
                redirectToAuth();
                return;
            }
            
            // Display user info
            displayUserInfo(currentUser);
            
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
        logoutButton.setOnClickListener(v -> logout());
        layout.addView(logoutButton);
        
        setContentView(layout);
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
    
    private void logout() {
        try {
            Log.d(TAG, "User logout requested");
            
            mAuth.signOut();
            
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            redirectToAuth();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
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