package com.rebuild.backend.model.entities.resume_entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PhoneNumber {

    private String countryCode;

    private String areaCode;

    private String restOfNumber;

    public String fullNumber(){
        return countryCode + areaCode + restOfNumber;
    }

    public String maskedNumber(){
        String unmaskedNumber = fullNumber();
        int firstUnmaskedIndex = unmaskedNumber.length() - 4;
        String lastPart = unmaskedNumber.substring(firstUnmaskedIndex);
        return "*".repeat(firstUnmaskedIndex) + lastPart;
    }


}
