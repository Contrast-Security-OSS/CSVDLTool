package com.contrastsecurity.csvdltool;

import io.github.cdimascio.dotenv.Dotenv;
import org.jasypt.util.text.BasicTextEncryptor;

public class CSVDLEncryptor {

    private static BasicTextEncryptor encryptor = new BasicTextEncryptor();
    private static String ENV_KEY = "CSVDEV_TOOL_PASS";

    public static String encrypt(String planText) {
        CSVDLEncryptor.encryptor.setPassword(getPassword());
        return CSVDLEncryptor.encryptor.encrypt(planText);
    }

    public static String decrypt(String encryptedText) {
        CSVDLEncryptor.encryptor.setPassword(getPassword());
        return CSVDLEncryptor.encryptor.decrypt(encryptedText);
    }

    private static String getPassword() {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get(ENV_KEY);
    }
}
