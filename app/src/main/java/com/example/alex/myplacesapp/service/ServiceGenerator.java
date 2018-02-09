package com.example.alex.myplacesapp.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Alex on 01.06.2017.
 */

public class ServiceGenerator {

    private static final String API_BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private static Retrofit retrofit;

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(API_BASE_URL);

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC);

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

    public static <S> S createService(Class <S> serviceClass) {
        if (!okHttpClient.interceptors().contains(logging)) {
            okHttpClient.addInterceptor(logging);
            builder.client(okHttpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }
}