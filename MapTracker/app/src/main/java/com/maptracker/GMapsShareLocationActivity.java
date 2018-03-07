package com.maptracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.azsdk.location.utils.CLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maptracker.PubNubManager;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.maptracker.R;
import com.utils.AppFlags;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GMapsShareLocationActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, LocationListener {


    @BindView(R.id.swAutoZoom)
    Switch swAutoZoom;

    @BindView(R.id.ivChat)
    ImageView ivChat;



    @BindView(R.id.rlMoreDetailTag)
    RelativeLayout rlMoreDetailTag;

    @BindView(R.id.tvMoreDetailTag)
    TextView tvMoreDetailTag;

    @BindView(R.id.rlDetails)
    RelativeLayout rlDetails;

    @BindView(R.id.rlLocation)
    RelativeLayout rlLocation;

    //SPEED
    @BindView(R.id.tvSpeed)
    TextView tvSpeed;
    @BindView(R.id.ivSpeed)
    ImageView ivSpeed;

    //LOCATION / ADDRESS
    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.tvAddress)
    TextView tvAddress;





    private static final String TAG = "Tracker - GMaps Share";
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mShareButton;

    // Google API - Locations
    private GoogleApiClient mGoogleApiClient;

    // Google Maps
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;
    private LatLng previousLatLong;

    // PubNub
    private PubNub mPubNub;
    private String channelName = "channel-west";

    boolean isAutoZoom = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_gmaps_view);
            ButterKnife.bind(this);

            // Get Channel Name
            Intent intent = getIntent();
            channelName = intent.getExtras().getString("channel");
            Log.d(TAG, "Passed Channel Name: " + channelName);

            // Start Google Client
            this.buildGoogleApiClient();

            // Set up View: Map & Action Bar
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            // Start PubNub
            mPubNub = com.maptracker.PubNubManager.startPubnub();

            mPubNub.grant()
                    .channels(Arrays.asList(channelName, "channel-east"))
                    //  .authKeys(Arrays.asList("Authkey-555", "Authkey-666"))
                    .manage(true)
                    .read(true)
                    .write(true)
                    .async(new PNCallback<PNAccessManagerGrantResult>() {
                        @Override
                        public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                            if (status.isError()) {
                                System.out.println("grant failed!");
                            } else {
                                System.out.println("Grant passed!");
                                System.out.println(result);
                            }
                        }
                    });

            // swAutoZoom = (Switch) findViewById(R.id.swAutoZoom);
            swAutoZoom.setChecked(isAutoZoom);

            swAutoZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isAutoZoom = isChecked;
                }
            });

            //ivChat = (ImageView) findViewById(R.id.ivChat);
            ivChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(GMapsShareLocationActivity.this, ChattingActivity.class));
                }
            });


            rlMoreDetailTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(rlMoreDetailTag.isSelected() == true)
                    {
                        rlMoreDetailTag.setSelected(false);
                        rlDetails.setVisibility(View.GONE);
                        tvMoreDetailTag.setText("Show detail");
                        tvAddress.setText("Fetch address");
                    }
                    else
                    {
                        rlMoreDetailTag.setSelected(true);
                        rlDetails.setVisibility(View.VISIBLE);
                        tvMoreDetailTag.setText("Hide detail");
                    }

                }
            });

            rlMoreDetailTag.setSelected(true);
            rlDetails.setVisibility(View.VISIBLE);
            tvMoreDetailTag.setText("Hide detail");


            rlLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mLatLng !=null) {
                        getAddressFromLocation(mLatLng.latitude, mLatLng.longitude);
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        mShareButton = menu.findItem(R.id.share_locations);
        return true;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    // =============================================================================================
    // Map CallBacks
    // =============================================================================================

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);
        Log.d(TAG, "Map Ready");
    }

    // =============================================================================================
    // Google Location API CallBacks
    // =============================================================================================

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google API for Location Management");
        if (mRequestingLocationUpdates) {
            LocationRequest mLocationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            initializePolyline();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection to Google API suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Log.d(TAG, "Location Detected");



            if (previousLatLong != null && (App.isCheckReachLocation(AppFlags.intOutRange, location.getLatitude(), location.getLongitude(), previousLatLong.latitude, previousLatLong.longitude) == false)) {
                App.showLog("===========OUT from RANGE================");
            } else {
                App.showLog("===========In to RANGE================");
                //intOnLocationChanged = intOnLocationChanged + 1;
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                previousLatLong = mLatLng;

                // Broadcast information on PubNub Channel
                PubNubManager.broadcastLocation(mPubNub, channelName, location.getLatitude(),location.getLongitude(), location.getAltitude(),speed);

                // Update Map
                updateCamera();
                updatePolyline();
            }


            if (location !=null && location.hasSpeed()) {

                speed = (float) (location.getSpeed() * 3.6);


                Formatter fmt = new Formatter(new StringBuilder());
                fmt.format(Locale.US, "%5.1f", speed);
                String strCurrentSpeed = fmt.toString();
                strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

                String strUnits = "km/h";

            //    String speed = String.format(Locale.ENGLISH, "%.0f", speed) + "km/h";

              /*  if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                    speed = String.format(Locale.ENGLISH, "%.0f", responseModel.getLocationLatLong().getSpeed() * 3.6 * 0.62137119) + "mi/h";
                }
                */
                //SpannableString s = new SpannableString(speed);
                //s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 4, s.length(), 0);



                tvSpeed.setText("Speed : " + strCurrentSpeed + " " + strUnits);
                if(speed < 60)
                    ivSpeed.setSelected(false);
                else
                {
                    ivSpeed.setSelected(true);
                }
                tvLocation.setText("Lat : "+location.getLatitude() +"\nLan : "+location.getLongitude());

            } else if (location != null) {
                CLocation cLocation = new CLocation(location);
                updateSpeed(cLocation);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "Building request");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    // =============================================================================================
    // Button CallBacks
    // =============================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.share_locations:
                Log.d(TAG, "'Share Your Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startSharingLocation();
                    mShareButton.setTitle("Stop Sharing Your Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopSharingLocation();
                    mShareButton.setTitle("Start Sharing Your Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startSharingLocation() {
        Log.d(TAG, "Starting Location Updates");
        mGoogleApiClient.connect();
    }

    public void stopSharingLocation() {
        Log.d(TAG, "Stop Location Updates & Disconect to Google API");
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    // =============================================================================================
    // Map Editing Methods
    // =============================================================================================

    private void initializePolyline() {
        mGoogleMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updatePolyline() {
        mPolylineOptions.add(mLatLng);
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updateCamera() {
        if (isAutoZoom == true && mGoogleMap != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
        }
    }









    float speed = 0;

   // int i = 0;
    String strLog = "";

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        try {

           // i = i + 1;
            Log.i("111", "====updateSpeed=====");
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

            String strUnits = "KM/H";


            strLog = strLog +
                    "\n--------------\n " +
                    strCurrentSpeed + " " + strUnits +
                    "\n--------------\n ";

            tvSpeed.setText("Speed : " + strCurrentSpeed + " " + strUnits);
            speed = Float.parseFloat(strCurrentSpeed);
            if(speed < 60)
                ivSpeed.setSelected(false);
            else
            {
                ivSpeed.setSelected(true);
            }
            tvLocation.setText("Lat : "+location.getLatitude() +"\nLan : "+location.getLongitude());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void getAddressFromLocation(double latitude, double longitude)
    {
        try{
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5


            String fullAddress = "";

           /* String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
*/

            if(addresses.get(0) !=null) {

                if (addresses.get(0).getAddressLine(0) != null)
                {
                    fullAddress = fullAddress+"\nAddress : "+addresses.get(0).getAddressLine(0);
                }

                if (addresses.get(0).getLocality() != null)
                {
                    fullAddress = fullAddress + "\nCity : "+addresses.get(0).getLocality();
                }
                if (addresses.get(0).getAdminArea() != null)
                {
                    fullAddress = fullAddress + "\nstate : "+addresses.get(0).getAdminArea();
                }
                if (addresses.get(0).getCountryName() != null)
                {
                    fullAddress = fullAddress + "\ncountry : "+addresses.get(0).getCountryName();
                }
                if (addresses.get(0).getPostalCode() != null)
                {
                    fullAddress = fullAddress + "\npostalCode : "+addresses.get(0).getPostalCode();
                }
                if (addresses.get(0).getFeatureName() != null)
                {
                    fullAddress = fullAddress + "\nknownName : "+addresses.get(0).getFeatureName();
                }
            }

            tvLocation.setText(
                    "Lat : " + latitude +
                            "\nLan : " + longitude
            );

            tvAddress.setText(
                    "---Last fetch address---"+
                    "\nLat : " + latitude +
                            "\nLan : " + longitude +
                            fullAddress
            );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }











}
