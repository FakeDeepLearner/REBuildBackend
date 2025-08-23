package com.rebuild.backend.utils.converters.encrypt;


import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncryptUtil {

    private final SecretKey secretKey;


    public EncryptUtil()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.secretKey = getSecretKey();
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] saltInBytes = System.getenv("DB_ENCRYPTION_SALT").getBytes();
        char[] passwordAsArray = System.getenv("DB_ENCRYPTION_PASSWORD").toCharArray();
        KeySpec secretSpec = new PBEKeySpec(passwordAsArray, saltInBytes, 100, 256);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] secretKeyBytes = secretKeyFactory.generateSecret(secretSpec).getEncoded();
        return new SecretKeySpec(secretKeyBytes, System.getenv("DB_ENCRYPT_ALGORITHM"));
    }

    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(System.getenv("DB_ENCRYPT_ALGORITHM"));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Arrays.toString(cipher.doFinal(plainText.getBytes()));
        // return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    public String decrypt(String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(System.getenv("DB_ENCRYPT_ALGORITHM"));
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return Arrays.toString(cipher.doFinal(cipherText.getBytes()));
        // return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
    }
}
