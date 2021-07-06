## 概要
TeamServerに登録されている脆弱性情報、ライブラリ情報をCSV形式でレポートを取得できるツールです。  
TeamServerから直接、csvレポートを出力することもできますが、本ツールではより多くの情報を取得、また  
出力する情報をカスタマイズすることができます。  
使用方法の詳細については [Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases) からダウンロードできるzipファイルに同梱のマニュアルpdfをご確認ください。


## 動作環境
Windows8.1、Windows10  
Java1.8.0_202

## ソースからビルドする場合

ビルドからではなく、すぐにお使いいただく場合は[リリースについて](#リリースについて)を参照ください。

### コマンドプロンプトでビルドする場合

```
gradle clean jar
```

build\libsの下にjarが作成されます。

### Eclipseでビルド、実行できるようにする場合

```
gradle cleanEclipse eclipse
```

Eclipseでプロジェクトをリフレッシュすると、あとは実行でcom.contrastsecurity.csvdltool.Mainで、ツールが起動します。



### 32bit版でjarを生成する場合はbuild.gradleの以下箇所を弄ってください。

- 64bitの場合（java 64bitでEclipseなど動かしている場合はこのままで良いです）

  ```properties
      compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
      //compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  ```
- 32bitの場合（exeを作るために32bit版のビルドをする場合）

  ```properties
      //compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
      compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
  ```



### exe化について

- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- jreがインストールされていない環境でも、jreフォルダを同梱することで環境に依存せずjavaを実行できるような設定になっています。  
  jreをDLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定に既になっています。
- 32bit版Javaにしている理由ですが、今はもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。



## 起動後の使い方について

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

## リリースについて
[Release](https://github.com/Contrast-Security-OSS/CSVDLTool/releases) で以下２種類のバイナリを提供しています。ビルド不要でダウンロード後すぐにお使いいただけます。
- CSVDLTool_X.X.X.zip  
  初回ダウンロードの場合はこちらをダウンロードして解凍して、お使いください。  
  jreフォルダ（1.8.0_202）が同梱されているため、exeの起動ですぐにツールを使用できます。
- CSVDLTool_X.X.X.exe  
  既にzipをダウンロード済みの場合はexeのダウンロードと入れ替えのみでツールを使用できます。


以上
