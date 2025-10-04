package com.cosmic.gatherly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

/**
 * Simple Firebase Authentication Activity
 * Handles login and registration using centralized AuthManager
 */
public class FirebaseAuthActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseAuthActivity";
    
    private AuthManager authManager;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private TextView switchModeText;
    private ProgressBar progressBar;
    private TextView titleText;
    
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_auth);
        
        // Get AuthManager instance from Application class
        authManager = ((MinimalApplication) getApplication()).getAuthManager();
        if (authManager == null) {
            Toast.makeText(this, "Authentication service unavailable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        initializeViews();
        setupClickListeners();
        setupAuthStateObserver();
        
        Log.d(TAG, "FirebaseAuthActivity created successfully with centralized AuthManager");
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // AuthState is now handled by the observer, no need for manual check
        Log.d(TAG, "FirebaseAuthActivity started - auth state managed by observer");
    }
    
    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        switchModeText = findViewById(R.id.switchModeText);
        progressBar = findViewById(R.id.progressBar);
        titleText = findViewById(R.id.titleText);
        
        updateUIForMode();
    }
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            if (isLoginMode) {
                signInUser();
            } else {
                registerUser();
            }
        });
        
        registerButton.setOnClickListener(v -> {
            switchMode();
        });
        
        switchModeText.setOnClickListener(v -> {
            switchMode();
        });
    }
    
    /**
     * Set up AuthState observer for navigation and UI updates
     */
    private void setupAuthStateObserver() {
        authManager.getAuthState().observe(this, authState -> {
            Log.d(TAG, "AuthState changed: " + authState.getStatus());
            
            switch (authState.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;
                    
                case AUTHENTICATED:
                    showLoading(false);
                    String email = authState.getUser() != null ? authState.getUser().getEmail() : "Unknown";
                    Toast.makeText(this, "Welcome, " + email + "!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                    break;
                    
                case UNAUTHENTICATED:
                    showLoading(false);
                    // Stay on auth screen
                    break;
                    
                case ERROR:
                    showLoading(false);
                    String errorMessage = authState.getErrorMessage();
                    Toast.makeText(this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
    
    private void switchMode() {
        isLoginMode = !isLoginMode;
        updateUIForMode();
    }
    
    private void updateUIForMode() {
        if (isLoginMode) {
            titleText.setText("Welcome Back!");
            loginButton.setText("Sign In");
            registerButton.setText("Need an account? Register");
            switchModeText.setText("Don't have an account? Sign up");
        } else {
            titleText.setText("Create Account");
            loginButton.setText("Register");
            registerButton.setText("Have an account? Sign In");
            switchModeText.setText("Already have an account? Sign in");
        }
    }
    
    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (!validateInput(email, password)) {
            return;
        }
        
        Log.d(TAG, "Attempting to sign in user: " + email);
        
        // Use AuthManager for sign in - loading state and navigation handled by observer
        authManager.signIn(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signIn:success");
                            // Success handling is done by AuthState observer
                        } else {
                            Log.w(TAG, "signIn:failure", task.getException());
                            // Error handling is done by AuthState observer
                        }
                    }
                });
    }
    
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (!validateInput(email, password)) {
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Attempting to register user: " + email);
        
        // Use AuthManager for sign up - loading state and navigation handled by observer
        authManager.signUp(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signUp:success");
                            // Success handling is done by AuthState observer
                        } else {
                            Log.w(TAG, "signUp:failure", task.getException());
                            // Error handling is done by AuthState observer
                        }
                    }
                });
    }
    
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
    }
    

    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}