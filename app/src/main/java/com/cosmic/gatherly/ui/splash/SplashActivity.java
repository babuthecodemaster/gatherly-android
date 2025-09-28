package com.cosmic.gatherly.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.util.FirebaseUtils;
import com.cosmic.gatherly.ui.auth.FirebaseAuthActivity;
import com.cosmic.gatherly.ui.main.SimpleMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "SplashActivity created successfully");
            
            // Initialize Firebase with error handling
            try {
                if (!FirebaseUtils.initializeFirebase(this)) {
                    Log.e(TAG, "Firebase initialization failed, continuing anyway");
                }
                
                // Initialize Firebase Auth
                mAuth = FirebaseAuth.getInstance();
                Log.d(TAG, "Firebase Auth initialized");
                
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Firebase", e);
                // Continue without Firebase - go directly to auth activity
                navigateToAuth();
                return;
            }

            // Delay the splash screen and check authentication status
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    checkAuthenticationStatus();
                } catch (Exception e) {
                    Log.e(TAG, "Error checking authentication status", e);
                    navigateToAuth();
                }
            }, SPLASH_DELAY);
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in SplashActivity onCreate", e);
            // Fallback - go directly to auth activity
            navigateToAuth();
        }
    }

    private void checkAuthenticationStatus() {
        try {
            if (mAuth == null) {
                Log.w(TAG, "FirebaseAuth is null, going to auth activity");
                navigateToAuth();
                return;
            }
            
            FirebaseUser currentUser = mAuth.getCurrentUser();
            
            if (currentUser != null) {
                // User is logged in, go to main activity
                Log.d(TAG, "User is signed in: " + currentUser.getEmail());
                navigateToMain();
            } else {
                // User is not logged in, go to Firebase auth activity
                Log.d(TAG, "User is not signed in, redirecting to auth");
                navigateToAuth();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAuthenticationStatus", e);
            navigateToAuth();
        }
    }
    
    private void navigateToAuth() {
        try {
            Intent intent = new Intent(this, FirebaseAuthActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to auth activity", e);
            // Last resort - close the app
            finish();
        }
    }
    
    private void navigateToMain() {
        try {
            Intent intent = new Intent(this, SimpleMainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main activity", e);
            // Fallback to auth activity
            navigateToAuth();
        }
    }
}