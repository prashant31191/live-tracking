package com.maptracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mapanim.MapsActivity;

import google.ads.AdsDisplayUtil;
import io.fabric.sdk.android.Fabric;

import com.maptracker.R;
import com.utils.PreferencesKeys;

public class MainActivity extends ActionBarActivity {


    private EditText etMobileNo,etName;

    private String channelName="",userName="Android-1";
    private static final String TAG = "Tracker - MainActivity";

    // ==============================================================================
    // Activity Life Cycle
    // ==============================================================================
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        etName = (EditText) findViewById(R.id.etName);
        etMobileNo = (EditText) findViewById(R.id.etMobileNo);

        if(App.sharePrefrences.getStringPref(PreferencesKeys.strUserName) !=null)
        {
            etName.setText(App.sharePrefrences.getStringPref(PreferencesKeys.strUserName));
        }
        if(App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo) !=null)
        {
            etMobileNo.setText(App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo));
        }

      /*  etMobileNo.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            channelName = etMobileNo.getText().toString().trim();
                            String message = "Chosen channel: " + channelName;
                            Toast.makeText(MainActivity.this, message,
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, message);
                            return true;
                        default:
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ==============================================================================
    // Button Actions
    // ==============================================================================

    public void shareLocation(View view) {
            Log.d(TAG, "Share Location With Google Maps Chosen on channel: "
                    + channelName);
            callActivity(GMapsShareLocationActivity.class);
    }

    public void followLocation(View view) {
        Log.d(TAG, "Follow Location With Google Maps Chosen on channel: "
                    + channelName);
            callActivity(GMapsFollowLocationActivity.class);
    }

    private void callActivity(Class<?> cls) {

        channelName = etMobileNo.getText().toString().trim();
        userName = etName.getText().toString().trim();
        String message = "Mobile number : " + channelName;


        if(channelName !=null && channelName.length() > 0) {

            Toast.makeText(MainActivity.this, message,Toast.LENGTH_SHORT).show();

            App.sharePrefrences.setPref(PreferencesKeys.strUserName,userName);
            App.sharePrefrences.setPref(PreferencesKeys.strUserMobileNo,channelName);

            logUser();

            Intent intent = new Intent(this, cls);
            intent.putExtra("channel", channelName);
            startActivity(intent);

            AdsDisplayUtil.openBnrIntAdsScreen(MainActivity.this, "", "");
        }
        else
        {
            message = "Please enter mobile number";
            //forceCrash();

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

/*
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("channel", channelName);
            startActivity(intent);
*/


        }
    }
    public void forceCrash() {
        throw new RuntimeException("This is a crash");
    }

    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(channelName);
        Crashlytics.setUserEmail(channelName);
        Crashlytics.setUserName(userName);
    }


}



