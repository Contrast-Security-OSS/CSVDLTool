# CSVDLTool [![GitHub](https://img.shields.io/github/license/Contrast-Security-OSS/CSVDLTool?color=blightgreen)](LICENSE) [![GitHub release (latest by date)](https://img.shields.io/github/v/release/Contrast-Security-OSS/CSVDLTool)](https://github.com/Contrast-Security-OSS/CSVDLTool/releases/latest)
[English](https://github.com/Contrast-Security-OSS/CSVDLTool/blob/main/README.md) | Japanese

TeamServerに登録されている脆弱性情報及びライブラリ情報のCSV形式レポートを取得するツールです。  
TeamServerから直接CSVレポートを出力することも出来ますが、本ツールではより多くの情報を取得したり  
その情報をカスタマイズしたりすることが出来ます。  

## 動作環境
#### OS
- Windows8.1、 10, 11
- macOS 11(Big Sur)かそれ以上

#### Runtime
- jre1.8

## インストール
[Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases/latest) で幾つかのバイナリを提供しています。ビルド不要でダウンロード後すぐに使用できます。  
#### Windows
- CSVDLTool_X.X.X.zip  
  初回ダウンロードの場合はこちらをダウンロードして解凍して、お使いください。  
  jreフォルダ（1.8.0_202）が同梱されているため、exeの起動ですぐにツールを使用できます。
- CSVDLTool_X.X.X.exe  
  既にzipをダウンロード済みの場合はexeのダウンロードと入れ替えのみでツールを使用できます。
#### macOS
- CSVDLTool_X.X.X.cli7z  
  下記コマンドで解凍してください。  
  ```bash
  # p7zipのインストールについては
  brew install p7zip
  # 解凍コマンド
  7z x CSVDLTool_X.X.X.cli7z
  ```
#### 監査ログ版について
上記のバイナリはトークン認証版となります。ファイル名に`auditlog`が含まれるバイナリはパスワード認証版となります。  
トークン認証版では認証時にサーバ側の監査ログに記録されません[^1]。 ですがパスワード認証版では監査ログに認証時の記録が行われます。  
ご使用環境に応じてバイナリを選択してください。
[^1]: 一部環境を除きます。

## 使用方法
- 設定画面で、TeamServerのURL、ユーザ名、サービスキーを個人のものに変更してください。
- 組織情報の追加ボタンで組織を追加してください。
  組織は複数登録が可能です。CSVレポートを取得する対象の組織にチェックを入れてください。
  ※ 必要に応じて、プロキシ設定も行っておいてください。
- 設定を閉じて、アプリの読み込みをして、アプリケーション一覧をロードします。
- 脆弱性のCSVを取得するアプリをダブルクリックなり矢印で選択して右のボックスに移動します。（これが対象となります）
- 取得ボタンを押下します。成功するとexeと同じ場所にcsvファイルが出力されています。

使用方法の詳細については [Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases/latest) からダウンロードできるzipファイルに同梱のマニュアルpdfをご確認ください。

### ビルド
### gradleによるビルド
環境にあわせて`build.gradle`の以下箇所を弄ってください。
#### Windows 64bitの場合（java 64bitでEclipseなど動かしている場合はこのままで良いです）
```gradle
compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### Windows 32bitの場合（exeを作るために32bit版のビルドをする場合）
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### macOSの場合
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```

### コマンドプロンプト、ターミナルでビルドする場合
#### Windows
```powershell
gradlew clean jar
```
#### macOS
```bash
./gradlew clean jar
```
`build\libs`の下にjarが作成されます。

### Eclipseでビルド、実行できるようにする場合
#### Windows
```powershell
gradlew cleanEclipse eclipse
```
#### macOS
```bash
./gradlew cleanEclipse eclipse
```
Eclipseでプロジェクトをリフレッシュすると、あとはJavaの実行で`com.contrastsecurity.csvdltool.Main`クラス指定で、ツールが起動します。

## 配布
#### Windows配布用のexe化について
- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- jreがインストールされていない環境でも、jreフォルダを同梱することで環境に依存せずjavaを実行できるような設定になっています。  
  jreをDLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定に既になっています。
- 32bit版Javaにしている理由ですが、今はもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。
#### Mac配布用のapp化について
- javapackagerを使っています。
- jreを同梱させるため、実施するMacに1.8.0_202のJREフォルダを任意の場所に配置しておいてください。
- jarpackage.sh内の3〜7行目を適宜、修正してください。
- jarpackage.shを実行します。
  ```bash
  ./jarpackage.sh
  ./jarpackage_auditlog.sh
  ```
  build/libs/bundle下にappフォルダが作られます。

#### exe, appへの署名について
まず、証明書ファイル(pfx)と証明書パスワードを入手してください。  
署名についは以下の手順で実行してください。  
#### Windows  
- エイリアスの確認
  ```powershell
  keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx
  # 証明書パスワードを入力
  ```
- 署名  
  launch4jのsign4jを使用します。
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\CSVDLTool_work\XXXXX.pfx --storepass [パスワード] C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.0.0.exe
  ```
- 署名の確認  
  署名の確認については、exeを右クリック->プロパティ で確認できます。
#### macOS
- 証明書ファイルの読み込み  
  pfxファイルをダブルクリックでキーチェーンアクセス.appに読み込ませます。証明書パスワード入力が必要  
  読み込めたら、Common Name(通称)をコピー
- 署名
  ```bash
  codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_2.0.0.app
  codesign --deep -s "Contrast Security, Inc." -v CSVDLTool_2.0.0_auditlog.app
  ```
- 署名の確認
  ```bash
  codesign -d --verbose=4 CSVDLTool_2.0.0.app
  codesign -d --verbose=4 CSVDLTool_2.0.0_auditlog.app
  ```
    
#### 圧縮について補足
- macOS
  ```bash
  7z a CSVDLTool_2.0.0.cli7z CSVDLTool_2.0.0.app/
  7z a CSVDLTool_2.0.0_auditlog.cli7z CSVDLTool_2.0.0_auditlog.app/
  ```
