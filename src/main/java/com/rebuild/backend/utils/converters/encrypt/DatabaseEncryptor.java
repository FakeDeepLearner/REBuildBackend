package com.rebuild.backend.utils.converters.encrypt;

import com.rebuild.backend.exceptions.ServerError;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Converter
public class DatabaseEncryptor implements AttributeConverter<String, String> {

    private final EncryptUtil encryptUtil;

    @Autowired
    public DatabaseEncryptor(EncryptUtil encryptUtil) {
        this.encryptUtil = encryptUtil;
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        try {
            return encryptUtil.encrypt(plainText);
        } catch (Exception e) {
           throw new ServerError();
        }
    }

    @Override
    public String convertToEntityAttribute(String cipherText) {
        try {
            return encryptUtil.decrypt(cipherText);
        } catch (Exception e) {
            throw new ServerError();
        }
    }
}
