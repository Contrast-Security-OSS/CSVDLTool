## DigiCert KeyLocker手順
### 環境構築
#### KeyLockerクライアントツール
- DigiCert Oneにログイン
- KeyLocker - リソースからクライアントツールをDL  
  https://one.digicert.com/signingmanager/client-tools  
  Keylockertools-mac-x64.tar.gz
- 解凍してセットアップ  
  ```bash
  tar zxvf Keylockertools-mac-x64.tar.gz
  ```
  2つのdmgファイルが展開されます。  
  ```
  hdiutil mount smctl-mac-x64.dmg
  hdiutil mount smpkcs11.dylib.dmg
  ```
  ```
  mkdir ~/digicert
  cp /Volumes/smctl-mac/smctl-mac-x64 ~/digicert/
  cp /Volumes/smpkcs11/smpkcs11.dylib ~/digicert/
  ```
