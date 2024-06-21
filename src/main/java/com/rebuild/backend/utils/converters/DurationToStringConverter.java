package com.rebuild.backend.utils.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


import java.time.Duration;

@Converter(autoApply = true)
public class DurationToStringConverter implements AttributeConverter<Duration, String> {

    @Override
    public String convertToDatabaseColumn(Duration duration) {
        return duration.toString();
    }

    @Override
    public Duration convertToEntityAttribute(String s) {
        return Duration.parse(s);
    }
}
