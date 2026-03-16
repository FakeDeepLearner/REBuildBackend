package com.rebuild.backend.utils;

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
}
