package com.contrastsecurity.csvdltool;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

import java.io.FileNotFoundException;

public class CSVDLEncryptor {

    private static BasicTextEncryptor encryptor = new BasicTextEncryptor();
    private static String ENV_KEY = "CSVDEV_TOOL_PASS";

    public static String encrypt(String planText) {
        try {
            CSVDLEncryptor.encryptor.setPassword(getPassword());
        } catch (FileNotFoundException fne) {
            return planText;
        }
        return CSVDLEncryptor.encryptor.encrypt(planText);
    }

    public static String decrypt(String encryptedText) {
        // .envファイルがない、復号できない場合はそのままの値を返す。
        try {
            CSVDLEncryptor.encryptor.setPassword(getPassword());
        } catch (FileNotFoundException fne) {
            return encryptedText;
        }
        try {
            return CSVDLEncryptor.encryptor.decrypt(encryptedText);
        } catch (EncryptionOperationNotPossibleException encryptionOperationNotPossibleException) {
            return encryptedText;
        }
    }

    private static String getPassword() throws FileNotFoundException {
        try {
            Dotenv dotenv = Dotenv.load();
            return dotenv.get(ENV_KEY);
        } catch (DotenvException de) {
            throw new FileNotFoundException();
        }
    }
}
