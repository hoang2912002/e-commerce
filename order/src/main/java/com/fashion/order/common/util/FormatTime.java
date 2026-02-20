package com.fashion.order.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FormatTime {
    public static String stringDateTimeInstant(Instant instant) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return fmt.format(instant);
    }
    public static String stringDateInstant(Instant instant) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return fmt.format(instant);
    }
    
    public static String StringDateLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return fmt.format(localDateTime);
    }

    public static LocalDateTime formatUnixToLocalDateTime(Long unixTime){
        Instant instant = Instant.ofEpochSecond(unixTime);
        return LocalDateTime.ofInstant(
                instant,
                ZoneId.of("Asia/Ho_Chi_Minh")
        );
    }

    public static Long formatLocalDateTimeToUnix(LocalDateTime localDateTime) {
        return localDateTime
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toEpochSecond();
    }

    public static LocalDateTime formatInstantToLocalDateTime(Instant instant) {
        return instant
            .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
            .toLocalDateTime();
    }

    public static Instant[] getMonthRange(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        
        Instant start = date.withDayOfMonth(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
                            
        Instant end = date.withDayOfMonth(1)
            .plusMonths(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
        return new Instant[]{start, end};
    }
}
