package com.models;

/**
 * Created by prashant.patel on 10/26/2017.
 */

public class CommentListModel {
    public  String strName = "";
    public  String strMessage = "";


    public CommentListModel(String strMessage, String strName) {
        this.strName = strName;
        this.strMessage = strMessage;
    }
}
