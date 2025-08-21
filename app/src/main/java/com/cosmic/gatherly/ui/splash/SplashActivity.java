package com.cosmic.gatherly.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.cosmic.gatherly.R;
import com.cosmic.gatherly.data.repository.AuthRepository;
import com.cosmic.gatherly.ui.auth.AuthActivity;
import com.cosmic.gatherly.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authRepository = new AuthRepository(this);

        // Delay the splash screen and check authentication status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationStatus();
        }, SPLASH_DELAY);
    }

    private void checkAuthenticationStatus() {
        if (authRepository.isLoggedIn()) {
            // User is logged in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User is not logged in, go to auth activity
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }
}