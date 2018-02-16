package com.example.alex.myplacesapp.model;

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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
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
