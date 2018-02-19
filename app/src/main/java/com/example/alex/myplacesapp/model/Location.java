package com.example.alex.myplacesapp.model;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Created by Alex on 01.02.2018.
 */

public class Location {
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;

    public Location() {}

    public Location(@NonNull LatLng latLng) {
        lat = latLng.latitude;
        lng = latLng.longitude;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%.1f,%.2f", lat, lng);
    }

    public Double round(Double value, int places) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(value));
        return bigDecimal.setScale(places, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
