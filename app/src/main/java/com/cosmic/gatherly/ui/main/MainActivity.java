package com.cosmic.gatherly.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.ui.auth.AuthActivity;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {
    
    private AuthRepository authRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAuthRepository();
        loadCurrentUser();
        setupInitialFragment();
    }

    private void setupAuthRepository() {
        authRepository = new AuthRepository(this);
    }

    private void loadCurrentUser() {
        currentUser = authRepository.getCachedUser();
        if (currentUser == null) {
            // User is not logged in, redirect to auth
            navigateToAuth();
            return;
        }

        // Optionally refresh user data from server
        authRepository.getCurrentUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    currentUser = user;
                    // Update UI with fresh user data if needed
                });
            }

            @Override
            public void onError(String message) {
                // Handle error quietly or show a subtle notification
                // Don't force logout on network errors
            }
        });
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
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}