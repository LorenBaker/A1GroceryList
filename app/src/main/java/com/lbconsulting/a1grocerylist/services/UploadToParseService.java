package com.lbconsulting.a1grocerylist.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * This service uploads dirty Parse objects
 */
public class UploadToParseService extends Service {

    private ServiceHandler mServiceHandler;

    private volatile boolean mSeekingNetwork = true;
    private volatile int INTERVAL;
    private volatile int mSlowInterval;
    private volatile int mFastInterval;
    private volatile boolean mStoppedSelf;

    private volatile int mNumberOfSleepIntervals;
    private volatile int mMaxNumberOfFastSleepIntervals;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.i("UploadToParseService", "onBind");
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i("UploadToParseService", "onCreate");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStoppedSelf) {
            MyLog.i("UploadToParseService", "onDestroy: Stopped self.");
        } else {
            MyLog.i("UploadToParseService", "onDestroy: Stopped by the Operating System.");
        }
        mSeekingNetwork = false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MyLog.e("UploadToParseService", "onLowMemory");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.i("UploadToParseService", "onStartCommand");
        try {
            mStoppedSelf = false;
            mFastInterval = MySettings.INTERVAL_FAST; // 30 seconds
            mSlowInterval = MySettings.INTERVAL_SLOW; // 15 minutes
            mMaxNumberOfFastSleepIntervals = MySettings.MAX_NUMBER_OF_FAST_SLEEP_INTERVALS;

            if (mSlowInterval < mFastInterval) {
                mSlowInterval = 30 * mFastInterval;
            }

            INTERVAL = mFastInterval;

            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);

        } catch (Exception e) {
            MyLog.e("UploadToParseService", "onStartCommand: Exception" + e.getMessage());
        }

        // If we get killed, after returning from here, restart
        return Service.START_STICKY;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int startID = msg.arg1;
            MyLog.i("UploadToParseService", "Started background thread with StartID = " + startID);
            while (mSeekingNetwork) {
                synchronized (this) {
                    while (mSeekingNetwork) {
                        if (isNetworkAvailable()) {
                            INTERVAL = mFastInterval;
                            mNumberOfSleepIntervals = 0;
                            uploadDirtyParseObjects();
                        } else {
                            try {
                                mNumberOfSleepIntervals++;
                                if (mNumberOfSleepIntervals > mMaxNumberOfFastSleepIntervals) {
                                    // The network has not been available for a long time ... increase the sleep interval
                                    INTERVAL = mSlowInterval;
                                }
                                MyLog.i("UploadToParseService", mNumberOfSleepIntervals + " SLEEPING " + INTERVAL / 1000 + " seconds.");
                                Thread.sleep(INTERVAL);
                            } catch (InterruptedException e) {
                                MyLog.e("UploadToParseService", "InterruptedException. " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        private void uploadDirtyParseObjects() {
            mSeekingNetwork = false;
            long startTime = System.currentTimeMillis();
            // TODO: Implement uploading of all dirty Parse objects
            uploadDirtyItemsToParse();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            MyLog.d("UploadToParseService", "Overall upload elapsed time = "
                    + NumberFormat.getNumberInstance(Locale.US).format(elapsedTime) + " milliseconds.");

            // We're done ... so stop the service
            mStoppedSelf = true;
            stopSelf();

        }

        private void uploadDirtyItemsToParse() {
            long startTime = System.currentTimeMillis();
            ParseQuery<Item> query;
            try {
                query = Item.getQuery();
                query.whereEqualTo(Item.IS_ITEM_DIRTY, true);
                query.fromLocalDatastore();
//                query.fromPin(MySettings.GROUP_NAME_ITEMS);
                List<Item> dirtyItemsList = query.find();
                MyLog.i("UploadToParseService", "uploadDirtyItemsToParse: Found "
                        + dirtyItemsList.size() + " dirty Items.");
                int count = 0;
                for (Item item : dirtyItemsList) {
                    try {
                        item.setItemDirty(false);
                        item.save();
                        count++;
                    } catch (ParseException e) {
                        item.setItemDirty(true);
                        MyLog.e("UploadToParseService", "Error saving dirty Item \"" + item.getItemName()
                                + "\" : ParseException" + e.getMessage());
                    }
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                MyLog.i("UploadToParseService", "Saved " + count + " Items to Parse. Duration = "
                        + NumberFormat.getNumberInstance(Locale.US).format(duration) + " milliseconds.");

            } catch (ParseException e) {
                MyLog.e("UploadToParseService", "Error finding dirty Items: ParseException" + e.getMessage());
            }
        }

        private boolean isNetworkAvailable() {

            boolean networkAvailable = false;
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if ((ni != null) && (ni.isConnected())) {
                // We have a network connection
                networkAvailable = true;
            }
            if (networkAvailable) {
                MyLog.i("UploadToParseService", "Network is available.");
            } else {
                MyLog.i("UploadToParseService", "Network NOT available.");
            }

            return networkAvailable;
        }


    }
}
