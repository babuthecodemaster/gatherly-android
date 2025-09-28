package com.cosmic.gatherly.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.ui.auth.AuthActivity;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {
    
    private AuthRepository authRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.util.Log.d("MainActivity", "🚀 MainActivity onCreate() started");
        
        try {
            setContentView(R.layout.activity_main);
            android.util.Log.d("MainActivity", "✅ Layout set successfully");

            // Log navigation source for debugging
            logNavigationSource();
            
            setupAuthRepository();
            loadCurrentUser();
            setupInitialFragment();
            
            android.util.Log.d("MainActivity", "✅ MainActivity initialization completed successfully");
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "❌ Error during MainActivity initialization", e);
            // Show error to user
            Toast.makeText(this, "Error loading main screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void logNavigationSource() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String source = intent.getStringExtra("source");
                String userId = intent.getStringExtra("user_id");
                String userEmail = intent.getStringExtra("user_email");
                
                android.util.Log.d("MainActivity", "📍 Navigation Details:");
                android.util.Log.d("MainActivity", "  Source: " + (source != null ? source : "unknown"));
                android.util.Log.d("MainActivity", "  User ID: " + (userId != null ? userId : "not provided"));
                android.util.Log.d("MainActivity", "  User Email: " + (userEmail != null ? userEmail : "not provided"));
                
                if ("auth_activity".equals(source)) {
                    android.util.Log.d("MainActivity", "✅ Successfully navigated from AuthActivity");
                    Toast.makeText(this, "Welcome! Login successful.", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.d("MainActivity", "ℹ️ Navigation from: " + source);
                }
            } else {
                android.util.Log.w("MainActivity", "⚠️ No intent data available");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "❌ Error logging navigation source", e);
        }
    }

    private void setupAuthRepository() {
        authRepository = new AuthRepository(this);
    }

    private void loadCurrentUser() {
        try {
            // Validate authentication repository is available
            if (authRepository == null) {
                android.util.Log.e("MainActivity", "AuthRepository is null, cannot load user");
                navigateToAuth();
                return;
            }
            
            // Check if user is logged in and session is valid
            if (!authRepository.isLoggedIn()) {
                android.util.Log.d("MainActivity", "User is not logged in, redirecting to auth");
                navigateToAuth();
                return;
            }
            
            if (!authRepository.isSessionValid()) {
                android.util.Log.e("MainActivity", "User session is invalid, redirecting to auth");
                navigateToAuth();
                return;
            }
            
            currentUser = authRepository.getCachedUser();
            if (currentUser == null) {
                android.util.Log.e("MainActivity", "Cached user is null despite being logged in, redirecting to auth");
                navigateToAuth();
                return;
            }
            
            android.util.Log.d("MainActivity", "Current user loaded: " + currentUser.getUsername() + " (" + currentUser.getEmail() + ")");

            // Optionally refresh user data from server
            authRepository.getCurrentUser(new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        currentUser = user;
                        android.util.Log.d("MainActivity", "User data refreshed from server");
                        // Update UI with fresh user data if needed
                    });
                }

                @Override
                public void onError(AuthError error) {
                    android.util.Log.w("MainActivity", "Failed to refresh user data from server: " + error.toString());
                    // Handle error quietly or show a subtle notification
                    // Don't force logout on network errors
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error loading current user", e);
            navigateToAuth();
        }
    }

    private void setupInitialFragment() {
        if (currentUser != null) {
            // Load the main chat interface
            loadFragment(MainChatFragment.newInstance());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onLogoutRequested() {
        authRepository.logout(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, getString(R.string.success_logout), Toast.LENGTH_SHORT).show();
                    navigateToAuth();
                });
            }

            @Override
            public void onError(AuthError error) {
                runOnUiThread(() -> {
                    String userMessage = error.getUserFriendlyMessage() != null ? 
                        error.getUserFriendlyMessage() : "Logout failed. Please try again.";
                    Toast.makeText(MainActivity.this, userMessage, Toast.LENGTH_SHORT).show();
                    // Still navigate to auth even if logout API fails
                    navigateToAuth();
                });
            }
        });
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    private void navigateToAuth() {
        try {
            android.util.Log.d("MainActivity", "Navigating to AuthActivity");
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("source", "main_activity");
            intent.putExtra("reason", "authentication_required");
            startActivity(intent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error navigating to AuthActivity", e);
            // If navigation fails, at least finish this activity to prevent user from being stuck
            finish();
        }
    }
}