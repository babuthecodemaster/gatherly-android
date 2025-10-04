#!/usr/bin/env pwsh

Write-Host "Starting UI/UX Integration Tests..." -ForegroundColor Green
Write-Host ""

Write-Host "Cleaning project..." -ForegroundColor Yellow
& ./gradlew clean

Write-Host ""
Write-Host "Building debug APK..." -ForegroundColor Yellow
& ./gradlew assembleDebug

Write-Host ""
Write-Host "Building test APK..." -ForegroundColor Yellow
& ./gradlew assembleDebugAndroidTest

Write-Host ""
Write-Host "Running UI Workflow Integration Tests..." -ForegroundColor Yellow
& ./gradlew connectedDebugAndroidTest --tests "com.cosmic.gatherly.UIWorkflowIntegrationTestSuite"

Write-Host ""
Write-Host "Integration tests completed!" -ForegroundColor Green
Write-Host "Check the test results in: app/build/reports/androidTests/connected/index.html" -ForegroundColor Cyan

Read-Host "Press Enter to continue..."