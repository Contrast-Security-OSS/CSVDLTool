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
  library="C:\DigiCert\smpkcs11.dll"
  slotListIndex=0
  ```
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
  
