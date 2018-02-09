package com.example.alex.myplacesapp.service;

import com.example.alex.myplacesapp.model.PlaceData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Alex on 01.02.2018.
 */

public interface PlaceDataClient {
    @GET("details/json")
    Call<PlaceData> getPlace(
            @Query("placeid") CharSequence placeId,
            @Query("key") String key
    );

    @GET("details/json")
    Call<ResponseBody> getPlaceResponse(
            @Query("placeid") CharSequence placeId,
            @Query("key") String key
    );
}
