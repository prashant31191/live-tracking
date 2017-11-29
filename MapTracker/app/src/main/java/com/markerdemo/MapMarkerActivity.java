package com.markerdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.maptracker.App;

import com.maptracker.R;
import com.markerdemo.api.ApiService;
import com.markerdemo.api.MapMarkersResponse;
import com.markerdemo.utils.CustomMarkerModel;
import com.markerdemo.utils.MarkerManager;
import com.markerdemo.utils.NetworkMarker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Prashant on 28-11-2017.
 */

public class MapMarkerActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMyLocationButtonClickListener {

    String TAG = MapMarkerActivity.class.getName();
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    Context mContext;


    // for the add marker with images
    private MarkerManager<NetworkMarker> networkMarkerManager;


    RecyclerView recyclerView;
    TextView tvNodata;
    ImageView ivMap, ivList;
    ProgressBar progressBar;
    RelativeLayout rlList;
    FrameLayout flMap;

    ApiService apiService;
    Call callApiMethod;


    String strNeLat = "33.637333985576554";
    String strNeLng = "-118.08047050008543";
    String strSwLat = "33.6816273140889";
    String strSwLng = "-117.91713469991453";

    ArrayList<MapMarkersResponse> arrayListMapMarkersResponse = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_marker);

        initViews();

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

    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ivMap = (ImageView) findViewById(R.id.ivMap);
        ivList = (ImageView) findViewById(R.id.ivList);
        rlList = (RelativeLayout) findViewById(R.id.rlList);
        flMap = (FrameLayout) findViewById(R.id.flMap);

        tvNodata = (TextView) findViewById(R.id.tvNodata);
        tvNodata.setVisibility(View.GONE);
        tvNodata.setText("No data found");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MapMarkerActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        mContext = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        apiService = App.getApiService();

        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapView(true);
            }
        });


        ivList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapView(false);
            }
        });
        ivList.performClick();
        asyncGetMapListData();
    }

    private void showMapView(boolean isMap) {
        if (isMap == true) {
            rlList.setVisibility(View.GONE);
            flMap.setVisibility(View.VISIBLE);
        } else {
            rlList.setVisibility(View.VISIBLE);
            flMap.setVisibility(View.GONE);
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


    private ArrayList<NetworkMarker> createNetworkMarkers(ArrayList<MapMarkersResponse> arrList) {


        ArrayList<NetworkMarker> networkMarkers = new ArrayList<>();

        for (MapMarkersResponse mMapMarkersResponse : arrList) {
            CustomMarkerModel customMarkerModel = new CustomMarkerModel();
            customMarkerModel.uid = mMapMarkersResponse.disp_id;

            double latitude = Double.parseDouble(mMapMarkersResponse.disp_latitude);
            double longitude = Double.parseDouble(mMapMarkersResponse.disp_longitude);
            LatLng location = new LatLng(latitude, longitude);

            customMarkerModel.latLng = location;
            customMarkerModel.imgUrl = mMapMarkersResponse.disp_image;
            customMarkerModel.data = mMapMarkersResponse.disp_name;
            customMarkerModel.detail = mMapMarkersResponse.disp_price_1_8_oz;


            networkMarkers.add(new NetworkMarker(this, customMarkerModel));
        }


        //NetworkMarker
        return networkMarkers;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        App.showLog("==", "========OnMapReady============");
        networkMarkerManager = new MarkerManager<>(googleMap);
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

        App.showLog(TAG + "=====onConnected=====");
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
            App.showLog(TAG, "ON connected");

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
        App.showLog(TAG + "=====onConnectionSuspended=====");
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        App.showLog(TAG + "=====onLocationChanged=====");
        try {
            if (location != null)
                changeMap(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void changeMap(Location location) {

        App.showLog(TAG, "Reaching map" + mMap);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

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

            strSwLat = "" + location.getLatitude();
            strSwLng = "" + location.getLongitude();

            App.showLog("333333333====Lat : " + strSwLat + "," + "Long : " + strSwLng);

            //11---
           /* updateLocation(new LatLng(location.getLatitude(), location.getLongitude()),false);
            mLocationMarkerText.setText("##--##Lat : " + strLat + "," + "Long : " + strLong);
*/

            asyncGetMapListData();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        App.showLog(TAG + "=====onConnectionFailed=====");
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        App.showLog(TAG + "=====onMyLocationButtonClick=====");
        if (mMap != null) {
            mMap.stopAnimation();
            Location myloc = mMap.getMyLocation();
            if (myloc != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myloc.getLatitude(), myloc.getLongitude())));

                strSwLat = "" + myloc.getLatitude();
                strSwLng = "" + myloc.getLongitude();

                App.showLog("11111111====Lat : " + strSwLat + "," + "Long : " + strSwLng);
            }

        }
        return true;
    }


    public void asyncGetMapListData() {

        try {
            progressBar.setVisibility(View.VISIBLE);

            //  HashMap hashMap = new HashMap();
            /*
            hashMap.put("nelat",strNeLat);
            hashMap.put("nelng",strNeLng);
            hashMap.put("swlat",strSwLat);
            hashMap.put("swlng",strSwLng);
*/
            RequestBody r_strNeLat = App.createPartFromString(strNeLat);
            RequestBody r_strNeLng = App.createPartFromString(strNeLng);
            RequestBody r_strSwLat = App.createPartFromString(strSwLat);
            RequestBody r_strSwLng = App.createPartFromString(strSwLng);

            HashMap<String, RequestBody> hashMap = new HashMap<>();
            hashMap.put("nelat", r_strNeLat);
            hashMap.put("nelng", r_strNeLng);
            hashMap.put("swlat", r_strSwLat);
            hashMap.put("swlng", r_strSwLng);

            //RequestBody avatarBody =RequestBody.create(MediaType.parse("image"),profilePhotoFile.toString());

            App.createPartFromString(strNeLng);


            //TypedString
            //   RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),hashMap);

            callApiMethod = apiService.getMarkersList(
                    "testing",
                    hashMap

            );


            callApiMethod.enqueue(new Callback<ArrayList<MapMarkersResponse>>() {
                @Override
                public void onResponse(Call<ArrayList<MapMarkersResponse>> call, Response<ArrayList<MapMarkersResponse>> response) {
                    try {
                        progressBar.setVisibility(View.GONE);


                        ArrayList<MapMarkersResponse> model = response.body();
                        if (model == null) {
                            //404 or the response cannot be converted to User.
                            App.showLog("---null response--", "==Something wrong=");
                            ResponseBody responseBody = response.errorBody();
                            if (responseBody != null) {
                                try {
                                    App.showLog("---error-", "" + responseBody.string());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            //200 sucess
                            App.showLog("===Response== " + response.body().toString());
                            App.showLog("==**==Success==**==asyncGetMapListData==> ", new Gson().toJson(response.body()));

                            if (model != null && model.size() > 0) {
                                arrayListMapMarkersResponse = model;
                                MapDataAdapter mapDataAdapter = new MapDataAdapter(MapMarkerActivity.this, arrayListMapMarkersResponse);
                                recyclerView.setAdapter(mapDataAdapter);
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                                recyclerView.setVisibility(View.VISIBLE);
                                tvNodata.setVisibility(View.GONE);

                                if (networkMarkerManager != null) {
                                    networkMarkerManager.clear();
                                    networkMarkerManager.addMarkers(createNetworkMarkers(arrayListMapMarkersResponse));
                                } else {
                                    App.showLog("======networkMarkerManager==NULL=====");
                                }
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                tvNodata.setVisibility(View.VISIBLE);

                                if (networkMarkerManager != null) {
                                    networkMarkerManager.clear();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(Call<ArrayList<MapMarkersResponse>> call, Throwable t) {

                    t.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    tvNodata.setVisibility(View.VISIBLE);


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            tvNodata.setVisibility(View.VISIBLE);
        }
    }


    public class MapDataAdapter extends RecyclerView.Adapter<MapDataAdapter.VersionViewHolder> {
        ArrayList<MapMarkersResponse> mArrListMapMarkersResponse;
        Context mContext;


        public MapDataAdapter(Context context, ArrayList<MapMarkersResponse> arrayListFollowers) {
            mArrListMapMarkersResponse = arrayListFollowers;
            mContext = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.raw_maplist, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
            try {
                MapMarkersResponse mapMarkersResponse = mArrListMapMarkersResponse.get(i);

                versionViewHolder.tvTitle.setText(mapMarkersResponse.disp_name);
                versionViewHolder.tvDetails.setText(mapMarkersResponse.disp_city + " " + mapMarkersResponse.disp_state);

                float fRating = 0f;
                fRating = Float.parseFloat(mapMarkersResponse.disp_rating);
                versionViewHolder.rbStars.setRating(fRating);

                Picasso.with(MapMarkerActivity.this).load(mapMarkersResponse.disp_image).fit().centerCrop().into(versionViewHolder.ivIcon);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mArrListMapMarkersResponse.size();
        }


        class VersionViewHolder extends RecyclerView.ViewHolder {

            TextView tvTitle, tvDetails;
            RelativeLayout rlMain;
            ImageView ivIcon;
            RatingBar rbStars;


            public VersionViewHolder(View itemView) {
                super(itemView);


                rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                tvDetails = (TextView) itemView.findViewById(R.id.tvDetails);
                rbStars = (RatingBar) itemView.findViewById(R.id.rbStars);

            }

        }
    }
}
