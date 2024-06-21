package com.rebuild.backend.utils.converters;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class StringToChronoUnitConverter implements Converter<String, ChronoUnit> {
    @Override
    public ChronoUnit convert(@NonNull String source) {
        return switch (source){
            case "hours", "" -> ChronoUnit.HOURS;
            case "minutes" -> ChronoUnit.MINUTES;
            case "days" -> ChronoUnit.DAYS;
        };
    }
}
