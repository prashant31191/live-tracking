package com.maptracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maptracker.PubNubManager;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.maptracker.R;
import com.utils.PreferencesKeys;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.maptracker.PubNubManager.*;

public class GMapsFollowLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

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


    private static final String TAG = "Tracker - GMaps Follow";

    private boolean isFirstMessage = true;
    private boolean isAutoZoom = true;
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mFollowButton;

    // Google Maps
    private GoogleMap mGoogleMap;
    //	private Polyline mPolyline;
    private PolylineOptions mPolylineOptions;
    private Marker mMarker;
    private MarkerOptions mMarkerOptions;
    private LatLng mLatLng;

    // PubNub
    private PubNub mPubNub;
    private String channelName = "channel-west";


    String speed = "";

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

            if (App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo) != null && App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo).length() > 1) {
                channelName = App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo);
                Log.d(TAG, "Passed Channel Name--sharePrefrences--: " + channelName);
            }

            //swAutoZoom = (Switch) findViewById(R.id.swAutoZoom);
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
                    startActivity(new Intent(GMapsFollowLocationActivity.this, ChattingActivity.class));
                }
            });


            rlMoreDetailTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (rlMoreDetailTag.isSelected() == true) {
                        rlMoreDetailTag.setSelected(false);
                        rlDetails.setVisibility(View.GONE);
                        tvMoreDetailTag.setText("Show detail");
                        tvAddress.setText("Fetch address");
                    } else {
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
                    if (mLatLng != null) {
                        getAddressFromLocation(mLatLng.latitude, mLatLng.longitude);
                    }
                }
            });


            // Set up View: Map & Action Bar
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // auto start view
                    Log.d(TAG, "'Follow Friend's Location' Button Pressed");
                    mRequestingLocationUpdates = !mRequestingLocationUpdates;
                    if (mRequestingLocationUpdates) {
                        startFollowingLocation();
                        mFollowButton.setTitle("Stop Viewing Your Friend's Location");
                    }
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.follow, menu);
        mFollowButton = menu.findItem(R.id.follow_locations);
        return true;
    }

    // =========================================================================
    // Map CallBacks
    // =========================================================================

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);
        Log.d(TAG, "Map Ready");
    }

    // =========================================================================
    // Button CallBacks
    // =========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.follow_locations:
                Log.d(TAG, "'Follow Friend's Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startFollowingLocation();
                    mFollowButton.setTitle("Stop Viewing Your Friend's Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopFollowingLocation();
                    mFollowButton.setTitle("Start Viewing Your Friend's Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);





        }
    }


    private void startFollowingLocation() {
        initializePolyline();

        // Start PubNub
        mPubNub = PubNubManager.startPubnub();
        mPubNub.grant()
                .channels(Arrays.asList(channelName, "channel-east"))
                //.authKeys(Arrays.asList("Authkey-555", "Authkey-666"))
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


        mPubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                Log.e("====", "====status====status========" + status);
                if (status.getOperation() != null) {
                    switch (status.getOperation()) {
                        // let's combine unsubscribe and subscribe handling for ease of use
                        case PNSubscribeOperation:
                        case PNUnsubscribeOperation:
                            // note: subscribe statuses never have traditional
                            // errors, they just have categories to represent the
                            // different issues or successes that occur as part of subscribe
                            switch (status.getCategory()) {
                                case PNConnectedCategory:
                                    // this is expected for a subscribe, this means there is no error or issue whatsoever
                                case PNReconnectedCategory:
                                    // this usually occurs if subscribe temporarily fails but reconnects. This means
                                    // there was an error but there is no longer any issue
                                case PNDisconnectedCategory:
                                    // this is the expected category for an unsubscribe. This means there
                                    // was no error in unsubscribing from everything
                                case PNUnexpectedDisconnectCategory:
                                    // this is usually an issue with the internet connection, this is an error, handle appropriately
                                case PNAccessDeniedCategory:
                                    // this means that PAM does allow this client to subscribe to this
                                    // channel and channel group configuration. This is another explicit error
                                default:
                                    // More errors can be directly specified by creating explicit cases for other
                                    // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                            }

                        case PNHeartbeatOperation:
                            // heartbeat operations can in fact have errors, so it is important to check first for an error.
                            // For more information on how to configure heartbeat notifications through the status
                            // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                            if (status.isError()) {
                                // There was an error with the heartbeat operation, handle here
                                Log.e("====", "====status====isError Yes========" + status);
                            } else {
                                // heartbeat operation was successful
                                Log.e("====", "====status====isError No========" + status);
                            }
                        default: {
                            // Encountered unknown status type
                        }
                    }
                } else {
                    // After a reconnection see status.getCategory()
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                Log.e("====", "====message======");
                try {
                    Log.e("====", "====message======" + message);
                    Log.e("====", "====message======" + message.getMessage());
                    Log.e("====", "====message======" + message.getChannel());
                    Log.e("====", "====message======" + message.getPublisher());


                    JSONObject mainObject = null;

                    mainObject = new JSONObject(message.getMessage().toString());


                    // {"message":"hello how are you ?","id":"1","alt":"0.0","name":"Josh","data":"add any data","lng":"72.501697","usertype":"1","lat":"23.0019311"}

                    if (mainObject.has("speed")) {

                        speed = mainObject.getString("speed");

                    }
                    if (mainObject.has("lat")) {
                        double mLat = mainObject.getDouble("lat");
                        double mLng = mainObject.getDouble("lng");


                        Log.e("====", "====mLat======" + mLat);
                        Log.e("====", "====mLng======" + mLng);

                        mLatLng = new LatLng(mLat, mLng);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updatePolyline();
                            updateCamera();
                            updateMarker();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                Log.e("====", "====presence======");
            }
        });
        mPubNub.subscribe()
                .channels(Arrays.asList(channelName)) // subscribe to channels
                .execute();


    }

    private void stopFollowingLocation() {
        //111
        mPubNub.unsubscribeAll();
        isFirstMessage = true;
    }

    // =========================================================================
    // Map Editing Methods
    // =========================================================================

    private void initializePolyline() {
        mGoogleMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        mGoogleMap.addPolyline(mPolylineOptions);

        mMarkerOptions = new MarkerOptions();
    }

    private void updatePolyline() {
        if (mLatLng != null && mGoogleMap != null) {


            tvLocation.setText(
                    "Lat : " + mLatLng.latitude +
                            "\nLan : " + mLatLng.longitude
            );


            tvSpeed.setText("Speed : " + speed + "KM/H ");

            mPolylineOptions.add(mLatLng);
            mGoogleMap.clear();
            mGoogleMap.addPolyline(mPolylineOptions);
        }
    }

    private void updateCamera() {
        if (mLatLng != null && isAutoZoom == true) {
            mGoogleMap
                    .animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
        }
    }

    private void updateMarker() {
//		if (!isFirstMessage) {
//			isFirstMessage = false;
//			mMarker.remove();
//		}
        if (mLatLng != null && mGoogleMap != null) {
            mMarker = mGoogleMap.addMarker(mMarkerOptions.position(mLatLng));
        }
    }


    private void getAddressFromLocation(double latitude, double longitude) {
        try {
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

            if (addresses.get(0) != null) {

                if (addresses.get(0).getAddressLine(0) != null) {
                    fullAddress = fullAddress + "\nAddress : " + addresses.get(0).getAddressLine(0);
                }

                if (addresses.get(0).getLocality() != null) {
                    fullAddress = fullAddress + "\nCity : " + addresses.get(0).getLocality();
                }
                if (addresses.get(0).getAdminArea() != null) {
                    fullAddress = fullAddress + "\nstate : " + addresses.get(0).getAdminArea();
                }
                if (addresses.get(0).getCountryName() != null) {
                    fullAddress = fullAddress + "\ncountry : " + addresses.get(0).getCountryName();
                }
                if (addresses.get(0).getPostalCode() != null) {
                    fullAddress = fullAddress + "\npostalCode : " + addresses.get(0).getPostalCode();
                }
                if (addresses.get(0).getFeatureName() != null) {
                    fullAddress = fullAddress + "\nknownName : " + addresses.get(0).getFeatureName();
                }
            }

            tvLocation.setText(
                    "Lat : " + latitude +
                            "\nLan : " + longitude
            );

            tvAddress.setText(
                    "---Last fetch address---" +
                            "\nLat : " + latitude +
                            "\nLan : " + longitude +
                            fullAddress
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
