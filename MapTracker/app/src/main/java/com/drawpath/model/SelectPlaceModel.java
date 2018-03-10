package com.drawpath.model;

//import com.google.android.gms.maps.model.MyLatLng;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class SelectPlaceModel extends RealmObject {

    @SerializedName("strPoint")
    public String strPoint="1";

    @SerializedName("strAddress")
    public String strAddress="";

    @SerializedName("strHint")
    public String strHint="Place ";

    @SerializedName("latLng")
    public MyLatLng latLng = null;

    public SelectPlaceModel(){}
    public SelectPlaceModel(String strPoint, String strAddress, String strHint, MyLatLng latLng)
    {
        this.strPoint = strPoint;
        this.strAddress = strAddress;
        this.strHint = strHint;
        this.latLng = latLng;
    }

}
