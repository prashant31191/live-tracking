package com.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by prashant.patel on 10/26/2017.
 */

public class CommentListModel extends RealmObject {


    @SerializedName("channel")
    public  String strChannel = "";

    @SerializedName("name")
    public  String strName = "";

    @SerializedName("message")
    public  String strMessage = "";

    public CommentListModel(){}

    public CommentListModel(String strMessage, String strName, String strChannel) {
        this.strChannel = strChannel;
        this.strName = strName;
        this.strMessage = strMessage;
    }
}
