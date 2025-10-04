package com.cosmic.gatherly;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all UI/UX improvements integration tests
 * Tests Requirements: All requirements validation from ui-ux-improvements spec
 * 
 * This suite runs all integration tests for the UI/UX improvements feature:
 * - Channel switching workflow tests
 * - File upload end-to-end process tests  
 * - Search functionality tests with real data
 * - Server switching with visual indicators tests
 * - Responsive behavior on mobile devices tests
 * - Authentication integration tests (existing)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    UIWorkflowIntegrationTest.class,
    FileUploadWorkflowTest.class,
    SearchWorkflowTest.class,
    ResponsiveBehaviorTest.class,
    AuthenticationIntegrationTest.class,
    AuthenticationUITest.class
})
public class UIWorkflowIntegrationTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}