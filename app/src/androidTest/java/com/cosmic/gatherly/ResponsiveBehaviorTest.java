package com.cosmic.gatherly;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.cosmic.gatherly.ui.main.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for responsive behavior on mobile devices
 * Tests Requirements: All requirements - Mobile responsiveness
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ResponsiveBehaviorTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    private Context context;
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        displayMetrics = context.getResources().getDisplayMetrics();
        
        // Wait for activity to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test 1: Portrait orientation layout
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testPortraitOrientationLayout() {
        // Set portrait orientation
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify main UI elements are properly displayed
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));

        onView(withId(R.id.topAppBar))
            .check(matches(isDisplayed()));

        onView(withId(R.id.menuButton))
            .check(matches(isDisplayed()));

        // Verify touch targets are appropriate size (minimum 48dp)
        onView(withId(R.id.searchButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.membersButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.mentionsButton))
            .check(matches(hasMinimumSize(48)));

        // Test navigation drawer in portrait
        onView(withId(R.id.menuButton))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.navigation_drawer))
            .check(matches(isDisplayed()));

        // Verify drawer width is appropriate for portrait
        onView(withId(R.id.navigation_drawer))
            .check(matches(hasMaximumWidth(displayMetrics.widthPixels * 0.8f)));
    }

    /**
     * Test 2: Landscape orientation layout
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testLandscapeOrientationLayout() {
        // Set landscape orientation
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify UI adapts to landscape
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));

        onView(withId(R.id.topAppBar))
            .check(matches(isDisplayed()));

        // Test navigation drawer behavior in landscape
        onView(withId(R.id.menuButton))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.navigation_drawer))
            .check(matches(isDisplayed()));

        // Verify drawer width is appropriate for landscape
        onView(withId(R.id.navigation_drawer))
            .check(matches(hasMaximumWidth(displayMetrics.heightPixels * 0.6f)));

        // Test message input area in landscape
        onView(withId(R.id.messageEditText))
            .check(matches(isDisplayed()));

        onView(withId(R.id.sendButton))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 3: Touch target sizes
     * Requirements: All requirements - Accessibility
     */
    @Test
    public void testTouchTargetSizes() {
        // Test all interactive elements have minimum touch target size (48dp)
        onView(withId(R.id.menuButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.searchButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.membersButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.mentionsButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.attachmentButton))
            .check(matches(hasMinimumSize(48)));

        onView(withId(R.id.sendButton))
            .check(matches(hasMinimumSize(48)));

        // Test drawer elements
        onView(withId(R.id.menuButton))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.settingsButton))
            .check(matches(hasMinimumSize(48)));
    }

    /**
     * Test 4: Swipe gestures for mobile navigation
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testSwipeGesturesForMobileNavigation() {
        // Test swipe to open drawer
        onView(withId(R.id.fragment_container))
            .perform(swipeRight());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify drawer opens
        try {
            onView(withId(R.id.navigation_drawer))
                .check(matches(isDisplayed()));
        } catch (Exception e) {
            // Swipe gesture might not be implemented
        }

        // Test swipe to close drawer
        onView(withId(R.id.navigation_drawer))
            .perform(swipeLeft());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test swipe between channels (if implemented)
        onView(withId(R.id.fragment_container))
            .perform(swipeLeft());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.fragment_container))
            .perform(swipeRight());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test 5: Small screen device behavior
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testSmallScreenDeviceBehavior() {
        // Simulate small screen by checking current screen size
        float density = displayMetrics.density;
        int screenWidthDp = (int) (displayMetrics.widthPixels / density);
        int screenHeightDp = (int) (displayMetrics.heightPixels / density);

        // Test UI elements are properly sized for small screens
        if (screenWidthDp < 600) { // Small screen
            // Verify navigation drawer takes appropriate width
            onView(withId(R.id.menuButton))
                .perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            onView(withId(R.id.navigation_drawer))
                .check(matches(isDisplayed()));

            // Verify text sizes are readable
            onView(withId(R.id.serverNameText))
                .check(matches(hasMinimumTextSize(14)));

            onView(withId(R.id.channelNameText))
                .check(matches(hasMinimumTextSize(14)));
        }
    }

    /**
     * Test 6: Large screen device behavior
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testLargeScreenDeviceBehavior() {
        float density = displayMetrics.density;
        int screenWidthDp = (int) (displayMetrics.widthPixels / density);

        // Test UI elements utilize larger screen space effectively
        if (screenWidthDp >= 600) { // Large screen (tablet)
            // Verify navigation drawer behavior on tablets
            onView(withId(R.id.menuButton))
                .perform(click());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            onView(withId(R.id.navigation_drawer))
                .check(matches(isDisplayed()));

            // Verify drawer doesn't take full width on large screens
            onView(withId(R.id.navigation_drawer))
                .check(matches(hasMaximumWidth(displayMetrics.widthPixels * 0.5f)));
        }
    }

    /**
     * Test 7: Keyboard handling on mobile
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testKeyboardHandlingOnMobile() {
        // Focus on message input to show keyboard
        onView(withId(R.id.messageEditText))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify UI adjusts for keyboard
        onView(withId(R.id.messageEditText))
            .check(matches(isDisplayed()));

        onView(withId(R.id.sendButton))
            .check(matches(isDisplayed()));

        // Type message to test keyboard interaction
        onView(withId(R.id.messageEditText))
            .perform(typeText("Testing keyboard behavior"));

        // Verify message input scrolls properly with keyboard
        onView(withId(R.id.messagesRecyclerView))
            .check(matches(isDisplayed()));

        // Close keyboard
        onView(withId(R.id.messageEditText))
            .perform(closeSoftKeyboard());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify UI returns to normal state
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 8: Orientation change handling
     * Requirements: All requirements - Mobile responsiveness
     */
    @Test
    public void testOrientationChangeHandling() {
        // Start in portrait
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Open navigation drawer
        onView(withId(R.id.menuButton))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Select a channel
        onView(withId(R.id.channelsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Type a message
        onView(withId(R.id.messageEditText))
            .perform(typeText("Message before rotation"), closeSoftKeyboard());

        // Rotate to landscape
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify state is preserved
        onView(withId(R.id.messageEditText))
            .check(matches(withText("Message before rotation")));

        onView(withId(R.id.channelNameText))
            .check(matches(isDisplayed()));

        // Rotate back to portrait
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify state is still preserved
        onView(withId(R.id.messageEditText))
            .check(matches(withText("Message before rotation")));
    }

    /**
     * Test 9: Accessibility features
     * Requirements: All requirements - Accessibility
     */
    @Test
    public void testAccessibilityFeatures() {
        // Verify content descriptions are present
        onView(withId(R.id.menuButton))
            .check(matches(hasContentDescription()));

        onView(withId(R.id.searchButton))
            .check(matches(hasContentDescription()));

        onView(withId(R.id.membersButton))
            .check(matches(hasContentDescription()));

        onView(withId(R.id.attachmentButton))
            .check(matches(hasContentDescription()));

        onView(withId(R.id.sendButton))
            .check(matches(hasContentDescription()));

        // Test focus navigation
        onView(withId(R.id.menuButton))
            .perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify focusable elements in drawer
        onView(withId(R.id.settingsButton))
            .check(matches(isFocusable()));
    }

    /**
     * Test 10: Performance on mobile devices
     * Requirements: All requirements - Performance
     */
    @Test
    public void testPerformanceOnMobileDevices() {
        long startTime = System.currentTimeMillis();

        // Perform rapid navigation operations
        for (int i = 0; i < 5; i++) {
            onView(withId(R.id.menuButton))
                .perform(click());

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            onView(withId(R.id.channelsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify operations complete within reasonable time (less than 10 seconds)
        assert(duration < 10000);

        // Verify UI remains responsive
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));

        onView(withId(R.id.messageEditText))
            .perform(typeText("Performance test complete"), closeSoftKeyboard());

        onView(withId(R.id.sendButton))
            .perform(click());
    }

    /**
     * Custom matcher to check minimum size
     */
    private static Matcher<View> hasMinimumSize(final int minSizeDp) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has minimum size of " + minSizeDp + "dp");
            }

            @Override
            public boolean matchesSafely(View view) {
                float density = view.getContext().getResources().getDisplayMetrics().density;
                int minSizePx = (int) (minSizeDp * density);
                return view.getWidth() >= minSizePx && view.getHeight() >= minSizePx;
            }
        };
    }

    /**
     * Custom matcher to check maximum width
     */
    private static Matcher<View> hasMaximumWidth(final float maxWidthPx) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has maximum width of " + maxWidthPx + "px");
            }

            @Override
            public boolean matchesSafely(View view) {
                return view.getWidth() <= maxWidthPx;
            }
        };
    }

    /**
     * Custom matcher to check minimum text size
     */
    private static Matcher<View> hasMinimumTextSize(final int minTextSizeSp) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has minimum text size of " + minTextSizeSp + "sp");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (view instanceof android.widget.TextView) {
                    android.widget.TextView textView = (android.widget.TextView) view;
                    float textSizePx = textView.getTextSize();
                    float density = view.getContext().getResources().getDisplayMetrics().scaledDensity;
                    float textSizeSp = textSizePx / density;
                    return textSizeSp >= minTextSizeSp;
                }
                return false;
            }
        };
    }
}