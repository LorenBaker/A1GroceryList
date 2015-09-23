package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds Location data
 */

import android.content.Context;

import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

@ParseClassName("StoreMap")
public class StoreMapEntry extends ParseObject {

    public static final String AUTHOR = "author";
    public static final String STORE = "store";
    public static final String GROUP = "group";
    public static final String LOCATION = "location";
    public static final String IS_DIRTY = "isDirty";

    public StoreMapEntry() {
        // A default constructor is required.
    }

    public static ParseQuery<StoreMapEntry> getQuery() {
        return ParseQuery.getQuery(StoreMapEntry.class);
    }

    public static StoreMapEntry getStoreMapEntry(String storeMapEntryID) {
        StoreMapEntry storeMapEntry = null;
        try {
            ParseQuery<StoreMapEntry> query = getQuery();
            query.whereEqualTo("objectId", storeMapEntryID);
            query.fromLocalDatastore();
            storeMapEntry = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("StoreMapEntry", "getStoreMapEntry: ParseException" + e.getMessage());
        }
        return storeMapEntry;
    }

    public static StoreMapEntry getStoreMapEntry(Store store, Group group) {
        StoreMapEntry storeMapEntry = null;
        try {
            ParseQuery<StoreMapEntry> query = getQuery();
            query.whereEqualTo(STORE, store);
            query.whereEqualTo(GROUP, group);
            query.fromLocalDatastore();
            storeMapEntry = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("StoreMapEntry", "getStoreMapEntry: ParseException" + e.getMessage());
        }
        return storeMapEntry;
    }


    public static Location getGroupLocation(Store store, Group group) {
        Location location = null;
        StoreMapEntry storeMapEntry = getStoreMapEntry(store, group);
        if (storeMapEntry != null) {
            location = storeMapEntry.getLocation();
        }
        return location;
    }

    public static List<StoreMapEntry> getStoreMap(Store store) {
        List<StoreMapEntry> storeMap = new ArrayList<>();
        try {
            ParseQuery<StoreMapEntry> query = getQuery();
            query.whereEqualTo(STORE, store);
            query.fromLocalDatastore();
            storeMap = query.find();
        } catch (ParseException e) {
            MyLog.e("StoreMapEntry", "getStoreMap: ParseException: " + e.getMessage());
        }
        return storeMap;
    }

    private static List<StoreMapEntry> getAllStoreMaps() {
        List<StoreMapEntry> storeMaps = new ArrayList<>();
        try {
            ParseQuery<StoreMapEntry> query = getQuery();
            query.fromLocalDatastore();
            storeMaps = query.find();
        } catch (ParseException e) {
            MyLog.e("StoreMapEntry", "getAllStoreMaps: ParseException: " + e.getMessage());
        }
        return storeMaps;
    }

    public static void unPinAll() {
        try {
            List<StoreMapEntry > allStoreMaps =getAllStoreMaps();
            ParseObject.unpinAll(allStoreMaps);
        } catch (ParseException e) {
            MyLog.e("StoreMapEntry", "unPinAll: ParseException" + e.getMessage());
        }
    }

    public static void setGroupLocation(Context context, Store store, Group group, Location location) {
        StoreMapEntry entry = getStoreMapEntry(store, group);
        if (entry != null) {
            EventBus.getDefault().post(new MyEvents.updateSeparatorText());
            syncStoreMapEntry(context, entry, location);
        } else {
            MyLog.e("StoreMapEntry", "setGroupLocation: Unable to find Store Map Entry!: Store: "
                    + store.getStoreChainAndRegionalName() + " Group: " + group.getGroupName());
        }
    }

    private static void syncStoreMapEntry(Context context, final StoreMapEntry storeMapEntry, final Location location) {
        if (storeMapEntry != null && location != null) {
            // set the local datastore store map entry
            storeMapEntry.setLocation(location);

            // sync the Parse store map entry
            if (A1Utils.isNetworkAvailable(context)) {
                final HashMap<String, String> params = new HashMap<String, String>();
                params.put("storeMapEntryID", storeMapEntry.getStoreMapEntryID());
                params.put("locationID", location.getLocationID());

                ParseCloud.callFunctionInBackground("syncStoreMapEntry", params, new FunctionCallback<String>() {

                    @Override
                    public void done(final String resultString, ParseException e) {
                        if (e == null) {
                            // Success.
                            MyLog.i("StoreMapEntry", resultString);
                            //EventBus.getDefault().post(new MyEvents.showOkDialog(resultString,""));
                        } else {
                            MyLog.e("StoreMapEntry", e.getMessage());
                            // set storeMapEntry dirty
                            storeMapEntry.setStoreMapEntryDirty(true);
                        }
                    }
                });

            } else {
                // the network is NOT available
//                storeMapEntry.setStoreMapEntryDirty(true);
                storeMapEntry.saveEventually();
            }
        }
    }

    public String getStoreMapEntryID() {
        return getObjectId();
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR);
    }

    public void setAuthor(ParseUser currentUser) {
        put(AUTHOR, currentUser);
    }

    public Store getStore() {
        return (Store) get(STORE);
    }

    public void setStore(Store store) {
        put(STORE, store);
    }

    public Group getGroup() {
        return (Group) get(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public Location getLocation() {
        return (Location) get(LOCATION);
    }

    private void setLocation(Location location) {
        put(LOCATION, location);
    }

    public boolean isStoreMapEntryDirty() {
        return getBoolean(IS_DIRTY);
    }

    public void setStoreMapEntryDirty(boolean storeMapEntryDirty) {
        put(IS_DIRTY, storeMapEntryDirty);
    }

    @Override
    public String toString() {
        return getGroup().getGroupName() + " --> " + getLocation().getLocationName();
    }

}


