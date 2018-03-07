package com.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maptracker.App;
import com.maptracker.R;
import com.azsdk.ads.AdsDisplayUtil;
import com.azsdk.ads.AdsLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.widget.Toast.LENGTH_SHORT;

public class AdsSampleActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.tvData)
    TextView tvData;
    @BindView(R.id.rlAds)
    RelativeLayout rlAds;

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

                AdsLoader.loadIntAds(AdsSampleActivity.this, App.getRandomIntId());

            }
        });


        //When click open Full screen ads
        //For Interstitial Ads

       /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //Load Interstitial Ads
                AdsLoader.loadIntAds(AdsSampleActivity.this, App.getRandomIntId());
            }
        });*/


        // code write in on create
        AdsLoader.setIntAdsNull();

    }

    @OnClick(R.id.tvData)
    public void clicktvData(TextView view) {
        view.setText("-clicktvData-getRandomBannerId--1");

        AdsLoader.loadBannerAds(AdsSampleActivity.this, rlAds, App.getRandomBannerId());
    }

    @OnLongClick(R.id.tvData)
    public boolean longClicktvData(TextView view) {
        view.setText("-longClicktvData-getRandomIntId--1");
        AdsLoader.loadIntAds(AdsSampleActivity.this, App.getRandomIntId());
        return true;
    }
    @OnLongClick(R.id.fab)
    public boolean longClickFab(FloatingActionButton view) {
        AdsDisplayUtil.openBnrIntAdsScreen(AdsSampleActivity.this, App.getRandomBannerId(), App.getRandomIntId());
        return true;
    }

/*
    @OnLongClick(R2.id.hello)
    boolean sayGetOffMe() {
        Toast.makeText(this, "Let go of me!", LENGTH_SHORT).show();
        return true;
    }*/


}
