package com.rebuild.backend.utils;


import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class YearMonthStringOperations {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuu-MM");

    public static YearMonth getYearMonth(String yearMonth)
    {
        if (yearMonth.equals("Present"))
        {
            return null;
        }
        return YearMonth.parse(yearMonth, yearMonthFormatter);
    }
}