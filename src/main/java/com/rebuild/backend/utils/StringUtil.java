package com.rebuild.backend.utils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


public class StringUtil {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuu-MM");

    public static YearMonth getYearMonth(String yearMonth)
    {
        if (yearMonth.equals("Present"))
        {
            return null;
        }
        return YearMonth.parse(yearMonth, yearMonthFormatter);
    }

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
