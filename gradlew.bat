@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set DIRNAME=%DIRNAME:~0,-1%

@rem Find java.exe
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
@rem Setup the command line
set WRAPPER_JAR=%DIRNAME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%WRAPPER_JAR%" (
    echo ERROR: gradle-wrapper.jar not found. Open the project in Android Studio and use File ^> Sync Project with Gradle Files to generate it.
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
