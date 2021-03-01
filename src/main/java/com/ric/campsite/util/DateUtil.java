package com.ric.campsite.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateUtil {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate parse(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    public static String format(LocalDate date) {
        return dateFormatter.format(date);
    }

}
