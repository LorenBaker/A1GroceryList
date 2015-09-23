package com.lbconsulting.a1grocerylist.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.DrawerArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.FragmentBackstack;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.dialogs.dialogEditItem;
import com.lbconsulting.a1grocerylist.dialogs.dialogShoppingListSorting;
import com.lbconsulting.a1grocerylist.fragments.fragCullItems;
import com.lbconsulting.a1grocerylist.fragments.fragEditNewStore;
import com.lbconsulting.a1grocerylist.fragments.fragMapStore;
import com.lbconsulting.a1grocerylist.fragments.fragMasterItemsList;
import com.lbconsulting.a1grocerylist.fragments.fragShoppingList;
import com.lbconsulting.a1grocerylist.fragments.fragShoppingListByGroup;
import com.lbconsulting.a1grocerylist.fragments.fragStoreListByStateCity;
import com.lbconsulting.a1grocerylist.services.SyncParseIntentService;
import com.lbconsulting.a1grocerylist.services.UploadToParseService;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.greenrobot.event.EventBus;


public class MainActivity extends Activity implements DrawerLayout.DrawerListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static int LOCATION_REQUEST_INTERVAL = 60000 * 10; // 10 minutes
    private final static int LOCATION_REQUEST_FASTEST_INTERVAL = 30000; // 30 seconds

    private String[] mDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private boolean mDrawerItemSelected;
    private ListView mDrawerList;
    private int mSelectedFragmentID;
    private FragmentBackstack mFragmentBackstack;

    private FrameLayout mFragmentContainer;

    private ActionBar mActionBar;

    private int mPreviousActiveFragmentID;
    private Intent mSycIntent;
    private boolean mSyncWithParseOnResume;
    private boolean mResumingFromStoresActivityOnBackPressed;
    private boolean mResumingFromOnRestart;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;
    private boolean mPlayServicesConnected = false;
    private static boolean mLocationFound = false;
    private Handler mStartSyncHandler = new Handler();
    private int mSyncAction;


    private static void showOkDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set dialog title and message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnOK = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnOK.setTextSize(18);
            }
        });

        // show it
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("MainActivity", "onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        MySettings.setContext(this);
        EventBus.getDefault().register(this);
        mActionBar = getActionBar();
        mPreviousActiveFragmentID = MySettings.FRAG_MASTER_ITEMS_LIST;
        mResumingFromOnRestart = false;
        mDrawerItemSelected = false;
        mFragmentBackstack = new FragmentBackstack();

        //region Drawer setup
        mDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        DrawerArrayAdapter adapter = new DrawerArrayAdapter(this, mDrawerItemTitles);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //endregion

        mFragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        mSyncWithParseOnResume = true;
        mResumingFromStoresActivityOnBackPressed = false;
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey(MySettings.ARG_SYNC_WITH_PARSE_ON_STARTUP)) {
            mSyncWithParseOnResume = args.getBoolean(MySettings.ARG_SYNC_WITH_PARSE_ON_STARTUP);
            mResumingFromStoresActivityOnBackPressed = args.getBoolean(MySettings.ARG_STORES_ACTIVITY_ON_BACK_PRESSED);
        }

        buildGoogleApiClient();
        createLocationRequest();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onBackPressed() {
        MyLog.i("MainActivity", "onBackPressed: Backstack size = " + mFragmentBackstack.size());

        if (mFragmentBackstack.isEmpty()) {
            finish();
        } else {
            int previousFragmentID = mFragmentBackstack.pop();
            showFragment(previousFragmentID, true);
        }
    }

    //region InstanceState Methods
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyLog.i("MainActivity", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MyLog.i("MainActivity", "onRestoreInstanceState");
    }
    //endregion


    @Override
    protected void onRestart() {
        MyLog.i("MainActivity", "onRestart");
        mResumingFromOnRestart = true;
        super.onRestart();
    }

    @Override
    protected void onStart() {
        MyLog.i("MainActivity", "onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        MyLog.i("MainActivity", "onStop");
        mGoogleApiClient.disconnect();
        mLocationFound = false;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i("MainActivity", "onResume");
        if (!mResumingFromOnRestart) {
            if (ParseUser.getCurrentUser().isNew() && mSyncWithParseOnResume) {
                MyLog.i("MainActivity", "onResume: initialize New User");
                initializeNewUser();

            } else {
                // not a new user
                if (mResumingFromStoresActivityOnBackPressed) {
                    int previousFragmentID = mFragmentBackstack.pop();
                    showFragment(previousFragmentID, true);
                } else {
                    showFragment(MySettings.getActiveFragmentID(), false);
                    if (mSyncWithParseOnResume) {
                        syncA1GroceryListData(MySettings.ACTION_UPLOAD_AND_DOWNLOAD);
                    }
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        MyLog.i("MainActivity", "onPause");
        mFragmentBackstack.save();
        if (mSycIntent != null) {
            stopService(mSycIntent);
        }
        uploadDirtyObjects();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.i("MainActivity", "onDestroy");
        EventBus.getDefault().unregister(this);
    }


    //region onEvents
    public void onEvent(MyEvents.syncA1GroceryListData event) {
        syncA1GroceryListData(event.getAction());
    }

    public void onEvent(MyEvents.setActionBarTitle event) {
        setActionBarTitle(event.getTitle());
    }

    public void onEvent(MyEvents.uploadDirtyObjects event) {
        uploadDirtyObjects();
    }

    private void setActionBarTitle(String title) {
        mActionBar.setTitle(title);
    }

    public void onEvent(MyEvents.setBackgroundColor event) {
        setBackgroundColor(event.getBackgroundColor());
    }

    private void setBackgroundColor(int color) {
        switch (color) {
            case MySettings.BACKGROUND_COLOR_GENOA:
                mFragmentContainer.setBackgroundResource(R.drawable.rec_genoa_no_stroke);
                break;

            case MySettings.BACKGROUND_COLOR_OPAL:
                mFragmentContainer.setBackgroundResource(R.drawable.rec_opal_no_stroke);
                break;
        }
    }

    public void onEvent(MyEvents.showOkDialog event) {
        showOkDialog(this, event.getTitle(), event.getMessage());
    }

    public void onEvent(MyEvents.showToast event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void onEvent(MyEvents.showEditItemDialog event) {
        showEditItemDialog(event.getItemID());
    }

    private void showEditItemDialog(String itemID) {
        FragmentManager fm = getFragmentManager();
        dialogEditItem dialog = dialogEditItem.newInstance(itemID, getString(R.string.edit_item_dialog_title));
        dialog.show(fm, "dialogEditItem");
    }

    public void onEvent(MyEvents.showFragment event) {
        showFragment(event.getFragmentID(), false);
    }

    //endregion

    private void showFragment(int fragmentID, boolean navigationFromBackstack) {
        FragmentManager fm = getFragmentManager();

        // add fragmentID to the backstack
        if (!navigationFromBackstack) {
            mFragmentBackstack.push(fragmentID);
        }

        switch (fragmentID) {

            case MySettings.FRAG_SHOPPING_LIST:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragShoppingList.newInstance(),
                                MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST))
                        .commit();
                break;

            case MySettings.FRAG_STORE_LIST_BY_AISLE:
                // Store lists
                MyLog.i("MainActivity", "showActivity: " + MySettings.getFragmentTag(MySettings.FRAG_STORE_LIST_BY_AISLE));
                Intent intent = new Intent(this, StoreListActivity.class);
                startActivity(intent);
                finish();
                break;

            case MySettings.FRAG_MASTER_ITEMS_LIST:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_MASTER_ITEMS_LIST));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragMasterItemsList.newInstance(),
                                MySettings.getFragmentTag(MySettings.FRAG_MASTER_ITEMS_LIST))
                        .commit();
                break;


            case MySettings.FRAG_SHOPPING_LIST_BY_GROUP:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST_BY_GROUP));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragShoppingListByGroup.newInstance(),
                                MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST_BY_GROUP))
                        .commit();
                break;

            case MySettings.FRAG_CULL_ITEMS:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_CULL_ITEMS));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragCullItems.newInstance(),
                                MySettings.getFragmentTag(MySettings.FRAG_CULL_ITEMS))
                        .commit();
                break;

            case MySettings.FRAG_SHOW_ALL_STORES:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOW_ALL_STORES));
                if(A1Utils.isNetworkAvailable(this)) {
                    fm.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.fragment_container,
                                    fragStoreListByStateCity.newInstance(),
                                    MySettings.getFragmentTag(MySettings.FRAG_SHOW_ALL_STORES))
                            .commit();
                }else {
                    mFragmentBackstack.pop();
                    String title = "Unable to show stores";
                    String msg = "Unable to retrieve stores. No internet connection available.";
                    showOkDialog(this,title,msg);
                }
                break;

            case MySettings.FRAG_MAP_STORE:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_MAP_STORE));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragMapStore.newInstance(),
                                MySettings.getFragmentTag(MySettings.FRAG_MAP_STORE))
                        .commit();
                break;


            case MySettings.FRAG_SETTINGS:
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SETTINGS));
                Toast.makeText(this, mDrawerItemTitles[MySettings.FRAG_SETTINGS] + " TO COME.", Toast.LENGTH_SHORT).show();
                break;


            case MySettings.FRAG_EDIT_NEW_STORE:
                String activeStoreID = MySettings.getActiveStoreID();
                MyLog.i("MainActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_EDIT_NEW_STORE));
                fm.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container,
                                fragEditNewStore.newInstance(activeStoreID),
                                MySettings.getFragmentTag(MySettings.FRAG_EDIT_NEW_STORE))
                        .commit();
                break;
        }
    }


    private void initializeNewUser() {
        // initializeNewUser on Parse
        final HashMap<String, Object> params = new HashMap<String, Object>();
        ParseCloud.callFunctionInBackground("initializeNewUser", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer numberOfItems, ParseException e) {
                if (e == null) {
                    // Success. New user initialized in Parse cloud
                    showFragment(MySettings.FRAG_MASTER_ITEMS_LIST, false);
                    syncA1GroceryListData(MySettings.ACTION_DOWNLOAD_ONLY);
                    ParseUser user = ParseUser.getCurrentUser();
                    MyLog.i("MainActivity", "New user \"" + user.getUsername() + "\" successfully initialized. " + numberOfItems + " Items created in Parse cloud.");

                } else {
                    // Failed to initialize new user.
                    MyLog.e("MainActivity", "initializeNewUser Failed to initialize new user. " + e.getMessage());
                }
            }
        });

        String title = "Initializing New User";
        String msg = "Welcome to the A1 Grocery List app.\n\n" +
                "Downloading initial data from the cloud.";
        showOkDialog(this, title, msg);
    }

    private void syncA1GroceryListData(int syncAction) {
        if (A1Utils.isNetworkAvailable(this)) {
            mSyncAction = syncAction;
            mStartSyncHandler.postDelayed(waitForLocationFound, 10);
        } else {
            String title = "Unable to Sync Data";
            String msg = "Internet network is not available. Please select \"Refresh\" after a network becomes available.";
            showOkDialog(this, title, msg);
        }
    }

    private Runnable waitForLocationFound = new Runnable() {
        @Override
        public void run() {
            if (mLocationFound) {
                proceedSyncA1GroceryListData();
            } else {
                mStartSyncHandler.postDelayed(this, 100);
            }
        }
    };

    private void proceedSyncA1GroceryListData() {
        MyLog.i("MainActivity", "Location found. Proceeding to Sync A1GroceryList Data");
        Location location = MySettings.getLastLocation();
        mSycIntent = new Intent(this, SyncParseIntentService.class);
        mSycIntent.putExtra(MySettings.ARG_UPLOAD_DOWNLOAD_ACTION, mSyncAction);
        mSycIntent.putExtra(MySettings.ARG_USER_LATITUDE, location.getLatitude());
        mSycIntent.putExtra(MySettings.ARG_USER_LONGITUDE, location.getLongitude());
        startService(mSycIntent);
    }

    private void uploadDirtyObjects() {
        MyLog.i("MainActivity", "uploadDirtyObjects");
        Intent uploadDirtyObjectsIntent = new Intent(this, UploadToParseService.class);
        startService(uploadDirtyObjectsIntent);
    }

    //region Options Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.i("MainActivity", "onCreateOptionsMenu: menu_activity_main");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem action_run_parse_test = menu.findItem(R.id.action_run_parse_test);
        final String lorenID = "yBR6QMerQ8";
        final String dadodID = "va0TTGwOk7";
        final String userID = ParseUser.getCurrentUser().getObjectId();
        if (userID.equals(lorenID) || userID.equals(dadodID)) {
            action_run_parse_test.setVisible(true);
        } else {
            action_run_parse_test.setVisible(false);
        }

        // Shown in Shopping Lists and Store Lists
        MenuItem action_deselect_struck_out_items = menu.findItem(R.id.action_deselect_struck_out_items);
        MenuItem action_add_items = menu.findItem(R.id.action_add_items);
        MenuItem action_deselect_all_items = menu.findItem(R.id.action_deselect_all_items);

        // Shown only in Shopping Lists but not if IsMapping Store
        MenuItem action_show_shopping_list_sort_dialog = menu.findItem(R.id.action_show_shopping_list_sort_dialog);

        // NOT shown in Edit/New store or if isMappingStore
        MenuItem action_refresh = menu.findItem(R.id.action_refresh);

        // set menu visibility
        switch (MySettings.getActiveFragmentID()) {

            case MySettings.FRAG_SHOPPING_LIST:
            case MySettings.FRAG_SHOPPING_LIST_BY_GROUP:
                action_deselect_struck_out_items.setVisible(true);
                action_add_items.setVisible(true);
                action_deselect_all_items.setVisible(true);
                action_show_shopping_list_sort_dialog.setVisible(true);
                action_refresh.setVisible(true);
                break;

            case MySettings.FRAG_MAP_STORE:
                // hide all non default menus
                action_deselect_struck_out_items.setVisible(false);
                action_add_items.setVisible(false);
                action_deselect_all_items.setVisible(false);
                action_show_shopping_list_sort_dialog.setVisible(false);
                action_refresh.setVisible(false);
                break;

            case MySettings.FRAG_EDIT_NEW_STORE:
            default:
                action_deselect_struck_out_items.setVisible(false);
                action_add_items.setVisible(false);
                action_deselect_all_items.setVisible(false);
                action_show_shopping_list_sort_dialog.setVisible(false);
                action_refresh.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        MyLog.i("MainActivity", "onOptionsItemSelected: menu itemID = " + item.getItemId());

        switch (item.getItemId()) {

            // TODO: Remove run parse test
            case R.id.action_run_parse_test:
                //Toast.makeText(this, "action_run_parse_test", Toast.LENGTH_SHORT).show();
                runParseTest();
                break;

            // Shown in Shopping Lists and Store Lists
            case R.id.action_deselect_struck_out_items:
                Item.deselectStruckOutItems(this);
                EventBus.getDefault().post(new MyEvents.updateUI(null));
                break;

            case R.id.action_add_items:
                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_MASTER_ITEMS_LIST));
                break;

            case R.id.action_deselect_all_items:
                Item.deselectAllItems(this);
                EventBus.getDefault().post(new MyEvents.updateUI(null));
                break;

            // Shown only in Shopping Lists but not if IsMapping Store
            case R.id.action_show_shopping_list_sort_dialog:
                FragmentManager fm = getFragmentManager();
                dialogShoppingListSorting dialog = dialogShoppingListSorting.newInstance();
                dialog.show(fm, "dialogShoppingListSorting");
                break;


            // NOT shown in Edit/New store or if isMappingStore
            case R.id.action_refresh:
                syncA1GroceryListData(MySettings.ACTION_UPLOAD_AND_DOWNLOAD);
//                Toast.makeText(this, "action_refresh", Toast.LENGTH_SHORT).show();
                break;

            // Shown in all fragment views.
            case R.id.action_settings:
                Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_about:
                Toast.makeText(this, "action_about", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_log_out:
                ParseUser.logOut();

                // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
                // logs out on older devices, we'll just exit.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Intent intent = new Intent(MainActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    finish();
                }
                break;

            default:
                return false;

        }
        return true;
    }


    //endregion

    //region Drawer Methods
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        MyLog.i("MainActivity", "onDrawerOpened");
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        MyLog.i("MainActivity", "onDrawerClosed");
        if (mDrawerItemSelected) {
            showFragment(mSelectedFragmentID, false);
            mDrawerItemSelected = false;
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    private void closeDrawer(int fragmentID) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerItemSelected = true;
        mDrawerList.setItemChecked(fragmentID, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    //endregion


    //region Google Play (Location) Services callbacks
    @Override
    public void onConnected(Bundle bundle) {
        MyLog.i("MainActivity", "Google Play Services onConnected.");
        mPlayServicesConnected = true;

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            MySettings.setLastLocation(lastLocation);
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        MyLog.i("MainActivity", "Google Play Services startLocationUpdates");
        if (mPlayServicesConnected) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationFound = true;
        MySettings.setLastLocation(location);
        String lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        MyLog.i("MainActivity", "onLocationChanged: time = " + lastUpdateTime);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mPlayServicesConnected = false;
        String errorMessage = "UNKNOWN CAUSE";
        switch (cause) {
            case CAUSE_NETWORK_LOST:
                errorMessage = "CAUSE_NETWORK_LOST";
                break;

            case CAUSE_SERVICE_DISCONNECTED:
                errorMessage = "CAUSE_SERVICE_DISCONNECTED";
                break;
        }
        MyLog.d("MainActivity", "Google Play Services onConnectionSuspended: cause = " + errorMessage);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mPlayServicesConnected = false;
        String errorMessage = "UNKNOWN CONNECTION RESULT ERROR";
        switch (connectionResult.getErrorCode()) {
            case 1:
                errorMessage = "SERVICE_MISSING";
                break;

            case 2:
                errorMessage = "SERVICE_VERSION_UPDATE_REQUIRED";
                break;

            case 3:
                errorMessage = "SERVICE_DISABLED";
                break;
        }
        MyLog.d("MainActivity", "Google Play Services onConnectionFailed: cause = " + errorMessage);
    }
    //endregion

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            MyLog.i("DrawerItemClickListener", "onItemClick: position = " + position);
            mSelectedFragmentID = position;
            closeDrawer(position);
        }
    }

    private void runParseTest() {
        // TODO: remove runParseTest
        MyLog.i("MainActivity", "runParseTest");

//        for (int i = 0; i < 21; i++) {
//            mFragmentBackstack.push(i);
//        }
//        mFragmentBackstack.save();
//        mFragmentBackstack = new FragmentBackstack();
//        int lastFragmentID = mFragmentBackstack.pop();
//        mFragmentBackstack.clear();
//        mFragmentBackstack.save();


//        final boolean selection = true;
//        final HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("isSelected", selection);
//        ParseCloud.callFunctionInBackground("setAllItemsSelection", params, new FunctionCallback<Integer>() {
//
//            @Override
//            public void done(final Integer numberOfSelectedItems, ParseException e) {
//                if (e == null) {
//                    // Success. Items deselected in Parse cloud
//                    MyLog.i("MainActivity", "set " + numberOfSelectedItems + " Items isSelected = " + selection);
//
//                } else {
//                    MyLog.e("MainActivity", "FAILED to setAllItemsSelection = " + selection);
//                }
//            }
//        });


        //uploadMethods.loadInitialDataToParse(this);

    }
}
