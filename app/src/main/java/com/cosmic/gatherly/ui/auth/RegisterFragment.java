package com.cosmic.gatherly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Beautiful Register Fragment using original Material Design with centralized AuthManager
 */
public class RegisterFragment extends Fragment {
    
    private static final String TAG = "RegisterFragment";
    
    private AuthManager authManager;
    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton registerButton;
    private ProgressBar registerProgressBar;
    private TextView errorMessageText;
    private MaterialButton retryButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get AuthManager instance from Application class
        authManager = ((MinimalApplication) requireActivity().getApplication()).getAuthManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the original beautiful register layout
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        registerProgressBar = view.findViewById(R.id.registerProgressBar);
        errorMessageText = view.findViewById(R.id.errorMessageText);
        retryButton = view.findViewById(R.id.retryButton);
        
        // Setup click listeners
        registerButton.setOnClickListener(v -> attemptRegister());
        retryButton.setOnClickListener(v -> {
            hideError();
            attemptRegister();
        });
        
        // Set up AuthState observer
        setupAuthStateObserver();
        
        Log.d(TAG, "Beautiful Register Fragment initialized with centralized AuthManager");
    }
    
    // Methods for compatibility with existing AuthActivity
    public void clearForm() {
        if (usernameEditText != null) usernameEditText.setText("");
        if (emailEditText != null) emailEditText.setText("");
        if (passwordEditText != null) passwordEditText.setText("");
        if (confirmPasswordEditText != null) confirmPasswordEditText.setText("");
        hideError();
    }
    
    public void onRegistrationComplete() {
        showLoading(false);
        navigateToMainApp();
    }
    
    public void onRegistrationError(String errorMessage) {
        showLoading(false);
        showError(errorMessage);
    }
    
    /**
     * Set up AuthState observer for navigation and UI updates
     */
    private void setupAuthStateObserver() {
        if (authManager != null) {
            authManager.getAuthState().observe(this, authState -> {
                Log.d(TAG, "AuthState changed: " + authState.getStatus());
                
                switch (authState.getStatus()) {
                    case LOADING:
                        showLoading(true);
                        hideError();
                        break;
                        
                    case AUTHENTICATED:
                        showLoading(false);
                        String email = authState.getUser() != null ? authState.getUser().getEmail() : "Unknown";
                        String displayName = authState.getUser() != null ? authState.getUser().getDisplayName() : "User";
                        Toast.makeText(getContext(), "Welcome to Gatherly, " + (displayName != null ? displayName : email) + "!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                        break;
                        
                    case UNAUTHENTICATED:
                        showLoading(false);
                        // Stay on register screen
                        break;
                        
                    case ERROR:
                        showLoading(false);
                        String errorMessage = authState.getErrorMessage();
                        showError("Registration failed: " + errorMessage);
                        break;
                }
            });
        }
    }
    
    private void attemptRegister() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        
        // Validation
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            return;
        }
        
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Please confirm your password");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            return;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords don't match");
            return;
        }
        
        // Use AuthManager for sign up - loading state and UI updates handled by observer
        if (authManager != null) {
            authManager.signUp(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful");
                            
                            // Update display name with username after successful registration
                            if (authManager.getCurrentUser() != null) {
                                authManager.getCurrentUser().updateProfile(
                                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                );
                            }
                            // Success handling is done by AuthState observer
                        } else {
                            Log.w(TAG, "Registration failed", task.getException());
                            // Error handling is done by AuthState observer
                        }
                    });
        } else {
            showError("Authentication service unavailable");
        }
    }
    
    private void showLoading(boolean show) {
        if (show) {
            registerProgressBar.setVisibility(View.VISIBLE);
            registerButton.setText("");
            registerButton.setEnabled(false);
            usernameEditText.setEnabled(false);
            emailEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            confirmPasswordEditText.setEnabled(false);
        } else {
            registerProgressBar.setVisibility(View.GONE);
            registerButton.setText("⭐ Join the Galaxy");
            registerButton.setEnabled(true);
            usernameEditText.setEnabled(true);
            emailEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
            confirmPasswordEditText.setEnabled(true);
        }
    }
    
    private void showError(String message) {
        errorMessageText.setText(message);
        errorMessageText.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
        errorMessageText.setBackgroundColor(0x22FF6B6B); // Light red background
        errorMessageText.setTextColor(0xFFFF6B6B); // Red text
    }
    
    private void hideError() {
        errorMessageText.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }
    

    
    private void navigateToMainApp() {
        try {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("source", "firebase_auth");
            intent.putExtra("auth_type", "firebase");
            startActivity(intent);
            requireActivity().finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main app", e);
            Toast.makeText(getContext(), "Error loading main app", Toast.LENGTH_SHORT).show();
        }
    }
}