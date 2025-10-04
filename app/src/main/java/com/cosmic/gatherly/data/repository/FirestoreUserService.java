package com.cosmic.gatherly.data.repository;

import android.util.Log;

import com.cosmic.gatherly.data.model.UserProfile;
import com.cosmic.gatherly.data.util.Logger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * FirestoreUserService handles all user profile operations with Firebase Firestore
 * Provides methods to create, read, and update user profiles in the cloud database
 */
public class FirestoreUserService {
    private static final String TAG = "FirestoreUserService";
    private static final String USERS_COLLECTION = "users";
    
    private final FirebaseFirestore mFirestore;
    
    /**
     * Constructor initializes Firestore instance
     */
    public FirestoreUserService() {
        mFirestore = FirebaseFirestore.getInstance();
        Logger.d(TAG, "FirestoreUserService initialized");
    }
    
    /**
     * Constructor with custom Firestore instance (for testing)
     * @param firestore Custom FirebaseFirestore instance
     */
    public FirestoreUserService(FirebaseFirestore firestore) {
        mFirestore = firestore;
        Logger.d(TAG, "FirestoreUserService initialized with custom Firestore instance");
    }
    
    /**
     * Creates a new user profile in Firestore
     * @param profile The UserProfile object to save
     * @return Task<Void> representing the async operation
     */
    public Task<Void> createUserProfile(UserProfile profile) {
        Logger.methodEntry(Logger.TAG_DATABASE, "createUserProfile");
        Logger.startTiming("FIRESTORE_CREATE_USER");
        
        if (profile == null) {
            Logger.w(TAG, "Cannot create user profile: profile is null");
            throw new IllegalArgumentException("UserProfile cannot be null");
        }
        
        if (profile.getUid() == null || profile.getUid().isEmpty()) {
            Logger.w(TAG, "Cannot create user profile: UID is null or empty");
            throw new IllegalArgumentException("UserProfile UID cannot be null or empty");
        }
        
        Logger.d(TAG, "Creating user profile for UID: %s", profile.getUid());
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(profile.getUid())
            .set(profile)
            .addOnSuccessListener(aVoid -> {
                Logger.i(TAG, "✅ User profile created successfully for UID: %s", profile.getUid());
                Logger.endTiming("FIRESTORE_CREATE_USER");
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to create user profile for UID: %s", exception, profile.getUid());
                Logger.endTiming("FIRESTORE_CREATE_USER");
            });
    }
    
    /**
     * Retrieves a user profile from Firestore by UID
     * @param uid The user's unique identifier
     * @return Task<UserProfile> representing the async operation
     */
    public Task<UserProfile> getUserProfile(String uid) {
        Logger.methodEntry(Logger.TAG_DATABASE, "getUserProfile");
        Logger.startTiming("FIRESTORE_GET_USER");
        
        if (uid == null || uid.isEmpty()) {
            Logger.w(TAG, "Cannot get user profile: UID is null or empty");
            throw new IllegalArgumentException("UID cannot be null or empty");
        }
        
        Logger.d(TAG, "Retrieving user profile for UID: %s", uid);
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .continueWith(task -> {
                Logger.endTiming("FIRESTORE_GET_USER");
                
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        UserProfile profile = document.toObject(UserProfile.class);
                        if (profile != null) {
                            Logger.i(TAG, "✅ User profile retrieved successfully for UID: %s", uid);
                            return profile;
                        } else {
                            Logger.w(TAG, "User profile document exists but failed to deserialize for UID: %s", uid);
                            throw new RuntimeException("Failed to deserialize user profile");
                        }
                    } else {
                        Logger.w(TAG, "User profile not found for UID: %s", uid);
                        throw new RuntimeException("User profile not found");
                    }
                } else {
                    Exception exception = task.getException();
                    Logger.e(TAG, "❌ Failed to retrieve user profile for UID: %s", exception, uid);
                    throw new RuntimeException("Failed to retrieve user profile", exception);
                }
            });
    }
    
    /**
     * Updates an existing user profile in Firestore using merge options
     * This will update only the fields that are set, preserving existing data
     * @param profile The UserProfile object with updated data
     * @return Task<Void> representing the async operation
     */
    public Task<Void> updateUserProfile(UserProfile profile) {
        Logger.methodEntry(Logger.TAG_DATABASE, "updateUserProfile");
        Logger.startTiming("FIRESTORE_UPDATE_USER");
        
        if (profile == null) {
            Logger.w(TAG, "Cannot update user profile: profile is null");
            throw new IllegalArgumentException("UserProfile cannot be null");
        }
        
        if (profile.getUid() == null || profile.getUid().isEmpty()) {
            Logger.w(TAG, "Cannot update user profile: UID is null or empty");
            throw new IllegalArgumentException("UserProfile UID cannot be null or empty");
        }
        
        // Update the last login timestamp
        profile.updateLastLogin();
        
        Logger.d(TAG, "Updating user profile for UID: %s", profile.getUid());
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(profile.getUid())
            .set(profile, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Logger.i(TAG, "✅ User profile updated successfully for UID: %s", profile.getUid());
                Logger.endTiming("FIRESTORE_UPDATE_USER");
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to update user profile for UID: %s", exception, profile.getUid());
                Logger.endTiming("FIRESTORE_UPDATE_USER");
            });
    }
    
    /**
     * Updates only the last login timestamp for a user
     * This is a lightweight operation for tracking user activity
     * @param uid The user's unique identifier
     * @return Task<Void> representing the async operation
     */
    public Task<Void> updateLastLogin(String uid) {
        Logger.methodEntry(Logger.TAG_DATABASE, "updateLastLogin");
        Logger.startTiming("FIRESTORE_UPDATE_LAST_LOGIN");
        
        if (uid == null || uid.isEmpty()) {
            Logger.w(TAG, "Cannot update last login: UID is null or empty");
            throw new IllegalArgumentException("UID cannot be null or empty");
        }
        
        Logger.d(TAG, "Updating last login timestamp for UID: %s", uid);
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .update("lastLoginAt", System.currentTimeMillis())
            .addOnSuccessListener(aVoid -> {
                Logger.i(TAG, "✅ Last login timestamp updated for UID: %s", uid);
                Logger.endTiming("FIRESTORE_UPDATE_LAST_LOGIN");
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to update last login for UID: %s", exception, uid);
                Logger.endTiming("FIRESTORE_UPDATE_LAST_LOGIN");
            });
    }
    
    /**
     * Checks if a user profile exists in Firestore
     * @param uid The user's unique identifier
     * @return Task<Boolean> representing the async operation
     */
    public Task<Boolean> userProfileExists(String uid) {
        Logger.methodEntry(Logger.TAG_DATABASE, "userProfileExists");
        Logger.startTiming("FIRESTORE_USER_EXISTS");
        
        if (uid == null || uid.isEmpty()) {
            Logger.w(TAG, "Cannot check user profile existence: UID is null or empty");
            throw new IllegalArgumentException("UID cannot be null or empty");
        }
        
        Logger.d(TAG, "Checking if user profile exists for UID: %s", uid);
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .continueWith(task -> {
                Logger.endTiming("FIRESTORE_USER_EXISTS");
                
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    boolean exists = document != null && document.exists();
                    Logger.d(TAG, "User profile exists check for UID %s: %s", uid, exists);
                    return exists;
                } else {
                    Exception exception = task.getException();
                    Logger.e(TAG, "❌ Failed to check user profile existence for UID: %s", exception, uid);
                    throw new RuntimeException("Failed to check user profile existence", exception);
                }
            });
    }
    
    /**
     * Deletes a user profile from Firestore
     * @param uid The user's unique identifier
     * @return Task<Void> representing the async operation
     */
    public Task<Void> deleteUserProfile(String uid) {
        Logger.methodEntry(Logger.TAG_DATABASE, "deleteUserProfile");
        Logger.startTiming("FIRESTORE_DELETE_USER");
        
        if (uid == null || uid.isEmpty()) {
            Logger.w(TAG, "Cannot delete user profile: UID is null or empty");
            throw new IllegalArgumentException("UID cannot be null or empty");
        }
        
        Logger.d(TAG, "Deleting user profile for UID: %s", uid);
        
        return mFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Logger.i(TAG, "✅ User profile deleted successfully for UID: %s", uid);
                Logger.endTiming("FIRESTORE_DELETE_USER");
            })
            .addOnFailureListener(exception -> {
                Logger.e(TAG, "❌ Failed to delete user profile for UID: %s", exception, uid);
                Logger.endTiming("FIRESTORE_DELETE_USER");
            });
    }
    
    /**
     * Gets the Firestore instance (for advanced operations if needed)
     * @return FirebaseFirestore instance
     */
    public FirebaseFirestore getFirestore() {
        return mFirestore;
    }
    
    /**
     * Gets the users collection name
     * @return The collection name used for storing user profiles
     */
    public static String getUsersCollectionName() {
        return USERS_COLLECTION;
    }
}