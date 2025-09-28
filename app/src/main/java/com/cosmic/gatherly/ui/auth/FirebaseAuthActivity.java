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

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.util.FirebaseUtils;
import com.cosmic.gatherly.ui.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Simple Firebase Authentication Activity
 * Handles login and registration using Firebase Auth
 */
public class FirebaseAuthActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseAuthActivity";
    
    private FirebaseAuth mAuth;
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
        
        // Initialize Firebase
        if (!FirebaseUtils.initializeFirebase(this)) {
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        initializeViews();
        setupClickListeners();
        
        Log.d(TAG, "FirebaseAuthActivity created successfully");
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            navigateToMainActivity();
        }
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
        
        showLoading(true);
        Log.d(TAG, "Attempting to sign in user: " + email);
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(FirebaseAuthActivity.this, 
                                "Welcome back, " + user.getEmail() + "!", 
                                Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            // Sign in failed
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = getErrorMessage(task.getException());
                            Toast.makeText(FirebaseAuthActivity.this, 
                                "Authentication failed: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
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
        
        showLoading(true);
        Log.d(TAG, "Attempting to register user: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            // Registration success
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(FirebaseAuthActivity.this, 
                                "Account created successfully! Welcome, " + user.getEmail() + "!", 
                                Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            // Registration failed
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = getErrorMessage(task.getException());
                            Toast.makeText(FirebaseAuthActivity.this, 
                                "Registration failed: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
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
    
    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String message = exception.getMessage();
        if (message == null) {
            return "Unknown error occurred";
        }
        
        // Simplify Firebase error messages for users
        if (message.contains("password is invalid")) {
            return "Invalid email or password";
        } else if (message.contains("no user record")) {
            return "No account found with this email";
        } else if (message.contains("email address is already")) {
            return "An account with this email already exists";
        } else if (message.contains("network error")) {
            return "Network error. Please check your connection";
        } else if (message.contains("too many requests")) {
            return "Too many attempts. Please try again later";
        } else {
            return message;
        }
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}