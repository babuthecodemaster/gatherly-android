package com.cosmic.gatherly.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.cosmic.gatherly.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {
    
    private static final String TAG = "RegisterFragment";
    
    private TextInputLayout usernameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton registerButton;
    private MaterialButton retryButton;
    private ProgressBar registerProgressBar;
    private TextView errorMessageText;
    
    private AuthCallback authCallback;
    private boolean isRegistering = false;

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
        try {
            View view = inflater.inflate(R.layout.fragment_register, container, false);
            
            if (view == null) {
                Log.e(TAG, "Failed to inflate fragment_register layout");
                showError("Failed to load registration form");
                return null;
            }
            
            if (!initializeViews(view)) {
                Log.e(TAG, "Failed to initialize views");
                showError("Registration form is not available");
                return null;
            }
            
            setupClickListeners();
            
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error creating RegisterFragment view", e);
            showError("Failed to load registration form");
            return null;
        }
    }

    private boolean initializeViews(View view) {
        try {
            usernameInputLayout = view.findViewById(R.id.usernameInputLayout);
            emailInputLayout = view.findViewById(R.id.emailInputLayout);
            passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
            confirmPasswordInputLayout = view.findViewById(R.id.confirmPasswordInputLayout);
            usernameEditText = view.findViewById(R.id.usernameEditText);
            emailEditText = view.findViewById(R.id.emailEditText);
            passwordEditText = view.findViewById(R.id.passwordEditText);
            confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
            registerButton = view.findViewById(R.id.registerButton);
            
            // Validate that all required views are found
            if (usernameInputLayout == null) {
                Log.e(TAG, "usernameInputLayout not found in layout");
                return false;
            }
            if (emailInputLayout == null) {
                Log.e(TAG, "emailInputLayout not found in layout");
                return false;
            }
            if (passwordInputLayout == null) {
                Log.e(TAG, "passwordInputLayout not found in layout");
                return false;
            }
            if (confirmPasswordInputLayout == null) {
                Log.e(TAG, "confirmPasswordInputLayout not found in layout");
                return false;
            }
            if (usernameEditText == null) {
                Log.e(TAG, "usernameEditText not found in layout");
                return false;
            }
            if (emailEditText == null) {
                Log.e(TAG, "emailEditText not found in layout");
                return false;
            }
            if (passwordEditText == null) {
                Log.e(TAG, "passwordEditText not found in layout");
                return false;
            }
            if (confirmPasswordEditText == null) {
                Log.e(TAG, "confirmPasswordEditText not found in layout");
                return false;
            }
            if (registerButton == null) {
                Log.e(TAG, "registerButton not found in layout");
                return false;
            }
            
            // Initialize new UI components
            retryButton = view.findViewById(R.id.retryButton);
            registerProgressBar = view.findViewById(R.id.registerProgressBar);
            errorMessageText = view.findViewById(R.id.errorMessageText);
            
            Log.d(TAG, "All views initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception during view initialization", e);
            return false;
        }
    }

    private void setupClickListeners() {
        try {
            if (registerButton != null) {
                registerButton.setOnClickListener(v -> {
                    try {
                        attemptRegister();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during registration attempt", e);
                        showErrorMessage("Registration failed. Please try again.");
                        resetButtonState();
                    }
                });
                
                if (retryButton != null) {
                    retryButton.setOnClickListener(v -> {
                        hideErrorMessage();
                        attemptRegister();
                    });
                }
                
                // Add real-time validation for confirm password field
                if (confirmPasswordEditText != null) {
                    confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            validatePasswordMatch();
                        }
                    });
                }
                
                Log.d(TAG, "Click listeners set up successfully");
            } else {
                Log.e(TAG, "Cannot set up click listeners - registerButton is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception setting up click listeners", e);
        }
    }

    private void attemptRegister() {
        try {
            // Prevent multiple registration attempts
            if (isRegistering) {
                Log.d(TAG, "Registration already in progress");
                return;
            }
            
            // Validate views are still available
            if (!validateViewsAvailable()) {
                showError("Registration form is not available");
                return;
            }
            
            // Clear previous errors
            clearErrors();

            String username = getTextSafely(usernameEditText);
            String email = getTextSafely(emailEditText);
            String password = getTextSafely(passwordEditText);

            boolean isValid = validateInputs(username, email, password);

            if (isValid && authCallback != null) {
                setRegistering(true);
                Log.d(TAG, "Starting registration process");
                authCallback.onRegisterRequested(username, email, password);
            } else if (authCallback == null) {
                Log.e(TAG, "AuthCallback is null - cannot proceed with registration");
                showError("Registration service is not available");
                resetButtonState();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in attemptRegister", e);
            showError("Registration failed. Please try again.");
            resetButtonState();
        }
    }
    
    private boolean validateViewsAvailable() {
        return usernameInputLayout != null && emailInputLayout != null && 
               passwordInputLayout != null && confirmPasswordInputLayout != null &&
               usernameEditText != null && emailEditText != null && 
               passwordEditText != null && confirmPasswordEditText != null &&
               registerButton != null;
    }
    
    private void clearErrors() {
        try {
            if (usernameInputLayout != null) {
                usernameInputLayout.setError(null);
            }
            if (emailInputLayout != null) {
                emailInputLayout.setError(null);
            }
            if (passwordInputLayout != null) {
                passwordInputLayout.setError(null);
            }
            if (confirmPasswordInputLayout != null) {
                confirmPasswordInputLayout.setError(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing previous errors", e);
        }
    }
    
    private String getTextSafely(TextInputEditText editText) {
        try {
            if (editText != null && editText.getText() != null) {
                return editText.getText().toString().trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting text from EditText", e);
        }
        return "";
    }
    
    private boolean validateInputs(String username, String email, String password) {
        boolean isValid = true;

        try {
            if (TextUtils.isEmpty(username) || username.length() < 3) {
                setErrorSafely(usernameInputLayout, "Username must be at least 3 characters");
                isValid = false;
            }

            if (TextUtils.isEmpty(email)) {
                String errorMsg = getString(R.string.error_invalid_credentials);
                setErrorSafely(emailInputLayout, errorMsg);
                isValid = false;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                setErrorSafely(passwordInputLayout, "Password must be at least 6 characters");
                isValid = false;
            }

            // Validate password confirmation
            String confirmPassword = getTextSafely(confirmPasswordEditText);
            if (TextUtils.isEmpty(confirmPassword)) {
                setErrorSafely(confirmPasswordInputLayout, "Please confirm your password");
                isValid = false;
            } else if (!password.equals(confirmPassword)) {
                setErrorSafely(confirmPasswordInputLayout, "Passwords do not match");
                isValid = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during input validation", e);
            showError("Validation failed. Please check your inputs.");
            isValid = false;
        }

        return isValid;
    }
    
    private void validatePasswordMatch() {
        try {
            String password = getTextSafely(passwordEditText);
            String confirmPassword = getTextSafely(confirmPasswordEditText);
            
            if (!TextUtils.isEmpty(confirmPassword)) {
                if (!password.equals(confirmPassword)) {
                    setErrorSafely(confirmPasswordInputLayout, "Passwords do not match");
                } else {
                    setErrorSafely(confirmPasswordInputLayout, null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating password match", e);
        }
    }
    
    private void setErrorSafely(TextInputLayout inputLayout, String error) {
        try {
            if (inputLayout != null) {
                inputLayout.setError(error);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting error message", e);
        }
    }
    
    private void setRegistering(boolean registering) {
        isRegistering = registering;
        try {
            if (registerButton != null) {
                registerButton.setEnabled(!registering);
                if (registering) {
                    registerButton.setText(getString(R.string.creating_identity));
                    registerButton.setIcon(null);
                    if (registerProgressBar != null) {
                        registerProgressBar.setVisibility(View.VISIBLE);
                    }
                    if (retryButton != null) {
                        retryButton.setVisibility(View.GONE);
                    }
                } else {
                    registerButton.setText(getString(R.string.register_button));
                    if (getContext() != null) {
                        registerButton.setIcon(androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.ic_star));
                    }
                    if (registerProgressBar != null) {
                        registerProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating button state", e);
        }
    }
    
    private void resetButtonState() {
        setRegistering(false);
    }
    
    private void showError(String message) {
        try {
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message", e);
        }
    }

    private void showErrorMessage(String message) {
        try {
            if (errorMessageText != null && message != null && !message.isEmpty()) {
                errorMessageText.setText(message);
                errorMessageText.setVisibility(View.VISIBLE);
                if (retryButton != null) {
                    retryButton.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message in UI", e);
        }
    }

    private void hideErrorMessage() {
        try {
            if (errorMessageText != null) {
                errorMessageText.setVisibility(View.GONE);
            }
            if (retryButton != null) {
                retryButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding error message", e);
        }
    }
    
    // Public method to reset state when registration completes
    public void onRegistrationComplete() {
        try {
            resetButtonState();
            hideErrorMessage();
            Log.d(TAG, "Registration process completed - loading state reset");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting registration state", e);
            // Force reset loading state even if there's an error
            isRegistering = false;
            if (registerButton != null) {
                registerButton.setEnabled(true);
                registerButton.setText(getString(R.string.register_button));
            }
            if (registerProgressBar != null) {
                registerProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Called when registration fails with an error message
     * Shows error feedback to the user
     */
    public void onRegistrationError(String errorMessage) {
        try {
            resetButtonState();
            showErrorMessage(errorMessage);
            Log.d(TAG, "Registration error handled - loading state reset and error shown");
        } catch (Exception e) {
            Log.e(TAG, "Error handling registration error", e);
            // Force reset loading state even if there's an error
            isRegistering = false;
            if (registerButton != null) {
                registerButton.setEnabled(true);
                registerButton.setText(getString(R.string.register_button));
            }
            if (registerProgressBar != null) {
                registerProgressBar.setVisibility(View.GONE);
            }
            // Try to show error message as toast if UI error handling fails
            if (getContext() != null && errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * Clears all form fields and resets the form state
     * Called when switching between login/registration tabs
     */
    public void clearForm() {
        try {
            if (usernameEditText != null) {
                usernameEditText.setText("");
            }
            if (emailEditText != null) {
                emailEditText.setText("");
            }
            if (passwordEditText != null) {
                passwordEditText.setText("");
            }
            if (confirmPasswordEditText != null) {
                confirmPasswordEditText.setText("");
            }
            
            // Clear any error states
            clearErrors();
            hideErrorMessage();
            
            // Reset loading state
            resetButtonState();
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("RegisterFragment", "Error clearing form", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        authCallback = null;
    }
}