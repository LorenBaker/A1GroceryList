package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds Location data
 */

import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Location")
public class Location extends ParseObject {
    //private static final String LOCATION_ID = "objectId";
    public static final String AUTHOR = "author";
    public static final String LOCATION_NAME = "locationName";
    public static final String IS_CHECKED = "isChecked";
    public static final String IS_DEFAULT = "isDefault";
    public static final String IS_DIRTY = "isDirty";
    public static final String SORT_KEY = "sortKey";


    public Location() {
        // A default constructor is required.
    }


    public String getLocationID() {
        return getObjectId();
    }

    public int getSortKey() {
        int sortKey = 0;
        try {
            sortKey= getInt(SORT_KEY);
        } catch (Exception e) {
            MyLog.e("Location", "getSortKey: Exception" + e.getMessage());
        }
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        put(SORT_KEY, sortKey);
    }

    public String getLocationName() {
        return getString(LOCATION_NAME);
    }

    public void setLocationName(String locationName) {
        put(LOCATION_NAME, locationName);
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR);
    }

    public void setAuthor(ParseUser currentUser) {
        put(AUTHOR, currentUser);
    }

    public boolean isChecked() {
        return getBoolean(IS_CHECKED);
    }

    public void setChecked(boolean isChecked) {
        put(IS_CHECKED, isChecked);
    }

    public boolean isLocationDirty() {
        return getBoolean(IS_DIRTY);
    }

    public void setLocationDirty(boolean locationDirty) {
        put(IS_DIRTY, locationDirty);
    }

    public boolean isDefault() {
        return getBoolean(IS_DEFAULT);
    }

    public void setDefault(boolean defaultLocation) {
        put(IS_DEFAULT, defaultLocation);
    }

    public static ParseQuery<Location> getQuery() {
        return ParseQuery.getQuery(Location.class);
    }


    @Override
    public String toString() {
        return getLocationName();
    }

    public static Location getLocation(String objectId) {
        Location group = null;
        try {
            ParseQuery<Location> query = getQuery();
            query.whereEqualTo("objectId", objectId);
            query.fromLocalDatastore();
            group = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("Location", "getLocation: ParseException: " + e.getMessage());
        }
        return group;
    }

    public static List<Location> getAllLocations() {
        List<Location> allLocations = new ArrayList<>();
        try {
            ParseQuery<Location> query = getQuery();
            query.orderByAscending(SORT_KEY);
            query.fromLocalDatastore();
            allLocations = query.find();
        } catch (ParseException e) {
            MyLog.e("Location", "getAllLocations: ParseException: " + e.getMessage());
        }
        return allLocations;
    }
    public static Location getDefaultLocation() {
        Location group = null;
        try {
            ParseQuery<Location> query = getQuery();
            query.whereEqualTo(IS_DEFAULT, true);
            query.fromLocalDatastore();
            group = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("Location", "getDefaultLocation: ParseException: " + e.getMessage());
        }
        return group;
    }

    public static void unPinAll() {
        try {
            List<Location> allLocations =getAllLocations();
            ParseObject.unpinAll(allLocations);
        } catch (ParseException e) {
            MyLog.e("Location", "unPinAll: ParseException" + e.getMessage());
        }
    }
}


