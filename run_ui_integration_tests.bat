@echo off
echo Starting UI/UX Integration Tests...
echo.

echo Cleaning project...
call gradlew clean

echo.
echo Building debug APK...
call gradlew assembleDebug

echo.
echo Building test APK...
call gradlew assembleDebugAndroidTest

echo.
echo Running UI Workflow Integration Tests...
call gradlew connectedDebugAndroidTest --tests "com.cosmic.gatherly.UIWorkflowIntegrationTestSuite"

echo.
echo Integration tests completed!
echo Check the test results in: app/build/reports/androidTests/connected/index.html

pause