package com.drawpath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maptracker.App;
import com.maptracker.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.utils.AppFlags;
import com.utils.DirectionsJSONParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by prashant.patel on 8/31/2017.
 */

public class ActFeedAddDetail extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = "ActFeedAddDetail ";
    private GoogleMap mMap;
    RelativeLayout rlBackFeedList;
    Context mContext;
    FloatingActionButton fbExpandCollapse;
    NestedScrollView nsvData;

    TextView tvKms, tvRouteFrom, tvRouteTo, tvPost;
    MaterialEditText etDate, etTime, etMessage;

    String strRouteFrom = "";
    String strRouteTo = "";



    int year = 2000, month = 1, day = 1;
    int cYear = 2000, cMonth = 1, cDay = 1;
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
    String date1 = "", strDate = "";
    Calendar currentDateCalender;

    LatLng latLongStart = null;
    LatLng latLongEnd = null;

    int mHour = -2, mMinute = -2;
    String strTime = "";

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ViewGroup.inflate(this, R.layout.act_feed_add_detail, llContainerSub);

        try {

            setContentView(R.layout.act_feed_add_detail);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mContext = this;



            initialization();
            setClickEvent();
            setFonts();

            mapFragment.getMapAsync(this);


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

        setAddAllMarkers();
    }

    private void initialization() {
        AppFlags.totalMeterRoute = 0;
        
        rlBackFeedList = (RelativeLayout) findViewById(R.id.rlBackFeedList);

        tvKms = (TextView) findViewById(R.id.tvKms);
        tvRouteFrom = (TextView) findViewById(R.id.tvRouteFrom);
        tvRouteTo = (TextView) findViewById(R.id.tvRouteTo);
        tvPost = (TextView) findViewById(R.id.tvPost);

        etDate = (MaterialEditText) findViewById(R.id.etDate);
        etTime = (MaterialEditText) findViewById(R.id.etTime);
        etMessage = (MaterialEditText) findViewById(R.id.etMessage);

        fbExpandCollapse = (FloatingActionButton) findViewById(R.id.fbExpandCollapse);
        nsvData = (NestedScrollView) findViewById(R.id.nsvData);

        fbExpandCollapse.setImageResource(R.drawable.ic_expand_less_green_24dp);
        fbExpandCollapse.setSelected(true);
        nsvData.setVisibility(View.GONE);





    }


    private void setClickEvent() {
        try {
            rlBackFeedList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            fbExpandCollapse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fbExpandCollapse.isSelected() == true)
                    {
                        fbExpandCollapse.setImageResource(R.drawable.ic_expand_more_green_24dp);
                        fbExpandCollapse.setSelected(false);
                        nsvData.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        fbExpandCollapse.setImageResource(R.drawable.ic_expand_less_green_24dp);
                        fbExpandCollapse.setSelected(true);
                        nsvData.setVisibility(View.GONE);
                    }
                }
            });


            etTime.setFocusable(false);
            etTime.setLongClickable(false);
            etTime.setClickable(true);

            etDate.setFocusable(false);
            etDate.setLongClickable(false);
            etDate.setClickable(true);

            etDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePickerDialog();
                }
            });

            etTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openTimePicker();
                }
            });


            // for the date picker

            currentDateCalender = Calendar.getInstance();

            date1 = outputFormat.format(currentDateCalender.getTime());


            year = currentDateCalender.get(Calendar.YEAR);
            month = currentDateCalender.get(Calendar.MONTH);
            day = currentDateCalender.get(Calendar.DAY_OF_MONTH);


            cYear = currentDateCalender.get(Calendar.YEAR);
            cMonth = currentDateCalender.get(Calendar.MONTH) + 1;
            cDay = currentDateCalender.get(Calendar.DAY_OF_MONTH);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void setFonts() {

        etDate.setTypeface(App.getFont_Regular());
        etTime.setTypeface(App.getFont_Regular());
        etMessage.setTypeface(App.getFont_Regular());

        tvKms.setTypeface(App.getFont_Regular());
        tvRouteFrom.setTypeface(App.getFont_Regular());
        tvRouteTo.setTypeface(App.getFont_Regular());
        tvPost.setTypeface(App.getFont_Bold());

    }


    private Marker customMarker;

    public void setAddAllMarkers() {
        try {
            mMap.clear();
            App.showLog("=========setAddAllMarkers===click=========");
            App.showLog("=========--AppFlags.arrayListSelectPlaceModel.size();-========" + AppFlags.arrayListSelectPlaceModel.size());

            for (int i = 0; i < AppFlags.arrayListSelectPlaceModel.size(); i++) {
                App.showLog("=====0000======" + i);

                if(i==0)
                {
                    strRouteFrom = AppFlags.arrayListSelectPlaceModel.get(i).strAddress;
                }

                if(i == AppFlags.arrayListSelectPlaceModel.size() - 1)
                {
                    strRouteTo = AppFlags.arrayListSelectPlaceModel.get(i).strAddress;

                    tvRouteFrom.setText("From : "+strRouteFrom);
                    tvRouteTo.setText("To : "+strRouteTo);
                    tvRouteFrom.setSelected(true);
                    tvRouteTo.setSelected(true);
                }



                if (AppFlags.arrayListSelectPlaceModel.get(i).latLng != null && AppFlags.arrayListSelectPlaceModel.get(i).strAddress.length() > 0) {
                    App.showLog("===if==0000======" + i);

                    latLongStart = AppFlags.arrayListSelectPlaceModel.get(0).latLng;
                    latLongEnd = AppFlags.arrayListSelectPlaceModel.get(AppFlags.arrayListSelectPlaceModel.size() - 1).latLng;

                    View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_feed_marker_layout, null);
                    TextView tvMarkerNumber = (TextView) markerView.findViewById(R.id.tvMarkerNumber);
                    tvMarkerNumber.setText("" + (i + 1));

                    customMarker = mMap.addMarker(new MarkerOptions()
                            .position(AppFlags.arrayListSelectPlaceModel.get(i).latLng)
                            .title(AppFlags.arrayListSelectPlaceModel.get(i).strAddress)
                            .snippet(AppFlags.arrayListSelectPlaceModel.get(i).strHint)
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(createDrawableFromView(markerView))));

                    App.showLog("=====i==" + i + "==i===LatLng======" + AppFlags.arrayListSelectPlaceModel.get(i).latLng);
                    if (i >= 1) {
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(AppFlags.arrayListSelectPlaceModel.get((i - 1)).latLng, AppFlags.arrayListSelectPlaceModel.get(i).latLng);
                        //String url = getMapsApiDirectionsUrl();
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }

                } else {
                    App.showLog("===else==0000======" + i);
                    App.showLog("=====null==latong===i==" + i);
                }

            }


            //   latLong = new LatLng(location.getLatitude(), location.getLongitude());

            /*
            if (latLongStart != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLongStart).zoom(12f).tilt(70).build();
                mMap.setPadding((int) App.pxFromDp(ActFeedDetail.this, 8), (int) App.pxFromDp(ActFeedDetail.this, 45), 0, 0); // (int left, int top, int right, int bottom)
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }*/


      /*  LatLng origin =  new LatLng(23.001933188854736,72.50203315168619);
        LatLng dest = new LatLng(23.003237097331496,72.50363811850546);

        getDirectionsUrl(origin,dest);*/

            mMap.setPadding((int) App.pxFromDp(ActFeedAddDetail.this, 8), (int) App.pxFromDp(ActFeedAddDetail.this, 45), 0, 0); // (int left, int top, int right, int bottom)

           /*
            no click effect show

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return true;
                }
            });*/


          /*  new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMapAnimation();
                }
            },10000);*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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


    //For Time picker

    private void openTimePicker() {
        try {
            // Get Current Time
            Calendar c = Calendar.getInstance();

            if (mHour < 0)
                mHour = c.get(Calendar.HOUR_OF_DAY);
            if (mMinute < 0)
                mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            strTime = App.dateConvert24to12Hour(hourOfDay + ":" + minute);
                            App.showLog("====strTime====" + strTime);
                            etTime.setText(strTime);

                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // END time picker


    //For Date picker

    static final int DATE_PICKER_ID = 1111;
    private String format = "";

    public void datePickerDialog() {
        showDialog(DATE_PICKER_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_PICKER_ID:

                // open datepicker dialog.
                // set date picker for current date
                // add pickerListener listner to date picker
              /*  DatePickerDialog datepickerdialog = new DatePickerDialog(this,
                        AlertDialog.THEME_HOLO_DARK ,pickerListener,year,month,day);*/

                return new DatePickerDialog(this, pickerListener, year, month, day);
        }
        return null;
    }


    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            try {
                //String date1;
                year = selectedYear;
                month = selectedMonth + 1;
                day = selectedDay;


                SimpleDateFormat dateFromate = new SimpleDateFormat("MM-dd-yyyy");
                Date dateSelected = dateFromate.parse(month + "-" + day + "-" + year);
                Date dateCurrent = dateFromate.parse(cMonth + "-" + cDay + "-" + cYear);

                System.out.println("dateSelected" + dateFromate.format(dateSelected));
                System.out.println("dateCurrent" + dateFromate.format(dateCurrent));


                if (dateSelected.after(dateCurrent)) {
                    System.out.println("Date1 is after Date2");

                    // Show selected date
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
                    String inputDateStr = (month) + "-" + (day) + "-" + (year);
                    Date date = null;
                    date = inputFormat.parse(inputDateStr);
                    date1 = outputFormat.format(date);
                    etDate.setText(date1);
                    //sendDateApi = (year) + "-" + (month) + "-" + (day);
                    strDate = (day) + "-" + (month) + "-" + (year);

                    App.showLog("==click==strDOB=" + strDate);
                } else {
                    App.showLog("==Please select valid date==strDOB=" + strDate);
                    App.showToastShort(ActFeedAddDetail.this, "Please select valid date.");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    // for the route drawing....

    ////////////////////////// Only for Pickup - Drop Lat/Long path///////////////////////////////////////
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        //String str_origin = "origin="+ tagPrevPickLatLng;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //String str_dest = "destination="+ tagPrevDropLatLng;


        // Sensor enabled
        String sensor = "alternatives=true&sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        App.showLog("==url== " + url);


        return url;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                App.showLog("=====Background Task=====", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            App.showLog("=====Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            try {
                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);

                 //       listAllLatLong.add(position);
                        App.showLog("==Map=====" + position.latitude + "," + position.longitude);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(6);
                    //lineOptions.color(Color.BLUE);
                    lineOptions.color(0x80000000);

                }

                // Drawing polyline in the Google Map for the i-th route
                if(lineOptions !=null) {
                    mMap.addPolyline(lineOptions);
                }
                setMapAnimation();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////


    //done route


    //List<LatLng> listAllLatLong = new ArrayList<>();
    private void setMapAnimation()
    {
        try
        {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    try {
                       /* LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(latLongStart);
                        builder.include(latLongEnd);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                        mMap.moveCamera(cu);
                       mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
*/


                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLongStart).zoom(19f).tilt(70).build();

                        if (ActivityCompat.checkSelfPermission(ActFeedAddDetail.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActFeedAddDetail.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));


                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                   // startAnim();
                }
            });

            float totalKm = AppFlags.totalMeterRoute / 1000;
            tvKms.setText(""+String.format("%.02f", totalKm));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void startAnim(){
        if(mMap != null) {
           // MapAnimator.getInstance().animateRoute(mMap, listAllLatLong);
        } else {
            Toast.makeText(getApplicationContext(), "Map not ready", Toast.LENGTH_LONG).show();
        }
    }

    public void resetAnimation(View view){
        startAnim();
    }

}
