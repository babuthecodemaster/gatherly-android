package com.cosmic.gatherly.ui.auth;

public interface AuthCallback {
    void onLoginRequested(String email, String password);
    void onRegisterRequested(String username, String email, String password);
}