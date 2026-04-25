package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.user_entities.User;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


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


    private static String determineMonthString(int monthValue){
        return switch (monthValue) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> throw new IllegalStateException("Unexpected value: " + monthValue);
        };
    }


    public static String transformYearMonth(YearMonth yearMonth)
    {
        if (yearMonth == null)
        {
            return "Present";
        }

        return determineMonthString(yearMonth.getMonthValue()) + " - " + yearMonth.getYear();
    }


    public static String getAnonymizedName(String userBaseName, UUID associatedPostId)
    {
        return "Anonymous" + "-" + userBaseName + "-" + associatedPostId.toString().substring(0, 8);
    }


    public static String generateResumeCacheKey(User user, UUID resumeId) {
        return user.getId() + ":" + resumeId;
    }
}
