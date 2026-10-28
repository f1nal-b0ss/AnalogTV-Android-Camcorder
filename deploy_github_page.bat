@echo off
setlocal DisableDelayedExpansion
title ANALOG TV 3.0 CAMCORDER - GITHUB PAGES DEPLOYER (f1nal-b0ss)
color 0A

set "GH_USER=f1nal-b0ss"
set "REPO_NAME=AnalogTV-Android-Camcorder"
set "REPO_URL=https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder.git"
set "PAGES_URL=https://f1nal-b0ss.github.io/AnalogTV-Android-Camcorder/"

echo ============================================================================
echo   ANALOG TV 3.0 CAMCORDER - GITHUB PAGES DEPLOYER (v2 FIX)
echo   Account    : https://github.com/%GH_USER%
echo   Repository : https://github.com/%GH_USER%/%REPO_NAME%
echo   Pages URL  : %PAGES_URL%
echo ============================================================================
echo.

REM --- STEP 1: Extract ZIP and flatten any old nested subfolder ---
if not exist "analog_tv_android_studio.zip" goto :CHECK_NESTED
echo [1/6] Extracting updated analog_tv_android_studio.zip into current folder...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path 'analog_tv_android_studio.zip' -DestinationPath '.' -Force"

:CHECK_NESTED
if not exist "AnalogTV-Android-Camcorder" goto :STEP_DOCS
echo [1b/6] Cleaning up old nested AnalogTV-Android-Camcorder subfolder...
if exist "AnalogTV-Android-Camcorder\README.md" xcopy /E /Y /I "AnalogTV-Android-Camcorder\*" "." >nul
rmdir /S /Q "AnalogTV-Android-Camcorder"

:STEP_DOCS
echo [2/6] Installing full interactive Live Demo and README at repo root and /docs...
if not exist "docs" mkdir "docs"
if not exist "assets" mkdir "assets"

REM Always copy the full interactive web app into both docs\index.html and root index.html
if exist "analog_tv_converter.html" copy /Y "analog_tv_converter.html" "docs\index.html" >nul
if exist "analog_tv_converter.html" copy /Y "analog_tv_converter.html" "index.html" >nul
if not exist "analog_tv_converter.html" if exist "docs\index.html" copy /Y "docs\index.html" "index.html" >nul

REM Copy banner image to all expected locations
if exist "banner.jpg" copy /Y "banner.jpg" "docs\banner.jpg" >nul
if exist "banner.jpg" copy /Y "banner.jpg" "assets\banner.jpg" >nul
if exist "assets\banner.jpg" copy /Y "assets\banner.jpg" "docs\banner.jpg" >nul
if exist "assets\banner.jpg" copy /Y "assets\banner.jpg" "banner.jpg" >nul

REM Create .nojekyll in both root and /docs so GitHub Pages serves all assets cleanly
type nul > "docs\.nojekyll"
type nul > ".nojekyll"

REM Ensure README.md exists at root even if user only ran the .bat without the new zip
if exist "README.md" goto :STEP_GIT
echo # AnalogTV 3.0 — Live VHS ^& Composite CRT Camcorder for Android> "README.md"
echo.>> "README.md"
echo ![AnalogTV 3.0 Banner](assets/banner.jpg)>> "README.md"
echo.>> "README.md"
echo ## [Launch Live Interactive Web Camcorder ^& VHS Converter](%PAGES_URL%)>> "README.md"
echo.>> "README.md"
echo Real-time YIQ / NTSC composite video simulator and Android OpenGL ES 2.0 camcorder.>> "README.md"

:STEP_GIT
echo [3/6] Checking Git installation and staging all root files...
where git >nul 2>nul
if errorlevel 1 goto :NO_GIT

if not exist ".git" git init
git checkout -B main

if exist ".gitignore" goto :STAGE_FILES
echo *.iml> ".gitignore"
echo .gradle/>> ".gitignore"
echo local.properties>> ".gitignore"
echo .idea/>> ".gitignore"
echo .DS_Store>> ".gitignore"
echo build/>> ".gitignore"
echo app/build/>> ".gitignore"
echo captures/>> ".gitignore"
echo analog_tv_android_studio.zip>> ".gitignore"

:STAGE_FILES
git add -A
git commit -m "Fix root README, Android Studio project structure, and full interactive GitHub Pages live demo"

echo [4/6] Configuring remote repository https://github.com/%GH_USER%/%REPO_NAME%...
git remote remove origin >nul 2>nul
git remote add origin %REPO_URL%

echo.
echo [5/6] Force-pushing clean root structure to %REPO_URL% ...
git push -u origin main --force
if errorlevel 1 goto :PUSH_ERROR

REM If GitHub CLI is available, also auto-enable GitHub Pages on /docs
where gh >nul 2>nul
if errorlevel 1 goto :STEP_FINISH
gh api -X POST repos/%GH_USER%/%REPO_NAME%/pages -f "source[branch]=main" -f "source[path]=/docs" >nul 2>nul
gh api -X PUT repos/%GH_USER%/%REPO_NAME%/pages -f "source[branch]=main" -f "source[path]=/docs" >nul 2>nul

:STEP_FINISH
echo.
echo [6/6] Verify GitHub Pages Source:
echo       1. Open https://github.com/%GH_USER%/%REPO_NAME%/settings/pages
echo       2. Under Build and deployment, set Source to Deploy from a branch
echo       3. Select branch main and either /(root) or /docs (both now contain the full Live Demo!)
echo.
echo ============================================================================
echo   UPDATE COMPLETE FOR @%GH_USER%!
echo   GitHub Repo  : https://github.com/%GH_USER%/%REPO_NAME%
echo   GitHub Pages : %PAGES_URL%
echo   APK Build    : https://github.com/%GH_USER%/%REPO_NAME%/actions
echo ============================================================================
echo Opening your updated repository and Live Demo in default browser...
start "" "https://github.com/%GH_USER%/%REPO_NAME%"
start "" "%PAGES_URL%"
pause
exit /b 0

:NO_GIT
color 0C
echo [ERROR] Git is not installed or not in your PATH.
echo Please install Git from https://git-scm.com/download/win and run this script again.
pause
exit /b 1

:PUSH_ERROR
color 0E
echo.
echo [WARNING] Git push encountered an issue.
echo Make sure you are signed in to GitHub as %GH_USER%.
pause
exit /b 1
