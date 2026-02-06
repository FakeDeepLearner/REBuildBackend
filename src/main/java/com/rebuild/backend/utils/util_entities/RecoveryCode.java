package com.rebuild.backend.utils.util_entities;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

public class RecoveryCode {

    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int CODE_GROUP_SIZE = 4;

    private static final int CODE_NUM_GROUPS = 4;

    private static final int CODE_TOTAL_LENGTH = CODE_GROUP_SIZE * CODE_NUM_GROUPS;

    private static final char CODE_GROUP_SEPARATOR = '-';

    private final String rawCode;

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();


    private RecoveryCode(String code)
    {
        this.rawCode = code;
    }

    public static RecoveryCode create()
    {
        SecureRandom random = new SecureRandom();

        char[] characters = new char[CODE_TOTAL_LENGTH];

        for (int i = 0; i < CODE_TOTAL_LENGTH; i++)
        {
            characters[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }

        return new RecoveryCode(new String(characters));
    }

    public static RecoveryCode fromInput(String userInputCode)
    {
        String normalizedCode = normalize(userInputCode);

        return new RecoveryCode(normalizedCode);
    }

    public String getDisplayValue()
    {
        //After every 4 characters, insert a dash(-), then trim the very final character (which is also a dash).
        return this.rawCode.replaceAll(
                "(.{" + CODE_GROUP_SIZE + "})",
                "$1" + CODE_GROUP_SEPARATOR
        ).replaceAll("-$", "");
    }


    public String hashedValue()
    {
        return encoder.encode(this.rawCode);
    }

    public boolean hashedValueMatches(String hashedCode)
    {
        return encoder.matches(this.rawCode, hashedCode);
    }


    private static String normalize(String input)
    {
        return input.replace(String.valueOf(CODE_GROUP_SEPARATOR), "").toUpperCase();
    }








}
