@echo off
echo Starting SafeGuard Backend Server...
cd /d "%~dp0server"

REM Check for Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found. Please install Python from python.org
    pause
    exit /b 1
)

REM Check for virtual environment
if not exist ".venv" (
    echo Creating virtual environment...
    python -m venv .venv
)

echo Activating virtual environment and installing dependencies...
call .venv\Scripts\activate
pip install -r requirements.txt

echo.
echo ==============================================
echo Server is starting on http://127.0.0.1:3000
echo Leave this window open while using the app!
echo ==============================================
echo.

uvicorn main:app --host 127.0.0.1 --port 3000
pause
