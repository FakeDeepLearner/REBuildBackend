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

    /*
    * Both date1 and date2 are of the form MM-yyyy.
    * Returns >0 if date1 is later than date2, 0 if they are equal,
    * <0 otherwise
    */
    public static int compareYearMonthStrings(String date1, String date2) {
        String year1 = date1.split("-")[1];
        String year2 = date2.split("-")[1];
        String month1 = date1.split("-")[0];
        String month2 = date2.split("-")[0];

        int year1Int = Integer.parseInt(year1);
        int year2Int = Integer.parseInt(year2);
        int month1Int = Integer.parseInt(month1);
        int month2Int = Integer.parseInt(month2);

        if(year1Int == year2Int){
            return month1Int - month2Int;
        }
        return year1Int - year2Int;


    }
}
