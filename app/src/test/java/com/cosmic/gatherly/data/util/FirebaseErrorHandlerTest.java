package com.cosmic.gatherly.data.util;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FirebaseErrorHandler utility class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FirebaseErrorHandlerTest {

    @Test
    public void testGetErrorMessage_withNullException_returnsGenericMessage() {
        String result = FirebaseErrorHandler.getErrorMessage(null);
        assertEquals("An unknown error occurred. Please try again.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseAuthException_userNotFound() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_USER_NOT_FOUND");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("No account found with this email address. Please check your email or create a new account.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseAuthException_wrongPassword() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_WRONG_PASSWORD");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Incorrect password. Please try again or reset your password.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseAuthException_emailAlreadyInUse() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_EMAIL_ALREADY_IN_USE");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("An account with this email already exists. Please sign in or use a different email.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseAuthException_networkError() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_NETWORK_REQUEST_FAILED");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Network connection failed. Please check your internet connection and try again.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseFirestoreException_permissionDenied() {
        FirebaseFirestoreException exception = mock(FirebaseFirestoreException.class);
        when(exception.getCode()).thenReturn(FirebaseFirestoreException.Code.PERMISSION_DENIED);
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Access denied. You don't have permission to perform this operation.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseFirestoreException_notFound() {
        FirebaseFirestoreException exception = mock(FirebaseFirestoreException.class);
        when(exception.getCode()).thenReturn(FirebaseFirestoreException.Code.NOT_FOUND);
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("The requested data was not found.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseNetworkException() {
        FirebaseNetworkException exception = mock(FirebaseNetworkException.class);
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Network connection failed. Please check your internet connection and try again.", result);
    }

    @Test
    public void testGetErrorMessage_withFirebaseTooManyRequestsException() {
        FirebaseTooManyRequestsException exception = mock(FirebaseTooManyRequestsException.class);
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Too many requests. Please wait a moment and try again.", result);
    }

    @Test
    public void testGetErrorMessage_withGenericException_networkRelated() {
        Exception exception = new Exception("Network connection timeout");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Network connection failed. Please check your internet connection and try again.", result);
    }

    @Test
    public void testGetErrorMessage_withGenericException_timeoutRelated() {
        Exception exception = new Exception("Operation timeout occurred");
        
        String result = FirebaseErrorHandler.getErrorMessage(exception);
        assertEquals("Operation timed out. Please check your connection and try again.", result);
    }

    @Test
    public void testIsNetworkError_withFirebaseNetworkException_returnsTrue() {
        FirebaseNetworkException exception = mock(FirebaseNetworkException.class);
        
        boolean result = FirebaseErrorHandler.isNetworkError(exception);
        assertTrue(result);
    }

    @Test
    public void testIsNetworkError_withFirebaseAuthNetworkError_returnsTrue() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_NETWORK_REQUEST_FAILED");
        
        boolean result = FirebaseErrorHandler.isNetworkError(exception);
        assertTrue(result);
    }

    @Test
    public void testIsNetworkError_withFirebaseFirestoreUnavailable_returnsTrue() {
        FirebaseFirestoreException exception = mock(FirebaseFirestoreException.class);
        when(exception.getCode()).thenReturn(FirebaseFirestoreException.Code.UNAVAILABLE);
        
        boolean result = FirebaseErrorHandler.isNetworkError(exception);
        assertTrue(result);
    }

    @Test
    public void testIsNetworkError_withNonNetworkError_returnsFalse() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_WRONG_PASSWORD");
        
        boolean result = FirebaseErrorHandler.isNetworkError(exception);
        assertFalse(result);
    }

    @Test
    public void testRequiresReAuthentication_withTokenExpired_returnsTrue() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_USER_TOKEN_EXPIRED");
        
        boolean result = FirebaseErrorHandler.requiresReAuthentication(exception);
        assertTrue(result);
    }

    @Test
    public void testRequiresReAuthentication_withFirestoreUnauthenticated_returnsTrue() {
        FirebaseFirestoreException exception = mock(FirebaseFirestoreException.class);
        when(exception.getCode()).thenReturn(FirebaseFirestoreException.Code.UNAUTHENTICATED);
        
        boolean result = FirebaseErrorHandler.requiresReAuthentication(exception);
        assertTrue(result);
    }

    @Test
    public void testRequiresReAuthentication_withNormalError_returnsFalse() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_WRONG_PASSWORD");
        
        boolean result = FirebaseErrorHandler.requiresReAuthentication(exception);
        assertFalse(result);
    }

    @Test
    public void testIsRetryable_withNetworkException_returnsTrue() {
        FirebaseNetworkException exception = mock(FirebaseNetworkException.class);
        
        boolean result = FirebaseErrorHandler.isRetryable(exception);
        assertTrue(result);
    }

    @Test
    public void testIsRetryable_withTooManyRequestsException_returnsTrue() {
        FirebaseTooManyRequestsException exception = mock(FirebaseTooManyRequestsException.class);
        
        boolean result = FirebaseErrorHandler.isRetryable(exception);
        assertTrue(result);
    }

    @Test
    public void testIsRetryable_withFirestoreUnavailable_returnsTrue() {
        FirebaseFirestoreException exception = mock(FirebaseFirestoreException.class);
        when(exception.getCode()).thenReturn(FirebaseFirestoreException.Code.UNAVAILABLE);
        
        boolean result = FirebaseErrorHandler.isRetryable(exception);
        assertTrue(result);
    }

    @Test
    public void testIsRetryable_withPermanentError_returnsFalse() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_WRONG_PASSWORD");
        
        boolean result = FirebaseErrorHandler.isRetryable(exception);
        assertFalse(result);
    }
}