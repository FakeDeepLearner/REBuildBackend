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

    @Override
    public void serialize(YearMonth yearMonth,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        // "null" represents the "Present" string
        if (yearMonth == null) {
            jsonGenerator.writeString("Present");
            return;
        }

        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        String serializedResult = determineMonthString(month) + " " + year;
        jsonGenerator.writeString(serializedResult);
    }
}
