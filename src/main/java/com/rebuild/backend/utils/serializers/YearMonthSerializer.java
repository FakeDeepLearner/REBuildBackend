package com.rebuild.backend.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.YearMonth;

@Component
public class YearMonthSerializer extends JsonSerializer<YearMonth> {

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

    @Override
    public void serialize(YearMonth yearMonth,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {


        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        String serializedResult = determineMonthString(month) + " " + year;
        jsonGenerator.writeString(serializedResult);
    }
}
