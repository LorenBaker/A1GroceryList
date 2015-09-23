package com.lbconsulting.a1grocerylist.classes;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;

/**
 * Common utilities for A1 Grocery List
 */
public class A1Utils {

    public static boolean isNetworkAvailable(Context context) {

        boolean networkAvailable = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if ((ni != null) && (ni.isConnected())) {
            // We have a network connection
            networkAvailable = true;
        }
        if (networkAvailable) {
            MyLog.i("A1Utils", "Network is available.");
        } else {
            MyLog.i("A1Utils", "Network NOT available.");
        }

        return networkAvailable;
    }


    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }


    public static boolean isValidLatitudeAndLongitude(double latitude, double longitude) {
        final double minLatitude = -90;
        final double maxLatitude = 90;
        final double minLongitude = -180;
        final double maxLongitude = 180;

        boolean result = false;

        if (!(latitude < minLatitude)) {
            if (!(latitude > maxLatitude)) {
                if (!(latitude < minLongitude)) {
                    if (!(latitude > maxLongitude)) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }
}
