#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
APP_NAME="CSVDLTool"
APP_VERSION="2.1.0"

jpackage \
  --name ${APP_NAME}-${APP_VERSION}-aarch64 \
  --mac-package-name ${APP_NAME} \
  --type "app-image" \
  --input build/libs \
  --dest build/ \
  --main-jar ${APP_NAME}-${APP_VERSION}.jar \
  --main-class com.contrastsecurity.csvdltool.Main \
  --java-options -XstartOnFirstThread \
  --java-options -Xms256m \
  --java-options -Xmx512m \
  --java-options "--add-opens java.base/java.time.format=ALL-UNNAMED" \
  --java-options "--add-opens java.base/java.util.regex=ALL-UNNAMED" \
  --icon src/main/resources/csvdltool.icns \
  --vendor "Contrast Security Japan G.K." \
  --copyright "Copyright (c) 2020 Contrast Security Japan G.K."

exit 0
