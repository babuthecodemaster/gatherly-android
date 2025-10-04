package com.cosmic.gatherly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.cosmic.gatherly.MinimalApplication;
import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.AuthState;
import com.cosmic.gatherly.data.repository.AuthManager;
import com.cosmic.gatherly.ui.main.MainActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Beautiful Auth Activity - Uses centralized AuthManager with original gorgeous layouts
 * This uses the original gorgeous layouts with centralized AuthManager integration
 */
public class UltraMinimalAuthActivity extends AppCompatActivity {
    
    private static final String TAG = "UltraMinimalAuth";
    
    private AuthManager authManager;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "Beautiful Auth Activity started with centralized AuthManager");
        
        try {
            // Get AuthManager instance from Application class
            authManager = ((MinimalApplication) getApplication()).getAuthManager();
            if (authManager == null) {
                Toast.makeText(this, "Authentication service unavailable", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            Log.d(TAG, "AuthManager initialized");
            
            // Use the original beautiful layout!
            setContentView(R.layout.activity_auth);
            
            setupUI();
            setupAuthStateObserver();
            
            Log.d(TAG, "Beautiful AuthManager UI created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating auth UI", e);
            Toast.makeText(this, "Error initializing authentication: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupUI() {
        // Setup TabLayout and ViewPager2 for beautiful transitions
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        
        // Create adapter for Login/Register fragments
        AuthPagerAdapter adapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Login" : "Register");
        }).attach();
    }
    
    /**
     * Set up AuthState observer for navigation and UI updates
     */
    private void setupAuthStateObserver() {
        authManager.getAuthState().observe(this, authState -> {
            Log.d(TAG, "AuthState changed: " + authState.getStatus());
            
            switch (authState.getStatus()) {
                case LOADING:
                    // Loading state is handled by fragments
                    break;
                    
                case AUTHENTICATED:
                    String email = authState.getUser() != null ? authState.getUser().getEmail() : "Unknown";
                    Log.d(TAG, "User authenticated: " + email);
                    // Go directly to main app - no intermediate screen needed
                    navigateToMainApp();
                    break;
                    
                case UNAUTHENTICATED:
                    // Stay on auth screen - fragments handle the UI
                    break;
                    
                case ERROR:
                    // Error handling is done by fragments
                    break;
            }
        });
    }
    
    // Adapter for ViewPager2 to handle Login/Register fragments
    private static class AuthPagerAdapter extends FragmentStateAdapter {
        public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new LoginFragment();
            } else {
                return new RegisterFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Login and Register
        }
    }
    
    private void navigateToMainApp() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("source", "firebase_auth");
            intent.putExtra("auth_type", "firebase");
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main app", e);
            Toast.makeText(this, "Error loading main app", Toast.LENGTH_SHORT).show();
        }
    }
}