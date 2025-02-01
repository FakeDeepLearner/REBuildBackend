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

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    }
}
