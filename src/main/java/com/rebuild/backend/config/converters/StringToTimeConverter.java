package com.rebuild.backend.config.converters;




import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class StringToTimeConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(@NonNull String source) {
        return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
