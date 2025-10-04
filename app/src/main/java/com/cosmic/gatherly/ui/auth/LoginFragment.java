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
 * Beautiful Login Fragment using original Material Design with centralized AuthManager
 */
public class LoginFragment extends Fragment {
    
    private static final String TAG = "LoginFragment";
    
    private AuthManager authManager;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar loginProgressBar;
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
        // Use the original beautiful login layout
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);
        errorMessageText = view.findViewById(R.id.errorMessageText);
        retryButton = view.findViewById(R.id.retryButton);
        
        // Setup click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        retryButton.setOnClickListener(v -> {
            hideError();
            attemptLogin();
        });
        
        // Set up AuthState observer
        setupAuthStateObserver();
        
        Log.d(TAG, "Beautiful Login Fragment initialized with centralized AuthManager");
    }
    
    // Methods for compatibility with existing AuthActivity
    public void clearForm() {
        if (emailEditText != null) emailEditText.setText("");
        if (passwordEditText != null) passwordEditText.setText("");
        hideError();
    }
    
    public void onLoginComplete() {
        showLoading(false);
        navigateToMainApp();
    }
    
    public void onLoginError(String errorMessage) {
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
                        Toast.makeText(getContext(), "Welcome back, " + email + "!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                        break;
                        
                    case UNAUTHENTICATED:
                        showLoading(false);
                        // Stay on login screen
                        break;
                        
                    case ERROR:
                        showLoading(false);
                        String errorMessage = authState.getErrorMessage();
                        showError("Sign in failed: " + errorMessage);
                        break;
                }
            });
        }
    }
    
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        // Validation
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
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
        
        // Use AuthManager for sign in - loading state and UI updates handled by observer
        if (authManager != null) {
            authManager.signIn(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sign in successful");
                            // Success handling is done by AuthState observer
                        } else {
                            Log.w(TAG, "Sign in failed", task.getException());
                            // Error handling is done by AuthState observer
                        }
                    });
        } else {
            showError("Authentication service unavailable");
        }
    }
    
    private void showLoading(boolean show) {
        if (show) {
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setText("");
            loginButton.setEnabled(false);
            emailEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
        } else {
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setText("🚀 Launch into Chat");
            loginButton.setEnabled(true);
            emailEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
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