package com.cosmic.gatherly.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.cosmic.gatherly.data.api.ApiClient;
import com.cosmic.gatherly.data.api.ApiService;
import com.cosmic.gatherly.data.model.User;
import com.cosmic.gatherly.data.request.LoginRequest;
import com.cosmic.gatherly.data.request.RegisterRequest;
import com.cosmic.gatherly.data.response.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String PREFS_NAME = "gatherly_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_STATUS = "status";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private ApiService apiService;
    private SharedPreferences prefs;

    public AuthRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    User user = createUserFromResponse(authResponse);
                    saveUserSession(user);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Invalid credentials");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void register(String username, String email, String password, AuthCallback callback) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    User user = createUserFromResponse(authResponse);
                    saveUserSession(user);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Registration failed");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void logout(AuthCallback callback) {
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                clearUserSession();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                clearUserSession(); // Clear session even if API call fails
                callback.onSuccess(null);
            }
        });
    }

    public void getCurrentUser(AuthCallback callback) {
        if (!isLoggedIn()) {
            callback.onError("Not logged in");
            return;
        }

        apiService.getCurrentUser().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    User user = createUserFromResponse(authResponse);
                    saveUserSession(user);
                    callback.onSuccess(user);
                } else {
                    clearUserSession();
                    callback.onError("Session expired");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public User getCachedUser() {
        if (!isLoggedIn()) {
            return null;
        }

        String id = prefs.getString(KEY_USER_ID, null);
        String username = prefs.getString(KEY_USERNAME, null);
        String email = prefs.getString(KEY_EMAIL, null);
        String avatar = prefs.getString(KEY_AVATAR, null);
        String status = prefs.getString(KEY_STATUS, "offline");

        if (id != null && username != null && email != null) {
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatar(avatar);
            user.setStatus(User.UserStatus.fromString(status));
            return user;
        }

        return null;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private User createUserFromResponse(AuthResponse response) {
        User user = new User();
        user.setId(response.getId());
        user.setUsername(response.getUsername());
        user.setEmail(response.getEmail());
        user.setAvatar(response.getAvatar());
        user.setStatus(User.UserStatus.fromString(response.getStatus()));
        return user;
    }

    private void saveUserSession(User user) {
        prefs.edit()
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_AVATAR, user.getAvatar())
                .putString(KEY_STATUS, user.getStatus().getValue())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    private void clearUserSession() {
        prefs.edit().clear().apply();
    }
}