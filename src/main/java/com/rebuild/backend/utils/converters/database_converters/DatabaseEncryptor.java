package com.rebuild.backend.utils.converters.database_converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
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
    private final BytesEncryptor bytesEncryptor = Encryptors.stronger(System.getenv("DB_ENCRYPTION_PASSWORD"),
            System.getenv("DB_ENCRYPTION_SALT"));


    // These methods are actually the implementation of a builtin TextEncoder. However, since I wanted the
    // support of the stronger BytesEncryptor, I just did that implementation myself.
    @Override
    public String convertToDatabaseColumn(String plainText) {
        return new String(Hex.encode(bytesEncryptor.encrypt(Utf8.encode(plainText))));
    }

    @Override
    public String convertToEntityAttribute(String cipherText) {
        return Utf8.decode(bytesEncryptor.decrypt(Hex.decode(cipherText)));
    }
}
