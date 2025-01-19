package com.rebuild.backend.utils.converters.encrypt;

import com.rebuild.backend.config.properties.DBEncryptData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component
public class EncryptUtil {

    private final DBEncryptData dbEncryptData;

    @Autowired
    public EncryptUtil(DBEncryptData dbEncryptData) {
        this.dbEncryptData = dbEncryptData;
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] saltInBytes = dbEncryptData.salt().getBytes();
        char[] passwordAsArray = dbEncryptData.password().toCharArray();
        KeySpec secretSpec = new PBEKeySpec(passwordAsArray, saltInBytes, 100, 256);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] secretKeyBytes = secretKeyFactory.generateSecret(secretSpec).getEncoded();
        return new SecretKeySpec(secretKeyBytes, dbEncryptData.algorithm());
    }

    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(dbEncryptData.algorithm());
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    public String decrypt(String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(dbEncryptData.algorithm());
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
    }
}
