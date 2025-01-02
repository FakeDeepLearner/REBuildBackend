package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.enums.CompareCriteria;
import com.rebuild.backend.model.entities.enums.ComparisonMethod;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

@Component
public class YearMonthStringOperations {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

    public static YearMonth getYearMonth(String yearMonth) {
        return YearMonth.parse(yearMonth, formatter);
    }

    private static int compareYearMonthStrings(String date1, String date2,
                                               CompareCriteria criteria) {
        String month1 = date1.split("-")[0];
        String month2 = date2.split("-")[0];
        String year1 = date1.split("-")[1];
        String year2 = date2.split("-")[1];

        int month1Int = Integer.parseInt(month1);
        int month2Int = Integer.parseInt(month2);
        int year1Int = Integer.parseInt(year1);
        int year2Int = Integer.parseInt(year2);

        if(criteria == CompareCriteria.YEAR){
            return year1Int - year2Int;
        }
        if(criteria == CompareCriteria.MONTH){
            return month1Int - month2Int;
        }

        return 0;
    }


    /*
    * Creates a predicate that compares an input with testDate and returns whether it is bigger than the test date
    */
    public static Predicate<String> createComparisonPredicate(String testDate,
                                                         ComparisonMethod method,
                                                         CompareCriteria criteria, int compareValue)
    {

        if(method ==  ComparisonMethod.EQUALS){
            return input -> compareYearMonthStrings(input, testDate, criteria) == compareValue;
        }

        if(method == ComparisonMethod.AFTER){
            return input -> compareYearMonthStrings(input, testDate, criteria) > compareValue;
        }

        else{
            return input -> compareYearMonthStrings(input, testDate, criteria) < compareValue;
        }
    }
}
