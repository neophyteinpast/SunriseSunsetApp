package com.example.alex.myplacesapp.service;

import com.example.alex.myplacesapp.model.SunriseSunset;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Alex on 02.02.2018.
 */

public interface SunriseSunsetClient {
    @GET()
    Call<SunriseSunset> getSunriseSunset(@Url String url,
                                         @Query("lat") double latitude,
                                         @Query("lng") double longtitude,
                                         @Query("date") String date,
                                         @Query("formatted") Integer formatted);
}
