package io.atlanticlab.pubnubmaptracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import google.ads.AdsDisplayUtil;

public class MainActivity extends ActionBarActivity {


    private EditText channelEditText,nameEditText;

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

        channelEditText = (EditText) findViewById(R.id.channelEditText);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
      /*  channelEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            channelName = channelEditText.getText().toString().trim();
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

        channelName = channelEditText.getText().toString().trim();
        userName = nameEditText.getText().toString().trim();
        String message = "Mobile number : " + channelName;


        if(channelName !=null && channelName.length() > 0) {

            Toast.makeText(MainActivity.this, message,
                    Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(this, cls);
            intent.putExtra("channel", channelName);
            startActivity(intent);

            AdsDisplayUtil.openBnrIntAdsScreen(MainActivity.this, "", "");
        }
        else
        {
            message = "Please enter mobile number";

            Toast.makeText(MainActivity.this, message,
                    Toast.LENGTH_SHORT).show();
        }
    }

}



