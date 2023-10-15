package com.acme.authorization.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private static final DateFormat displayableDateTime = new SimpleDateFormat("E, dd-MMM-yyyy HH:mm");
    private static final DateFormat displayableDate = new SimpleDateFormat("E, dd-MMM-yyyy");
    private static DateFormat utcDate = null;
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

    public static String displayDateTime(Date date) {
        try {
            return displayableDateTime.format(date);
        } catch (Exception e){
            System.err.println(e.getMessage());
            return "Incorrect date";
        }
    }

    public static String displayDate(Date date) {
        try {
            return displayableDate.format(date);
        } catch (Exception e){
            System.err.println(e.getMessage());
            return "Incorrect date";
        }
    }

    static void initUtcDate(){
        if (utcDate == null) {
            utcDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            utcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    public static String formattedUtcDate(Date date) {
        initUtcDate();
        try {
            return utcDate.format(date);
        } catch (Exception e){
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static Date fromUtc(String date) {
        initUtcDate();
        try {
            return utcDate.parse(date);
        } catch (Exception e){
            System.err.println(e.getMessage());
            return null;
        }
    }
}
