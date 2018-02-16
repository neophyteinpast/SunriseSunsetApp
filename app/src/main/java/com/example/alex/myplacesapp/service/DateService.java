package com.example.alex.myplacesapp.service;

import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Alex on 07.02.2018.
 */

public class DateService {
    private static final String TAG = DateService.class.getSimpleName();
    private static final String DATE_FORMAT = "y-MM-dd";
    private static final String PARSE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+00:00";
    private static final String TIME_FORMAT = "hh:mm:ss a";

    public static String getCurrentDate() {
        Date currentDate = new Date();
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(currentDate);
    }

    // check if date == current date return "today"
    public static String getDateFormat(String date) {
        Date currentDate = new Date();
        if (date != null
                && date.equals(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(currentDate))) {
            Log.d(TAG, "getDateFormat(" + date + "): today" );
            return "today";
        } else return date;
    }

    public static String getDate(int year, int month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Log.d(TAG, "getDate(): year = " + year + ", month = " + month + ", day = " + day);
        return localDate.format(formatter);
    }

    public static String getZoneTime(String input, String timeZone) {
        LocalDateTime localDateTime =
                LocalDateTime.parse(input, DateTimeFormatter.ofPattern(PARSE_DATE_FORMAT));
        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.of("+00:00"));

        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
        ZoneOffset realZoneOffset = zonedDateTime.getOffset(); // create zoneOffset of +05:00
        OffsetDateTime correctedOffsetDateTime =
                offsetDateTime.withOffsetSameInstant(realZoneOffset);

        // get time
        LocalTime localTime = correctedOffsetDateTime.toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        return localTime.format(formatter);
    }

    public static String getTime(long seconds) {
        LocalTime localTime = LocalTime.ofSecondOfDay(seconds);
        int hours = localTime.getHour();
        int minutes = localTime.getMinute();
        return String.format("Day length: %d hours, %d minutes", hours, minutes);
    }

    public static String getFormattedDateTime(String dateInString, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.now(zoneId);
        String timeInString = localDateTime.format(dateTimeFormatter);
        return String.format(
                "%s. Current time: %s.",
                getFormattedDate(dateInString), timeInString);
    }

    private static String getFormattedDate(String dateInString) {
        LocalDate localDate = LocalDate.parse(dateInString);
        String monthName = localDate.getMonth().name();
        int day = localDate.getDayOfMonth();
        int year = localDate.getYear();
        return String.format(Locale.ENGLISH, "%s %d, %d", monthName, day, year);
    }
}
