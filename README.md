#### コマンドプロンプトでビルドする場合

```
gradle clean jar
```

build\libsの下にjarが作成されます。

#### Eclipseでビルド、実行できるようにする場合

```
gradle cleanEclipse eclipse
```

Eclipseでプロジェクトをリフレッシュすると、あとは実行でcom.contrastsecurity.csvdltool.Mainで、ツールが起動します。



#### 32bit版でjarを生成する場合はbuild.gradleの以下箇所を弄ってください。

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



#### exe化について

- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- 同梱のjreは、1.8.0_202(32bit)ですが、DLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定になっています。
- 32bitにしている理由ですが、いまもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。



以上