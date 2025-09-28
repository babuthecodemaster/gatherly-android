package com.cosmic.gatherly.data.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePreferences {
    private static final String TAG = "SecurePreferences";
    private static final String PREFS_NAME = "gatherly_secure_prefs";
    
    // Keys for secure storage
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_STATUS = "status";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_LAST_SYNC = "last_sync";
    
    private SharedPreferences encryptedPrefs;
    private SharedPreferences fallbackPrefs;
    private boolean useEncryption;
    
    public SecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            useEncryption = true;
            Log.d(TAG, "Encrypted preferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, "Failed to initialize encrypted preferences, falling back to regular preferences", e);
            fallbackPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            useEncryption = false;
        }
    }
    
    private SharedPreferences getPrefs() {
        return useEncryption ? encryptedPrefs : fallbackPrefs;
    }
    
    public void putString(String key, String value) {
        try {
            getPrefs().edit().putString(key, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error storing string value", e);
        }
    }
    
    public String getString(String key, String defaultValue) {
        try {
            return getPrefs().getString(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving string value", e);
            return defaultValue;
        }
    }
    
    public void putBoolean(String key, boolean value) {
        try {
            getPrefs().edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error storing boolean value", e);
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getPrefs().getBoolean(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving boolean value", e);
            return defaultValue;
        }
    }
    
    public void putLong(String key, long value) {
        try {
            getPrefs().edit().putLong(key, value).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error storing long value", e);
        }
    }
    
    public long getLong(String key, long defaultValue) {
        try {
            return getPrefs().getLong(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving long value", e);
            return defaultValue;
        }
    }
    
    public void remove(String key) {
        try {
            getPrefs().edit().remove(key).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error removing key", e);
        }
    }
    
    public void clear() {
        try {
            getPrefs().edit().clear().apply();
            Log.d(TAG, "Preferences cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing preferences", e);
        }
    }
    
    public boolean contains(String key) {
        try {
            return getPrefs().contains(key);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if key exists", e);
            return false;
        }
    }
    
    public boolean isUsingEncryption() {
        return useEncryption;
    }
}