package drawpath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maptracker.App;
import com.maptracker.R;
import com.utils.AppFlags;

import java.util.ArrayList;

import drawpath.model.SelectPlaceModel;

public class ActPostARoute extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    String TAG = "==ActPostARoute==";

    LinearLayout llListData;
    RecyclerView rvLocationPoints;
    FloatingActionButton fbAddPoint, fbDone;




    ArrayList<SelectPlaceModel> arrayListSelectPlaceModel = new ArrayList<>();
    NotificationAdapter notificationAdapter;


    int lastSelectedPos = 1;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    Context mContext;

    private LatLng mCenterLatLong;


    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;
    /**
     * The formatted location address.
     */
    String mAddressOutput;
    String mAreaOutput;
    String mCityOutput;
    String mStateOutput;
    TextView mLocationText;
  //  TextView mLocationMarkerText;
    TextView tvCMarkerNo;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    String strLat = "10.0";
    String strLong = "20.0";

    public LatLng latLng = null;
    SelectPlaceModel selectPlaceModel = new SelectPlaceModel();

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_post_a_route);
        //ViewGroup.inflate(this, R.layout.act_post_a_route, llContainerSub);

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mContext = this;
            initialization();
            setStaticData();

            setClickEvent();
            setFonts();

            mapFragment.getMapAsync(this);

            mResultReceiver = new AddressResultReceiver(new Handler());
            if (checkPlayServices()) {
                // If this check succeeds, proceed with normal processing.
                // Otherwise, prompt user to get valid Play Services APK.
                if (!App.isLocationEnabled(mContext)) {
                    // notify user
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setMessage("Location not enabled!");
                    dialog.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                }
                buildGoogleApiClient();
            } else {
                Toast.makeText(mContext, "Location not supported in this device", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO: handle exceptione.
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change" + "", cameraPosition + "");
                mCenterLatLong = cameraPosition.target;

                try {

                    Location mLocation = new Location("");
                    mLocation.setLatitude(mCenterLatLong.latitude);
                    mLocation.setLongitude(mCenterLatLong.longitude);

                    startIntentService(mLocation);

                    latLng = new LatLng(mCenterLatLong.latitude, mCenterLatLong.longitude);

                    strLat = "" + mCenterLatLong.latitude;
                    strLong = "" + mCenterLatLong.longitude;

                  //  mLocationMarkerText.setText("Lat : " + strLat + "," + "Long : " + strLong);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            changeMap(mLastLocation);
            Log.d(TAG, "ON connected");

        } else
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null)
                changeMap(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }*/
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }


    private void changeMap(Location location) {

        Log.d(TAG, "Reaching map" + mMap);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong;


            latLong = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(19f).tilt(70).build();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            latLng = new LatLng(location.getLatitude(), location.getLongitude());

            strLat = "" + location.getLatitude();
            strLong = "" + location.getLongitude();

            // mLocationMarkerText.setText("Lat : " + strLat + "," + "Long : " + strLong);
            startIntentService(location);


        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(App.LocationConstants.RESULT_DATA_KEY);

            mAreaOutput = resultData.getString(App.LocationConstants.LOCATION_DATA_AREA);

            mCityOutput = resultData.getString(App.LocationConstants.LOCATION_DATA_CITY);
            mStateOutput = resultData.getString(App.LocationConstants.LOCATION_DATA_STREET);

            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == App.LocationConstants.SUCCESS_RESULT) {
                //  showToast(getString(R.string.address_found));


            }


        }

    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        //  mLocationAddressTextView.setText(mAddressOutput);
        try {
            if (mAddressOutput != null) {

                if(mAddressOutput.length() < 2)
                {
                    mAddressOutput = "test 1231234";
                }

                App.showLog("====mAddressOutput-00===" + mAddressOutput);
                mAddressOutput = mAddressOutput.replaceAll("\\r\\n|\\r|\\n", ", ");
                mLocationText.setText(mAddressOutput + "");
                App.showLog("====mAddressOutput-11===" + mAddressOutput);

                selectPlaceModel = new SelectPlaceModel();

                //    arrayListSelectPlaceModel.add(new SelectPlaceModel("" + lastAddedPos + 1, "" , "Place " + lastAddedPos + 1, null));

                selectPlaceModel.strPoint = "" + lastSelectedPos + 1;
                selectPlaceModel.strAddress = mAddressOutput;
                selectPlaceModel.strHint = "Place " + lastSelectedPos + 1;
                selectPlaceModel.latLng = latLng;


                arrayListSelectPlaceModel.set(lastSelectedPos - 1, selectPlaceModel);

                notificationAdapter.notifyDataSetChanged();

                tvCMarkerNo.setText("" + lastSelectedPos);


                for (int i = 0; i < arrayListSelectPlaceModel.size(); i++) {
                    if (arrayListSelectPlaceModel.get(i).strAddress.length() < 1) {
                        //  lastSelectedPos = i+1;
                        fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                        fbDone.setTag("0");
                        if (i > 2) {
                            App.showSnackBar(rvLocationPoints, "Please choose your route #" + lastSelectedPos + " point.");
                        }
                        break;
                    } else {
                        if (i == (arrayListSelectPlaceModel.size() - 1)) {
                            fbDone.setImageResource(R.drawable.ic_done_black_24dp);
                            fbDone.setTag("1");
                        }
                    }
                }

                //rvLocationPoints.smoothScrollToPosition(lastSelectedPos - 1);
                //App.showSnackBar(rvLocationPoints, "" + mAddressOutput);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(App.LocationConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(App.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }


    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(mContext, data);
                // TODO call location based filter
                LatLng latLong;
                latLong = place.getLatLng();
                //

                mAddressOutput = place.getName().toString().replaceAll("\\r\\n|\\r|\\n", ", ");
                mLocationText.setText(mAddressOutput);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(19f).tilt(70).build();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));


            }


        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(mContext, data);
        } else if (resultCode == RESULT_CANCELED) {
            // Indicates that the activity closed before a selection was made. For example if
            // the user pressed the back button.
        }
    }

    // for the menu


    private void setStaticData() {
        arrayListSelectPlaceModel.add(new SelectPlaceModel("1", "", "Place 1", null));
        arrayListSelectPlaceModel.add(new SelectPlaceModel("2", "", "Place 2", null));
        arrayListSelectPlaceModel.add(new SelectPlaceModel("3", "", "Place 3", null));

        notificationAdapter = new NotificationAdapter(ActPostARoute.this, arrayListSelectPlaceModel);
        rvLocationPoints.setAdapter(notificationAdapter);
        rvLocationPoints.setItemAnimator(new DefaultItemAnimator());
    }


    private void initialization() {

        rvLocationPoints = (RecyclerView) findViewById(R.id.rvLocationPoints);
        fbAddPoint = (FloatingActionButton) findViewById(R.id.fbAddPoint);
        fbDone = (FloatingActionButton) findViewById(R.id.fbDone);
        //mLocationMarkerText = (TextView) findViewById(R.id.mLocationMarkerText);
        tvCMarkerNo = (TextView) findViewById(R.id.tvCMarkerNo);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActPostARoute.this);
        rvLocationPoints.setLayoutManager(linearLayoutManager);
        rvLocationPoints.setHasFixedSize(true);

        fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
        fbDone.setTag("0");
    }


    private void setClickEvent() {
        try {

            fbAddPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < arrayListSelectPlaceModel.size(); i++) {
                        if (arrayListSelectPlaceModel.get(i).strAddress.length() > 0) {
                            if (i == (arrayListSelectPlaceModel.size() - 1)) {
                                lastSelectedPos = (i + 2);
                                arrayListSelectPlaceModel.add(new SelectPlaceModel("" + (i + 1), "", "Place " + (i + 1), null));
                                notificationAdapter.notifyDataSetChanged();
                                rvLocationPoints.smoothScrollToPosition(i + 1);
                                fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                                fbDone.setTag("0");
                                setAddAllMarkers();
                                break;
                            }
                        } else {
                            lastSelectedPos = i + 1;
                            App.showSnackBar(rvLocationPoints, "Please choose your route #" + (i + 1) + " point.");
                            break;
                        }
                    }

                }
            });

          /*  fbDone.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    switchOnTethering();
                    return false;
                }
            });*/

            fbDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < arrayListSelectPlaceModel.size(); i++) {

                        if (arrayListSelectPlaceModel.get(i).strAddress.length() < 1) {
                            lastSelectedPos = i + 1;
                            fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                            fbDone.setTag("0");
                            App.showSnackBar(rvLocationPoints, "Please choose your route #" + lastSelectedPos + " point.");
                            break;
                        } else {
                            if (i == (arrayListSelectPlaceModel.size() - 1)) {
                                fbDone.setImageResource(R.drawable.ic_done_black_24dp);
                                fbDone.setTag("1");
                            }
                        }

                    }

                    if(fbDone.getTag().toString().equalsIgnoreCase("1"))
                    {
                        AppFlags.arrayListSelectPlaceModel = arrayListSelectPlaceModel;

                        Intent intent = new Intent(ActPostARoute.this,ActFeedAddDetail.class);
                        intent.putExtra(AppFlags.tagFrom,"ActPostARoute");
                        startActivity(intent);

                    }
                    else
                    {
                        setAddAllMarkers();
                    }
                }
            });


            for (int i = 0; i < arrayListSelectPlaceModel.size(); i++) {
                if (arrayListSelectPlaceModel.get(i).strAddress.length() < 1) {
                    lastSelectedPos = i + 1;
                    fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                    fbDone.setTag("0");
                    App.showSnackBar(rvLocationPoints, "Please choose your route #" + lastSelectedPos + " point.");
                    break;
                } else {
                    if (i == (arrayListSelectPlaceModel.size() - 1)) {
                        fbDone.setImageResource(R.drawable.ic_done_black_24dp);
                        fbDone.setTag("1");
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void setFonts() {
        //etCurrentPassword.setTypeface(App.getFont_Regular());
        tvCMarkerNo.setTypeface(App.getFont_Regular());

    }


    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VersionViewHolder> {
       // ArrayList<SelectPlaceModel> mArrListNotificationListModel;
        Context mContext;


        public
        NotificationAdapter(Context context, ArrayList<SelectPlaceModel> arrayListFollowers) {
            //mArrListNotificationListModel = arrayListFollowers;
            mContext = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.raw_post_route_place, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
            try {
                //SelectPlaceModel selectPlaceModel = mArrListNotificationListModel.get(i);
                SelectPlaceModel selectPlaceModel = arrayListSelectPlaceModel.get(i);
                versionViewHolder.tvNumber.setText("" + (i + 1));
                versionViewHolder.tvAddress.setText(selectPlaceModel.strAddress);
                //versionViewHolder.tvAddress.setHint(selectPlaceModel.strHint);
                versionViewHolder.tvAddress.setHint("Place " + (i + 1));
                versionViewHolder.ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItem(i);
                    }
                });

                if (mLocationText == null) {
                    App.showLog("==mLocationText==null==select point for====" + i);
                    mLocationText = versionViewHolder.tvAddress;
                    lastSelectedPos = i + 1;
                }

                if (lastSelectedPos > 2) {
                    mLocationText = versionViewHolder.tvAddress;
                }

                versionViewHolder.tvAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        App.showLog("====select point for====" + i);

                       // System.out.println("============" + (SelectPlaceModel) mArrListNotificationListModel.get(i));
                        System.out.println("============" + (SelectPlaceModel) arrayListSelectPlaceModel.get(i));
                        //    mLocationText = versionViewHolder.tvAddress;
                        //    lastSelectedPos = i+1;
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            //return mArrListNotificationListModel.size();
            if(arrayListSelectPlaceModel == null)
            {
                return 0;
            }
            return arrayListSelectPlaceModel.size();
        }


        public void removeItem(int position) {

           /* if (mArrListNotificationListModel.size() > 3) {
                mArrListNotificationListModel.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mArrListNotificationListModel.size());
                fbDone.setImageResource(R.drawable.ic_done_black_24dp);

                lastSelectedPos = mArrListNotificationListModel.size();
            } else {
                mArrListNotificationListModel.set(position, new SelectPlaceModel("" + (position + 1), "", "Place " + (position + 1), null));
                notifyItemRangeChanged(position, mArrListNotificationListModel.size());
                fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
            }*/

            if (arrayListSelectPlaceModel.size() > 3) {
                arrayListSelectPlaceModel.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, arrayListSelectPlaceModel.size());
                fbDone.setImageResource(R.drawable.ic_done_black_24dp);
                fbDone.setTag("1");

                lastSelectedPos = arrayListSelectPlaceModel.size();
            } else {
                arrayListSelectPlaceModel.set(position, new SelectPlaceModel("" + (position + 1), "", "Place " + (position + 1), null));
                notifyItemRangeChanged(position, arrayListSelectPlaceModel.size());
                fbDone.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                fbDone.setTag("0");
            }

            setAddAllMarkers();





        }


        class VersionViewHolder extends RecyclerView.ViewHolder {

            TextView tvNumber, tvAddress;
            RelativeLayout rlMainItem;
            ImageView ivClose;
            //View vDividerLine;

            public VersionViewHolder(View itemView) {
                super(itemView);

                rlMainItem = (RelativeLayout) itemView.findViewById(R.id.rlMainItem);
                tvNumber = (TextView) itemView.findViewById(R.id.tvNumber);
                tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
                ivClose = (ImageView) itemView.findViewById(R.id.ivClose);

                tvNumber.setTypeface(App.getFont_Bold());
                tvAddress.setTypeface(App.getFont_Regular());
            }

        }
    }




    private Marker customMarker;

    public void setAddAllMarkers() {
        mMap.clear();
        App.showLog("=========setAddAllMarkers===click=========");

        for (int i = 0; i < arrayListSelectPlaceModel.size(); i++) {
            App.showLog("=====0000======" + i);
            if (arrayListSelectPlaceModel.get(i).latLng != null && arrayListSelectPlaceModel.get(i).strAddress.length() > 0) {
                App.showLog("===if==0000======" + i);
                View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_feed_marker_layout, null);
                TextView tvMarkerNumber = (TextView) markerView.findViewById(R.id.tvMarkerNumber);
                tvMarkerNumber.setTypeface(App.getFont_Regular());
                tvMarkerNumber.setText("" + (i + 1));

                customMarker = mMap.addMarker(new MarkerOptions()
                        .position(arrayListSelectPlaceModel.get(i).latLng)
                        .title(arrayListSelectPlaceModel.get(i).strAddress)
                        .snippet(arrayListSelectPlaceModel.get(i).strHint)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(createDrawableFromView(markerView))));
            } else {
                App.showLog("===else==0000======" + i);
                App.showLog("=====null==latong===i==" + i);
            }

        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
    }
    public Bitmap createDrawableFromView(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

}