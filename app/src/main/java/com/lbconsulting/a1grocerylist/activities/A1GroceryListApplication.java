package com.lbconsulting.a1grocerylist.activities;

import android.app.Application;

import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Initial_Items;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.database.Location;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreChain;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;


public class A1GroceryListApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i("A1GroceryListApplication", "onCreate");

        // TODO: Enable crash reporting and other analytics
        // Initialize Crash Reporting.
        //ParseCrashReporting.enable(this);

        // Add your initialization code here
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(Store.class);
        ParseObject.registerSubclass(StoreChain.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Initial_Items.class);
        ParseObject.registerSubclass(StoreMapEntry.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Z1uTyZFcvSsV74AdrqbfWPe44WhqtTvwmJupITew", "ZuBh1PV8oBebw2xgpURpdF5XDms5zS11QpYW9Kpn");

        //user's data is only accessible by the user unless explicit permission is given
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);

        MyLog.i("A1GroceryListApplication", "onCreate: Parse initialized");

    }

    @Override
    public void onTerminate() {
        MyLog.i("A1GroceryListApplication", "onTerminate");
        super.onTerminate();
    }
}
