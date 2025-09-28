package com.cosmic.gatherly.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cosmic.gatherly.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private MaterialButton retryButton;
    private ProgressBar loginProgressBar;
    private TextView errorMessageText;
    
    private AuthCallback authCallback;
    private boolean isLoading = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AuthCallback) {
            authCallback = (AuthCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AuthCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        retryButton = view.findViewById(R.id.retryButton);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);
        errorMessageText = view.findViewById(R.id.errorMessageText);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        retryButton.setOnClickListener(v -> {
            hideError();
            attemptLogin();
        });
    }

    private void attemptLogin() {
        // Prevent multiple login attempts
        if (isLoading) {
            return;
        }

        // Clear previous errors
        clearErrors();
        hideError();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = validateInputs(email, password);

        if (isValid && authCallback != null) {
            setLoadingState(true);
            authCallback.onLoginRequested(email, password);
        }
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_credentials));
            isValid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        
        if (loading) {
            // Show loading state
            loginButton.setEnabled(false);
            loginButton.setText(getString(R.string.connecting));
            loginButton.setIcon(null);
            loginProgressBar.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
        } else {
            // Reset to normal state
            loginButton.setEnabled(true);
            loginButton.setText(getString(R.string.login_button));
            if (getContext() != null) {
                loginButton.setIcon(androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.ic_rocket));
            }
            loginProgressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (errorMessageText != null && message != null && !message.isEmpty()) {
            errorMessageText.setText(message);
            errorMessageText.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideError() {
        if (errorMessageText != null) {
            errorMessageText.setVisibility(View.GONE);
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }
    }
    
    /**
     * Called when login process is complete (success or failure)
     * Resets the UI state to allow new login attempts
     */
    public void onLoginComplete() {
        try {
            setLoadingState(false);
            android.util.Log.d("LoginFragment", "Login process completed - loading state reset");
        } catch (Exception e) {
            android.util.Log.e("LoginFragment", "Error resetting login state", e);
            // Force reset loading state even if there's an error
            isLoading = false;
            if (loginButton != null) {
                loginButton.setEnabled(true);
                loginButton.setText(getString(R.string.login_button));
            }
            if (loginProgressBar != null) {
                loginProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Called when login fails with an error message
     * Shows error feedback to the user
     */
    public void onLoginError(String errorMessage) {
        try {
            setLoadingState(false);
            showError(errorMessage);
            android.util.Log.d("LoginFragment", "Login error handled - loading state reset and error shown");
        } catch (Exception e) {
            android.util.Log.e("LoginFragment", "Error handling login error", e);
            // Force reset loading state even if there's an error
            isLoading = false;
            if (loginButton != null) {
                loginButton.setEnabled(true);
                loginButton.setText(getString(R.string.login_button));
            }
            if (loginProgressBar != null) {
                loginProgressBar.setVisibility(View.GONE);
            }
            // Try to show error message as toast if UI error handling fails
            if (getContext() != null && errorMessage != null) {
                android.widget.Toast.makeText(getContext(), errorMessage, android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * Clears all form fields and resets the form state
     * Called when switching between login/registration tabs
     */
    public void clearForm() {
        try {
            if (emailEditText != null) {
                emailEditText.setText("");
            }
            if (passwordEditText != null) {
                passwordEditText.setText("");
            }
            
            // Clear any error states
            clearErrors();
            hideError();
            
            // Reset loading state
            setLoadingState(false);
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("LoginFragment", "Error clearing form", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        authCallback = null;
    }
}