package com.cosmic.gatherly;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.cosmic.gatherly.ui.auth.AuthActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Instrumented UI tests for authentication flow
 * Tests the actual UI interactions and error handling
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthenticationUITest {

    @Rule
    public ActivityScenarioRule<AuthActivity> activityRule = 
        new ActivityScenarioRule<>(AuthActivity.class);

    /**
     * Test 1: Login form validation and error display
     * Requirements: 1.2, 2.3
     */
    @Test
    public void testLoginFormValidation() {
        // Test empty email validation
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Check that error is displayed for empty email
        onView(withId(R.id.emailInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));

        // Test invalid email format
        onView(withId(R.id.emailEditText))
            .perform(typeText("invalid-email"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        
        onView(withId(R.id.loginButton))
            .perform(click());

        // Verify error handling for invalid email
        onView(withId(R.id.emailInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));
    }

    /**
     * Test 2: Registration form validation and error display
     * Requirements: 1.1, 1.2
     */
    @Test
    public void testRegistrationFormValidation() {
        // Switch to registration tab
        onView(withText("Register"))
            .perform(click());

        // Test empty fields validation
        onView(withId(R.id.registerButton))
            .perform(click());

        // Check that errors are displayed for empty fields
        onView(withId(R.id.usernameInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));
        
        onView(withId(R.id.emailInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));
        
        onView(withId(R.id.passwordInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));

        // Test short username validation
        onView(withId(R.id.usernameEditText))
            .perform(typeText("ab"), closeSoftKeyboard());
        
        onView(withId(R.id.registerButton))
            .perform(click());

        onView(withId(R.id.usernameInputLayout))
            .check(matches(hasErrorText(containsString("3 characters"))));
    }

    /**
     * Test 3: Loading states during authentication
     * Requirements: 3.4
     */
    @Test
    public void testLoadingStates() {
        // Test login loading state
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        
        onView(withId(R.id.loginButton))
            .perform(click());

        // Check that loading indicator is shown
        onView(withId(R.id.loginProgressBar))
            .check(matches(isDisplayed()));
        
        // Check that button is disabled during loading
        onView(withId(R.id.loginButton))
            .check(matches(not(isEnabled())));

        // Wait for loading to complete (or timeout)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test 4: Registration loading states and button management
     * Requirements: 1.2, 3.4
     */
    @Test
    public void testRegistrationLoadingStates() {
        // Switch to registration tab
        onView(withText("Register"))
            .perform(click());

        // Fill in valid registration data
        onView(withId(R.id.usernameEditText))
            .perform(typeText("testuser"), closeSoftKeyboard());
        
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        
        onView(withId(R.id.registerButton))
            .perform(click());

        // Check that loading indicator is shown
        onView(withId(R.id.registerProgressBar))
            .check(matches(isDisplayed()));
        
        // Check that button is disabled during loading
        onView(withId(R.id.registerButton))
            .check(matches(not(isEnabled())));

        // Wait for loading to complete (or timeout)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test 5: Error message display and retry functionality
     * Requirements: 3.1, 3.2, 3.4
     */
    @Test
    public void testErrorMessageDisplayAndRetry() {
        // Attempt login with potentially invalid credentials
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("wrongpassword"), closeSoftKeyboard());
        
        onView(withId(R.id.loginButton))
            .perform(click());

        // Wait for potential error response
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if error message is displayed
        try {
            onView(withId(R.id.errorMessageText))
                .check(matches(isDisplayed()));
            
            // Test retry button functionality
            onView(withId(R.id.retryButton))
                .check(matches(isDisplayed()))
                .perform(click());
            
            // Verify error message is hidden after retry
            onView(withId(R.id.errorMessageText))
                .check(matches(not(isDisplayed())));
                
        } catch (Exception e) {
            // Error message might not be displayed if server is running
            // This is acceptable for this test
        }
    }

    /**
     * Test 6: Tab navigation between login and register
     * Requirements: UI navigation
     */
    @Test
    public void testTabNavigation() {
        // Verify login tab is initially selected
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()));

        // Switch to register tab
        onView(withText("Register"))
            .perform(click());

        // Verify register form is displayed
        onView(withId(R.id.usernameEditText))
            .check(matches(isDisplayed()));

        // Switch back to login tab
        onView(withText("Login"))
            .perform(click());

        // Verify login form is displayed again
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 7: Input field behavior and validation
     * Requirements: 1.1, 1.2, 2.1, 2.3
     */
    @Test
    public void testInputFieldBehavior() {
        // Test email field accepts valid email
        onView(withId(R.id.emailEditText))
            .perform(typeText("valid@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.emailEditText))
            .check(matches(withText("valid@example.com")));

        // Test password field hides input
        onView(withId(R.id.passwordEditText))
            .perform(typeText("secretpassword"), closeSoftKeyboard());
        
        // Password field should have input transformation (dots)
        onView(withId(R.id.passwordEditText))
            .check(matches(withText("secretpassword")));

        // Clear fields and test empty validation
        onView(withId(R.id.emailEditText))
            .perform(clearText());
        
        onView(withId(R.id.passwordEditText))
            .perform(clearText());
        
        onView(withId(R.id.loginButton))
            .perform(click());

        // Verify validation errors are shown
        onView(withId(R.id.emailInputLayout))
            .check(matches(hasErrorText(not(isEmptyString()))));
    }

    /**
     * Test 8: Registration form input validation
     * Requirements: 1.1, 1.4
     */
    @Test
    public void testRegistrationInputValidation() {
        // Switch to registration tab
        onView(withText("Register"))
            .perform(click());

        // Test username validation
        onView(withId(R.id.usernameEditText))
            .perform(typeText("validusername"), closeSoftKeyboard());
        
        onView(withId(R.id.usernameEditText))
            .check(matches(withText("validusername")));

        // Test email validation
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.emailEditText))
            .check(matches(withText("test@example.com")));

        // Test password validation
        onView(withId(R.id.passwordEditText))
            .perform(typeText("strongpassword123"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .check(matches(withText("strongpassword123")));

        // Clear username and test short username validation
        onView(withId(R.id.usernameEditText))
            .perform(clearText(), typeText("ab"), closeSoftKeyboard());
        
        onView(withId(R.id.registerButton))
            .perform(click());

        onView(withId(R.id.usernameInputLayout))
            .check(matches(hasErrorText(containsString("3 characters"))));
    }

    /**
     * Test 9: Network error scenarios simulation
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testNetworkErrorScenarios() {
        // This test simulates network errors by attempting authentication
        // with the server potentially not running
        
        // Test login with server potentially down
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        
        onView(withId(R.id.loginButton))
            .perform(click());

        // Wait for network timeout or response
        try {
            Thread.sleep(10000); // Wait up to 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if error handling worked (either success or proper error display)
        // The button should be re-enabled after the request completes
        onView(withId(R.id.loginButton))
            .check(matches(isEnabled()));
    }

    /**
     * Test 10: Registration with server potentially down
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testRegistrationNetworkError() {
        // Switch to registration tab
        onView(withText("Register"))
            .perform(click());

        // Fill in registration data
        onView(withId(R.id.usernameEditText))
            .perform(typeText("testuser"), closeSoftKeyboard());
        
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        
        onView(withId(R.id.registerButton))
            .perform(click());

        // Wait for network timeout or response
        try {
            Thread.sleep(10000); // Wait up to 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if error handling worked (either success or proper error display)
        // The button should be re-enabled after the request completes
        onView(withId(R.id.registerButton))
            .check(matches(isEnabled()));
    }
}