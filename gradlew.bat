@echo off
setlocal
set GRADLE_VERSION=8.13
set DIST_ROOT=%USERPROFILE%\.gradle\auralis-bootstrap
set DIST_DIR=%DIST_ROOT%\gradle-%GRADLE_VERSION%
set ZIP_FILE=%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip
if not exist "%DIST_DIR%\bin\gradle.bat" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  echo Downloading Gradle %GRADLE_VERSION% for Auralis Player...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'; Expand-Archive -LiteralPath '%ZIP_FILE%' -DestinationPath '%DIST_ROOT%' -Force"
  if errorlevel 1 exit /b 1
)
call "%DIST_DIR%\bin\gradle.bat" %*
endlocal
