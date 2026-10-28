@echo off
setlocal DisableDelayedExpansion
title AnalogTV 3.0 Android Camcorder - GitHub Pages and APK Deployer

REM ============================================================================
REM AnalogTV 3.0 Android Camcorder - Automated GitHub Pages & Repo Deployer
REM Target GitHub Account : https://github.com/f1nal-b0ss
REM Target Repository     : https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder
REM Target GitHub Pages   : https://f1nal-b0ss.github.io/AnalogTV-Android-Camcorder/
REM ============================================================================

set "GH_USER=f1nal-b0ss"
set "REPO_NAME=AnalogTV-Android-Camcorder"
set "BRANCH=main"
set "REPO_URL=https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder"
set "PAGES_URL=https://f1nal-b0ss.github.io/AnalogTV-Android-Camcorder/"

echo ============================================================================
echo   ANALOG TV 3.0 CAMCORDER - GITHUB PAGES AND APK DEPLOYER
echo   Account    : https://github.com/%GH_USER%
echo   Repository : %REPO_URL%
echo   Pages URL  : %PAGES_URL%
echo ============================================================================
echo.

REM --- Step 1: Check if running next to analog_tv_android_studio.zip ---
if exist "app\src\main\AndroidManifest.xml" goto :STEP1_ALREADY_EXTRACTED
if not exist "analog_tv_android_studio.zip" goto :STEP1_NO_ZIP
echo [1/6] Found analog_tv_android_studio.zip - extracting project files...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path 'analog_tv_android_studio.zip' -DestinationPath '.' -Force"
goto :STEP2

:STEP1_ALREADY_EXTRACTED
echo [1/6] Project files already extracted in current folder.
goto :STEP2

:STEP1_NO_ZIP
echo [1/6] Using current directory as project workspace...

:STEP2
REM --- Step 2: Ensure docs/ folder exists for GitHub Pages ---
echo [2/6] Preparing GitHub Pages /docs folder and repository metadata for %GH_USER%...
if not exist "docs" mkdir "docs"

REM Copy analog_tv_converter.html into docs/index.html if available in current folder
if not exist "analog_tv_converter.html" goto :CHECK_DOCS_INDEX
echo       Copying analog_tv_converter.html to docs\index.html...
copy /Y "analog_tv_converter.html" "docs\index.html" >nul

:CHECK_DOCS_INDEX
if exist "assets\banner.jpg" copy /Y "assets\banner.jpg" "docs\banner.jpg" >nul
if exist "docs\index.html" goto :CREATE_NOJEKYLL

echo       Generating fallback docs\index.html landing page...
>  "docs\index.html" echo ^<!DOCTYPE html^>
>> "docs\index.html" echo ^<html lang="en"^>^<head^>^<meta charset="UTF-8"^>
>> "docs\index.html" echo ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^>
>> "docs\index.html" echo ^<title^>AnalogTV 3.0 Camcorder - @%GH_USER%^</title^>
>> "docs\index.html" echo ^<style^>body{margin:0;background:#0c0c10;color:#ececf2;font-family:system-ui,sans-serif;text-align:center;padding:48px 20px}
>> "docs\index.html" echo .card{max-width:760px;margin:0 auto;background:#15161e;border:1px solid #2b2d3c;border-radius:16px;padding:36px;box-shadow:0 16px 48px rgba(0,0,0,.6)}
>> "docs\index.html" echo h1{color:#ff5500;letter-spacing:1px;margin-top:0}a.btn{display:inline-block;margin:10px;padding:12px 24px;border-radius:8px;background:#ff5500;color:#fff;text-decoration:none;font-weight:700}
>> "docs\index.html" echo a.btn.sec{background:#232533;border:1px solid #3c3f56}img{max-width:100%%;border-radius:10px;margin:18px 0}^</style^>^</head^>
>> "docs\index.html" echo ^<body^>^<div class="card"^>
>> "docs\index.html" echo ^<h1^>ANALOG TV 3.0 // LIVE VHS CAMCORDER^</h1^>
>> "docs\index.html" echo ^<p^>Real-Time Composite NTSC/PAL/SECAM Signal Simulation and Native Android OpenGL ES 2.0 APK by ^<strong^>@%GH_USER%^</strong^>.^</p^>
>> "docs\index.html" echo ^<img src="banner.jpg" alt="AnalogTV 3.0 Banner" onerror="this.style.display='none'"^>
>> "docs\index.html" echo ^<div^>^<a class="btn" href="%REPO_URL%/releases/latest"^>Download Latest Android APK^</a^>
>> "docs\index.html" echo ^<a class="btn sec" href="%REPO_URL%"^>View Source on GitHub @%GH_USER%^</a^>^</div^>
>> "docs\index.html" echo ^</div^>^</body^>^</html^>

:CREATE_NOJEKYLL
REM Create .nojekyll so GitHub Pages serves all static assets cleanly
type nul > "docs\.nojekyll"

REM --- Step 3: Verify Git is installed ---
echo [3/6] Checking Git installation...
where git >nul 2>nul
if errorlevel 1 goto :ERR_NO_GIT

REM Initialize Git repository and commit files
if not exist ".git" git init
git checkout -B %BRANCH%
git add -A
git diff --cached --quiet
if errorlevel 1 git commit -m "Deploy AnalogTV 3.0 Android Camcorder and GitHub Pages for %GH_USER%"

REM --- Step 4: Check GitHub CLI (gh) or fallback to Standard Git Push ---
echo [4/6] Configuring remote repository %REPO_URL%...
where gh >nul 2>nul
if errorlevel 1 goto :MANUAL_GIT_PUSH

echo       GitHub CLI detected. Checking authentication status...
gh auth status >nul 2>nul
if errorlevel 1 goto :GH_LOGIN
goto :GH_CREATE_AND_PUSH

:GH_LOGIN
echo       Please sign in to GitHub for account %GH_USER%...
gh auth login

:GH_CREATE_AND_PUSH
echo [5/6] Creating or updating repository %GH_USER%/%REPO_NAME% and pushing code...
gh repo view %GH_USER%/%REPO_NAME% >nul 2>nul
if errorlevel 1 goto :GH_REPO_CREATE
git remote remove origin >nul 2>nul
git remote add origin %REPO_URL%.git
git push -u origin %BRANCH% --force
goto :GH_ENABLE_PAGES

:GH_REPO_CREATE
gh repo create %GH_USER%/%REPO_NAME% --public --source=. --remote=origin --push

:GH_ENABLE_PAGES
echo [6/6] Enabling GitHub Pages from branch %BRANCH% /docs folder...
gh api -X POST repos/%GH_USER%/%REPO_NAME%/pages -f "source[branch]=%BRANCH%" -f "source[path]=/docs" >nul 2>nul
if errorlevel 1 gh api -X PUT repos/%GH_USER%/%REPO_NAME%/pages -f "source[branch]=%BRANCH%" -f "source[path]=/docs" >nul 2>nul
goto :DONE

:MANUAL_GIT_PUSH
echo       GitHub CLI not found - using standard Git remote push...
git remote remove origin >nul 2>nul
git remote add origin %REPO_URL%.git
echo.
echo [5/6] Pushing to %REPO_URL%.git ...
echo       NOTE: Make sure you have created an empty public repo named "%REPO_NAME%"
echo       at https://github.com/new under account %GH_USER%.
git push -u origin %BRANCH%
if errorlevel 1 goto :ERR_PUSH_FAILED

echo.
echo [6/6] Final Step to Activate GitHub Pages:
echo       1. Open %REPO_URL%/settings/pages
echo       2. Under "Build and deployment", set Source to "Deploy from a branch"
echo       3. Select branch "%BRANCH%" and folder "/docs", then click Save.
goto :DONE

:ERR_NO_GIT
echo [ERROR] Git is not installed or not in your PATH.
echo Please install Git for Windows from https://git-scm.com/download/win and re-run this script.
pause
exit /b 1

:ERR_PUSH_FAILED
echo.
echo [!] Push did not complete yet. If %REPO_URL% does not exist yet:
echo     1. Create a new empty repository named "%REPO_NAME%" at https://github.com/new
echo     2. Re-run this deploy_github_page.bat script.
pause
exit /b 1

:DONE
echo.
echo ============================================================================
echo   DEPLOYMENT COMPLETE FOR @%GH_USER%!
echo   GitHub Repo  : %REPO_URL%
echo   GitHub Pages : %PAGES_URL%
echo   APK Build    : %REPO_URL%/actions
echo ============================================================================
echo Opening your repository and GitHub Pages in default browser...
start "" "%REPO_URL%"
start "" "%PAGES_URL%"
pause
exit /b 0
