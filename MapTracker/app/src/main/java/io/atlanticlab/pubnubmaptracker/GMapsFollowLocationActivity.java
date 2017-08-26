package io.atlanticlab.pubnubmaptracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class GMapsFollowLocationActivity extends ActionBarActivity implements
        OnMapReadyCallback {

    // =========================================================================
    // Properties
    // =========================================================================

    private static final String TAG = "Tracker - GMaps Follow";

    private boolean isFirstMessage = true;
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
    private String channelName  = "channel-west";

    // =========================================================================
    // Activity Life Cycle
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmaps_view);

        // Get Channel Name
        Intent intent = getIntent();
        channelName = intent.getExtras().getString("channel");
        Log.d(TAG, "Passed Channel Name: " + channelName);

        // Set up View: Map & Action Bar
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
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

                    double mLat = mainObject.getDouble("lat");
                    double mLng = mainObject.getDouble("lng");


                    Log.e("====", "====mLat======" + mLat);
                    Log.e("====", "====mLng======" + mLng);

                    mLatLng = new LatLng(mLat, mLng);


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
        mPolylineOptions.add(mLatLng);
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updateCamera() {
        mGoogleMap
                .animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }

    private void updateMarker() {
//		if (!isFirstMessage) {
//			isFirstMessage = false;
//			mMarker.remove();
//		}
        mMarker = mGoogleMap.addMarker(mMarkerOptions.position(mLatLng));
    }
}
