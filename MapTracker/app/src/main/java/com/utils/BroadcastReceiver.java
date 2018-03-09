package com.utils;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;


import com.maptracker.App;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BroadcastReceiver extends android.content.BroadcastReceiver {


    String TAG = "BroadcastReceiver";
    final public static String ONE_TIME = "onetime";
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        App.showLog(TAG , "==onReceive==");

        mContext = context;

        if (mContext == null) {
            mContext = App.mContext;
        }


        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        //Acquire the lock
        wl.acquire();

        //You can do the processing here.
        Bundle extras = intent.getExtras();
        StringBuilder msgStr = new StringBuilder();

        if (extras != null && extras.getBoolean(ONE_TIME, Boolean.FALSE)) {
            //Make sure this intent has been sent by the one-time timer button.
            msgStr.append("One time Timer : ");
        }


        Format formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

        msgStr.append(formatter.format(new Date()));

        String strCurrTime = msgStr.toString();


        if (strCurrTime.equalsIgnoreCase(App.strPrevTime)) {

        } else {
            App.strPrevTime = strCurrTime;

            App.showLog(TAG, ""+strCurrTime);
            //checkDatabseDataForNotify(strCurrTime, context);


            try{

                boolean foregroud = new ForegroundCheckTask().execute(context).get();

                if (foregroud) {
                    App.showLog(TAG, "Application is Running.");
                }
                else
                {
                    App.showLog(TAG, "Application is not Running.");
                }

            }catch (Exception e) {
                e.printStackTrace();
            }

        }


        //Release the lock
        wl.release();
    }

    public void SetAlarm(Context context) {
        App.showLog(TAG, "==SetAlarm==");
        mContext = context;
        //  strPrevTime = "";
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, BroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 20, alarmIntent);
    }

    public void CancelAlarm(Context context) {
        App.showLog(TAG, "==CancelAlarm==");
        mContext = context;
        Intent intent = new Intent(context, BroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }





    /*
    * Check app is ForeGround  -- https://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
    * */
    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }

            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses)
            {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(App.packageTrack))
                {
                    return true;
                }
            }
            return false;
        }
    }



}
