## DigiCert KeyLocker Procedure (Windows)

### Environment Setup

#### Setting Environment Variables

- Log in to DigiCert One.
- Create an API key and an authentication certificate.
  If you have already created them, you can reuse them.
  Make a note of the API key and certificate password, and download the authentication certificate.

#### KeyLocker Client Tool

- Download the client tool from KeyLocker - Resources.
  [https://one.digicert.com/signingmanager/client-tools](https://one.digicert.com/signingmanager/client-tools)
  Keylockertools-windows-x64.msi
- Install DigiCert Keylocker Tools.
  The default location, *C:\Program Files\DigiCert\DigiCert Keylocker Tools*, is fine, but a shorter path is better, so install it under *C:\DigiCert*.
- Install Click-to-sign.
  There is a *DigiCert_Click_to_sign.msi* file in the installed folder, so use this to install.
  For this, the default location, *C:\Program Files\DigiCert\Click-to-sign\*, is fine.

#### Creating Configuration Files

- Create a PKCS11 configuration file.
  *C:\DigiCert\pkcs11properties.cfg*
  ```properties
  name=signingmanager 
  library="C:/DigiCert/smpkcs11.dll"
  slotListIndex=0
  ```
  Using backslashes in the path will cause an error during signing.

#### Miscellaneous Setup

- Run Click to sign.exe.
  *C:\Program Files\DigiCert\Click-to-sign\Click to sign.exe*
  - After launching, click Next, and enter the following on the next screen, then click Next.
    - HOST: [https://clientauth.one.digicert.com](https://clientauth.one.digicert.com)
    - API Key: The API key obtained from DigiCert One
    - Client Authentication Certificate: The *Certificate_pkcs12.p12* file downloaded from DigiCert One
    - Client Authentication Certificate: The password for the *Certificate_pkcs12.p12* file downloaded from DigiCert One
    - Pkcs11 Confiuration File: The *pkcs11properties.cfg* file created above
    - Check the "Save API..." checkbox.
  - If successful, you can see the details of the key pair. Continue clicking Next until you reach Finish to complete the process.

  These steps will also set all the environment variables for you.

#### Preparation for using Launch4j's sign4j

- Download jsign-5.0.jar.
  Follow the instructions at [https://docs.digicert.com/ja/software-trust-manager/sign-with-digicert-signing-tools/third-party-signing-tool-integrations/jsign.html](https://docs.digicert.com/ja/software-trust-manager/sign-with-digicert-signing-tools/third-party-signing-tool-integrations/jsign.html) to download jsign-5.0.jar.
  [https://github.com/ebourg/jsign/releases/download/5.0/jsign-5.0.jar](https://github.com/ebourg/jsign/releases/download/5.0/jsign-5.0.jar)
  You can download it to any location, but we will proceed assuming it's downloaded under `C:\DigiCert`.

### Signing

- Signing command
  Launch Command Prompt as an administrator.
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar C:\DigiCert\jsign-5.0.jar --keystore "C:\DigiCert\pkcs11properties.cfg" --storetype PKCS11 C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.1.4.exe
  sign4j.exe java -jar C:\DigiCert\jsign-5.0.jar --keystore "C:\DigiCert\pkcs11properties.cfg" --storetype PKCS11 C:\Users\turbou\Desktop\CSVDLTool_work\common\CSVDLTool_2.1.4_auditlog.exe
  ```
  - You can check the digital signature by right-clicking on the file and selecting Properties. 

