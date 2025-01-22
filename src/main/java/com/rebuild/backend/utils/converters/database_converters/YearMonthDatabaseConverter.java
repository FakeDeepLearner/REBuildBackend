package com.rebuild.backend.utils.converters.database_converters;


import com.rebuild.backend.exceptions.ServerError;
import com.rebuild.backend.utils.converters.encrypt.EncryptUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@Converter
public class YearMonthDatabaseConverter implements AttributeConverter<YearMonth, String> {

    private final EncryptUtil encryptUtil;

    @Autowired
    public YearMonthDatabaseConverter(EncryptUtil encryptUtil) {
        this.encryptUtil = encryptUtil;
    }

    private String determineMonthString(int monthValue){
        return switch (monthValue) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> throw new IllegalStateException("Unexpected value: " + monthValue);
        };
    }

    private int determineStringMonth(String monthString){
        return switch (monthString){
            case "January" -> 1;
            case "February"->  2;
            case "March"->  3;
            case "April"->  4;
            case "May"->  5;
            case "June"->  6;
            case "July"->  7;
            case "August"->  8;
            case "September"->  9;
            case "October"->  10;
            case "November"->  11;
            case "December"->  12;

            default -> throw new IllegalStateException("Unexpected value: " + monthString);
        };
    }

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
<<<<<<< HEAD
        return determineMonthString(month) + " " + year;
    }

    @Override
    public YearMonth convertToEntityAttribute(String s) {
        String monthString = s.split(" ")[0];
        int year = Integer.parseInt(s.split(" ")[1]);
        int month = determineStringMonth(monthString);
=======
        String monthString = month < 10 ? "0" + month : String.valueOf(month);
        String combinedPlainText = monthString + "-" + year;
        try {
            return encryptUtil.encrypt(combinedPlainText);
        } catch (Exception e) {
            throw new ServerError();
        }
    }

    @Override
    public YearMonth convertToEntityAttribute(String cipherText) {
        String plainYearMonth;
        try {
            plainYearMonth = encryptUtil.decrypt(cipherText);
        } catch (Exception e) {
            throw new ServerError();
        }
        int month = Integer.parseInt(plainYearMonth.split("-")[0]);
        int year = Integer.parseInt(plainYearMonth.split("-")[1]);
>>>>>>> e616d8e (Start on spec system)
        return YearMonth.of(year, month);
    }
}
