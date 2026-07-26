@echo off
setlocal
set "APP_HOME=%~dp0"

if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_CMD=java.exe"
)

"%JAVA_CMD%" "%APP_HOME%gradle\wrapper\GradleBootstrap.java" "%APP_HOME%" %*
exit /b %ERRORLEVEL%
