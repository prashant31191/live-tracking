package google.ads;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActAdsInterstitial extends Activity {

    private static final int START_LEVEL = 1;
    private int mLevel;
    private Button mNextLevelButton;
    private InterstitialAd mInterstitialAd;
    private TextView mLevelTextView,app_title;

    Bundle extras = null;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_ads_interstitial);
        // Create the next level button, which tries to show an interstitial when clicked.
        mNextLevelButton = ((Button) findViewById(R.id.next_level_button));
        // Create the text view to show the level number.
        app_title = (TextView) findViewById(R.id.app_title);
        mLevelTextView = (TextView) findViewById(R.id.level);



        extras = getIntent().getExtras();
        String interstitial_ad_unit_id = "";
        String banner_ad_unit_id = "";

        if (extras != null) {
            interstitial_ad_unit_id = extras.getString("interstitial_ad_unit_id");
            banner_ad_unit_id = extras.getString("banner_ad_unit_id");
            // and get whatever type user account id is
        }

        if(interstitial_ad_unit_id !=null && interstitial_ad_unit_id.length() > 0){
            AdsDisplayUtil.setStrAdsIntId(interstitial_ad_unit_id);
        }
        else {
            AdsDisplayUtil.setStrAdsIntId(getString(R.string.interstitial_ad_unit_id));
        }
        if(banner_ad_unit_id !=null && banner_ad_unit_id.length() > 0){
            AdsDisplayUtil.setStrAdsBnrId(banner_ad_unit_id);
        }
        else {
            AdsDisplayUtil.setStrAdsBnrId(getString(R.string.banner_ad_unit_id));
        }



        mNextLevelButton.setEnabled(false);
        app_title.setText("Loading.....");
        mNextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterstitial();
            }
        });


        mLevel = START_LEVEL;

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        setDisplayBanner();

    }

    private void setDisplayBanner()
    {


        //String deviceid = tm.getDeviceId();

        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(AdsDisplayUtil.getStrAdsBnrId());

        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.test);
        layout.addView(mAdView);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("33BE2250B43518CCDA7DE426D04EE232")
                .build();
        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        //.addTestDevice(deviceid).build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);




       /* mAdView = (AdView) findViewById(R.id.adView);
        //mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(AdsDisplayUtil.getStrAdsBnrId());
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("33BE2250B43518CCDA7DE426D04EE232")
                .build();
        mAdView.loadAd(adRequest);*/


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.i("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.i("Ads", "onAdClosed");
            }
        });
    }

    int i=0;

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(AdsDisplayUtil.getStrAdsIntId());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if(i==0)
                {
                    mNextLevelButton.performClick();
                }
                i = i+1;

                mNextLevelButton.setEnabled(true);
                app_title.setText("Click on NEXT LEVEL - "+i);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mNextLevelButton.setEnabled(true);
                app_title.setText("Oops, Please try after some time");
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                goToNextLevel();
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            goToNextLevel();
        }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        mNextLevelButton.setEnabled(false);
        app_title.setText("Loading.....");
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        mLevelTextView.setText("Level " + (++mLevel));
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }
}
