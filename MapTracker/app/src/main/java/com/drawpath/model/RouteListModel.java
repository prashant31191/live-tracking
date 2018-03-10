package com.drawpath.model;

import com.google.gson.annotations.SerializedName;
import com.maptracker.App;

import java.io.Serializable;
import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class RouteListModel extends RealmObject implements Serializable {

    @SerializedName("title")
    public String title = "";

    @SerializedName("detail")
    public String detail = "";




    @PrimaryKey
    @Index
    @SerializedName("timestamp")
    public String timestamp = ""+ App.getCurrentDateTime();

    @SerializedName("favourite")
    public String favourite = "0";

    @SerializedName("route_kms")
    public String route_kms = "0";

    @SerializedName("route_from")
    public String route_from = "";

    @SerializedName("route_to")
    public String route_to = "";


    @SerializedName("main_route_list")
   public RealmList<SelectPlaceModel> realmListSelectPlaceModel;


/*
    @SerializedName("SelectPlaceModel")
    public SelectPlaceModel selectPlaceModel;*/

    public RouteListModel(String title, String detail)
    {
        this.title = title;
        this.detail = detail;
    }

    public RouteListModel(){}
}
