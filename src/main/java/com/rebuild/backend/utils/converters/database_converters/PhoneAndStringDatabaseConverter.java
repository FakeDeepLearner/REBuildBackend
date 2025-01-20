package com.rebuild.backend.utils.converters.database_converters;

import com.rebuild.backend.exceptions.ServerError;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.utils.converters.encrypt.EncryptUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter
public class PhoneAndStringDatabaseConverter implements AttributeConverter<PhoneNumber, String> {

    private final EncryptUtil encryptUtil;

    @Autowired
    public PhoneAndStringDatabaseConverter(EncryptUtil encryptUtil) {
        this.encryptUtil = encryptUtil;
    }

    @Override
    public String convertToDatabaseColumn(PhoneNumber phoneNumber) {
        String dbFormat = phoneNumber.databaseStorageFormat();
        try {
            return encryptUtil.encrypt(dbFormat);
        } catch (Exception e) {
            throw new ServerError();
        }
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String cipherText) {

        //The phone number of a user can be null, so we have to check this
        if (cipherText == null || cipherText.isEmpty()){
            return null;
        }

        String actualNumber;
        try {
             actualNumber = encryptUtil.decrypt(cipherText);

        } catch (Exception e) {
            throw new ServerError();
        }
        String[] numberParts = actualNumber.split("-");
        return new PhoneNumber(numberParts[0], numberParts[1], numberParts[2]);
    }
}
