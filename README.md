# CSVDLTool [![GitHub](https://img.shields.io/github/license/Contrast-Security-OSS/CSVDLTool?color=blightgreen)](LICENSE) [![GitHub release (latest by date)](https://img.shields.io/github/v/release/Contrast-Security-OSS/CSVDLTool)](https://github.com/Contrast-Security-OSS/CSVDLTool/releases/latest)
English | [Japanese](https://github.com/Contrast-Security-OSS/CSVDLTool/blob/main/README.ja.md)

`CSVDLTool` is a tool to help users who use Contrast.  
Users can export a lot of information from TeamServer, but with this tool even more information can be exported in CSV format.  

## System Requirements
#### OS
- Windows8.1, 10, 11
- macOS 11(Big Sur) or higher
#### Runtime  
- jdk17

## Installation
There are several binaries on [Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases/latest) page. You can use CSVDLTool without build process.  
#### Windows
- CSVDLTool_X.X.X.zip  
  If you are downloading for the first time, please download this binary.  
  As jre folder(1.8.0_202) is included, you can use `CSVDLTool` without installing JRE.  
- CSVDLTool_X.X.X.exe  
  If you have already downloaded the zip, you can update CSVDLTool just by replacing the exe file.
#### macOS
- CSVDLTool_X.X.X.cli7z  
  Unzip this file with the command below.  
  ```bash
  # Installing p7zip
  brew install p7zip
  # Unzip command
  7z x CSVDLTool_X.X.X.cli7z
  ```
#### About AuditLog Version
As binaries above are Token authentication version.  
Binaries which contains `auditlog` in file name are Password authentication version.  
Authentication by Token authentication version is not recorded in audit log[^1], but Password authentication version is recorded in audit log.  
Select which tool to use according to your situation.

[^1]:Except for some environments.

## Usage
1. Set the Team Server URL, username and service key in `General` preference page.
1. Add organization.  
  You can regist multiple oraganition.  
  *Note: If needed connection settings(e.g. use proxy), complete the settings in `Connection` preference page.*

After initial setting, you can export data at ASSESS, PROTECT and SERVER tabs.

## Building of CSVDLTool
### Building with build.gradle
Modify the following line of `build.gradle` according to your environment.
#### Windows 64bit
```gradle
compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### Windows 32bit
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### macOS
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```

### Build and Export Jar
#### Windows
```powershell
gradlew clean jar
```
#### macOS
```bash
./gradlew clean jar
```
jar file will be created under `build\libs`.

### Build and Run on Eclipse IDE
#### Windows
```powershell
gradlew cleanEclipse eclipse
```
#### macOS
```bash
./gradlew cleanEclipse eclipse
```
Refresh your project in Eclipse. Clean and Build if necessary.  
Specify `com.contrastsecurity.csvdltool.Main` class and execute Java. CSVDLTool will launch.

## Distribution
### Convert jar to executable binary
#### Windows (jar to exe)
- Using [launch4j](https://launch4j.sourceforge.net/).
- We prepare ready-to-use configuration file for launch4j.  
  - Correct the path according to your environment.  
  - Setting to bundle a jre folder to exe file is already enabled.  

#### macOS (jar to app)
1. Using javapackager.
1. If bundle jre, place the jre folder anywhere on the file system.
1. If needed, correct lines 3 to 7 in jarpackage.sh.
1. run `jarpackage.sh`.  
    ```bash
    ./jarpackage.sh
    ./jarpackage_auditlog.sh
    ```
    app folder will be created under `build\libs/bundle`.

### Digital Signature
First of all, get the certificate file(pfx) and the certificate password.    
#### Windows
- Check arias
  ```powershell
  keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx
  # Enter a certificate password.
  ```
- Sign  
  Using sign4j in launch4j.  
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx --storepass [] C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.1.6.exe
  ```
- Confirm Digital Signatures  
  You can confirm the signature in the properties of the exe file.
#### macOS
- Install certifiate to your PC.  
  Double-click the pfx file to install into KeychainAccess. At that time, certificate password is needed.  
  After installation, copy the `Common Name`.
- Sign  
  Using codesign.  
  ```bash
  codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_2.1.6.app
  codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_2.1.6_auditlog.app
  ```
- Confirm Digital Signatures
  ```bash
  codesign -d --verbose=4 CSVDLTool_2.1.6.app
  codesign -d --verbose=4 CSVDLTool_2.1.6_auditlog.app
  ```
    
#### How to compress
- Windows  
  Using 7-Zip.
- macOS
  ```bash
  # Installing p7zip
  brew install p7zip
  # Compress
  7z a CSVDLTool_2.1.6.cli7z CSVDLTool_2.1.6.app/
  7z a CSVDLTool_2.1.6_auditlog.cli7z CSVDLTool_2.1.6_auditlog.app/
  ```

