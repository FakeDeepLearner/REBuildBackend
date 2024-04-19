package com.rebuild.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneNumber {

    private String countryCode;

    private String areaCode;

    private String restOfNumber;


}
