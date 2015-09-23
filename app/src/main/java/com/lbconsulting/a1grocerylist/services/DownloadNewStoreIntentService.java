package com.lbconsulting.a1grocerylist.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreChain;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * An intent services that down loads newly created store
 */
public class DownloadNewStoreIntentService extends IntentService {

    private boolean mSeekingNetwork = true;
    private int mFastInterval;
    private int mSlowInterval;
    private int INTERVAL;
    private int mNumberOfSleepIntervals;
    private int mMaxNumberOfFastSleepIntervals;

    private String mStoreChainID;
    private String mStoreRegionalName;

    private List<Store> mStoreList;
    private List<List<StoreMapEntry>> mListOfStoreMaps;

    private boolean mDownloadNotificationShowing;


    public DownloadNewStoreIntentService() {
        super("DownloadNewStoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        MyLog.i("DownloadNewStoreIntentService", "STARTED.");

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;

        } else {
            mStoreChainID = extras.getString(MySettings.ARG_STORE_CHAIN_ID);
            mStoreRegionalName = extras.getString(MySettings.ARG_STORE_REGIONAL_NAME);
        }

        mSeekingNetwork = true;
        mDownloadNotificationShowing = false;

        mFastInterval = MySettings.INTERVAL_FAST; // 30 seconds
        mSlowInterval = MySettings.INTERVAL_SLOW; // 15 minutes
        mMaxNumberOfFastSleepIntervals = MySettings.MAX_NUMBER_OF_FAST_SLEEP_INTERVALS; // 60

        if (mSlowInterval < mFastInterval) {
            mSlowInterval = 30 * mFastInterval;
        }

        INTERVAL = mFastInterval;
        long startTime = 0;
        while (mSeekingNetwork) {
            if (A1Utils.isNetworkAvailable(this)) {
                startTime = System.currentTimeMillis();
                mSeekingNetwork = false;
                showDownLoadNotification();
                downloadNewlyCreatedStore();
                pinDownloadedData();

            } else {
                try {
                    mNumberOfSleepIntervals++;
                    if (mNumberOfSleepIntervals > mMaxNumberOfFastSleepIntervals) {
                        // The network has not been available for a long time ... increase the sleep interval
                        INTERVAL = mSlowInterval;
                    }
                    MyLog.i("DownloadNewStoreIntentService", mNumberOfSleepIntervals + " SLEEPING " + INTERVAL / 1000 + " seconds.");
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    MyLog.e("DownloadNewStoreIntentService", "InterruptedException. " + e.getMessage());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTimeMills = endTime - startTime;
        double elapsedTimeSeconds = elapsedTimeMills / 1000.0;
        String msg = String.format("Downloading new store elapsed time =  %.2f", elapsedTimeSeconds) + " seconds. DONE.";
        MyLog.d("DownloadNewStoreIntentService", msg);
        cancelDownLoadNotification();
    }


    private void pinDownloadedData() {
        MyLog.i("DownloadNewStoreIntentService", "pinDownloadedData");
        try {

            if (mListOfStoreMaps != null && mListOfStoreMaps.size() > 0) {
                for (List<StoreMapEntry> storeMap : mListOfStoreMaps) {
//                    ParseObject.pinAll(MySettings.GROUP_NAME_STORE_MAPS, storeMap);
                    ParseObject.pinAll( storeMap);
                }
            }

            if (mStoreList != null && mStoreList.size() > 0) {
//                ParseObject.pinAll(MySettings.GROUP_NAME_STORES, mStoreList);
                ParseObject.pinAll( mStoreList);
            }


        } catch (ParseException e) {
            MyLog.e("DownloadNewStoreIntentService", "pinDownloadedData: ParseException" + e.getMessage());
        }
    }


    private void downloadNewlyCreatedStore() {
// get store chain object
        StoreChain storeChain = StoreChain.getStoreChain(mStoreChainID);
        ParseQuery<Store> query = Store.getQuery();
        query.whereEqualTo(Store.STORE_CHAIN, storeChain);
        query.whereEqualTo(Store.STORE_REGIONAL_NAME, mStoreRegionalName);
        query.include(Store.STORE_CHAIN);
        query.setLimit(MySettings.QUERY_LIMIT_NEAREST_STORES);
        // Query for new results from the network.
        try {
            mStoreList = query.find();
            if (mStoreList != null && mStoreList.size() > 0) {
                MyLog.i("DownloadNewStoreIntentService", "downloaded " + mStoreList.size() + " Stores from Parse.");

                // get the store map for each store
                mListOfStoreMaps = new ArrayList<>();
                for (Store store : mStoreList) {
                    downloadStoreMapFromParse(store);
                }
                MyLog.i("DownloadNewStoreIntentService", "downloaded " + mListOfStoreMaps.size() + " Store Maps from Parse.");

            } else {
                MyLog.i("DownloadNewStoreIntentService", "downloadStoresFromParse: Fetched NO Stores from Parse cloud.");
            }

        } catch (ParseException e) {
            MyLog.e("DownloadNewStoreIntentService", "downloadStoresFromParse: ParseException" + e.getMessage());
        }

    }

    private void downloadStoreMapFromParse(Store store) {
        ParseQuery<StoreMapEntry> storeMapQuery = StoreMapEntry.getQuery();
        storeMapQuery.whereEqualTo(StoreMapEntry.STORE, store);
        storeMapQuery.include(StoreMapEntry.STORE);
        storeMapQuery.include(StoreMapEntry.GROUP);
        storeMapQuery.include(StoreMapEntry.LOCATION);

        // Query for new results from the network.
        try {
            List<StoreMapEntry> storeMap = storeMapQuery.find();
            if (storeMap != null && storeMap.size() > 0) {
                MyLog.i("DownloadNewStoreIntentService", "downloadStoreMapFromParse for " + store.getStoreChainAndRegionalName());
                mListOfStoreMaps.add(storeMap);
            }

        } catch (ParseException e) {
            MyLog.e("DownloadNewStoreIntentService", "downloadStoreMapFromParse for " + store.getStoreChainAndRegionalName() + ": ParseException" + e.getMessage());
        }

    }


    private void showDownLoadNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                DownloadNewStoreIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setContentTitle(getString(R.string.notification_downloading_title));
        notificationBuilder.setContentText(getString(R.string.notification_downloading_text));
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setTicker(getString(R.string.notification_download_ticker));
        notificationManager
                .notify(MySettings.NOTIFICATION_DOWNLOAD_ID, notificationBuilder.build());

        mDownloadNotificationShowing = true;
    }

    private void cancelDownLoadNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                DownloadNewStoreIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setContentTitle(getString(R.string.notification_downloading_title));
        notificationBuilder.setContentText(getString(R.string.notification_downloading_text));
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setTicker(getString(R.string.notification_download_ticker));
        notificationManager.cancelAll();
        mDownloadNotificationShowing = false;
    }


    @Override
    public void onDestroy() {
        if (mDownloadNotificationShowing) {
            cancelDownLoadNotification();
        }
        MyLog.i("DownloadNewStoreIntentService", "onDestroy: mDownloadNotificationShowing = " + mDownloadNotificationShowing);
        super.onDestroy();
    }
}
