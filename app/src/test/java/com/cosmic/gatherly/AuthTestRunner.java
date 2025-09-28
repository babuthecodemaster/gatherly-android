package com.cosmic.gatherly;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite runner for all authentication tests
 * This class runs all authentication-related tests in sequence
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AuthenticationFlowTest.class,
    AuthValidationTest.class,
    AuthErrorHandlingTest.class,
    AuthNetworkTest.class
})
public class AuthTestRunner {
    // This class remains empty, it is used only as a holder for the above annotations
}