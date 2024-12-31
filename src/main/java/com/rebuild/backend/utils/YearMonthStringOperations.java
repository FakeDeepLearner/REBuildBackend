package com.rebuild.backend.utils;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class YearMonthStringOperations {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

    public static YearMonth getYearMonth(String yearMonth) {
        return YearMonth.parse(yearMonth, formatter);
    }
}
