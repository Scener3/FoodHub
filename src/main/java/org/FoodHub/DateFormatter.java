package org.FoodHub;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    private String formattedUTCDate;
    public DateFormatter(Long dateNum){
        if (dateNum == null){
            this.formattedUTCDate = "N/A";
            return;
        }

        Instant instant = Instant.ofEpochMilli(dateNum);
        ZonedDateTime zonedTime = instant.atZone(ZoneOffset.UTC);

        DateTimeFormatter formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        formattedUTCDate = zonedTime.format(formattedDate);
    }

    public String getDate(){
        return formattedUTCDate;
    }
}
