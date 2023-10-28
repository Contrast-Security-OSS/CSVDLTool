## DigiCert KeyLocker手順
### 環境構築
#### 環境変数の設定
- DigiCert Oneにログイン
- APIキー、認証証明書を作成  
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
  Keylockertools-mac-x64.tar.gz
- 解凍してセットアップ  
  ```bash
  # 解凍
  tar zxvf Keylockertools-mac-x64.tar.gz
  # 2つのdmgファイルが展開されるのでマウント
  hdiutil mount smctl-mac-x64.dmg
  hdiutil mount smpkcs11.dylib.dmg
  # クライアントツールの格納先（ユーザーフォーム直下としてますが、パスを通せばどこでも良い）
  mkdir ~/digicert
  # クライアントツールの格納
  cp /Volumes/smctl-mac/smctl-mac-x64 ~/digicert/
  cp /Volumes/smpkcs11/smpkcs11.dylib ~/digicert/
  ```
  パスを通す
  ```bash
  vim ~/.bash_profile
  ```
  ```
  export PATH=/Users/turbou/digicert:$PATH
  ```
  ```bash
  source ~/.bash_profile
  ```
- 動作確認
  ```bash
  smctl-mac-x64 healthcheck
  ```
  冒頭にこのように出ていればOK
  ```
  --------- User credentials ------
  Status: Connected
  ```

