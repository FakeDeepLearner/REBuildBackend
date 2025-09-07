package com.rebuild.backend.utils.converters.database_converters;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@Converter
public class YearMonthDatabaseConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth.toString();
    }


    @Override
    public YearMonth convertToEntityAttribute(String s) {
        return YearMonth.parse(s);
    }
}
