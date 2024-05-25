package com.rebuild.backend.config.converters;

import com.rebuild.backend.model.constraints.entities.PhoneNumber;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPhoneConverter implements Converter<String, PhoneNumber> {
    @Override
    public PhoneNumber convert(@NonNull String source) {
        String[] parts = source.split("-");
        try {
            return new PhoneNumber(parts[0], parts[1], parts[2]);
        }
        catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException();
        }
    }
}
