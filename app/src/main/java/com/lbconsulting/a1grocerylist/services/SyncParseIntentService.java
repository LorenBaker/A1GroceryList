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
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.database.Location;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreChain;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * An intent services that uploads dirty parse items
 * then download all tables
 */
public class SyncParseIntentService extends IntentService {

    private boolean mSeekingNetwork = true;
    private int mFastInterval;
    private int mSlowInterval;
    private int INTERVAL;
    private int mNumberOfSleepIntervals;
    private int mMaxNumberOfFastSleepIntervals;

    private int mAction;
    private String mActionString;
    private double mUserLatitude;
    private double mUserLongitude;

    private List<Group> mGroupList;
    private List<Location> mLocationList;
    private List<StoreChain> mStoreChainList;
    private List<Store> mStoreList;
    private List<List<StoreMapEntry>> mListOfStoreMaps;
    private List<Item> mItemList;

    private boolean mDownloadNotificationShowing;


    public SyncParseIntentService() {
        super("SyncParseIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        MyLog.i("SyncParseIntentService", "STARTED.");

        Bundle extras = intent.getExtras();
        if (extras == null) {
            mUserLatitude = MySettings.LATITUDE_NA;
            mUserLongitude = MySettings.LONGITUDE_NA;
        } else {
            mAction = extras.getInt(MySettings.ARG_UPLOAD_DOWNLOAD_ACTION);
            mUserLatitude = extras.getDouble(MySettings.ARG_USER_LATITUDE);
            mUserLongitude = extras.getDouble(MySettings.ARG_USER_LONGITUDE);
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
                switch (mAction) {
                    case MySettings.ACTION_DOWNLOAD_ONLY:
                        mActionString = "ACTION_DOWNLOAD_ONLY";
                        downloadTablesToLocalDatastore();
                        break;

                    case MySettings.ACTION_UPLOAD_ONLY:
                        mActionString = "ACTION_UPLOAD_ONLY";
                        uploadDirtyParseObjects();
                        break;

                    case MySettings.ACTION_UPLOAD_AND_DOWNLOAD:
                        mActionString = "ACTION_UPLOAD_AND_DOWNLOAD";
                        uploadDirtyParseObjects();
                        downloadTablesToLocalDatastore();
                        break;
                }

            } else {
                try {
                    mNumberOfSleepIntervals++;
                    if (mNumberOfSleepIntervals > mMaxNumberOfFastSleepIntervals) {
                        // The network has not been available for a long time ... increase the sleep interval
                        INTERVAL = mSlowInterval;
                    }
                    MyLog.i("SyncParseIntentService", mNumberOfSleepIntervals + " SLEEPING " + INTERVAL / 1000 + " seconds.");
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    MyLog.e("SyncParseIntentService", "InterruptedException. " + e.getMessage());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTimeMills = endTime - startTime;
        double elapsedTimeSeconds = elapsedTimeMills / 1000.0;
        String msg = String.format("Overall sync elapsed time =  %.2f", elapsedTimeSeconds) + " seconds. " + mActionString + " DONE";
        MyLog.d("SyncParseIntentService", msg);
    }

    private void uploadDirtyParseObjects() {
        long startTime = System.currentTimeMillis();
        // TODO: Implement uploading of all dirty Parse objects
        uploadDirtyItemsToParse();

        long endTime = System.currentTimeMillis();
        long elapsedTimeMills = endTime - startTime;
        double elapsedTimeSeconds = elapsedTimeMills / 1000.0;
        String msg = String.format("Upload dirty objects elapsed time =  %.2f", elapsedTimeSeconds) + " seconds.";
        MyLog.i("SyncParseIntentService", msg);
    }

    private void uploadDirtyItemsToParse() {
        long startTime = System.currentTimeMillis();
        ParseQuery<Item> query;
        try {
            query = Item.getQuery();
            query.whereEqualTo(Item.IS_ITEM_DIRTY, true);
            query.fromLocalDatastore();
            List<Item> dirtyItemsList = query.find();
            MyLog.i("SyncParseIntentService", "uploadDirtyItemsToParse: Found "
                    + dirtyItemsList.size() + " dirty Items.");
            int count = 0;
            for (Item item : dirtyItemsList) {
                try {
                    item.setItemDirty(false);
                    item.save();
                    count++;
                } catch (ParseException e) {
                    item.setItemDirty(true);
                    MyLog.e("SyncParseIntentService", "Error saving dirty Item \"" + item.getItemName()
                            + "\" : ParseException" + e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            long durationMills = endTime - startTime;
            double durationSeconds = durationMills / 1000.0;
            String msg = String.format("Saved " + count + " Items to Parse. Duration =  %.2f", durationSeconds) + " seconds.";
            MyLog.i("SyncParseIntentService", msg);

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "Error finding dirty Items: ParseException" + e.getMessage());
        }
    }


    //region Download Tables From Parse
    private void downloadTablesToLocalDatastore() {
        long startTime = System.currentTimeMillis();

        showDownLoadNotification();

        downloadGroupsFromParse();
        downloadLocationsFromParse();
        downloadStoreChainsFromParse();
        downloadStoresFromParse();
        downloadItemsFromParse();

        // downloads done
        pinDownloadedData();

        EventBus.getDefault().post(new MyEvents.updateUI(null));
        cancelDownLoadNotification();

        long endTime = System.currentTimeMillis();
        long elapsedTimeMills = endTime - startTime;
        double elapsedTimeSeconds = elapsedTimeMills / 1000.0;
        String msg = String.format("Download tables elapsed time =  %.2f", elapsedTimeSeconds) + " seconds.";
        MyLog.i("SyncParseIntentService", msg);
    }

    private void pinDownloadedData() {
        MyLog.i("SyncParseIntentService", "pinDownloadedData");
        try {
            unpinEverything();

            if (mListOfStoreMaps != null && mListOfStoreMaps.size() > 0) {
                for (List<StoreMapEntry> storeMap : mListOfStoreMaps) {
                    ParseObject.pinAll( storeMap);
                }
            }

            if (mItemList != null && mItemList.size() > 0) {
                ParseObject.pinAll(mItemList);
                setLargestSortKey(mItemList);
            }

            if (mGroupList != null && mGroupList.size() > 0) {
                ParseObject.pinAll(mGroupList);
            }

            if (mLocationList != null && mLocationList.size() > 0) {
                ParseObject.pinAll( mLocationList);
            }

            if (mStoreChainList != null && mStoreChainList.size() > 0) {
                ParseObject.pinAll(mStoreChainList);
            }

            if (mStoreList != null && mStoreList.size() > 0) {
                ParseObject.pinAll( mStoreList);
            }


        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "pinDownloadedData: ParseException" + e.getMessage());
        }
    }

    private void unpinEverything() {
        try {
            StoreMapEntry.unpinAll();
            Item.unpinAll();
            Group.unpinAll();
            Location.unpinAll();
            StoreChain.unpinAll();
            Store.unpinAll();
        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "unpinEverything: ParseException" + e.getMessage());
        }
    }

    private void downloadGroupsFromParse() {
        ParseQuery<Group> query = Group.getQuery();
        query.orderByAscending(Group.GROUP_NAME);
        query.setLimit(MySettings.QUERY_LIMIT_GROUPS);
        // Query for new results from the network.
        try {
            mGroupList = query.find();
            MyLog.i("SyncParseIntentService", "downloaded " + mGroupList.size() + " Groups from Parse.");

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadGroupsFromParse: ParseException" + e.getMessage());
        }
    }

    private void downloadLocationsFromParse() {
        ParseQuery<Location> query = Location.getQuery();
        query.orderByAscending(Location.SORT_KEY);
        query.setLimit(MySettings.QUERY_LIMIT_LOCATIONS);
        // Query for new results from the network.
        try {
            mLocationList = query.find();
            MyLog.i("SyncParseIntentService", "downloaded " + mLocationList.size() + " Locations from Parse.");

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadLocationsFromParse: ParseException" + e.getMessage());
        }
    }

    private void downloadStoreChainsFromParse() {
        ParseQuery<StoreChain> query = StoreChain.getQuery();
        query.orderByAscending(StoreChain.STORE_CHAIN_NAME);
        query.setLimit(MySettings.QUERY_LIMIT_STORE_CHAINS);
        // Query for new results from the network.
        try {
            mStoreChainList = query.find();
            MyLog.i("SyncParseIntentService", "downloaded " + mStoreChainList.size() + " Store Chains from Parse.");

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadStoreChainsFromParse: ParseException" + e.getMessage());
        }

    }

    private void downloadStoresFromParse() {
        ParseGeoPoint userLocation;
        ParseQuery<Store> query = Store.getQuery();
        if (mUserLatitude != MySettings.LATITUDE_NA && mUserLongitude != MySettings.LONGITUDE_NA) {
            userLocation = new ParseGeoPoint(mUserLatitude, mUserLongitude);
            query.whereNear("storeGeoPoint", userLocation);
        }
        query.include(Store.STORE_CHAIN);
        query.setLimit(MySettings.QUERY_LIMIT_NEAREST_STORES);
        // Query for new results from the network.
        try {
            mStoreList = query.find();
            if (mStoreList != null && mStoreList.size() > 0) {
                MyLog.i("SyncParseIntentService", "downloaded " + mStoreList.size() + " Stores from Parse.");

                // get the store map for each store
                mListOfStoreMaps = new ArrayList<>();
                int sortKey = 1;
                for (Store store : mStoreList) {
                    store.setSortKey(sortKey);
                    sortKey++;
                    downloadStoreMapFromParse(store);
                }
                MyLog.i("SyncParseIntentService", "downloaded " + mListOfStoreMaps.size() + " Store Maps from Parse.");

            } else {
                MyLog.i("SyncParseIntentService", "downloadStoresFromParse: Fetched NO Stores from Parse cloud.");
            }

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadStoresFromParse: ParseException" + e.getMessage());
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
                MyLog.i("SyncParseIntentService", "downloadStoreMapFromParse for " + store.getStoreChainAndRegionalName());
                mListOfStoreMaps.add(storeMap);
            }

        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadStoreMapFromParse for " + store.getStoreChainAndRegionalName() + ": ParseException" + e.getMessage());
        }

    }

    private void downloadItemsFromParse() {
        ParseQuery<Item> query = Item.getQuery();
        query.whereEqualTo(Item.AUTHOR, ParseUser.getCurrentUser());
        query.include(Item.GROUP);
        query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
        query.setLimit(MySettings.QUERY_LIMIT_ITEMS);

        // Query for new results from the network.
        try {
            mItemList = query.find();
            MyLog.i("SyncParseIntentService", "download " + mItemList.size() + " Items from Parse.");


        } catch (ParseException e) {
            MyLog.e("SyncParseIntentService", "downloadItemsFromParse: ParseException" + e.getMessage());
        }
    }

    private void setLargestSortKey(List<Item> itemList) {
        int largestSortKey = 0;
        for (Item item : itemList) {
            if (item.getSortKey() > largestSortKey) {
                largestSortKey = item.getSortKey();
            }
        }
        Item.setLargestSortKey(largestSortKey);
    }

    private void showDownLoadNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                SyncParseIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT));
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
                SyncParseIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setContentTitle(getString(R.string.notification_downloading_title));
        notificationBuilder.setContentText(getString(R.string.notification_downloading_text));
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setTicker(getString(R.string.notification_download_ticker));
        notificationManager.cancelAll();
        mDownloadNotificationShowing = false;
    }


    //endregion


    @Override
    public void onDestroy() {
        if (mDownloadNotificationShowing) {
            cancelDownLoadNotification();
        }
        MyLog.i("SyncParseIntentService", "onDestroy: mDownloadNotificationShowing = " + mDownloadNotificationShowing);
        super.onDestroy();
    }
}
