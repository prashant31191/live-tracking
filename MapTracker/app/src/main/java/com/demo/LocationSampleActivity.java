package com.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.azsdk.location.utils.CLocation;
import com.azsdk.location.utils.ErrorModel;
import com.azsdk.location.utils.MyLocationService;
import com.azsdk.location.utils.ResponseModel;
import com.maptracker.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Formatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationSampleActivity extends AppCompatActivity {
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.tvData)
    TextView tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_sample);
        ButterKnife.bind(this);

        tvData.setText("Please wait...");

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                startService(new Intent(LocationSampleActivity.this, MyLocationService.class));

                tvData.setText("Clear");
            }
        });


    }


    @Override
    public void onStart() {
        try {
            super.onStart();
            EventBus.getDefault().unregister(this);
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        try {
            super.onStop();
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.tvData)
    public void clicktvData(TextView view) {
        view.setText("Clear");
    }

    int counter = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseModel(ResponseModel responseModel) {

        Log.i("==onResponseModel==", "=====Call--1--==");
        counter = counter + 1;
        if (responseModel != null) {
            tvData.append(counter + " # OK \n" +
                    "Lat : " + responseModel.getLocationLatLong().getLatitude() +
                    "Lng : " + responseModel.getLocationLatLong().getLongitude() +
                    "macAdd : " + responseModel.getMacAdressId() +
                    "\n\n"

            );

            if (responseModel.getLocationLatLong().hasSpeed()) {
            /*progressBarCircularIndeterminate.setVisibility(View.GONE);*/
                String speed = String.format(Locale.ENGLISH, "%.0f", responseModel.getLocationLatLong().getSpeed() * 3.6) + "km/h";

              /*  if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                    speed = String.format(Locale.ENGLISH, "%.0f", responseModel.getLocationLatLong().getSpeed() * 3.6 * 0.62137119) + "mi/h";
                }
                */
                SpannableString s = new SpannableString(speed);
                s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 4, s.length(), 0);
                tvData.append("===Speed=1==" + s);
            } else if (responseModel.getLocationLatLong() != null) {
                CLocation cLocation = new CLocation(responseModel.getLocationLatLong());
                updateSpeed(cLocation);
            }
        }

    }

    ;


    /*  @Subscribe
      public void onResponseModel2(ResponseModel responseModel) {
          Log.i("==onResponseModel2==","=====Call--2--==");
          counter = counter + 1;
          if(responseModel !=null) {
              tvData.append(counter + " # OK \n"+
                      "Lat : "+responseModel.getLocationLatLong().getLatitude()+
                      "Lng : "+responseModel.getLocationLatLong().getLongitude()+
                      "macAdd : "+responseModel.getMacAdressId()+
                      "\n\n"

              );
          }

      };*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorModel(ErrorModel errorModel) {
        Log.i("==onErrorModel==", "=====Call--3--==");
        counter = counter + 1;
        if (errorModel != null) {
            tvData.append(counter + " # Error \n" +
                    "Error : " + errorModel.getException().getMessage() +
                    "Code : " + errorModel.getStatusCode() +
                    "\n\n"

            );
        }
    }

    ;


    int i = 0;
    String strLog = "";

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        try {

            i = i + 1;
            Log.i("111", "====updateSpeed=====i===" + i);
            // Log.i("111","====location=====getLongitude==="+location.getLongitude());
            // Log.i("111","====location=====getLatitude==="+location.getLatitude());


            float nCurrentSpeed = 0;

            if (location != null) {
                location.setUseMetricunits(true);
                nCurrentSpeed = location.getSpeed();
            }

            Formatter fmt = new Formatter(new StringBuilder());
            fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
            String strCurrentSpeed = fmt.toString();
            strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

            String strUnits = "miles/hour";


            strLog = strLog +
                    "\n--------------\n " +
                    strCurrentSpeed + " " + strUnits +
                    "\n--------------\n ";

            tvData.append("\n Speed2 = " + strCurrentSpeed + " " + strUnits);

           /* float speed = Float.parseFloat(strCurrentSpeed);
            if(speed<100)
                mGaugeView.setTargetValue(speed);
            else
            {
                tvLog.setText("out of speed"+strLog);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
