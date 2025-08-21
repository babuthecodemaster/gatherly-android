package com.cosmic.gatherly.ui.main;

import com.cosmic.gatherly.data.model.User;

public interface MainActivityCallback {
    void onLogoutRequested();
    User getCurrentUser();
}