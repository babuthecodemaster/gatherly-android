package com.cosmic.gatherly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Minimal Firebase Auth Activity - No dependencies on existing infrastructure
 * This should work without any crashes
 */
public class MinimalFirebaseAuthActivity extends AppCompatActivity {
    private static final String TAG = "MinimalFirebaseAuth";
    
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView statusText;
    
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "MinimalFirebaseAuthActivity started");
        
        try {
            // Initialize Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized");
            }
            
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            Log.d(TAG, "Firebase Auth initialized");
            
            // Create UI programmatically to avoid layout issues
            createUI();
            
            // Check if user is already signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "User already signed in: " + currentUser.getEmail());
                showSuccess("Already signed in as " + currentUser.getEmail());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showError("Initialization failed: " + e.getMessage());
        }
    }
    
    private void createUI() {
        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(64, 64, 64, 64);
        mainLayout.setBackgroundColor(0xFF0A0A0F); // Dark background
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Firebase Auth Test");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(24);
        titleText.setPadding(0, 0, 0, 32);
        mainLayout.addView(titleText);
        
        // Status text
        statusText = new TextView(this);
        statusText.setText("Enter your credentials");
        statusText.setTextColor(0xFFB8B8CC);
        statusText.setTextSize(16);
        statusText.setPadding(0, 0, 0, 24);
        mainLayout.addView(statusText);
        
        // Email input
        emailEditText = new EditText(this);
        emailEditText.setHint("Email");
        emailEditText.setTextColor(0xFFFFFFFF);
        emailEditText.setHintTextColor(0xFF888888);
        emailEditText.setBackgroundColor(0xFF2A2A3E);
        emailEditText.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emailParams.setMargins(0, 0, 0, 16);
        emailEditText.setLayoutParams(emailParams);
        mainLayout.addView(emailEditText);
        
        // Password input
        passwordEditText = new EditText(this);
        passwordEditText.setHint("Password");
        passwordEditText.setTextColor(0xFFFFFFFF);
        passwordEditText.setHintTextColor(0xFF888888);
        passwordEditText.setBackgroundColor(0xFF2A2A3E);
        passwordEditText.setPadding(16, 16, 16, 16);
        passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams passwordParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        passwordParams.setMargins(0, 0, 0, 24);
        passwordEditText.setLayoutParams(passwordParams);
        mainLayout.addView(passwordEditText);
        
        // Login button
        loginButton = new Button(this);
        loginButton.setText("Sign In");
        loginButton.setBackgroundColor(0xFF6C63FF);
        loginButton.setTextColor(0xFFFFFFFF);
        loginButton.setOnClickListener(v -> handleAuth());
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        loginParams.setMargins(0, 0, 0, 16);
        loginButton.setLayoutParams(loginParams);
        mainLayout.addView(loginButton);
        
        // Register button
        registerButton = new Button(this);
        registerButton.setText("Switch to Register");
        registerButton.setBackgroundColor(0xFF4A4A5E);
        registerButton.setTextColor(0xFFFFFFFF);
        registerButton.setOnClickListener(v -> switchMode());
        LinearLayout.LayoutParams registerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        registerParams.setMargins(0, 0, 0, 24);
        registerButton.setLayoutParams(registerParams);
        mainLayout.addView(registerButton);
        
        // Progress bar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        mainLayout.addView(progressBar);
        
        setContentView(mainLayout);
    }
    
    private void switchMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            loginButton.setText("Sign In");
            registerButton.setText("Switch to Register");
            statusText.setText("Sign in to your account");
        } else {
            loginButton.setText("Register");
            registerButton.setText("Switch to Sign In");
            statusText.setText("Create a new account");
        }
    }
    
    private void handleAuth() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        showLoading(true);
        
        if (isLoginMode) {
            signIn(email, password);
        } else {
            register(email, password);
        }
    }
    
    private void signIn(String email, String password) {
        Log.d(TAG, "Attempting sign in for: " + email);
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sign in successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            showSuccess("Welcome back, " + user.getEmail() + "!");
                        } else {
                            Log.w(TAG, "Sign in failed", task.getException());
                            showError("Sign in failed: " + getErrorMessage(task.getException()));
                        }
                    }
                });
    }
    
    private void register(String email, String password) {
        Log.d(TAG, "Attempting registration for: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            showSuccess("Account created! Welcome, " + user.getEmail() + "!");
                        } else {
                            Log.w(TAG, "Registration failed", task.getException());
                            showError("Registration failed: " + getErrorMessage(task.getException()));
                        }
                    }
                });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
    }
    
    private void showError(String message) {
        statusText.setText("❌ " + message);
        statusText.setTextColor(0xFFFF6B6B);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }
    
    private void showSuccess(String message) {
        statusText.setText("✅ " + message);
        statusText.setTextColor(0xFF4ECDC4);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, message);
    }
    
    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Unknown error";
        
        String message = exception.getMessage();
        if (message == null) return "Unknown error";
        
        if (message.contains("password is invalid")) {
            return "Invalid email or password";
        } else if (message.contains("no user record")) {
            return "No account found with this email";
        } else if (message.contains("email address is already")) {
            return "Account already exists with this email";
        } else if (message.contains("network error")) {
            return "Network error. Check your connection";
        } else {
            return message;
        }
    }
}