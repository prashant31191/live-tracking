package com.markerdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.maptracker.App;
import com.maptracker.R;
import com.oguzbabaoglu.fancymarkers.BitmapGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by prashant.patel on 4/26/2017.
 */

public class NetworkMarker extends CustomMarker { //implements Target {

    private static final String URL = "http://lorempixel.com/200/200?seed=";
    private static volatile int seed; // Bypass cache

    private LatLng position;
    private String strSnippet;
    private View view;
    private ImageView markerImage;
    private TextView tvMarkerData;
    private ImageView markerBackground;

    private Context mContext;

    CompanyLogoTarget target = null;
    String imageUrl = "";


    public NetworkMarker(Context context, LatLng position, String imageUrl2) {
        mContext = context;
        this.position = position;
        this.strSnippet = imageUrl2;

        view = LayoutInflater.from(context).inflate(R.layout.view_network_marker, null);
        markerImage = (ImageView) view.findViewById(R.id.marker_image);
        markerBackground = (ImageView) view.findViewById(R.id.marker_background);

        App.showLog("=========NetworkMarker=======imageUrl==="+imageUrl);
        App.showLog("===CompanyLogoTarget==target====");
        imageUrl = imageUrl2;
        target = new CompanyLogoTarget();

        view.setTag(imageUrl);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.showLog("===view==click======"+view.getTag());
            }
        });
    }


  //  public NetworkMarker(Context context, MyLatLng position, String imageUrl2) {
  public NetworkMarker(Context context, CustomMarkerModel customMarkerModel) {
        mContext = context;
        this.position = customMarkerModel.latLng;
        this.strSnippet = customMarkerModel.data;

        view = LayoutInflater.from(context).inflate(R.layout.view_network_marker, null);
      tvMarkerData = (TextView) view.findViewById(R.id.tvMarkerData);
        markerImage = (ImageView) view.findViewById(R.id.marker_image);
        markerBackground = (ImageView) view.findViewById(R.id.marker_background);

       /* if(customMarkerModel.data !=null && customMarkerModel.data.contains("END@!@#@"))
        {
            markerBackground.setImageResource(R.drawable.ic_pin_green);
        }
        else
        {
            markerBackground.setImageResource(R.drawable.ic_pin_gray);
        }*/



      tvMarkerData.setText("$ "+customMarkerModel.detail);

        App.showLog("=========NetworkMarker=======imageUrl==="+imageUrl);
        App.showLog("===CompanyLogoTarget==target====");
        imageUrl = customMarkerModel.imgUrl;
        target = new CompanyLogoTarget();

        view.setTag(imageUrl);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.showLog("===view==click======"+view.getTag());
            }
        });
    }

    @Override
    public void onAdd() {
        Picasso.with(mContext).load(imageUrl).into(target);
        super.onAdd();
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        return BitmapGenerator.fromView(view);
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getSnippet() {
        return strSnippet;
    }

    @Override
    public boolean onStateChange(boolean selected) {

        if (selected) {
            markerBackground.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        } else {
            markerBackground.clearColorFilter();
        }

        return true;
    }


    private class CompanyLogoTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            App.showLog("===CompanyLogoTarget==onBitmapLoaded====");
            Log.v("IMG Downloader", "onBitmapLoaded ...");
            try {

                Log.v("IMG Downloader", "Writing image data...");
           /* OutputStream os = mContext.getContentResolver().openOutputStream(uri, "w");
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            Log.v("IMG Downloader", "Compressed? : " + compressed);
            os.close();*/


                //final Bitmap bitmap = response.getBitmap();

                // Set image and update view
                markerImage.setImageBitmap(bitmap);
                updateView();


            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("App","Success to load company logo in onBitmapLoaded method");
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            App.showLog("===CompanyLogoTarget==onBitmapFailed====");
            Log.v("IMG Downloader", "Bitmap Failed...");
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            App.showLog("===CompanyLogoTarget==onPrepareLoad====");
            Log.v("IMG Downloader", "Bitmap Preparing Load...");
        }

    }


}
