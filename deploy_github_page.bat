@echo off
setlocal DisableDelayedExpansion
title ANALOG TV 3.0 CAMCORDER - GITHUB ACTIONS & PAGES FIX (f1nal-b0ss)
color 0A

set "GH_USER=f1nal-b0ss"
set "REPO_NAME=AnalogTV-Android-Camcorder"
set "REPO_URL=https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder.git"
set "PAGES_URL=https://f1nal-b0ss.github.io/AnalogTV-Android-Camcorder/"

echo ============================================================================
echo   ANALOG TV 3.0 CAMCORDER - GITHUB ACTIONS APK BUILD ^& PAGES FIX (v3)
echo   Account    : https://github.com/%GH_USER%
echo   Repository : https://github.com/%GH_USER%/%REPO_NAME%
echo   Pages URL  : %PAGES_URL%
echo ============================================================================
echo.

REM --- STEP 1: Extract ZIP if present and flatten any old nested folder ---
if not exist "analog_tv_android_studio.zip" goto :CHECK_NESTED
echo [1/6] Extracting analog_tv_android_studio.zip into current folder...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path 'analog_tv_android_studio.zip' -DestinationPath '.' -Force"

:CHECK_NESTED
if not exist "AnalogTV-Android-Camcorder" goto :STEP_FIX_CI
echo [1b/6] Flattening old nested AnalogTV-Android-Camcorder subfolder...
if exist "AnalogTV-Android-Camcorder\README.md" xcopy /E /Y /I "AnalogTV-Android-Camcorder\*" "." >nul
rmdir /S /Q "AnalogTV-Android-Camcorder"

:STEP_FIX_CI
echo [2/6] Applying GitHub Actions SDK License Fix, Node 24 / setup-java@v5 ^& Gradle fixes...
if not exist ".github" mkdir ".github"
if not exist ".github\workflows" mkdir ".github\workflows"

echo name: Build ^& Release Android Camcorder APK> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo on:>> ".github\workflows\build-apk.yml"
echo   push:>> ".github\workflows\build-apk.yml"
echo     branches: [ "main", "master" ]>> ".github\workflows\build-apk.yml"
echo   pull_request:>> ".github\workflows\build-apk.yml"
echo     branches: [ "main", "master" ]>> ".github\workflows\build-apk.yml"
echo   workflow_dispatch:>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo env:>> ".github\workflows\build-apk.yml"
echo   FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo permissions:>> ".github\workflows\build-apk.yml"
echo   contents: write>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo jobs:>> ".github\workflows\build-apk.yml"
echo   build-apk:>> ".github\workflows\build-apk.yml"
echo     runs-on: ubuntu-latest>> ".github\workflows\build-apk.yml"
echo     steps:>> ".github\workflows\build-apk.yml"
echo       - name: Checkout Repository>> ".github\workflows\build-apk.yml"
echo         uses: actions/checkout@v4>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Set up JDK 17>> ".github\workflows\build-apk.yml"
echo         uses: actions/setup-java@v5>> ".github\workflows\build-apk.yml"
echo         with:>> ".github\workflows\build-apk.yml"
echo           distribution: 'temurin'>> ".github\workflows\build-apk.yml"
echo           java-version: '17'>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Accept Android SDK Licenses ^& Configure SDK 34>> ".github\workflows\build-apk.yml"
echo         run: ^|>> ".github\workflows\build-apk.yml"
echo           ANDROID_SDK_DIR="${ANDROID_HOME:-/usr/local/lib/android/sdk}">> ".github\workflows\build-apk.yml"
echo           mkdir -p "$ANDROID_SDK_DIR/licenses">> ".github\workflows\build-apk.yml"
echo           printf "\n8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e\n24333f8a63b6825ea9c5514f83c2829b004d1fee\n84831b9409646a918e30573bab4c9c91346d8abd\n" ^> "$ANDROID_SDK_DIR/licenses/android-sdk-license">> ".github\workflows\build-apk.yml"
echo           printf "\n84831b9409646a918e30573bab4c9c91346d8abd\n504667f4c0de7af1a06de9f4b1727b84351f2910\n" ^> "$ANDROID_SDK_DIR/licenses/android-sdk-preview-license">> ".github\workflows\build-apk.yml"
echo           printf "\n601085b94cd77f0b54ff86406957099ebe79c4d6\n" ^> "$ANDROID_SDK_DIR/licenses/android-googletv-license">> ".github\workflows\build-apk.yml"
echo           printf "\n33b6a2b64607f11b759f320ef9dff4ae5c47d97a\n" ^> "$ANDROID_SDK_DIR/licenses/google-gdk-license">> ".github\workflows\build-apk.yml"
echo           SDKMANAGER=$(find "$ANDROID_SDK_DIR/cmdline-tools" -name sdkmanager 2^>/dev/null ^| sort -V ^| tail -n 1)>> ".github\workflows\build-apk.yml"
echo           if [ -n "$SDKMANAGER" ]; then>> ".github\workflows\build-apk.yml"
echo             yes ^| "$SDKMANAGER" --licenses ^> /dev/null 2^>^&1 ^|^| true>> ".github\workflows\build-apk.yml"
echo             "$SDKMANAGER" --install "platforms;android-34" "build-tools;34.0.0" ^> /dev/null 2^>^&1 ^|^| true>> ".github\workflows\build-apk.yml"
echo           fi>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Setup Gradle 8.7>> ".github\workflows\build-apk.yml"
echo         uses: gradle/actions/setup-gradle@v4>> ".github\workflows\build-apk.yml"
echo         with:>> ".github\workflows\build-apk.yml"
echo           gradle-version: '8.7'>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Build Debug APK>> ".github\workflows\build-apk.yml"
echo         run: gradle assembleDebug --no-daemon --stacktrace>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Prepare APK for Download>> ".github\workflows\build-apk.yml"
echo         run: cp app/build/outputs/apk/debug/app-debug.apk AnalogTV-Camcorder-v3.0-debug.apk>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Upload Compiled APK Artifact>> ".github\workflows\build-apk.yml"
echo         uses: actions/upload-artifact@v4>> ".github\workflows\build-apk.yml"
echo         with:>> ".github\workflows\build-apk.yml"
echo           name: AnalogTV-Camcorder-v3.0-APK>> ".github\workflows\build-apk.yml"
echo           path: AnalogTV-Camcorder-v3.0-debug.apk>> ".github\workflows\build-apk.yml"
echo.>> ".github\workflows\build-apk.yml"
echo       - name: Publish GitHub Release on Push>> ".github\workflows\build-apk.yml"
echo         if: github.event_name == 'push'>> ".github\workflows\build-apk.yml"
echo         continue-on-error: true>> ".github\workflows\build-apk.yml"
echo         uses: softprops/action-gh-release@v2>> ".github\workflows\build-apk.yml"
echo         with:>> ".github\workflows\build-apk.yml"
echo           tag_name: v3.0.${{ github.run_number }}>> ".github\workflows\build-apk.yml"
echo           name: "AnalogTV 3.0 Camcorder for Android Build #${{ github.run_number }}">> ".github\workflows\build-apk.yml"
echo           body: "Automated Android APK build of AnalogTV 3.0 Live VHS Camcorder.">> ".github\workflows\build-apk.yml"
echo           files: AnalogTV-Camcorder-v3.0-debug.apk>> ".github\workflows\build-apk.yml"

REM Write gradle.properties (required for AndroidX)
echo org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8> "gradle.properties"
echo android.useAndroidX=true>> "gradle.properties"
echo android.nonTransitiveRClass=true>> "gradle.properties"
echo kotlin.code.style=official>> "gradle.properties"

REM Fix AndroidManifest.xml screenOrientation attribute casing for AAPT2
if not exist "app\src\main" mkdir "app\src\main"
echo ^<?xml version="1.0" encoding="utf-8"?^>> "app\src\main\AndroidManifest.xml"
echo ^<manifest xmlns:android="http://schemas.android.com/apk/res/android"^>>> "app\src\main\AndroidManifest.xml"
echo     ^<uses-feature android:glEsVersion="0x00030000" android:required="true" /^>>> "app\src\main\AndroidManifest.xml"
echo     ^<uses-feature android:name="android.hardware.camera.any" android:required="false" /^>>> "app\src\main\AndroidManifest.xml"
echo     ^<uses-permission android:name="android.permission.CAMERA" /^>>> "app\src\main\AndroidManifest.xml"
echo     ^<uses-permission android:name="android.permission.RECORD_AUDIO" /^>>> "app\src\main\AndroidManifest.xml"
echo     ^<application android:allowBackup="true" android:label="AnalogTV 3.0 Camcorder" android:supportsRtl="true" android:theme="@android:style/Theme.Material.NoActionBar"^>>> "app\src\main\AndroidManifest.xml"
echo         ^<activity android:name=".MainActivity" android:exported="true" android:screenOrientation="unspecified" android:configChanges="orientation|screenSize|keyboardHidden"^>>> "app\src\main\AndroidManifest.xml"
echo             ^<intent-filter^>>> "app\src\main\AndroidManifest.xml"
echo                 ^<action android:name="android.intent.action.MAIN" /^>>> "app\src\main\AndroidManifest.xml"
echo                 ^<category android:name="android.intent.category.LAUNCHER" /^>>> "app\src\main\AndroidManifest.xml"
echo             ^</intent-filter^>>> "app\src\main\AndroidManifest.xml"
echo         ^</activity^>>> "app\src\main\AndroidManifest.xml"
echo     ^</application^>>> "app\src\main\AndroidManifest.xml"
echo ^</manifest^>>> "app\src\main\AndroidManifest.xml"

REM Ensure Media3 @OptIn(UnstableApi::class) annotation in VideoConverterPipeline.kt
if not exist "app\src\main\java\com\retro\analogtv" mkdir "app\src\main\java\com\retro\analogtv"
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path 'app/src/main/java/com/retro/analogtv/VideoConverterPipeline.kt') { $c = Get-Content -Raw 'app/src/main/java/com/retro/analogtv/VideoConverterPipeline.kt'; if ($c -notmatch 'UnstableApi') { $c = $c -replace 'class VideoConverterPipeline', '@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class) class VideoConverterPipeline'; Set-Content -Path 'app/src/main/java/com/retro/analogtv/VideoConverterPipeline.kt' -Value $c -Encoding UTF8 } }"
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path 'app/src/main/java/com/retro/analogtv/MainActivity.kt') { $c = Get-Content -Raw 'app/src/main/java/com/retro/analogtv/MainActivity.kt'; if ($c -notmatch 'ExperimentalMaterial3Api') { $c = '@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)' + [Environment]::NewLine + $c; Set-Content -Path 'app/src/main/java/com/retro/analogtv/MainActivity.kt' -Value $c -Encoding UTF8 } }"

echo [3/6] Ensuring interactive Live Demo and README are in root and /docs...
if not exist "docs" mkdir "docs"
if not exist "assets" mkdir "assets"
if exist "analog_tv_converter.html" copy /Y "analog_tv_converter.html" "docs\index.html" >nul
if exist "analog_tv_converter.html" copy /Y "analog_tv_converter.html" "index.html" >nul
if exist "banner.jpg" copy /Y "banner.jpg" "docs\banner.jpg" >nul
if exist "banner.jpg" copy /Y "banner.jpg" "assets\banner.jpg" >nul
type nul > "docs\.nojekyll"
type nul > ".nojekyll"

echo [4/6] Staging and committing updated workflow and Android files...
where git >nul 2>nul
if errorlevel 1 goto :NO_GIT

if not exist ".git" git init
git checkout -B main

echo *.iml> ".gitignore"
echo .gradle/>> ".gitignore"
echo local.properties>> ".gitignore"
echo .idea/>> ".gitignore"
echo .DS_Store>> ".gitignore"
echo build/>> ".gitignore"
echo app/build/>> ".gitignore"
echo captures/>> ".gitignore"
echo analog_tv_android_studio.zip>> ".gitignore"
echo deploy_github_page.bat>> ".gitignore"

git add -A
git commit -m "Fix GitHub Actions Android SDK licenses, setup-java@v5, gradle.properties, and AAPT2 manifest"

echo [5/6] Pushing fix to https://github.com/%GH_USER%/%REPO_NAME% ...
git remote remove origin >nul 2>nul
git remote add origin %REPO_URL%
git push -u origin main --force
if errorlevel 1 goto :PUSH_ERROR

echo.
echo ============================================================================
echo   GITHUB ACTIONS FIX PUSHED FOR @%GH_USER%!
echo   GitHub Actions : https://github.com/%GH_USER%/%REPO_NAME%/actions
echo   GitHub Repo    : https://github.com/%GH_USER%/%REPO_NAME%
echo   GitHub Pages   : %PAGES_URL%
echo ============================================================================
echo Opening GitHub Actions to watch your APK build succeed...
start "" "https://github.com/%GH_USER%/%REPO_NAME%/actions"
pause
exit /b 0

:NO_GIT
color 0C
echo [ERROR] Git is not installed or not in your PATH.
pause
exit /b 1

:PUSH_ERROR
color 0E
echo [WARNING] Git push encountered an issue. Make sure you are signed in to GitHub.
pause
exit /b 1
