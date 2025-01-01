package com.rebuild.backend.utils.converters.database_converters;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneAndStringDatabaseConverter implements AttributeConverter<PhoneNumber, String> {
    @Override
    public String convertToDatabaseColumn(PhoneNumber phoneNumber) {
        return phoneNumber.databaseStorageFormat();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String s) {

        //The phone number of a user can be null, so we have to check this
        if (s == null || s.isEmpty()){
            return null;
        }
        String[] numberParts = s.split("-");
        return new PhoneNumber(numberParts[0], numberParts[1], numberParts[2]);

    }
}
