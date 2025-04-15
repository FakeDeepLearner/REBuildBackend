package com.rebuild.backend.utils;


import com.rebuild.backend.model.entities.enums.CompareCriteria;
import com.rebuild.backend.model.entities.enums.ComparisonMethod;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class YearMonthStringOperations {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

    public static YearMonth getYearMonth(String yearMonth) {
        return YearMonth.parse(yearMonth, yearMonthFormatter);
    }


    public static int compare(String date1, String date2, CompareCriteria criteria,
                               ComparisonMethod method) {
        String[] split1 = date1.split("-");
        String[] split2 = date2.split("-");

        int year1 = Integer.parseInt(split1[1]);
        int year2 = Integer.parseInt(split2[1]);

        int month1 = Integer.parseInt(split1[0]);
        int month2 = Integer.parseInt(split2[0]);

        int initialResult = 0;

        if(criteria == CompareCriteria.MONTH){
            initialResult = month1 - month2;
        }
        else if(criteria == CompareCriteria.YEAR){
            initialResult = year1 - year2;
        }

        if(method == ComparisonMethod.AFTER){
            return initialResult;
        }

        else{
            return -initialResult;
        }
    }
}