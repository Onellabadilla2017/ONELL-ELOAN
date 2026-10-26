@rem Minimal Windows Gradle wrapper script para sa GitHub Actions CI
@off

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

java -Xmx64m "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "app/" com.android.tools.build.gradle.internal.Launcher %*
