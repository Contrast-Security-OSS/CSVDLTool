#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home"
BUNDLE_JRE_HOME="/Users/turbou/Downloads/jre1.8.0_202.jre"
APP_NAME="CSVDLTool"
APP_VERSION="2.1.3"
APP_DIR_NAME="${APP_NAME}_${APP_VERSION}_auditlog.app"

javapackager \
  -deploy -Bruntime=${BUNDLE_JRE_HOME} \
  -native image \
  -srcdir ./build/libs \
  -srcfiles ${APP_NAME}-${APP_VERSION}.jar \
  -outdir  ./build/libs \
  -outfile ${APP_DIR_NAME} \
  -appclass com.contrastsecurity.csvdltool.Main \
  -name "${APP_NAME}_${APP_VERSION}_auditlog" \
  -title "${APP_NAME}" \
  -BjvmOptions=-XstartOnFirstThread \
  -BjvmOptions=-Xms256m \
  -BjvmOptions=-Xmx512m \
  -BjvmProperties=auth=password \
  -vendor "Contrast Security Japan G.K." \
  -Bicon=src/main/resources/csvdltool.icns \
  -Bmac.CFBundleVersion=${APP_VERSION} \
  -nosign \
  -v

echo ""
echo "If that succeeded, it created \"build/libs/bundles/${APP_DIR_NAME}\""

exit 0
