package com.example.alex.myplacesapp.service;

import com.example.alex.myplacesapp.model.Location;
import com.example.alex.myplacesapp.model.time_zone.TimeZoneData;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Alex on 09.02.2018.
 */

public interface TimeZoneClient {
    @GET
    Call<TimeZoneData> getTimeZone(@Url String url,
                                   @Query("location") Location location,
                                   @Query("timestamp") long timestamp,
                                   @Query("key") String key);
}
