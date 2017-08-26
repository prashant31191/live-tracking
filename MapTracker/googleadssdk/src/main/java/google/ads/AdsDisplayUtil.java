package google.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by prashant.patel on 8/25/2017.
 */

public class  AdsDisplayUtil
{
    public static String strAdsBnrId = "";
    public static String strAdsIntId = "";

    public  AdsDisplayUtil(String bnrId,String intId)
    {
        strAdsBnrId = bnrId;
        strAdsIntId = intId;
    }

    public static String getStrAdsBnrId() {
        return strAdsBnrId;
    }

    public static String getStrAdsIntId() {
        return strAdsIntId;
    }

    public static void setStrAdsBnrId(String strAdsBnrId1) {
        strAdsBnrId = strAdsBnrId1;
    }

    public static void setStrAdsIntId(String strAdsIntId1) {
        strAdsIntId = strAdsIntId1;
    }

    public static void openIntAdsScreen(Context context,String strAdsIntId1) {
        strAdsIntId = strAdsIntId1;

        Intent intent = new Intent(context,ActAdsInterstitial.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("interstitial_ad_unit_id",strAdsIntId);
        context.startActivity(intent);
    }
    public static void openBnrAdsScreen(Context context,String strAdsBnrId1) {
        strAdsBnrId = strAdsBnrId1;

        Intent intent = new Intent(context,ActAdsInterstitial.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("banner_ad_unit_id",strAdsBnrId);
        context.startActivity(intent);
    }

    public static void openBnrIntAdsScreen(Context context,String strAdsBnrId1,String strAdsIntId1) {
        strAdsBnrId = strAdsBnrId1;
        strAdsIntId = strAdsIntId1;

        Intent intent = new Intent(context,ActAdsInterstitial.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("interstitial_ad_unit_id",strAdsIntId);
        intent.putExtra("banner_ad_unit_id",strAdsBnrId);
        context.startActivity(intent);
    }

}
