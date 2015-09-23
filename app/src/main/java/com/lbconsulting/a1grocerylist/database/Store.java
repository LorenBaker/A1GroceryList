package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds Store data
 */

import android.content.Context;

import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

@ParseClassName("Store")
public class Store extends ParseObject {
    public static final String AUTHOR = "author";
    public static final String STORE_CHAIN = "storeChain";
    public static final String STORE_REGIONAL_NAME = "storeRegionalName";
    public static final String ADDRESS1 = "address1";   // number and street
    public static final String ADDRESS2 = "address2";   // ste, etc.
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String ZIP = "zip";
    public static final String COUNTRY = "country";
    public static final String WEBSITE_URL = "websiteURL";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String STORE_GEO_POINT = "storeGeoPoint";
    public static final String IS_CHECKED = "isChecked";
    public static final String IS_DIRTY = "isDirty";
    public static final String SORT_KEY = "sortOrder";

    public static int getSortingKey() {
        return mSortingKey;
    }

    public static void setSortingKey(int sortingKey) {
        Store.mSortingKey = sortingKey;
    }

    private static int mSortingKey;

    public Store() {
        // A default constructor is required.
    }

    public static ParseQuery<Store> getQuery() {
        return ParseQuery.getQuery(Store.class);
    }

    public static Store getStore(String storeID) {
        Store store = null;
        if(!storeID.equals(MySettings.NOT_AVAILABLE)) {
            try {
                ParseQuery<Store> query = getQuery();
                query.whereEqualTo("objectId", storeID);
                query.include(STORE_CHAIN);
                query.fromLocalDatastore();
                store = query.getFirst();
            } catch (ParseException e) {
                MyLog.e("Store", "getStore: ParseException: " + e.getMessage());
            }
        }
        return store;
    }

    public static List<Store> getAllStoresFromParse() {
        // TODO: change getAllStoresFromParse into cloud code
        List<Store> storeList = new ArrayList<>();
        try {
            ParseQuery<Store> query = getQuery();
            query.include(STORE_CHAIN);
            query.setLimit(MySettings.QUERY_LIMIT_STORES);
            storeList = query.find();
        } catch (ParseException e) {
            MyLog.e("Store", "getAllStoresFromParse: ParseException" + e.getMessage());
        }

        return storeList;
    }

    public static List<Store> getAllStores() {
        List<Store> storeList = new ArrayList<>();
        try {
            ParseQuery<Store> query = getQuery();
            query.include(STORE_CHAIN);
            query.orderByAscending(SORT_KEY);
            query.fromLocalDatastore();
            storeList = query.find();
        } catch (ParseException e) {
            MyLog.e("Store", "getAllStores: ParseException" + e.getMessage());
        }

        return storeList;
    }
    public static void createAndSaveStore(final Context context, StoreChain storeChain, String regionalStoreName,
                                          String address1, String address2, String city, String state,
                                          String zip, String country) {

        if (!A1Utils.isNetworkAvailable(context)) {
            String title = "Network Not Available";
            String msg = "Sorry, but you can not create a Store without an internet network connection.";
            EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
            return;
        }

        ParseACL storeACL = new ParseACL(ParseUser.getCurrentUser());
        storeACL.setPublicReadAccess(true);
        storeACL.setPublicWriteAccess(true);

        mSortingKey++;

        final Store store = new Store();

        store.setStoreChain(storeChain);
        store.setStoreRegionalName(regionalStoreName);
        store.setAuthor(ParseUser.getCurrentUser());
        store.setAddress1(address1);
        store.setAddress2(address2);
        store.setCity(city);
        store.setState(state);
        store.setZip(zip);
        store.setCountry(country);
        store.setPhoneNumber("");
        store.setWebsiteUrl("");
        store.setStoreDirty(false);
        store.setChecked(false);
        store.setSortKey(mSortingKey);
      //  store.setStoreGeoPoint(null);
        store.setACL(storeACL);

        store.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // success
                    String title = "Created \"" + store.getStoreChainAndRegionalName() + "\".";
                    String msg = "Now initializing and downloading the new store's map.";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, ""));
                    MyLog.i("Store", title);

                } else {
                    String title = "Failed to created \"" + store.getStoreChainAndRegionalName() + "\".";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, ""));
                    MyLog.e("Store", title);
                }
            }
        });
    }


    public String getStoreID() {
        return getObjectId();
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR);
    }

    public void setAuthor(ParseUser currentUser) {
        put(AUTHOR, currentUser);
    }

    public StoreChain getStoreChain() {
        return (StoreChain) getParseObject(STORE_CHAIN);
    }

    public void setStoreChain(StoreChain storeChain) {
        put(STORE_CHAIN, storeChain);
    }

    public String getStoreRegionalName() {
        return getString(STORE_REGIONAL_NAME);
    }

    public void setStoreRegionalName(String locationName) {
        put(STORE_REGIONAL_NAME, locationName);
    }

    public String getStoreFullName() {
        return getStoreChain().getStoreChainName() + " " + getStoreRegionalName()
                + "\n" + getCity() + " " + getState();
    }

    public String getStoreChainAndRegionalName() {
        return getStoreChain().getStoreChainName() + " " + getStoreRegionalName();
    }

    public String getAddress1() {
        return getString(ADDRESS1);
    }

    public void setAddress1(String address1) {
        put(ADDRESS1, address1);
    }

    public String getAddress2() {
        return getString(ADDRESS2);
    }

    public void setAddress2(String address2) {
        put(ADDRESS2, address2);
    }

    public String getCity() {
        return getString(CITY);
    }

    public void setCity(String city) {
        put(CITY, city);
    }

    public String getState() {
        return getString(STATE);
    }

    public void setState(String state) {
        put(STATE, state);
    }

    public String getZip() {
        return getString(ZIP);
    }

    public void setZip(String zipCode) {
        put(ZIP, zipCode);
    }

    public String getCountry() {
        return getString(COUNTRY);
    }

    public void setCountry(String country) {
        put(COUNTRY, country);
    }

    public String getWebsiteUrl() {
        return getString(WEBSITE_URL);
    }

    public void setWebsiteUrl(String websiteUrl) {
        put(WEBSITE_URL, websiteUrl);
    }

    public String getPhoneNumber() {
        return getString(PHONE_NUMBER);
    }

    public void setPhoneNumber(String phoneNumber) {
        put(PHONE_NUMBER, phoneNumber);
    }

    public ParseGeoPoint getStoreGeoPoint() {
        return getParseGeoPoint(STORE_GEO_POINT);
    }

    public void setStoreGeoPoint(ParseGeoPoint storeGeoPoint) {
        put(STORE_GEO_POINT, storeGeoPoint);
    }

    public void setStoreGeoPoint(Double storeLatitude, Double storeLongitude) {
        ParseGeoPoint storeGeoPoint = new ParseGeoPoint(storeLatitude, storeLongitude);
        put(STORE_GEO_POINT, storeGeoPoint);
    }

    public Double getStoreLatitude() {
        return getStoreGeoPoint().getLatitude();
    }

    public Double getStoreLongitude() {
        return getStoreGeoPoint().getLongitude();
    }

    public int getSortKey() {
        return getInt(SORT_KEY);
    }

    public void setSortKey(int sortKey) {
        put(SORT_KEY, sortKey);
    }

    public boolean isChecked() {
        return getBoolean(IS_CHECKED);
    }

    public void setChecked(boolean isChecked) {
        put(IS_CHECKED, isChecked);
    }

    public boolean isStoreDirty() {
        return getBoolean(IS_DIRTY);
    }

    public void setStoreDirty(boolean storeDirty) {
        put(IS_DIRTY, storeDirty);
    }

    public static void unPinAll() {
        try {
            List<Store > allStores =getAllStores();
            ParseObject.unpinAll(allStores);
        } catch (ParseException e) {
            MyLog.e("Store", "unPinAll: ParseException" + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return getStoreChainAndRegionalName();
    }

}


