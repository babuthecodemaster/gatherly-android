package com.cosmic.gatherly.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.ui.auth.MinimalFirebaseAuthActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Minimal Splash Activity - No dependencies, no crashes
 */
public class MinimalSplashActivity extends AppCompatActivity {
    
    private static final String TAG = "MinimalSplash";
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "MinimalSplashActivity started");
        
        try {
            // Create simple UI programmatically
            createSimpleUI();
            
            // Initialize Firebase and check auth after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                initializeAndNavigate();
            }, SPLASH_DELAY);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in splash onCreate", e);
            // Go directly to auth if anything fails
            navigateToAuth();
        }
    }
    
    private void createSimpleUI() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0A0A0F); // Dark background
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(64, 64, 64, 64);
        
        // App name
        TextView appName = new TextView(this);
        appName.setText("Gatherly");
        appName.setTextColor(0xFFFFFFFF);
        appName.setTextSize(32);
        appName.setPadding(0, 0, 0, 32);
        layout.addView(appName);
        
        // Loading text
        TextView loadingText = new TextView(this);
        loadingText.setText("Loading...");
        loadingText.setTextColor(0xFF6C63FF);
        loadingText.setTextSize(16);
        loadingText.setPadding(0, 0, 0, 24);
        layout.addView(loadingText);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(this);
        layout.addView(progressBar);
        
        setContentView(layout);
    }
    
    private void initializeAndNavigate() {
        try {
            Log.d(TAG, "Initializing Firebase");
            
            // Initialize Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            }
            
            // Check authentication
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            
            if (currentUser != null) {
                Log.d(TAG, "User is signed in: " + currentUser.getEmail());
                // For now, just go to auth activity to show success
                navigateToAuth();
            } else {
                Log.d(TAG, "User not signed in, going to auth");
                navigateToAuth();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during initialization", e);
            navigateToAuth();
        }
    }
    
    private void navigateToAuth() {
        try {
            Intent intent = new Intent(this, MinimalFirebaseAuthActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to auth", e);
            finish();
        }
    }
}