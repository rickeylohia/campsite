package com.ric.campsite.util;

public class StringUtil {

    public static boolean isBlankOrNull(String value) {
        return value == null || value.isBlank();
    }
}
