@echo off
echo ========================================
echo    Gatherly Android App Installer
echo ========================================
echo.
echo APK Location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo INSTALLATION METHODS:
echo.
echo Method 1 - Manual Installation:
echo 1. Connect your phone via USB
echo 2. Copy app-debug.apk to your phone's Downloads folder
echo 3. On your phone, open File Manager
echo 4. Go to Downloads and tap app-debug.apk
echo 5. Tap Install
echo.
echo Method 2 - ADB Installation (if ADB is installed):
echo adb install app\build\outputs\apk\debug\app-debug.apk
echo.
echo ========================================
echo Make sure USB Debugging is enabled!
echo ========================================
pause