package com.maptracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

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

import com.maptracker.R;

public class GMapsShareLocationActivity extends ActionBarActivity implements
        OnMapReadyCallback, ConnectionCallbacks, LocationListener {

    // =============================================================================================
    // Properties
    // =============================================================================================

    private static final String TAG = "Tracker - GMaps Share";
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mShareButton;

    // Google API - Locations
    private GoogleApiClient mGoogleApiClient;

    // Google Maps
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;

    // PubNub
    private PubNub mPubNub;
    private String channelName  = "channel-west";

    Switch swAutoZoom;
    ImageView ivChat;
    boolean isAutoZoom = true;

    // =============================================================================================
    // Activity Life Cycle
    // =============================================================================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmaps_view);

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
                        if (status.isError()){
                            System.out.println("grant failed!");
                        } else {
                            System.out.println("Grant passed!");
                            System.out.println(result);
                        }
                    }
                });

        swAutoZoom = (Switch) findViewById(R.id.swAutoZoom);
        swAutoZoom.setChecked(isAutoZoom);

        swAutoZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAutoZoom =  isChecked;
            }
        });

        ivChat = (ImageView) findViewById(R.id.ivChat);
        ivChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GMapsShareLocationActivity.this,ChattingActivity.class));
            }
        });
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
        Log.d(TAG, "Location Detected");
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Broadcast information on PubNub Channel
        PubNubManager.broadcastLocation(mPubNub, channelName, location.getLatitude(),
                location.getLongitude(), location.getAltitude());

        // Update Map
        updateCamera();
        updatePolyline();
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
        if(isAutoZoom == true && mGoogleMap !=null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
        }
    }
}
