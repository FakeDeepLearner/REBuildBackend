package com.rebuild.backend.utils;

import com.rebuild.backend.config.properties.PepperValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component(value = "peppered")
public class PepperedEncoder implements PasswordEncoder {

    private final PepperValue pepperValue;

    private final BCryptPasswordEncoder encoder;

    @Autowired
    public PepperedEncoder(PepperValue pepperValue) {
        this.pepperValue = pepperValue;
        this.encoder = new BCryptPasswordEncoder();
    }


    @Override
    public String encode(CharSequence rawPassword) {
        String passwordToEncode = rawPassword + pepperValue.pepperValue();
        return encoder.encode(passwordToEncode);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String pepperedPassword = rawPassword + pepperValue.pepperValue();
        return encoder.matches(pepperedPassword, encodedPassword);
    }
}
