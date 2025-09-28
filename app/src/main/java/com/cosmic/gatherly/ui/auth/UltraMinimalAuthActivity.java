package com.cosmic.gatherly.ui.auth;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Ultra Minimal Auth Activity - No Firebase, just UI test
 * This tests if the basic app structure works
 */
public class UltraMinimalAuthActivity extends Activity {
    
    private static final String TAG = "UltraMinimalAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "UltraMinimalAuthActivity started");
        
        try {
            createUI();
            Log.d(TAG, "Auth UI created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating auth UI", e);
            showError("Error creating UI: " + e.getMessage());
        }
    }
    
    private void createUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 50, 50, 50);
        mainLayout.setBackgroundColor(0xFF1A1A2E); // Dark blue background
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Gatherly - Auth Test");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(24);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(titleText);
        
        // Status
        TextView statusText = new TextView(this);
        statusText.setText("✅ App is working!\nBasic UI loaded successfully.");
        statusText.setTextColor(0xFF4ECDC4);
        statusText.setTextSize(16);
        statusText.setPadding(0, 0, 0, 30);
        statusText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(statusText);
        
        // Email input
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email (test input)");
        emailInput.setTextColor(0xFFFFFFFF);
        emailInput.setHintTextColor(0xFF888888);
        emailInput.setBackgroundColor(0xFF2A2A3E);
        emailInput.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emailParams.setMargins(0, 0, 0, 20);
        emailInput.setLayoutParams(emailParams);
        mainLayout.addView(emailInput);
        
        // Password input
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password (test input)");
        passwordInput.setTextColor(0xFFFFFFFF);
        passwordInput.setHintTextColor(0xFF888888);
        passwordInput.setBackgroundColor(0xFF2A2A3E);
        passwordInput.setPadding(20, 20, 20, 20);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams passwordParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        passwordParams.setMargins(0, 0, 0, 30);
        passwordInput.setLayoutParams(passwordParams);
        mainLayout.addView(passwordInput);
        
        // Test button
        Button testButton = new Button(this);
        testButton.setText("Test Button (No Firebase)");
        testButton.setBackgroundColor(0xFF6C63FF);
        testButton.setTextColor(0xFFFFFFFF);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(UltraMinimalAuthActivity.this, 
                        "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UltraMinimalAuthActivity.this, 
                        "✅ UI Test Successful!\nEmail: " + email, Toast.LENGTH_LONG).show();
                    statusText.setText("✅ UI Test Passed!\nInputs working correctly.\n\nNext: Add Firebase Auth");
                }
            }
        });
        mainLayout.addView(testButton);
        
        // Info text
        TextView infoText = new TextView(this);
        infoText.setText("\nThis is a basic UI test.\nIf you can see this and interact with the button,\nthe app structure is working correctly.");
        infoText.setTextColor(0xFF888888);
        infoText.setTextSize(12);
        infoText.setPadding(0, 20, 0, 0);
        infoText.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(infoText);
        
        setContentView(mainLayout);
    }
    
    private void showError(String message) {
        try {
            LinearLayout errorLayout = new LinearLayout(this);
            errorLayout.setOrientation(LinearLayout.VERTICAL);
            errorLayout.setBackgroundColor(0xFF000000);
            errorLayout.setGravity(android.view.Gravity.CENTER);
            errorLayout.setPadding(50, 50, 50, 50);
            
            TextView errorText = new TextView(this);
            errorText.setText("❌ Error\n\n" + message);
            errorText.setTextColor(0xFFFF6B6B);
            errorText.setTextSize(16);
            errorText.setGravity(android.view.Gravity.CENTER);
            
            errorLayout.addView(errorText);
            setContentView(errorLayout);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message", e);
        }
    }
}