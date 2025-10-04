package com.cosmic.gatherly.data.model;

import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

/**
 * UserProfile model class for Firebase Firestore integration
 * Represents user profile data stored in Firestore database
 */
public class UserProfile {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private long createdAt;
    private long lastLoginAt;
    private Map<String, Object> preferences;

    /**
     * Default constructor required for Firestore deserialization
     */
    public UserProfile() {
        this.preferences = new HashMap<>();
    }

    /**
     * Constructor that creates UserProfile from FirebaseUser
     * @param firebaseUser The Firebase user object
     */
    public UserProfile(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            this.uid = firebaseUser.getUid();
            this.email = firebaseUser.getEmail();
            this.displayName = firebaseUser.getDisplayName();
            this.photoUrl = firebaseUser.getPhotoUrl() != null ? 
                firebaseUser.getPhotoUrl().toString() : null;
        }
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
        this.preferences = new HashMap<>();
    }

    /**
     * Full constructor for creating UserProfile with all fields
     */
    public UserProfile(String uid, String email, String displayName, String photoUrl, 
                      long createdAt, long lastLoginAt, Map<String, Object> preferences) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.preferences = preferences != null ? preferences : new HashMap<>();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences != null ? preferences : new HashMap<>();
    }

    /**
     * Helper method to get a preference value
     * @param key The preference key
     * @return The preference value or null if not found
     */
    public Object getPreference(String key) {
        return preferences != null ? preferences.get(key) : null;
    }

    /**
     * Helper method to set a preference value
     * @param key The preference key
     * @param value The preference value
     */
    public void setPreference(String key, Object value) {
        if (preferences == null) {
            preferences = new HashMap<>();
        }
        preferences.put(key, value);
    }

    /**
     * Updates the last login timestamp to current time
     */
    public void updateLastLogin() {
        this.lastLoginAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                ", preferences=" + preferences +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        return uid != null ? uid.equals(that.uid) : that.uid == null;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }
}