## DigiCert KeyLocker手順（MacOS）
### 環境構築
#### 環境変数の設定
- DigiCert Oneにログイン
- APIキー、認証証明書を作成  
  それぞれAPIキー、証明書パスワードのメモと認証証明書のダウンロード
- 環境変数のセット
  ```bash
  export SM_HOST=https://clientauth.one.digicert.com
  # 自身アカウントのAPIトークン
  export SM_API_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  # 自身アカウントの生成してDLした証明書のパス
  export SM_CLIENT_CERT_FILE=/Users/turbou/Downloads/Certificate_pkcs12.p12
  # 自身アカウントの生成した証明書DLの際に表示されたパスワード
  export SM_CLIENT_CERT_PWD=XXXXXXX
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
#### キーチェーンアクセスに証明書を登録
- 証明書の保存
  ```bash
  smctl-mac-x64 credentials save $SM_API_KEY $SM_CLIENT_CERT_PWD
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
  
