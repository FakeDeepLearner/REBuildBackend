package com.rebuild.backend.utils;

import com.rebuild.backend.model.dtos.auth_dtos.ClerkEmail;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkInformation;


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

}
