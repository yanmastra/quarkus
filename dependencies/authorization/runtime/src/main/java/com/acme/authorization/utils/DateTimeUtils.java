package com.acme.authorization.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    public static Date getExpiredToken() {
        String expInMinutes = System.getProperty("access_token_expired_in", "30");
        int exp = Integer.parseInt(expInMinutes);
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, exp);
        return calendar.getTime();
    }

    public static Date getExpiredRefreshToken() {
        String expInMinutes = System.getProperty("refresh_token_expired_in", "4320");
        int exp = Integer.parseInt(expInMinutes);
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, exp);
        return calendar.getTime();
    }
}
