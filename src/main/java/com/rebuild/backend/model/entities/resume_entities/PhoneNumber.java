package com.rebuild.backend.model.entities.resume_entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PhoneNumber {

    private String countryCode;

    private String areaCode;

    private String restOfNumber;


}
