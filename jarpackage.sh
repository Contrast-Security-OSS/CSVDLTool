#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home"
BUNDLE_JRE_HOME="/Users/turbou/Downloads/jre1.8.0_202.jre"
APP_NAME="CSVDLTool"
APP_VERSION="2.1.0"
APP_DIR_NAME="${APP_NAME}_${APP_VERSION}.app"

javapackager \
  -deploy -Bruntime= \
  -native image \
  -srcdir ./build/libs \
  -srcfiles ${APP_NAME}-${APP_VERSION}.jar \
  -outdir  ./build/libs \
  -outfile ${APP_DIR_NAME} \
  -appclass com.contrastsecurity.csvdltool.Main \
  -name "${APP_NAME}_${APP_VERSION}" \
  -title "${APP_NAME}" \
  -BjvmOptions=-XstartOnFirstThread \
  -BjvmOptions=-Xms256m \
  -BjvmOptions=-Xmx512m \
  -vendor "Contrast Security Japan G.K." \
  -Bicon=src/main/resources/csvdltool.icns \
  -Bmac.CFBundleVersion=${APP_VERSION} \
  -nosign

echo ""
echo "If that succeeded, it created \"build/libs/bundles/${APP_DIR_NAME}\""

exit 0
