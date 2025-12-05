package com.rebuild.backend.utils.converters.database_converters;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;


@Component
@Converter
/*
* The implementation in this class is inspired from
* https://docs.spring.io/spring-security/reference/features/integrations/cryptography.html
* */
public class DatabaseEncryptor implements AttributeConverter<String, String> {

    // The password and salt are used to prevent dictionary attacks in the event that the (encrypted) database
    // is leaked
    private final TextEncryptor encryptor;

    @Autowired
    public DatabaseEncryptor(Dotenv dotenv) {
        //This uses the stronger BytesEncryptor version (it uses GCM instead of CBC), which is what we want.
        this.encryptor = Encryptors.delux(dotenv.get("DB_ENCRYPTION_PASSWORD"),
                dotenv.get("DB_ENCRYPTION_SALT"));
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        return encryptor.encrypt(plainText);
    }

    @Override
    public String convertToEntityAttribute(String cipherText) {
        return encryptor.decrypt(cipherText);
    }
}
