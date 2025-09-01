package com.rebuild.backend.utils.converters.database_converters;

import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ExperienceTypesConverter implements AttributeConverter<Collection<ExperienceType>, String> {
    @Override
    public String convertToDatabaseColumn(Collection<ExperienceType> experienceTypes) {
        if (experienceTypes == null || experienceTypes.isEmpty())
        {
            return "";
        }

        //we store the constants by their names with a comma separating them
        return experienceTypes.stream().
                map(ExperienceType::getStoredValue).
                collect(Collectors.joining(","));
    }

    @Override
    public Collection<ExperienceType> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty())
        {
            return Collections.emptyList();
        }

        return Arrays.stream(s.split(",")).
                map(String::trim).
                map(ExperienceType::fromValue).
                toList();
    }
}
