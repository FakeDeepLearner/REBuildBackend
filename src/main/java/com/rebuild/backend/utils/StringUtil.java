package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ExperienceBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ProjectBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractExperience;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractProject;
import com.rebuild.backend.utils.exceptions.ApiException;
import org.springframework.http.HttpStatus;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class StringUtil {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuu-MM");

    private static YearMonth generateYearMonthValue(String yearMonth, boolean isStartDate)
    {
        boolean nullOrBlank = yearMonth == null || yearMonth.isBlank();
        if (isStartDate && nullOrBlank)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A start date must be provided");
        }
        // If it is not a start date (so it is an end date)
        // and it is left empty, we treat the end date as empty
        //If we enter this branch, we know that isStartDate is false, so we can safely do this
        if (nullOrBlank)
        {
            return null;
        }
        try {
            return YearMonth.parse(yearMonth, yearMonthFormatter);
        }
        catch (DateTimeParseException e)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This date value cannot be parsed");
        }
    }


    public static YearMonth generateStartDate(String input)
    {
        return generateYearMonthValue(input, true);
    }

    public static YearMonth generateEndDate(String input)
    {
        return generateYearMonthValue(input, false);
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


    public static String getYearMonthDisplayValue(YearMonth yearMonth)
    {
        if (yearMonth == null)
        {
            return "Present";
        }

        return determineMonthString(yearMonth.getMonthValue()) + " - " + yearMonth.getYear();
    }


    public static String determineDisplayedCommentName(boolean commentIsAnonymized, String defaultName,
                                                       String anonymizedName)
    {
        if (!commentIsAnonymized)
        {
            return defaultName;
        }
        return "Anonymous#" + anonymizedName;
    }




    public static String generateResumeCacheKey(User user, UUID resumeId) {
        return user.getId() + ":" + resumeId;
    }

}
