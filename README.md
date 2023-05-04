# CSVDLTool
English | [Japanese](https://github.com/Contrast-Security-OSS/CSVDLTool/blob/main/README.ja.md)

`CSVDLTool` is a tool to help users who use Contrast.  
Users can export a lot of information from TeamServer, but with this tool even more information can be exported in CSV format.  

## System Requirements
Windows8.1、Windows10, MacOS11(Big Sur) or higher  
jre1.8.0_202

## Installation
There are several binaries on [Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases) page. You can use CSVDLTool without build process.  
- Windows
  - CSVDLTool_X.X.X.zip  
    If you are downloading for the first time, please download this binary.  
    As jre folder(1.8.0_202) is included, you can use `CSVDLTool` without installing JRE.  
  - CSVDLTool_X.X.X.exe  
    If you have already downloaded the zip, you can update CSVDLTool just by replacing the exe file.
- Mac
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
Authentication by Token authentication version is not recorded in audit log, but Password authentication version is recorded in audit log.  
Select which tool to use according to your situation.

## Usage
- contrast_security.yamlをエージェントのDLウィザードからDLしてexeと同じ場所に配置してください。（任意）
  任意というのは、yamlから読み込まなくても、あとで全て手入力でも可能だからです。
- 設定画面で、TeamServerのURL、ユーザ名、サービスキーを個人のものに変更してください。
- 組織情報の追加ボタンで組織を追加してください。
  組織は複数登録が可能です。CSVレポートを取得する対象の組織にチェックを入れてください。
  ※ 必要に応じて、プロキシ設定も行っておいてください。
- 設定を閉じて、アプリの読み込みをして、アプリケーション一覧をロードします。
- 脆弱性のCSVを取得するアプリをダブルクリックなり矢印で選択して右のボックスに移動します。（これが対象となります）
- 取得ボタンを押下します。成功するとexeと同じ場所にcsvファイルが出力されています。

使用方法の詳細については [Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases) からダウンロードできるzipファイルに同梱のマニュアルpdfをご確認ください。
## Build
### build.gradle
Modify the following line of build.gradle according to your environment.
- Windows 64bit
  ```gradle
  compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
  //compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  //compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
  ```
- Windows 32bit
  ```gradle
  //compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
  compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  //compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
  ```
- MacOS
  ```gradle
  //compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
  //compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
  ```

### Build and Export Jar
- Windows
  ```powershell
  gradlew clean jar
  ```
- Mac
  ```bash
  ./gradlew clean jar
  ```
jar file will be created under `build\libs`.

### Build and Run on Eclipse IDE
- Windows
  ```powershell
  gradlew cleanEclipse eclipse
  ```
- Mac
  ```bash
  ./gradlew cleanEclipse eclipse
  ```
Refresh your project in Eclipse. Clean and Build if necessary.  
Specify `com.contrastsecurity.csvdltool.Main` class and execute Java. CSVDLTool will launch.

## Distribution

### Windows配布用のexe化について
- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- jreがインストールされていない環境でも、jreフォルダを同梱することで環境に依存せずjavaを実行できるような設定になっています。  
  jreをDLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定に既になっています。
- 32bit版Javaにしている理由ですが、今はもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。

### Mac配布用のapp化について
- javapackagerを使っています。
- jreを同梱させるため、実施するMacに1.8.0_202のJREフォルダを任意の場所に配置しておいてください。
- jarpackage.sh内の3〜7行目を適宜、修正してください。
- jarpackage.shを実行します。
  ```bash
  ./jarpackage.sh
  ./jarpackage_auditlog.sh
  ```
  build/libs/bundle下にappフォルダが作られます。

### exe, appへの署名について
まず、証明書ファイル(pfx)と証明書パスワードを入手してください。  
署名についは以下の手順で実行してください。  
- Windows  
  - エイリアスの確認
    ```powershell
    keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx
    # 証明書パスワードを入力
    ```
  - 署名  
    launch4jのsign4jを使用します。
    ```powershell
    cd C:\Program Files (x86)\launch4j\sign4j
    sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx --storepass [パスワード] C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_1.9.2.exe
    ```
  - 署名の確認  
    署名の確認については、exeを右クリック->プロパティ で確認できます。
- Mac
  - 証明書ファイルの読み込み  
    pfxファイルをダブルクリックでキーチェーンアクセス.appに読み込ませます。証明書パスワード入力が必要  
    読み込めたら、Common Name(通称)をコピー
  - 署名
    ```bash
    codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_1.9.2.app
    codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_1.9.2_auditlog.app
    ```
  - 署名の確認
    ```bash
    codesign -d --verbose=4 CSVDLTool_1.9.2.app
    codesign -d --verbose=4 CSVDLTool_1.9.2_auditlog.app
    ```
    
### 圧縮について補足
- Mac
  ```bash
  7z a CSVDLTool_1.9.2.cli7z CSVDLTool_1.9.2.app/
  7z a CSVDLTool_1.9.2_auditlog.cli7z CSVDLTool_1.9.2_auditlog.app/
  ```

