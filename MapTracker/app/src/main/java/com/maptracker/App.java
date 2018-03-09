package com.maptracker;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.util.Base64;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.markerdemo.api.ApiService;
import com.markerdemo.utils.BasicAuthInterceptor;
import com.sendbird.android.SendBird;
import com.utils.BroadcastReceiver;
import com.utils.PreferencesKeys;
import com.utils.SharePrefrences;

import org.json.JSONArray;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.


    private static final String APP_ID_SENDBIRD = "B1CF96C5-B670-4672-8071-952972B781FE";
    private static final String TWITTER_KEY = "sdasd";
    private static final String TWITTER_SECRET = "asdqaweq2e41234123aedasd";

    public static String packageTrack = "com.aznews";


    public static String strApiUrl = "https://www.wikileaf.org/masterapi/";
    public static String username = "testuser";
    public static String password = "jzP!/8'x6+,ggxPC";

    //AIzaSyCJl2Hpk1iYW5OS51awouSbIUxU5gcRcFU
    //AIzaSyB1VPsu5_nBEX1DAIWyey-ZphC7Ee3FGas



    private static final String TAG = App.class.getSimpleName();

    // fullscreen
    public static boolean blnFullscreenActvitity = false;

    public static String DB_NAME = "maptracker.db";


    // app folder name
    public static String APP_FOLDERNAME = ".maptracker";

    // share pref name

    public static String PREF_NAME = "maptracker_app";

    // class for the share pref keys and valyes get set
    public static SharePrefrences sharePrefrences;

    // for the Google login
    public static GoogleApiClient mGoogleApiClient;


    // intent passing tags or string key names
    public static String ITAG_FROM = "from";
    public static String ITAG_TITLE = "title";
    public static String ITAG_DETAILS = "details";

    // for the app context
    public static Context mContext;

    // for the set app fontface or type face
    static Typeface tf_Regular, tf_Bold;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    // application on create methode for the create and int base values
    @Override
    public void onCreate() {
        super.onCreate();



        try {
            MultiDex.install(this);
            mContext = getApplicationContext();
            sharePrefrences = new SharePrefrences(App.this);

            Realm.init(this);

            getFont_Regular();
            getFont_Bold();
            createAppFolder();

            //startService(new Intent(this, LoginSessionService.class));
            Fabric.with(this, new Crashlytics());

            SendBird.init(APP_ID_SENDBIRD, mContext);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    private void createAppFolder() {
        try {
            String sdCardPath = Environment.getExternalStorageDirectory().toString();
            File file2 = new File(sdCardPath + "/" + App.APP_FOLDERNAME + "");
            if (!file2.exists()) {
                if (!file2.mkdirs()) {
                    System.out.println("==Create Directory " + App.APP_FOLDERNAME + "====");
                } else {
                    System.out.println("==No--1Create Directory " + App.APP_FOLDERNAME + "====");
                }
            } else {
                System.out.println("== already created---No--2Create Directory " + App.APP_FOLDERNAME + "====");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Typeface getFont_Regular() {
        tf_Regular = Typeface.createFromAsset(mContext.getAssets(), "font/roboto_light.ttf");
        return tf_Regular;
    }


    public static Typeface getFont_Bold() {
        tf_Bold = Typeface.createFromAsset(mContext.getAssets(), "font/roboto_regular.ttf");
        return tf_Bold;
    }


    public static String setLabelText(String newString , String defaultString) {
        if(newString !=null) {
            return newString;
        } else {
            showLog("==setLabelText====LABEL===null===newString====set default text==");
            return defaultString;
        }
    }


    public static String setAlertText(String newString , String defaultString) {
        if(newString !=null) {
            return newString;
        } else {
            showLog("==setAlertText===null===newString====set default text==");
            return defaultString;
        }
    }



    public static String getddMMMyy(String convert_date_string){
        String final_date="";
        String date1 = "";
        if (convert_date_string !=null) {

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat  outputFormat = new SimpleDateFormat("dd MMM ''yy h:mm a");
                String inputDateStr = convert_date_string;
                Date date = null;
                date = inputFormat.parse(inputDateStr);
                //String outputDateStr = outputFormat.format(date);
                date1 = outputFormat.format(date);
                final_date = date1.toLowerCase();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return final_date;
    }



    public static String getCurrentDateTime() {
        String current_date = "";
        Calendar c = Calendar.getInstance();
        //System.out.println("Current time => " + c.getTime());

        SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        current_date = postFormater.format(c.getTime());

        return  current_date;
    }


    /*public static Typeface getFontRoboto() {
        tfRoboto = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
        return tfRoboto;
    }

    public static Typeface getFontShortStack() {
        tfShortStack = Typeface.createFromAsset(mContext.getAssets(), "fonts/ShortStack-Regular.ttf");
        return tfShortStack;
    }


    public static Typeface getFontDosisBold() {
        tfDosisBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-Bold.ttf");
        return tfDosisBold;
    }

    public static Typeface getFontDosisExtraBold() {
        tfDosisExtraBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-ExtraBold.ttf");
        return tfDosisExtraBold;
    }

    public static Typeface getFontDosisExtraLight() {
        tfDosisExtraLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-ExtraLight.ttf");
        return tfDosisExtraLight;
    }

    public static Typeface getFontDosisLight() {
        tfDosisLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-Light.ttf");
        return tfDosisLight;
    }


    public static Typeface getFontDosisMedium() {
        tfDosisMedium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-Medium.ttf");
        return tfDosisMedium;
    }

    public static Typeface getFontDosisRegular() {
        tfDosisRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-Regular.ttf");
        return tfDosisRegular;
    }

    public static Typeface getFontDosisSemiBold() {
        tfDosisSemiBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Dosis-SemiBold.ttf");
        return tfDosisSemiBold;
    }*/


    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public static void showToastShort(Context context, String strMessage) {
        Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
    }




    public static void showLog(String strMessage) {
        Log.v("==App==", "--strMessage--" + strMessage);
    }


    public static void showLogApi(String strMessage) {
        //Log.v("==App==", "--strMessage--" + strMessage);
        System.out.println("--API-MESSAGE--" + strMessage);
    }

    public static void showLogApiRespose(String op,Response response) {

        Log.i("=op==>" + op, "response==>" + new Gson().toJson(response.body()));
    }



    public static void showLogResponce(String strTag, String strResponse) {
        Log.i("==App==strTag==" + strTag, "--strResponse--" + strResponse);
    }


    public static void showLog(String strTag, String strMessage) {
        Log.v("==App==strTag==" + strTag, "--strMessage--" + strMessage);
    }


    public static void setTaskBarColored(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        }
    }


    public static boolean isInternetAvail(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public static String getOnlyDigits(String s) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll("");
        return number;
    }


    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll("");
        return number;
    }


    public static String getOnlyAlfaNumeric(String s) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll(" ");
        return number;
    }


    public void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void hideSoftKeyboardMy(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public static void myStartActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    public static void myFinishActivityRefresh(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public static void myFinishActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public static  void GenerateKeyHash() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getApplicationContext().getPackageName(),
                    PackageManager.GET_SIGNATURES); //GypUQe9I2FJr2sVzdm1ExpuWc4U= android pc -2 key
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                App.showLog("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void myFinishStartActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }



    public class LocationConstants {
        public static final int SUCCESS_RESULT = 0;

        public static final int FAILURE_RESULT = 1;

        public static final String PACKAGE_NAME = "com.maptracker";

        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

        public static final String LOCATION_DATA_AREA = PACKAGE_NAME + ".LOCATION_DATA_AREA";
        public static final String LOCATION_DATA_CITY = PACKAGE_NAME + ".LOCATION_DATA_CITY";
        public static final String LOCATION_DATA_STREET = PACKAGE_NAME + ".LOCATION_DATA_STREET";


    }


    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static String convertTo24Hour(String Time) {
        DateFormat f1 = new SimpleDateFormat("hh:mm a"); //11:00 pm
        Date d = null;
        try {
            d = f1.parse(Time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat("HH:mm:ss");
        String x = f2.format(d); // "23:00"

        return x;
    }


    public static String convertTo12Hour(String Time) {
        DateFormat f1 = new SimpleDateFormat("HH:mm:ss"); // "23:00:00"
        Date d = null;
        try {
            d = f1.parse(Time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat("hh:mm a");
        String x = f2.format(d); //11:00 pm

        return x;
    }


    public static void simpleLogout2(Activity act) {
        App.sharePrefrences.setPref(PreferencesKeys.strLogin, "0");
        App.sharePrefrences.setPref(PreferencesKeys.strSocialLogin, "0");
        App.sharePrefrences.setPref(PreferencesKeys.strUserId, "");

        NotificationManager notifManager= (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

        Intent iv = new Intent(act, LoginActivity.class);
        iv.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //iv.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        iv.putExtra(App.ITAG_FROM, "BaseActivity");
        act.startActivity(iv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            act.finishAffinity();
        }
        else
        {
            act.finish();
        }
    }


    public static void expand(final View v) {
        //v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT); //WRAP_CONTENT
        v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        //? WindowManager.LayoutParams.WRAP_CONTENT //WRAP_CONTENT
                        ? WindowManager.LayoutParams.MATCH_PARENT //WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }



    public static JSONArray jsonArrayRoute = new JSONArray();

    public static void setJsonArrayRoute(float meters, double eDLat, double eDLon) {
        try {
            //if (meters % 10 == 0) // 10 meter insert data
            {
                JSONObject jsonObjectRoute = new JSONObject();
                jsonObjectRoute.put("latlon", "" + eDLat + "," + eDLon);
                jsonArrayRoute.put(jsonObjectRoute);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getDistanceInKMtoMeter(float KMS) {
        float distanceInMeters = KMS;


        distanceInMeters = ((distanceInMeters) * 1000);


        // distance = locationA.distanceTo(locationB);   // in meters
        //  distance = locationA.distanceTo(locationB)/1000;   // in km

        String strCalKMtoMETER = String.format("%.02f", distanceInMeters);

        App.showLog("======METER====" + strCalKMtoMETER);

        return strCalKMtoMETER;
    }


    public static String convertMeterToKMString(float meters) {
        float distanceInMeters = 0;

        distanceInMeters = meters / 1000;
        String strCalKM = "0";
        strCalKM = String.format("%.02f", distanceInMeters);

        App.showLog("======KM====" + strCalKM);

        return strCalKM;
    }



    public static void showSnackBar(View view, String strMessage) {
        Snackbar snackbar = Snackbar.make(view, strMessage, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.BLACK);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }


    public static void showSnackBarLong(View view, String strMessage) {
        Snackbar.make(view, strMessage, Snackbar.LENGTH_LONG).show();
    }


    public static OkHttpClient getOkHttpClient()
    {
        return  new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

    }

    public static Retrofit getRetrofit()
    {
        return  new Retrofit.Builder().baseUrl(strApiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    public static ApiService getApiService()
    {
        // ApiService apiService = getRetrofit().create(ApiService.class);
        return getRetrofit().create(ApiService.class);
    }



    public static RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value);
    }






    public static String ADS_APP_bnr_1 = "ca-app-pub-4346653435295459/3038075274";
    public static String ADS_APP_bnr_2 = "ca-app-pub-4346653435295459/4099165067";

    public static String getRandomBannerId()
    {
        String strArr[] =
                {
                        App.ADS_APP_bnr_1,
                        App.ADS_APP_bnr_2,


                        App.ADS_APP_bnr_1,
                        App.ADS_APP_bnr_2,

                        App.ADS_APP_bnr_1,
                        App.ADS_APP_bnr_2

                };


        //return App.ADS_APP_bnr_1;
        return getRandomFromStringArr(strArr);

    }
    public static String ADS_APP_int_1 = "ca-app-pub-4346653435295459/2538671383";
    public static String ADS_APP_int_2 = "ca-app-pub-4346653435295459/9564434109";

    public static String getRandomIntId()
    {
        String strArr[] =
                {
                        App.ADS_APP_int_1,
                        App.ADS_APP_int_2,


                        App.ADS_APP_int_1,
                        App.ADS_APP_int_2,

                        App.ADS_APP_int_1,
                        App.ADS_APP_int_2

                };


        //return App.ADS_APP_bnr_1;
        return getRandomFromStringArr(strArr);

    }



    public static String getRandomFromStringArr(String[] strArr)
    {
        String strRandom = strArr[new Random().nextInt(strArr.length)];

        App.showLog("===getRandomFromStringArr===="+strRandom);
        return  strRandom;
    }





    public static boolean isCheckReachLocation(int rangeMeter,double sDLat, double sDLon, double eDLat, double eDLon)
    {
        float distanceInMeters = 0;

        Location startLocation = new Location("Start");
        startLocation.setLatitude(sDLat);
        startLocation.setLongitude(sDLon);

        Location targetLocation = new Location("Ending");
        targetLocation.setLatitude(eDLat);
        targetLocation.setLongitude(eDLon);

        distanceInMeters =  (targetLocation.distanceTo(startLocation));


        // distance = locationA.distanceTo(locationB);   // in meters
        //  distance = locationA.distanceTo(locationB)/1000;   // in km

        String strCalMeters = String.format("%.02f", distanceInMeters);

        App.showLog("===checkReachLocation====strCalMeters====meters===="+strCalMeters);

        if(distanceInMeters > rangeMeter )
        {
            return false;
        }
        else
        {
            App.showLog("====-----REACHED----=====checkReachLocation====strCalMeters====meters===="+strCalMeters);
            return true;
        }
    }


    public static String getDistanceInKM(double sDLat, double sDLon, double eDLat, double eDLon)
    {
        float distanceInMeters = 0;

        Location startLocation = new Location("Start");
        startLocation.setLatitude(sDLat);
        startLocation.setLongitude(sDLon);

        Location targetLocation = new Location("Ending");
        targetLocation.setLatitude(eDLat);
        targetLocation.setLongitude(eDLon);

        distanceInMeters =  (targetLocation.distanceTo(startLocation) / 1000);


        // distance = locationA.distanceTo(locationB);   // in meters
        //  distance = locationA.distanceTo(locationB)/1000;   // in km

        String strCalKM = String.format("%.02f", distanceInMeters);

        App.showLog("======KM===="+strCalKM);

        return strCalKM;
    }


    public static String getDistanceInMeter(double sDLat, double sDLon, double eDLat, double eDLon)
    {
        float distanceInMeters = 0;

        Location startLocation = new Location("Start");
        startLocation.setLatitude(sDLat);
        startLocation.setLongitude(sDLon);

        Location targetLocation = new Location("Ending");
        targetLocation.setLatitude(eDLat);
        targetLocation.setLongitude(eDLon);

        distanceInMeters =  (targetLocation.distanceTo(startLocation));


        // distance = locationA.distanceTo(locationB);   // in meters
        //  distance = locationA.distanceTo(locationB)/1000;   // in km

        String strCalMeter = String.format("%.02f", distanceInMeters);

        App.showLog("======METER===="+strCalMeter);

        return strCalMeter;
    }


    /*
    * Background Service
    * */
    public static String strPrevTime = "";
    public static BroadcastReceiver alarm;
    public static void startAlarmServices(Context context)
    {
        if(alarm == null)
        {
            alarm = new BroadcastReceiver();
        }
        if(alarm != null)
        {
            alarm.CancelAlarm(context);
            alarm.SetAlarm(context);
        }
        else
        {
            App.showLog(TAG, "Alarm is null");
        }

    }



    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }



    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static String dateConvert24to12Hour(String str24Hour)
    {
        showLog("======dateConvert24to12Hour======");
        showLog("===str24Hour===="+str24Hour);
        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
        Date _24HourDt = null;
        try {
            _24HourDt = _24HourSDF.parse(str24Hour);
        } catch (Exception e) {
            e.printStackTrace();
            return str24Hour;
        }
        String str12Hour = _12HourSDF.format(_24HourDt);
        showLog("===str12Hour===="+str12Hour);
        return str12Hour;

    }
}
