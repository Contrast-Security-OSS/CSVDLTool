## DigiCert KeyLocker手順（Windows）
### 環境構築
#### 環境変数の設定
- DigiCert Oneにログイン
- APIキー、認証証明書を作成  
  すでに作成済みなら使いまわしても良いです。  
  それぞれAPIキー、証明書パスワードのメモと認証証明書のダウンロード
#### KeyLockerクライアントツール
- KeyLocker - リソースからクライアントツールをDL  
  https://one.digicert.com/signingmanager/client-tools  
  Keylockertools-windows-x64.msi
- DigiCert Keylocker Toolsのインストール  
  デフォルトの*C:\Program Files\DigiCert\DigiCert Keylocker Tools*でも良いですが、パスが短いほうが良いので  
  *C:\DigiCert*下にインストールします。
- Click-to-signのインストール  
  インストールしたフォルダに*DigiCert_Click_to_sign.msi*があるので、これでインストールを行います。  
  これについては場所はデフォルトの*C:\Program Files\DigiCert\Click-to-sign\*でも良いです。
#### 設定ファイルの作成
- PKCS11コンフィグファイルの作成  
  *C:\DigiCert\pkcs11properties.cfg*
  ```properties
  name=signingmanager 
  library="C:/DigiCert/smpkcs11.dll"
  slotListIndex=0
  ```
  パスに円マークを使ったりすると署名のところでエラーになります。
#### もろもろセットアップ
- Click to sign.exeを実行する  
  *C:\Program Files\DigiCert\Click-to-sign\Click to sign.exe*  
  - 起動後、Nextで次画面で以下を入力してNext  
    - HOST: https://clientauth.one.digicert.com
    - API Key: DigiCert Oneから取得したAPIキー
    - Client Authentication Certificate: DigiCert OneからDLした*Certificate_pkcs12.p12*ファイル
    - Client Authentication Certificate: DigiCert OneからDLした*Certificate_pkcs12.p12*ファイルのパスワード
    - Pkcs11 Confiuration File: 上で作成した*pkcs11properties.cfg*ファイル
    - Save APIなんちゃらのチェックボックスにチェックする
  - 成功するとキーペアの詳細が確認できます。さらにそのままNextを繰り返してFinishまで行って完了です。

  これらの作業を行うことで環境変数もすべてセットしてくれます。
#### Launch4jのsign4jを使うための準備
- jsign-5.0.jarのダウンロード  
  https://docs.digicert.com/ja/software-trust-manager/sign-with-digicert-signing-tools/third-party-signing-tool-integrations/jsign.html  
  に沿って、jsign-5.0.jarをダウンロードする。  
  https://github.com/ebourg/jsign/releases/download/5.0/jsign-5.0.jar  
  ダウンロード先は任意ですが、`C:\DigiCert`の下にDLした前提で進めます。  
### 署名
- 署名コマンド  
  コマンドプロンプトは管理者として実行で起動してください。
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar C:\DigiCert\jsign-5.0.jar --keystore "C:\DigiCert\pkcs11properties.cfg" --storetype PKCS11 C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.1.2.exe
  sign4j.exe java -jar C:\DigiCert\jsign-5.0.jar --keystore "C:\DigiCert\pkcs11properties.cfg" --storetype PKCS11 C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.1.2_auditlog.exe
  ```
  - 電子署名についてはファイルを右クリックのプロパティで確認ができます。
  
