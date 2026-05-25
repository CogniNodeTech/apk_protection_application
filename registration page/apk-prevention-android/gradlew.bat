@rem
@rem Copyright 2015 the original author or authors.
@rem Licensed under the Apache License, Version 2.0

@if "%DEBUG%"=="" @echo off

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set DIRNAME=%DIRNAME:~0,-1%

if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:execute
set WRAPPER_JAR=%DIRNAME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%WRAPPER_JAR%" (
    echo ERROR: gradle-wrapper.jar not found at %WRAPPER_JAR%
    goto fail
)
set CLASSPATH=%WRAPPER_JAR%

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@if "%ERRORLEVEL%"=="0" goto mainEnd
:fail
exit /b 1
:mainEnd
exit /b 0
