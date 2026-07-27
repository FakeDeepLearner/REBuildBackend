package com.rebuild.backend.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkEmail;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkInformation;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkPhoneNumber;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.UserAuthException;
import org.springframework.http.HttpStatus;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class StringUtil {

    public static String maskString(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }

        String[] separatedParts = s.split(" ");


        StringBuilder result = new StringBuilder();

        for (String part : separatedParts) {
            String transformedPart;
            if (part.length() == 1) {
                transformedPart = part;
            } else {
                transformedPart = part.replace(part.substring(1), "*".repeat(part.length() - 2));
            }

            result.append(transformedPart).append(" ");

        }

        return result.toString();
    }


    public static String findPrimaryEmail(ClerkInformation information)
    {
        String primaryEmailId = information.primaryEmailAddressId();

        return information.emailAddresses()
                .stream().filter(clerkEmail -> clerkEmail.id().equals(primaryEmailId))
                .map(ClerkEmail::emailAddress).findFirst().orElse(null);
    }

    private static String extractPrimaryPhoneNumber(String primaryPhone)
    {
        try {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phonenumber = phoneNumberUtil.parse(primaryPhone, null);

        boolean isValid = phoneNumberUtil.isValidNumber(phonenumber);

        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid phone number");
        }


        return Long.toString(phonenumber.getNationalNumber());
        }
        catch (NumberParseException e)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    public static String findPrimaryPhoneNumber(ClerkInformation information) {
        String primaryPhoneNumberId = information.primaryPhoneNumberId();

        String primaryPhone = information.phoneNumbers()
                .stream().filter(clerkPhoneNumber -> clerkPhoneNumber.id().equals(primaryPhoneNumberId))
                .map(ClerkPhoneNumber::phoneNumber).findFirst().orElse(null);

        if (primaryPhone == null) {
            return null;
        }

        return extractPrimaryPhoneNumber(primaryPhone);
    }

}
