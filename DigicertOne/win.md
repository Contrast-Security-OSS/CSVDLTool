## DigiCert KeyLocker手順（Windows）
### 環境構築
#### 環境変数の設定
- DigiCert Oneにログイン
- APIキー、認証証明書を作成  
  すでに作成済みなら使いまわしても良いです。  
  それぞれAPIキー、証明書パスワードのメモと認証証明書のダウンロード
- 環境変数のセット
  ```
  export SM_HOST=https://clientauth.one.digicert.com
  export SM_API_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  export SM_CLIENT_CERT_FILE=/Users/turbou/Downloads/Certificate_pkcs12.p12
  ```
#### KeyLockerクライアントツール
- KeyLocker - リソースからクライアントツールをDL  
  https://one.digicert.com/signingmanager/client-tools  
  Keylockertools-windows-x64.msi
- DigiCert Keylocker Toolsのインストール  
  デフォルトの```C:\Program Files\DigiCert\DigiCert Keylocker Tools```でも良いですが、パスが短いほうが良いので  
  ```C:\DigiCert```下にインストールします。
- Click-to-signのインストール  
  インストールしたフォルダに```DigiCert_Click_to_sign.msi```があるので、これでインストールを行います。  
  これについては場所はデフォルトの```C:\Program Files\DigiCert\Click-to-sign\```でも良いです。
#### キーチェーンアクセスに証明書を登録
- 証明書の保存
  ```bash
  smctl-mac-x64 credentials save <API Key> <Client certificate password>
  ```
#### 設定ファイルの作成
- PKCS11コンフィグファイルの作成
  ```bash
  vim ~/digicert/pkcs11properties.cfg
  ```
  ```properties
  name=signingmanager 
  library=/Users/turbou/digicert/smpkcs11.dylib
  slotListIndex=0
  ```
#### 確認
- 動作確認
  ```bash
  smctl-mac-x64 healthcheck
  ```
  冒頭にこのように出ていればOK
  ```
  --------- User credentials ------
  Status: Connected
  ```
### 署名
- 署名コマンド
  ```bash
  smctl-mac-x64 sign \
      --keypair-alias key_455812447 \
      --verbose=true \
      --config-file /Users/turbou/digicert/pkcs11properties.cfg \
      --input /Users/turbou/Documents/git/CSVDLTool/build/CSVDLTool_aarch64.app/
  ```
  - キーペアはKeyLockerの証明書管理に置かれている証明書のキーペアを使います.
  - インプットは署名するappファイルですが、最後のスラッシュがないとエラーになります。
  
