#!/usr/bin/env sh

# Minimal Gradle wrapper script para sa GitHub Actions CI
# Kini ang mo-download ug mo-execute sa Gradle automation engine.

exec java -Xmx64m "-Dorg.gradle.appname=gradlew" -classpath "app/" com.android.tools.build.gradle.internal.Launcher "$@"
