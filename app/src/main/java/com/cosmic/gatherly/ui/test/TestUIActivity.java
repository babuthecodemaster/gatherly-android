package com.cosmic.gatherly.ui.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Simple test activity to verify UI elements are responding
 */
public class TestUIActivity extends Activity {
    
    private static final String TAG = "TestUIActivity";
    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "TestUIActivity started");
        
        createTestUI();
    }
    
    private void createTestUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 50, 50, 50);
        mainLayout.setBackgroundColor(0xFF0A0A0F); // Dark background
        mainLayout.setGravity(android.view.Gravity.CENTER);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("🧪 UI Responsiveness Test");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(24);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(titleText);
        
        // Click counter
        TextView counterText = new TextView(this);
        counterText.setText("Clicks: 0");
        counterText.setTextColor(0xFF4ECDC4);
        counterText.setTextSize(18);
        counterText.setPadding(0, 0, 0, 20);
        counterText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(counterText);
        
        // Test button 1
        Button testButton1 = new Button(this);
        testButton1.setText("Click Me!");
        testButton1.setBackgroundColor(0xFF6C63FF);
        testButton1.setTextColor(0xFFFFFFFF);
        testButton1.setOnClickListener(v -> {
            clickCount++;
            counterText.setText("Clicks: " + clickCount);
            Toast.makeText(this, "Button 1 clicked! Count: " + clickCount, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Button 1 clicked, count: " + clickCount);
        });
        LinearLayout.LayoutParams buttonParams1 = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams1.setMargins(0, 10, 0, 10);
        testButton1.setLayoutParams(buttonParams1);
        mainLayout.addView(testButton1);
        
        // Test button 2
        Button testButton2 = new Button(this);
        testButton2.setText("Another Button");
        testButton2.setBackgroundColor(0xFF4ECDC4);
        testButton2.setTextColor(0xFF000000);
        testButton2.setOnClickListener(v -> {
            Toast.makeText(this, "Button 2 is working perfectly!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Button 2 clicked");
        });
        LinearLayout.LayoutParams buttonParams2 = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams2.setMargins(0, 10, 0, 10);
        testButton2.setLayoutParams(buttonParams2);
        mainLayout.addView(testButton2);
        
        // Reset button
        Button resetButton = new Button(this);
        resetButton.setText("Reset Counter");
        resetButton.setBackgroundColor(0xFFFF6B6B);
        resetButton.setTextColor(0xFFFFFFFF);
        resetButton.setOnClickListener(v -> {
            clickCount = 0;
            counterText.setText("Clicks: 0");
            Toast.makeText(this, "Counter reset!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Counter reset");
        });
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        resetParams.setMargins(0, 20, 0, 10);
        resetButton.setLayoutParams(resetParams);
        mainLayout.addView(resetButton);
        
        // Status text
        TextView statusText = new TextView(this);
        statusText.setText("✅ All UI elements are responding correctly!\n\nIf you can see this and buttons work,\nyour integration is successful.");
        statusText.setTextColor(0xFF90EE90);
        statusText.setTextSize(14);
        statusText.setPadding(0, 30, 0, 0);
        statusText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(statusText);
        
        setContentView(mainLayout);
        
        Log.d(TAG, "Test UI created successfully");
        Toast.makeText(this, "UI Test Activity loaded - try clicking buttons!", Toast.LENGTH_LONG).show();
    }
}