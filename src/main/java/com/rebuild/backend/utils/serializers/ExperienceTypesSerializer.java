package com.rebuild.backend.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class ExperienceTypesSerializer extends JsonSerializer<Collection<ExperienceType>> {
    @Override
    public void serialize(Collection<ExperienceType> experienceTypes,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        for (ExperienceType experienceType : experienceTypes) {
            jsonGenerator.writeString(experienceType.storedValue);
            jsonGenerator.writeString(" ");
        }

        jsonGenerator.writeEndArray();

    }
}
