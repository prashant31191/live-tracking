package com.drawpath.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by prashant.chovatiya on 3/10/2018.
 */

public class MyLatLng extends RealmObject {

    @SerializedName("area")
    public String area;

    @SerializedName("latitude")
    public  double latitude;

    @SerializedName("longitude")
    public  double longitude;

    public MyLatLng(){}
    public MyLatLng(String area, double latitude, double longitude) {
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
