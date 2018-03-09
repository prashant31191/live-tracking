package com.utils;

import java.util.ArrayList;

import drawpath.model.SelectPlaceModel;

/**
 * Created by Admin on 12/5/2016.
 */

public class AppFlags
{



    // for the pubnum and live tracking
    public static String strRemainingKmToReach = "0";
    public static String strTravelledKmFromPickup= "0";
    public static String strRideStatus= "1";
    public static int  intReachRange = 200;



    public static String tagTotalRouteTimeTextHMS = "total_route_time_HMS";

    public static String tagFrom = "tagFrom";

    public static String strNetError = "Oops, no internet connection";

    public static int  intOutRange = 500;
    //For the added feed route list
    public static ArrayList<SelectPlaceModel> arrayListSelectPlaceModel = new ArrayList<>();

    public static float totalMeterRoute = 1;
}
