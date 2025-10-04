package com.cosmic.gatherly.data.model;

import com.google.firebase.auth.FirebaseUser;

/**
 * AuthState model for managing authentication state throughout the application
 * Provides a centralized way to represent different authentication states
 */
public class AuthState {
    
    /**
     * Enum representing different authentication states
     */
    public enum Status {
        LOADING,        // Authentication check in progress
        AUTHENTICATED,  // User is successfully authenticated
        UNAUTHENTICATED, // User is not authenticated
        ERROR          // Authentication error occurred
    }

    private Status status;
    private FirebaseUser user;
    private UserProfile userProfile;
    private String errorMessage;

    /**
     * Private constructor to enforce use of static factory methods
     */
    private AuthState(Status status, FirebaseUser user, UserProfile userProfile, String errorMessage) {
        this.status = status;
        this.user = user;
        this.userProfile = userProfile;
        this.errorMessage = errorMessage;
    }

    // Static factory methods for creating different auth states

    /**
     * Creates a loading auth state
     * @return AuthState with LOADING status
     */
    public static AuthState loading() {
        return new AuthState(Status.LOADING, null, null, null);
    }

    /**
     * Creates an authenticated auth state
     * @param user The authenticated Firebase user
     * @param userProfile The user's profile data
     * @return AuthState with AUTHENTICATED status
     */
    public static AuthState authenticated(FirebaseUser user, UserProfile userProfile) {
        return new AuthState(Status.AUTHENTICATED, user, userProfile, null);
    }

    /**
     * Creates an authenticated auth state with only Firebase user
     * @param user The authenticated Firebase user
     * @return AuthState with AUTHENTICATED status
     */
    public static AuthState authenticated(FirebaseUser user) {
        return new AuthState(Status.AUTHENTICATED, user, null, null);
    }

    /**
     * Creates an unauthenticated auth state
     * @return AuthState with UNAUTHENTICATED status
     */
    public static AuthState unauthenticated() {
        return new AuthState(Status.UNAUTHENTICATED, null, null, null);
    }

    /**
     * Creates an error auth state
     * @param errorMessage The error message describing what went wrong
     * @return AuthState with ERROR status
     */
    public static AuthState error(String errorMessage) {
        return new AuthState(Status.ERROR, null, null, errorMessage);
    }

    /**
     * Creates an error auth state with user context
     * @param errorMessage The error message describing what went wrong
     * @param user The Firebase user (if available)
     * @return AuthState with ERROR status
     */
    public static AuthState error(String errorMessage, FirebaseUser user) {
        return new AuthState(Status.ERROR, user, null, errorMessage);
    }

    // Getters
    public Status getStatus() {
        return status;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Convenience methods for checking state
    
    /**
     * @return true if the current state is LOADING
     */
    public boolean isLoading() {
        return status == Status.LOADING;
    }

    /**
     * @return true if the current state is AUTHENTICATED
     */
    public boolean isAuthenticated() {
        return status == Status.AUTHENTICATED;
    }

    /**
     * @return true if the current state is UNAUTHENTICATED
     */
    public boolean isUnauthenticated() {
        return status == Status.UNAUTHENTICATED;
    }

    /**
     * @return true if the current state is ERROR
     */
    public boolean isError() {
        return status == Status.ERROR;
    }

    /**
     * @return true if user data is available (either FirebaseUser or UserProfile)
     */
    public boolean hasUserData() {
        return user != null || userProfile != null;
    }

    /**
     * @return the user's UID if available, null otherwise
     */
    public String getUserId() {
        if (user != null) {
            return user.getUid();
        } else if (userProfile != null) {
            return userProfile.getUid();
        }
        return null;
    }

    /**
     * @return the user's email if available, null otherwise
     */
    public String getUserEmail() {
        if (user != null) {
            return user.getEmail();
        } else if (userProfile != null) {
            return userProfile.getEmail();
        }
        return null;
    }

    /**
     * Creates a new AuthState with updated user profile while keeping other fields
     * @param userProfile The updated user profile
     * @return New AuthState with updated profile
     */
    public AuthState withUserProfile(UserProfile userProfile) {
        return new AuthState(this.status, this.user, userProfile, this.errorMessage);
    }

    /**
     * Creates a new AuthState with updated error message while keeping other fields
     * @param errorMessage The updated error message
     * @return New AuthState with updated error message
     */
    public AuthState withError(String errorMessage) {
        return new AuthState(Status.ERROR, this.user, this.userProfile, errorMessage);
    }

    @Override
    public String toString() {
        return "AuthState{" +
                "status=" + status +
                ", user=" + (user != null ? user.getUid() : "null") +
                ", userProfile=" + (userProfile != null ? userProfile.getUid() : "null") +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthState authState = (AuthState) o;

        if (status != authState.status) return false;
        if (user != null ? !user.getUid().equals(authState.user != null ? authState.user.getUid() : null) : authState.user != null) return false;
        if (userProfile != null ? !userProfile.equals(authState.userProfile) : authState.userProfile != null) return false;
        return errorMessage != null ? errorMessage.equals(authState.errorMessage) : authState.errorMessage == null;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (user != null ? user.getUid().hashCode() : 0);
        result = 31 * result + (userProfile != null ? userProfile.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }
}