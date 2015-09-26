package com.lbconsulting.a1grocerylist.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.DrawerArrayAdapter;
import com.lbconsulting.a1grocerylist.adapters.StoreListPagerAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.dialogs.dialogEditItem;
import com.parse.ParseUser;

import de.greenrobot.event.EventBus;


public class StoreListActivity extends Activity implements DrawerLayout.DrawerListener {

    private String[] mDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private int mSelectedFragmentID;

    private ViewPager mStoreListPager;
    private StoreListPagerAdapter mStoreListPagerAdapter;

    private ActionBar mActionBar;
    private String mActiveStoreID;


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
        MyLog.i("StoreListActivity", "onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_store_list);

        MySettings.setContext(this);
        EventBus.getDefault().register(this);
        mActionBar = getActionBar();

        mActiveStoreID = MySettings.getActiveStoreID();
        mStoreListPager = (ViewPager) findViewById(R.id.storeListPager);

        //region Drawer setup
        mDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        DrawerArrayAdapter adapter = new DrawerArrayAdapter(this, mDrawerItemTitles);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //endregion

    }

    @Override
    public void onBackPressed() {
        MyLog.i("StoreListActivity", "onBackPressed");
        Intent intent = new Intent(this, MainActivity.class);
        // suppress syncing with Parse when StoreListActivity starts
        Bundle args = new Bundle();
        args.putBoolean(MySettings.ARG_SYNC_WITH_PARSE_ON_STARTUP, false);
        args.putBoolean(MySettings.ARG_STORES_ACTIVITY_ON_BACK_PRESSED, true);
        intent.putExtras(args);
        startActivity(intent);
        finish();
    }

    //region InstanceState Methods
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        MyLog.i("StoreListActivity", "onSaveInstanceState");
//    }
//
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        MyLog.i("StoreListActivity", "onRestoreInstanceState");
//        mActiveStoreID = MySettings.getActiveStoreID();
//    }
    //endregion


    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i("StoreListActivity", "onResume");

        setActionBarTitle("Stores");

        mActiveStoreID = MySettings.getActiveStoreID();
        // create and set pager adapter
        mStoreListPagerAdapter = new StoreListPagerAdapter(getFragmentManager());
        mStoreListPager.setAdapter(mStoreListPagerAdapter);
        mStoreListPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // A list page has been selected
                mActiveStoreID = mStoreListPagerAdapter.getStoreID(position);
                MySettings.setActiveStoreID(mActiveStoreID);
            }
        });

        int pagerPosition = mStoreListPagerAdapter.findStoreIDPosition(mActiveStoreID);
        mStoreListPager.setCurrentItem(pagerPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.i("StoreListActivity", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.i("StoreListActivity", "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    //region onEvents

    private void setActionBarTitle(String title) {
        mActionBar.setTitle(title);
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


    public void onEvent(MyEvents.updateUI event) {
        if (mStoreListPagerAdapter != null) {
            mStoreListPagerAdapter.loadData();
            mStoreListPagerAdapter.notifyDataSetChanged();
        }
    }
    //endregion

    private void showFragment(int fragmentID) {
        FragmentManager fm = getFragmentManager();
        Intent intent = new Intent(this, MainActivity.class);
        // suppress syncing with Parse when StoreListActivity starts
        Bundle args = new Bundle();
        args.putBoolean(MySettings.ARG_SYNC_WITH_PARSE_ON_STARTUP, false);
        args.putBoolean(MySettings.ARG_STORES_ACTIVITY_ON_BACK_PRESSED, false);
        intent.putExtras(args);

        boolean startMainActivity = true;
        switch (fragmentID) {

            case MySettings.FRAG_SHOPPING_LIST:
                MySettings.setActiveFragmentID(MySettings.FRAG_SHOPPING_LIST);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST));
                break;

            case MySettings.FRAG_STORE_LIST_BY_AISLE:
                // Store lists
                // Do nothing
                startMainActivity = false;
                break;

            case MySettings.FRAG_MASTER_ITEMS_LIST:
                MySettings.setActiveFragmentID(MySettings.FRAG_MASTER_ITEMS_LIST);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_MASTER_ITEMS_LIST));
                break;

            case MySettings.FRAG_SHOPPING_LIST_BY_GROUP:
                MySettings.setActiveFragmentID(MySettings.FRAG_SHOPPING_LIST_BY_GROUP);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOPPING_LIST_BY_GROUP));
                break;

            case MySettings.FRAG_CULL_ITEMS:
                MySettings.setActiveFragmentID(MySettings.FRAG_CULL_ITEMS);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_CULL_ITEMS));
                break;

            case MySettings.FRAG_SHOW_ALL_STORES:
                startMainActivity = false;
//                MySettings.setActiveFragmentID(MySettings.FRAG_SHOW_ALL_STORES);
                Toast.makeText(this, mDrawerItemTitles[MySettings.FRAG_SHOW_ALL_STORES] + " TO COME.", Toast.LENGTH_SHORT).show();
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SHOW_ALL_STORES));
                break;

            case MySettings.FRAG_MAP_STORE:
                MySettings.setActiveFragmentID(MySettings.FRAG_MAP_STORE);
                MySettings.setStoreIDtoMapID(mActiveStoreID);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_MAP_STORE));
                break;

            case MySettings.FRAG_SETTINGS:
                startMainActivity = false;
//                MySettings.setActiveFragmentID(MySettings.FRAG_SETTINGS);
                Toast.makeText(this, mDrawerItemTitles[MySettings.FRAG_SETTINGS] + " TO COME.", Toast.LENGTH_SHORT).show();
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_SETTINGS));
                break;

            case MySettings.FRAG_EDIT_NEW_STORE:
                MySettings.setActiveFragmentID(MySettings.FRAG_EDIT_NEW_STORE);
                MyLog.i("StoreListActivity", "showFragment: " + MySettings.getFragmentTag(MySettings.FRAG_EDIT_NEW_STORE));
                break;

            default:
                MySettings.setActiveFragmentID(MySettings.FRAG_SHOPPING_LIST);
                MyLog.i("StoreListActivity", "showFragment: " + " Default");

        }
        if (startMainActivity) {
            startActivity(intent);
            finish();
        } else {
            startMainActivity = true;
        }
    }

    //region Options Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.i("StoreListActivity", "onCreateOptionsMenu: menu_activity_main");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_store_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        MyLog.i("StoreListActivity", "onOptionsItemSelected: menu itemID = " + item.getItemId());

        switch (item.getItemId()) {

            case R.id.action_deselect_struck_out_items:
                Item.deselectStruckOutItems(this);
                EventBus.getDefault().post(new MyEvents.updateUI(null));
                break;

            case R.id.action_add_items:
                showFragment(MySettings.FRAG_MASTER_ITEMS_LIST);
                break;

            case R.id.action_place:
                Toast.makeText(this, "action_place", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_deselect_all_items:
                Item.deselectAllItems(this);
                EventBus.getDefault().post(new MyEvents.updateUI(null));
                break;

            case R.id.action_new_store:
                MySettings.setIsNewStore(true);
                showFragment(MySettings.FRAG_EDIT_NEW_STORE);
                break;

            case R.id.action_edit_store:
                MySettings.setIsNewStore(false);
                showFragment(MySettings.FRAG_EDIT_NEW_STORE);
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
                    Intent intent = new Intent(StoreListActivity.this, DispatchActivity.class);
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
        MyLog.i("StoreListActivity", "onDrawerOpened");
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        MyLog.i("StoreListActivity", "onDrawerClosed");
        showFragment(mSelectedFragmentID);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    private void closeDrawer(int fragmentID) {

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(fragmentID, true);
        mDrawerLayout.closeDrawer(mDrawerList);
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


}
