package com.rebuild.backend.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;


@Component
public class LocalDateSerializer extends JsonSerializer<LocalDate> {
    @Override
    public void serialize(LocalDate localDate,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        int year = localDate.getYear();
        String month = localDate.getMonth().toString();
        int day = localDate.getDayOfMonth();
        jsonGenerator.writeString(day + " " + month + " " + year);

    }
}
