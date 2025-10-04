package com.cosmic.gatherly.ui.main;

import com.google.firebase.auth.FirebaseUser;

public interface MainActivityCallback {
    void onLogoutRequested();
    FirebaseUser getCurrentUser();
}