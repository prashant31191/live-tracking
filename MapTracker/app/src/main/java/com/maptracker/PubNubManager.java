package com.maptracker;

/**
 * Created by norvan on 1/22/15.
 */

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.utils.DateTimeUtil;
import com.utils.PreferencesKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PubNubManager {

    private static LatLng mLatLng;
    public static final String GOOGLE_MAP_KEY = "AIzaSyDbma9fzwvndbIxfWfxJYTO-5hvyuAblMk";     // Replace with your GOOGLE MAP key
    public static  String UUID_NAME = "jonny_android";     // Replace with your publish key
    public static  String UUID_MessageText = "=Message=";     // Replace with your publish key



    public static final String PUBNUB_PUBLISH_KEY = "pub-c-e2a499c9-c269-44ec-8d75-487c10193115";     // Replace with your publish key
    public static final String PUBNUB_SUBSCRIBE_KEY = "sub-c-e60fdc70-8a2b-11e7-91ed-aa3b4df5deac";   // Replace with your subscribe key
    public static final String PUBNUB_SECRET_KEY = "sec-c-ZmM3YjBhNjctNmZmYi00OTliLWJkNmYtYjM5MjFjYjdkNGU3";   // Replace with your sceret key



    // public static final String PUBNUB_AUTH_KEY = "Authkey-555";   // Replace with your auth key
    // public static final String CHANNEL_NAME = "channel-west";     // replace with more meaningful channel name

    public final static String TAG = "==PUBNUB==";

    public static PubNub startPubnub() {
        Log.d(TAG, "Initializing PubNub");

        if(App.sharePrefrences.getStringPref(PreferencesKeys.strUserName) !=null && App.sharePrefrences.getStringPref(PreferencesKeys.strUserName).length() > 1)
        {
            UUID_NAME = App.sharePrefrences.getStringPref(PreferencesKeys.strUserName);
        }

        PNConfiguration config = new PNConfiguration();

        config.setPublishKey(PubNubManager.PUBNUB_PUBLISH_KEY);
        config.setSubscribeKey(PubNubManager.PUBNUB_SUBSCRIBE_KEY);
        config.setSecretKey(PubNubManager.PUBNUB_SECRET_KEY);

        //config.setAuthKey(PubNubManager.PUBNUB_AUTH_KEY);
        config.setUuid(UUID_NAME);

        config.setSecure(true);

        Log.e("==UUID_NAME=","==app=====>>"+UUID_NAME);

        return new PubNub(config);
    }

    public static void broadcastLocation(PubNub pubnub, String channelName, double latitude,double longitude, double altitude,float speed) {

        JSONObject json_message = new JSONObject();
        try {

            json_message.put("sender", UUID_NAME);
            json_message.put("message", UUID_MessageText);
            json_message.put("timestamp", DateTimeUtil.getTimeStampUtc());


            json_message.put("lat", latitude);
            json_message.put("lng", longitude);
            json_message.put("alt", altitude);
            json_message.put("speed", ""+speed);





        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        /*slatitude = ""+latitude;
        slongitude = ""+longitude;
        saltitude = ""+altitude;*/

        /*final Map<String, String> message = ImmutableMap.<String, String>of(

                "sender", "app",
                "lat", "23.0018051",
                "lng", "72.5017013"
              *//*  "message", mMessage.getText().toString(),
                "timestamp", DateTimeUtil.getTimeStampUtc()*//*
        );*/

        Map<String, String> message = new HashMap<String, String>();
        message.put("name", UUID_NAME);
        message.put("id", "1");
        message.put("usertype", "1");
        message.put("data", "add any data");
        message.put("message", UUID_MessageText);
        message.put("lat", ""+latitude);
        message.put("lng", ""+longitude);
        message.put("alt", ""+altitude);
        message.put("timestamp", ""+DateTimeUtil.getTimeStampUtc());
        message.put("speed", ""+speed);


        Log.d(TAG, "Sending JSON ####broadcastLocation#### Message: " + json_message.toString());
        //pubnub.publish(channelName, UUID_MessageText, publishCallback);
        pubnub.addListener(publishCallback);
       // pubnub.publish().channel(CHANNEL_NAME).message(message).async(
        pubnub.publish().channel(channelName).message(message).async(
                new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        try {
                            if (!status.isError()) {
                              //111  Log.v(TAG, "publish(" + JsonUtil.asJson(result) + ")");
                            } else {
                              //111  Log.v(TAG, "publishErr(" + JsonUtil.asJson(status) + ")");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
        );

    }


    public static void broadcastLocationChat(PubNub pubnub, String channelName,String strMessage) {

        JSONObject json_message = new JSONObject();
        double latitude=0,longitude=0,altitude=0;

        try {


            json_message.put("sender", UUID_NAME);
            json_message.put("message", UUID_MessageText);
            json_message.put("timestamp", DateTimeUtil.getTimeStampUtc());


            json_message.put("lat", latitude);
            json_message.put("lng", longitude);
            json_message.put("alt", altitude);





        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        /*slatitude = ""+latitude;
        slongitude = ""+longitude;
        saltitude = ""+altitude;*/

        /*final Map<String, String> message = ImmutableMap.<String, String>of(

                "sender", "app",
                "lat", "23.0018051",
                "lng", "72.5017013"
              *//*  "message", mMessage.getText().toString(),
                "timestamp", DateTimeUtil.getTimeStampUtc()*//*
        );*/

        Map<String, String> message = new HashMap<String, String>();
        message.put("name", UUID_NAME);
        message.put("id", "1");
        message.put("usertype", "1");
        message.put("data", "add any data");
        message.put("message", strMessage);
        message.put("lat", ""+latitude);
        message.put("lng", ""+longitude);
        message.put("alt", ""+altitude);
        message.put("timestamp", ""+DateTimeUtil.getTimeStampUtc());


        Log.d(TAG, "Sending JSON ####broadcastLocation#### Message: " + json_message.toString());
        //pubnub.publish(channelName, UUID_MessageText, publishCallback);
        pubnub.addListener(publishCallback);
        // pubnub.publish().channel(CHANNEL_NAME).message(message).async(
        pubnub.publish().channel(channelName).message(message).async(
                new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        try {
                            if (!status.isError()) {
                                //111  Log.v(TAG, "publish(" + JsonUtil.asJson(result) + ")");
                            } else {
                                //111  Log.v(TAG, "publishErr(" + JsonUtil.asJson(status) + ")");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
        );

    }



    public static SubscribeCallback publishCallback = new SubscribeCallback() {

        @Override
        public void status(PubNub pubnub, PNStatus status) {

        }

        @Override
        public void message(PubNub pubnub, PNMessageResult message) {

            try {
                Log.v(TAG, "message---send--->>(" + message.getMessage() + ")");
                 //111       Log.v(TAG, "message--->>(" + JsonUtil.asJson(message) + ")");


              /*  JSONObject jsonMessage = (JSONObject) message;
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                mLatLng = new LatLng(mLat, mLng);

                Log.d("PUBNUB==1==", "--SET Lat--"+mLat);
                Log.d("PUBNUB==2==", "--SET Lng--"+mLng);*/

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

        }

        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            Log.v(TAG, "presence--->>(" + presence + ")");
        }

    };

}
