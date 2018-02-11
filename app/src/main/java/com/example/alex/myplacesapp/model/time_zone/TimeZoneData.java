package com.example.alex.myplacesapp.model.time_zone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 09.02.2018.
 */

public class TimeZoneData {
    @SerializedName("dstOffset")
    @Expose
    private String dstOffset;
    @SerializedName("rawOffset")
    @Expose
    private String rawOffset;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("timeZoneId")
    @Expose
    private String timeZoneId;
    @SerializedName("timeZoneName")
    @Expose
    private String timeZoneName;

    public String getDstOffset() {
        return dstOffset;
    }

    public void setDstOffset(String dstOffset) {
        this.dstOffset = dstOffset;
    }

    public String getRawOffset() {
        return rawOffset;
    }

    public void setRawOffset(String rawOffset) {
        this.rawOffset = rawOffset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }
}
