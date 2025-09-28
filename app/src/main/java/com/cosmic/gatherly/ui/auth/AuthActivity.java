package com.cosmic.gatherly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.ui.main.MainActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity implements AuthCallback {
    
    private static final long LOADING_TIMEOUT_MS = 90000; // 90 seconds timeout for loading animations
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AuthRepository authRepository;
    private AuthPagerAdapter pagerAdapter;
    private Handler timeoutHandler;
    private Runnable loginTimeoutRunnable;
    private Runnable registerTimeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initializeViews();
        setupAuthRepository();
        setupViewPager();
        setupTimeoutHandler();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Clear all forms when activity resumes to ensure no pre-filled values
        clearAllFormsOnStart();
    }
    
    private void clearAllFormsOnStart() {
        // Use a small delay to ensure fragments are fully initialized
        viewPager.post(() -> clearAllForms());
    }

    private void initializeViews() {
        try {
            tabLayout = findViewById(R.id.tabLayout);
            viewPager = findViewById(R.id.viewPager);
            
            if (tabLayout == null) {
                throw new RuntimeException("TabLayout not found in activity_auth layout");
            }
            if (viewPager == null) {
                throw new RuntimeException("ViewPager2 not found in activity_auth layout");
            }
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error initializing views", e);
            finish(); // Close activity if critical views are missing
        }
    }

    private void setupAuthRepository() {
        try {
            // Ensure Firebase is initialized before setting up auth repository
            boolean firebaseHealthy = com.cosmic.gatherly.data.util.FirebaseUtils.isFirebaseHealthy();
            if (!firebaseHealthy) {
                android.util.Log.w("AuthActivity", "Firebase is not healthy, attempting re-initialization");
                com.cosmic.gatherly.data.util.FirebaseUtils.initializeFirebase(this);
            }
            
            authRepository = new AuthRepository(this);
            
            // Perform health check on auth repository
            boolean authHealthy = authRepository.performHealthCheck();
            if (!authHealthy) {
                android.util.Log.w("AuthActivity", "AuthRepository health check failed");
                Toast.makeText(this, "Authentication service may not be fully available", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error setting up AuthRepository", e);
            Toast.makeText(this, "Error initializing authentication service", Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupTimeoutHandler() {
        timeoutHandler = new Handler(Looper.getMainLooper());
    }

    private void setupViewPager() {
        try {
            if (viewPager == null || tabLayout == null) {
                android.util.Log.e("AuthActivity", "Cannot setup ViewPager - views are null");
                return;
            }
            
            pagerAdapter = new AuthPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                try {
                    switch (position) {
                        case 0:
                            tab.setText(getString(R.string.login));
                            break;
                        case 1:
                            tab.setText(getString(R.string.register));
                            break;
                    }
                } catch (Exception e) {
                    android.util.Log.e("AuthActivity", "Error setting tab text", e);
                }
            }).attach();
            
            // Add tab selection listener to clear forms when switching
            setupTabSelectionListener();
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error setting up ViewPager", e);
        }
    }
    
    private void setupTabSelectionListener() {
        try {
            if (tabLayout != null) {
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // Clear forms when switching tabs
                        clearAllForms();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        // No action needed
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // No action needed
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error setting up tab selection listener", e);
        }
    }
    
    private void clearAllForms() {
        try {
            // Clear login form
            Fragment loginFragment = getSupportFragmentManager().findFragmentByTag("f0");
            if (loginFragment instanceof LoginFragment) {
                ((LoginFragment) loginFragment).clearForm();
            }
            
            // Clear register form
            Fragment registerFragment = getSupportFragmentManager().findFragmentByTag("f1");
            if (registerFragment instanceof RegisterFragment) {
                ((RegisterFragment) registerFragment).clearForm();
            }
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error clearing forms", e);
        }
    }

    @Override
    public void onLoginRequested(String email, String password) {
        // Perform pre-authentication checks
        if (!performPreAuthenticationChecks("login")) {
            return;
        }
        
        // Start timeout handler for login
        startLoginTimeout();
        
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    // Cancel timeout handler since we got a response
                    cancelLoginTimeout();
                    
                    // ALWAYS stop loading animation first, regardless of navigation outcome
                    resetLoginFragmentState();
                    
                    Toast.makeText(AuthActivity.this, getString(R.string.success_login), Toast.LENGTH_SHORT).show();
                    
                    // Simple navigation - just go to MainActivity
                    android.util.Log.d("AuthActivity", "Login successful, navigating to MainActivity");
                    android.util.Log.d("AuthActivity", String.format("User: %s (%s)", user.getUsername(), user.getEmail()));
                    
                    try {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("source", "auth_activity");
                        intent.putExtra("user_id", user.getId());
                        intent.putExtra("user_email", user.getEmail());
                        
                        startActivity(intent);
                        finish();
                        
                        android.util.Log.d("AuthActivity", "✅ Successfully navigated to MainActivity");
                    } catch (Exception e) {
                        android.util.Log.e("AuthActivity", "❌ Navigation failed", e);
                        resetLoginFragmentStateWithError("Navigation failed. Please try again.");
                        Toast.makeText(AuthActivity.this, "Navigation failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(AuthError error) {
                runOnUiThread(() -> {
                    // Cancel timeout handler since we got a response
                    cancelLoginTimeout();
                    
                    android.util.Log.e("AuthActivity", "Login failed: " + error.toString());
                    String userMessage = error.getUserFriendlyMessage() != null ? 
                        error.getUserFriendlyMessage() : "Login failed. Please try again.";
                    
                    // Determine if this is a timeout error and customize message
                    if (isTimeoutError(error)) {
                        userMessage = "Request timed out. Please check your connection and try again.";
                        android.util.Log.w("AuthActivity", "Login request timed out");
                    }
                    
                    // Check if this is a server connectivity issue
                    if (isServerConnectivityError(error)) {
                        userMessage += "\n\nTap here to check server status.";
                        showServerConnectivityError(userMessage);
                    } else {
                        // ALWAYS stop loading animation and show error
                        resetLoginFragmentStateWithError(userMessage);
                        Toast.makeText(AuthActivity.this, userMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRegisterRequested(String username, String email, String password) {
        // Perform pre-authentication checks
        if (!performPreAuthenticationChecks("registration")) {
            return;
        }
        
        // Start timeout handler for registration
        startRegisterTimeout();
        
        authRepository.register(username, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    // Cancel timeout handler since we got a response
                    cancelRegisterTimeout();
                    
                    // ALWAYS stop loading animation first, regardless of navigation outcome
                    resetRegisterFragmentState();
                    
                    Toast.makeText(AuthActivity.this, getString(R.string.success_register), Toast.LENGTH_SHORT).show();
                    
                    // Simple navigation - just go to MainActivity
                    android.util.Log.d("AuthActivity", "Registration successful, navigating to MainActivity");
                    android.util.Log.d("AuthActivity", String.format("User: %s (%s)", user.getUsername(), user.getEmail()));
                    
                    try {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("source", "auth_activity");
                        intent.putExtra("user_id", user.getId());
                        intent.putExtra("user_email", user.getEmail());
                        
                        startActivity(intent);
                        finish();
                        
                        android.util.Log.d("AuthActivity", "✅ Successfully navigated to MainActivity");
                    } catch (Exception e) {
                        android.util.Log.e("AuthActivity", "❌ Navigation failed", e);
                        resetRegisterFragmentStateWithError("Navigation failed. Please try again.");
                        Toast.makeText(AuthActivity.this, "Navigation failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(AuthError error) {
                runOnUiThread(() -> {
                    // Cancel timeout handler since we got a response
                    cancelRegisterTimeout();
                    
                    android.util.Log.e("AuthActivity", "Registration failed: " + error.toString());
                    String userMessage = error.getUserFriendlyMessage() != null ? 
                        error.getUserFriendlyMessage() : "Registration failed. Please try again.";
                    
                    // Determine if this is a timeout error and customize message
                    if (isTimeoutError(error)) {
                        userMessage = "Request timed out. Please check your connection and try again.";
                        android.util.Log.w("AuthActivity", "Registration request timed out");
                    }
                    
                    // Check if this is a server connectivity issue
                    if (isServerConnectivityError(error)) {
                        userMessage += "\n\nTap here to check server status.";
                        showServerConnectivityError(userMessage);
                    } else {
                        // ALWAYS stop loading animation and show error
                        resetRegisterFragmentStateWithError(userMessage);
                        Toast.makeText(AuthActivity.this, userMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void resetRegisterFragmentState() {
        try {
            if (pagerAdapter != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f1"); // ViewPager2 uses "f" + position as tag
                if (fragment instanceof RegisterFragment) {
                    ((RegisterFragment) fragment).onRegistrationComplete();
                    android.util.Log.d("AuthActivity", "Register fragment state reset successfully");
                } else {
                    android.util.Log.w("AuthActivity", "Register fragment not found or wrong type");
                }
            } else {
                android.util.Log.w("AuthActivity", "PagerAdapter is null, cannot reset register fragment state");
            }
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("AuthActivity", "Error resetting register fragment state", e);
        }
    }

    private void resetRegisterFragmentStateWithError(String errorMessage) {
        try {
            if (pagerAdapter != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f1"); // ViewPager2 uses "f" + position as tag
                if (fragment instanceof RegisterFragment) {
                    ((RegisterFragment) fragment).onRegistrationError(errorMessage);
                    android.util.Log.d("AuthActivity", "Register fragment error state set successfully");
                } else {
                    android.util.Log.w("AuthActivity", "Register fragment not found for error state");
                }
            } else {
                android.util.Log.w("AuthActivity", "PagerAdapter is null, cannot set register fragment error state");
            }
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("AuthActivity", "Error resetting register fragment state with error", e);
            // Fallback to basic reset to ensure loading animation stops
            resetRegisterFragmentState();
        }
    }
    
    private void resetLoginFragmentState() {
        try {
            if (pagerAdapter != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f0"); // ViewPager2 uses "f" + position as tag
                if (fragment instanceof LoginFragment) {
                    ((LoginFragment) fragment).onLoginComplete();
                    android.util.Log.d("AuthActivity", "Login fragment state reset successfully");
                } else {
                    android.util.Log.w("AuthActivity", "Login fragment not found or wrong type");
                }
            } else {
                android.util.Log.w("AuthActivity", "PagerAdapter is null, cannot reset login fragment state");
            }
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("AuthActivity", "Error resetting login fragment state", e);
        }
    }

    private void resetLoginFragmentStateWithError(String errorMessage) {
        try {
            if (pagerAdapter != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f0"); // ViewPager2 uses "f" + position as tag
                if (fragment instanceof LoginFragment) {
                    ((LoginFragment) fragment).onLoginError(errorMessage);
                    android.util.Log.d("AuthActivity", "Login fragment error state set successfully");
                } else {
                    android.util.Log.w("AuthActivity", "Login fragment not found for error state");
                }
            } else {
                android.util.Log.w("AuthActivity", "PagerAdapter is null, cannot set login fragment error state");
            }
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("AuthActivity", "Error resetting login fragment state with error", e);
            // Fallback to basic reset to ensure loading animation stops
            resetLoginFragmentState();
        }
    }


    
    /**
     * Determines if the given error is a timeout error
     * @param error The authentication error to check
     * @return true if this is a timeout error, false otherwise
     */
    private boolean isTimeoutError(AuthError error) {
        if (error == null) {
            return false;
        }
        
        // Check if error type is explicitly TIMEOUT_ERROR
        if (error.getType() == AuthError.Type.TIMEOUT_ERROR) {
            return true;
        }
        
        // Check error type for network errors that might be timeouts
        if (error.getType() == AuthError.Type.NETWORK_ERROR) {
            // Check error message for timeout indicators
            String message = error.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                if (lowerMessage.contains("timeout") || 
                    lowerMessage.contains("timed out") ||
                    lowerMessage.contains("connection timeout") ||
                    lowerMessage.contains("read timeout") ||
                    lowerMessage.contains("connect timeout")) {
                    return true;
                }
            }
        }
        
        // Check if the cause is a timeout exception
        Throwable cause = error.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                String lowerCauseMessage = causeMessage.toLowerCase();
                if (lowerCauseMessage.contains("timeout") ||
                    lowerCauseMessage.contains("timed out")) {
                    return true;
                }
            }
            
            // Check for specific timeout exception types
            if (cause instanceof java.net.SocketTimeoutException ||
                cause instanceof java.util.concurrent.TimeoutException ||
                (cause.getClass().getSimpleName().toLowerCase().contains("timeout"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if the given error is a server connectivity error
     * @param error The authentication error to check
     * @return true if this is a server connectivity error, false otherwise
     */
    private boolean isServerConnectivityError(AuthError error) {
        if (error == null) {
            return false;
        }
        
        // Check if error type is network error
        if (error.getType() == AuthError.Type.NETWORK_ERROR) {
            String message = error.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                // Check for server connectivity indicators
                if (lowerMessage.contains("server unreachable") ||
                    lowerMessage.contains("unable to connect to server") ||
                    lowerMessage.contains("connection refused") ||
                    lowerMessage.contains("failed to connect") ||
                    lowerMessage.contains("server unavailable") ||
                    lowerMessage.contains("server is not reachable")) {
                    return true;
                }
            }
        }
        
        // Check the cause for connectivity issues
        Throwable cause = error.getCause();
        if (cause != null) {
            if (cause instanceof java.net.ConnectException ||
                cause instanceof java.net.UnknownHostException) {
                return true;
            }
            
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                String lowerCauseMessage = causeMessage.toLowerCase();
                if (lowerCauseMessage.contains("connection refused") ||
                    lowerCauseMessage.contains("failed to connect") ||
                    lowerCauseMessage.contains("unable to resolve host")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Show server connectivity error with option to check server status
     */
    private void showServerConnectivityError(String message) {
        try {
            // Reset fragment states first
            resetLoginFragmentStateWithError(message);
            resetRegisterFragmentStateWithError(message);
            
            // Show toast with server status option
            Toast toast = Toast.makeText(this, message + "\n\nLong press to check server status", Toast.LENGTH_LONG);
            toast.show();
            
            // Also show a dialog with server status option
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Server Connection Issue")
                .setMessage(message + "\n\nWould you like to check the server status?")
                .setPositiveButton("Check Server Status", (dialog, which) -> {
                    showServerStatusDialog();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNeutralButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    // User can manually retry by clicking login/register again
                })
                .show();
                
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error showing server connectivity error", e);
            // Fallback to simple toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Show server status dialog
     */
    private void showServerStatusDialog() {
        try {
            if (authRepository != null) {
                ServerStatusDialog dialog = new ServerStatusDialog(this, authRepository);
                dialog.show();
            } else {
                Toast.makeText(this, "Cannot check server status - authentication service not available", 
                    Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", "Error showing server status dialog", e);
            Toast.makeText(this, "Error opening server status dialog", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Perform comprehensive pre-authentication checks
     */
    private boolean performPreAuthenticationChecks(String operation) {
        try {
            android.util.Log.d("AuthActivity", String.format("Performing pre-authentication checks for %s", operation));
            
            // Check network connectivity
            if (!com.cosmic.gatherly.data.util.NetworkUtils.isNetworkAvailable(this)) {
                String networkType = com.cosmic.gatherly.data.util.NetworkUtils.getNetworkType(this);
                String errorMessage = "No internet connection detected";
                if (!"No Connection".equals(networkType) && !"Unknown".equals(networkType)) {
                    errorMessage += " via " + networkType.toLowerCase();
                }
                errorMessage += ". Please check your network settings and try again.";
                
                android.util.Log.w("AuthActivity", String.format("Pre-auth check failed: No network connectivity (%s)", networkType));
                
                if ("login".equals(operation)) {
                    resetLoginFragmentStateWithError(errorMessage);
                } else {
                    resetRegisterFragmentStateWithError(errorMessage);
                }
                
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                return false;
            }
            
            // Check Firebase health
            boolean firebaseHealthy = com.cosmic.gatherly.data.util.FirebaseUtils.isFirebaseHealthy();
            if (!firebaseHealthy) {
                android.util.Log.w("AuthActivity", "Pre-auth check: Firebase is not healthy, attempting recovery");
                
                // Attempt Firebase re-initialization
                boolean firebaseRecovered = com.cosmic.gatherly.data.util.FirebaseUtils.initializeFirebase(this);
                if (!firebaseRecovered) {
                    android.util.Log.e("AuthActivity", "Pre-auth check failed: Firebase recovery failed");
                    
                    String errorMessage = "Authentication service is not available. Please restart the app and try again.";
                    if ("login".equals(operation)) {
                        resetLoginFragmentStateWithError(errorMessage);
                    } else {
                        resetRegisterFragmentStateWithError(errorMessage);
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    android.util.Log.i("AuthActivity", "Firebase recovery successful");
                }
            }
            
            // Check AuthRepository health
            if (authRepository != null) {
                boolean authHealthy = authRepository.performHealthCheck();
                if (!authHealthy) {
                    android.util.Log.w("AuthActivity", "Pre-auth check: AuthRepository is not healthy");
                    
                    String errorMessage = "Authentication service is experiencing issues. Please try again.";
                    if ("login".equals(operation)) {
                        resetLoginFragmentStateWithError(errorMessage);
                    } else {
                        resetRegisterFragmentStateWithError(errorMessage);
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                android.util.Log.e("AuthActivity", "Pre-auth check failed: AuthRepository is null");
                
                String errorMessage = "Authentication service is not initialized. Please restart the app.";
                if ("login".equals(operation)) {
                    resetLoginFragmentStateWithError(errorMessage);
                } else {
                    resetRegisterFragmentStateWithError(errorMessage);
                }
                
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                return false;
            }
            
            // Log network diagnostics for debugging
            String networkDiagnostics = com.cosmic.gatherly.data.util.NetworkUtils.performNetworkDiagnostics(this);
            android.util.Log.d("AuthActivity", String.format("Pre-auth network diagnostics:\n%s", networkDiagnostics));
            
            android.util.Log.d("AuthActivity", String.format("✅ All pre-authentication checks passed for %s", operation));
            return true;
            
        } catch (Exception e) {
            android.util.Log.e("AuthActivity", String.format("Error during pre-authentication checks for %s", operation), e);
            
            String errorMessage = "System check failed. Please try again.";
            if ("login".equals(operation)) {
                resetLoginFragmentStateWithError(errorMessage);
            } else {
                resetRegisterFragmentStateWithError(errorMessage);
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            return false;
        }
    }
    
    /**
     * Starts a timeout handler for login operations
     */
    private void startLoginTimeout() {
        cancelLoginTimeout(); // Cancel any existing timeout
        
        loginTimeoutRunnable = () -> {
            android.util.Log.w("AuthActivity", "Login operation timed out - forcing UI reset");
            resetLoginFragmentStateWithError("Request timed out. Please check your connection and try again.");
            Toast.makeText(AuthActivity.this, "Login timed out. Please try again.", Toast.LENGTH_LONG).show();
        };
        
        timeoutHandler.postDelayed(loginTimeoutRunnable, LOADING_TIMEOUT_MS);
        android.util.Log.d("AuthActivity", "Login timeout handler started (" + LOADING_TIMEOUT_MS + "ms)");
    }
    
    /**
     * Cancels the login timeout handler
     */
    private void cancelLoginTimeout() {
        if (timeoutHandler != null && loginTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(loginTimeoutRunnable);
            loginTimeoutRunnable = null;
            android.util.Log.d("AuthActivity", "Login timeout handler cancelled");
        }
    }
    
    /**
     * Starts a timeout handler for registration operations
     */
    private void startRegisterTimeout() {
        cancelRegisterTimeout(); // Cancel any existing timeout
        
        registerTimeoutRunnable = () -> {
            android.util.Log.w("AuthActivity", "Registration operation timed out - forcing UI reset");
            resetRegisterFragmentStateWithError("Request timed out. Please check your connection and try again.");
            Toast.makeText(AuthActivity.this, "Registration timed out. Please try again.", Toast.LENGTH_LONG).show();
        };
        
        timeoutHandler.postDelayed(registerTimeoutRunnable, LOADING_TIMEOUT_MS);
        android.util.Log.d("AuthActivity", "Registration timeout handler started (" + LOADING_TIMEOUT_MS + "ms)");
    }
    
    /**
     * Cancels the registration timeout handler
     */
    private void cancelRegisterTimeout() {
        if (timeoutHandler != null && registerTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(registerTimeoutRunnable);
            registerTimeoutRunnable = null;
            android.util.Log.d("AuthActivity", "Registration timeout handler cancelled");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timeout handlers
        cancelLoginTimeout();
        cancelRegisterTimeout();
    }
    


    private static class AuthPagerAdapter extends FragmentStateAdapter {

        public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new LoginFragment();
                case 1:
                    return new RegisterFragment();
                default:
                    return new LoginFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}