package com.cosmic.gatherly.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Ultra Minimal Splash Activity - Absolutely no dependencies
 * This should work on any Android device without crashes
 */
public class UltraMinimalSplashActivity extends Activity {
    
    private static final String TAG = "UltraMinimalSplash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "UltraMinimalSplashActivity started");
        
        try {
            // Create the simplest possible UI
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(0xFF000000); // Black background
            layout.setGravity(android.view.Gravity.CENTER);
            
            TextView textView = new TextView(this);
            textView.setText("Gatherly\n\nLoading...");
            textView.setTextColor(0xFFFFFFFF); // White text
            textView.setTextSize(20);
            textView.setGravity(android.view.Gravity.CENTER);
            
            layout.addView(textView);
            setContentView(layout);
            
            Log.d(TAG, "UI created successfully");
            
            // Navigate after 3 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Log.d(TAG, "Navigating to auth activity");
                    Intent intent = new Intent(this, com.cosmic.gatherly.ui.auth.UltraMinimalAuthActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating", e);
                    // Just show a message if navigation fails
                    textView.setText("Gatherly\n\nNavigation Error\nPlease restart app");
                }
            }, 3000);
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            // Even if everything fails, don't crash
            try {
                TextView errorText = new TextView(this);
                errorText.setText("Error loading app\nPlease restart");
                errorText.setTextColor(0xFFFF0000);
                errorText.setGravity(android.view.Gravity.CENTER);
                setContentView(errorText);
            } catch (Exception e2) {
                Log.e(TAG, "Even error handling failed", e2);
            }
        }
    }
}