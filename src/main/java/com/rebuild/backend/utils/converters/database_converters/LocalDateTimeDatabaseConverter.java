package com.rebuild.backend.utils.converters.database_converters;

import com.rebuild.backend.exceptions.ServerError;
import com.rebuild.backend.utils.converters.encrypt.EncryptUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Converter
public class LocalDateTimeDatabaseConverter implements AttributeConverter<LocalDateTime, String> {

    private final EncryptUtil encryptUtil;

    @Autowired
    public LocalDateTimeDatabaseConverter(EncryptUtil encryptUtil) {
        this.encryptUtil = encryptUtil;
    }

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        String convertedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        try {
            return encryptUtil.encrypt(convertedTime);
        } catch (Exception e) {
            throw new ServerError();
        }
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String cipherText) {
        String plainTime;
        try {
            plainTime = encryptUtil.decrypt(cipherText);
        } catch (Exception e) {
            throw new ServerError();
        }
        return LocalDateTime.parse(plainTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    }
}
