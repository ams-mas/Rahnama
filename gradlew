#!/bin/sh
# Gradle wrapper launcher. Use Android Studio's bundled Gradle if wrapper JAR has not been generated yet.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR is missing. Open the project in Android Studio and run: gradle wrapper --gradle-version 8.11.1"
  exit 1
fi
exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
