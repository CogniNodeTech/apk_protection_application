@echo off
REM SafeGuard - Build and Run Interface
REM Run this from project root: d:\apk_prevention

set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

REM Check for Android SDK
set "SDK_PATH="
if defined ANDROID_HOME set "SDK_PATH=%ANDROID_HOME%"
if defined ANDROID_SDK_ROOT set "SDK_PATH=%ANDROID_SDK_ROOT%"
if not "%SDK_PATH%"=="" (
    if exist "%SDK_PATH%\platform-tools\adb.exe" (
        echo [OK] Android SDK found: %SDK_PATH%
    ) else (
        set "SDK_PATH="
    )
)

if "%SDK_PATH%"=="" (
    if exist "local.properties" (
        for /f "tokens=2 delims==" %%a in ('findstr "sdk.dir" local.properties') do set SDK_PATH=%%a
        set SDK_PATH=%SDK_PATH:\=\%
    )
)

if "%SDK_PATH%"=="" (
    echo.
    echo [SETUP REQUIRED] Android SDK not found.
    echo.
    echo Option 1: Install Android Studio from https://developer.android.com/studio
    echo            Then open this folder in Android Studio - it will set sdk.dir in local.properties
    echo.
    echo Option 2: Edit local.properties in this folder and set sdk.dir to your SDK path, e.g.:
    echo            sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
    echo.
    echo Then run this script again: run.bat
    echo.
    pause
    exit /b 1
)

echo.
echo Building SafeGuard (debug)...
echo.

call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo BUILD FAILED. Check errors above. Fix SDK path in local.properties if needed.
    pause
    exit /b 1
)

echo.
echo ========== BUILD SUCCESS ==========
echo.
echo APK output: app\build\outputs\apk\debug\app-debug.apk
echo.
echo To install on connected device/emulator, run:
echo   gradlew.bat installDebug
echo.
pause
exit /b 0
