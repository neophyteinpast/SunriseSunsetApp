package com.example.alex.myplacesapp.model;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 06.02.2018.
 */

public class TimeZoneOffset {

    public static String findTimeZone(String cityName) {

        Set<String> allZones = ZoneId.getAvailableZoneIds();
        List<String> zoneList = new ArrayList<>(allZones);
        Collections.sort(zoneList);
        for (String s: zoneList) {
            if (cityName.equals("Newark")) {
                return "GMT";
            }
            if (s.contains(findAndReplace(cityName))) {
                return s;
            }
        }
        return null;
    }

    private static String findAndReplace(String cityName) {
        if (cityName.contains(" ")) {
            System.out.println("findAndReplace(): " + cityName);
            return cityName.replace(" ", "_");
        }
        System.out.println("cityName = " + cityName);
        return cityName;
    }

    private static void getZonedTimeOffset() {

    }
}
