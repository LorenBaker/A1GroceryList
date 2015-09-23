package com.lbconsulting.a1grocerylist.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

/**
 * Helper methods for Application Settings
 */
public class MySettings {

    public static final int FRAG_SHOPPING_LIST = 0;
    public static final int FRAG_STORE_LIST_BY_AISLE = 1;
    public static final int FRAG_MASTER_ITEMS_LIST = 2;
    public static final int FRAG_SHOPPING_LIST_BY_GROUP = 3;
    public static final int FRAG_CULL_ITEMS = 4;
    public static final int FRAG_SHOW_ALL_STORES = 5;
    public static final int FRAG_MAP_STORE = 6;
    public static final int FRAG_SETTINGS = 7;
    public static final int FRAG_EDIT_NEW_STORE = 100;

    public static final int ACTION_DOWNLOAD_ONLY = 2;
    public static final int ACTION_UPLOAD_AND_DOWNLOAD = 3;
    public static final int ACTION_UPLOAD_ONLY = 1;

    public static final int BACKGROUND_COLOR_GENOA = 0;
    public static final int BACKGROUND_COLOR_OPAL = 1;

    public static final int INTERVAL_FAST = 30000; // 30 seconds
    public static final int INTERVAL_SLOW = 15 * 60000; // 15 minutes

    public static final int MAX_NUMBER_OF_FAST_SLEEP_INTERVALS = 60;
    public static final int QUERY_LIMIT_GROUPS = 50;
    public static final int QUERY_LIMIT_ITEMS = 500;
    public static final int QUERY_LIMIT_STORES = 100;
    public static final int QUERY_LIMIT_LOCATIONS = 50;
    public static final int QUERY_LIMIT_STORE_CHAINS = 500;
    public static final int QUERY_LIMIT_NEAREST_STORES = 5;
    public static final int QUERY_LIMIT_STORE_MAPS = QUERY_LIMIT_NEAREST_STORES * QUERY_LIMIT_GROUPS;
    public static final int INITIAL_NUMBER_OF_AISLES = 25;

    public static final int NOTIFICATION_DOWNLOAD_ID = 33;
    public static final int SORT_ALPHABETICAL = 0;

    public static final int SORT_BY_GROUP = 2;
    public static final int SORT_DATE_UPDATED = 4;
    public static final int SORT_FAVORITES_FIRST = 3;
    public static final int SORT_MANUALLY = 5;
    public static final int SORT_REVERSE_ALPHABETICAL = 1;
    public static final int SORT_SELECTED_FIRST = 6;
    public static final String ARG_UPLOAD_DOWNLOAD_ACTION = "argUploadDownloadAction";

    public static final String ARG_USER_LATITUDE = "argUserLatitude";
    public static final String ARG_USER_LONGITUDE = "argUserLongitude";
    public static final String ARG_STORE_CHAIN_ID = "argStoreChainID";
    public static final String ARG_STORE_REGIONAL_NAME = "argStoreRegionalName";
    public static final String ARG_SYNC_WITH_PARSE_ON_STARTUP = "argSyncWithParseOnStartup";
    public static final String ARG_STORES_ACTIVITY_ON_BACK_PRESSED = "argStoresActivityOnBackPressed";
    public static final String NOT_AVAILABLE = "N/A";

    public static final String SETTING_ACTIVE_FRAGMENT_ID = "activeFragments";


    public static final String SETTING_ACTIVE_STORE_ID = "activeStoreID";
    public static final String SETTING_STORE_ID_TO_MAP = "storeIDtoMap";
    public static final String SETTING_IS_NEW_STORE = "isNewStoreID";
    public static final String SETTING_LAST_SYNC_DATE_GROUPS = "lastSyncDateGroups";
    public static final String SETTING_LAST_SYNC_DATE_ITEMS = "lastSyncDateItems";
    public static final String SETTING_LAST_SYNC_DATE_LOCATIONS = "lastSyncDateLocations";
    public static final String SETTING_LAST_SYNC_DATE_STORE_CHAINS = "lastSyncDateStoreChains";
    public static final String SETTING_LAST_SYNC_DATE_STORE_MAPS = "lastSyncDateStoreMaps";
    public static final String SETTING_LAST_SYNC_DATE_STORES = "lastSyncDateStores";
    public static final String SETTING_MASTER_LIST_SORT_ORDER = "masterListSortOrder";
    public static final String SETTING_SHOPPING_LIST_SORT_ORDER = "shoppingListSortOrder";
    public static final String SETTING_SHOW_FAVORITES = "showFavorites";
    public static final String SETTING_STORE_LIST_SORT_ORDER = "storeListSortOrder";

    public static final String SETTING_FRAGMENT_BACKSTACK = "fragmentBackstack";
    public static final String SETTING_FRAGMENT_BACKSTACK_MAX_SIZE = "fragmentBackstackMaxSize";
    private static final int STARTING_BACKSTACK_SIZE = 26;

    public static final String SETTING_LAST_LONGITUDE = "lastLongitude";
    public static final String SETTING_LAST_LATITUDE = "lastLatitude";

    public static  final double LATITUDE_NA = 100;
    public static  final double LONGITUDE_NA = 400;

    private static final String A1_GROCERY_LIST_SAVED_STATES = "a1GroceryListSavedStates";
    private static Context mContext;

    public static void setContext(Context context) {

        mContext = context;
    }

    public static String getFragmentTag(int fragmentID) {
        String fragmentTag = "";
        switch (fragmentID) {
            case FRAG_SHOPPING_LIST:
                fragmentTag = "FRAG_SHOPPING_LIST";
                break;

            case FRAG_STORE_LIST_BY_AISLE:
                fragmentTag = "FRAG_STORE_LIST_BY_AISLE";
                break;

            case FRAG_MASTER_ITEMS_LIST:
            fragmentTag = "FRAG_MASTER_ITEMS_LIST";
            break;

            case FRAG_SHOPPING_LIST_BY_GROUP:
                fragmentTag = "FRAG_SHOPPING_LIST_BY_GROUP";
                break;

            case FRAG_CULL_ITEMS:
                fragmentTag = "FRAG_CULL_ITEMS";
                break;

            case FRAG_SHOW_ALL_STORES:
                fragmentTag = "FRAG_SHOW_ALL_STORES";
                break;

            case FRAG_MAP_STORE:
                fragmentTag = "FRAG_MAP_STORE";
                break;

            case FRAG_SETTINGS:
                fragmentTag = "FRAG_SETTINGS";
                break;

            case FRAG_EDIT_NEW_STORE:
                fragmentTag = "FRAG_EDIT_NEW_STORE";
                break;
        }

        return fragmentTag;
    }

    //region ActiveFragmentID
    public static int getActiveFragmentID() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getInt(SETTING_ACTIVE_FRAGMENT_ID, FRAG_MASTER_ITEMS_LIST);
    }

    public static void setActiveFragmentID(int activeFragmentID) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putInt(SETTING_ACTIVE_FRAGMENT_ID, activeFragmentID);
        editor.apply();
    }
    //endregion

    //region ActiveStoreID
    public static String getActiveStoreID() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getString(SETTING_ACTIVE_STORE_ID, NOT_AVAILABLE);
    }


    public static void setActiveStoreID(String activeStoreID) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putString(SETTING_ACTIVE_STORE_ID, activeStoreID);
        editor.apply();
    }
    //endregion

    //region Store to Map
    public static String getStoreIDtoMap() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getString(SETTING_STORE_ID_TO_MAP, NOT_AVAILABLE);
    }


    public static void setStoreIDtoMapID(String storeID) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putString(SETTING_STORE_ID_TO_MAP, storeID);
        editor.apply();
    }
    //endregion

    //region IsNewStore
    public static boolean IsNewStore() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getBoolean(SETTING_IS_NEW_STORE, false);
    }

    public static void setIsNewStore(boolean isNewStore) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putBoolean(SETTING_IS_NEW_STORE, isNewStore);
        editor.apply();
    }
    //endregion

    //region SortOrder
    public static int getMasterListSortOrder() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getInt(SETTING_MASTER_LIST_SORT_ORDER, SORT_ALPHABETICAL);
    }

    public static void setMasterListSortOrder(int sortOrder) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putInt(SETTING_MASTER_LIST_SORT_ORDER, sortOrder);
        editor.apply();
    }

    public static int getShoppingListSortOrder() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getInt(SETTING_SHOPPING_LIST_SORT_ORDER, SORT_ALPHABETICAL);
    }

    public static void setShoppingListSortOrder(int sortOrder) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putInt(SETTING_SHOPPING_LIST_SORT_ORDER, sortOrder);
        editor.apply();
    }
    //endregion

    //region ShowFavorites
    public static boolean showFavorites() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getBoolean(SETTING_SHOW_FAVORITES, false);
    }

    public static void setShowFavorites(boolean showFavorites) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putBoolean(SETTING_SHOW_FAVORITES, showFavorites);
        editor.apply();
    }
    //endregion

    //region Last Location
    public static Location getLastLocation() {
        Location location = null;
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        long latitude = savedState.getLong(SETTING_LAST_LATITUDE, 0);
        long longitude = savedState.getLong(SETTING_LAST_LONGITUDE, 0);
// TODO: Use non-valid latitude and longitude defaults
        if (latitude != 0) {
            location = new Location("");
            location.setLatitude(Double.longBitsToDouble(latitude));
            location.setLongitude(Double.longBitsToDouble(longitude));
        }

        return location;
    }

    public static void setLastLocation(Location location) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        if (location == null) {
            editor.putLong(SETTING_LAST_LATITUDE, 0);
            editor.putLong(SETTING_LAST_LONGITUDE, 0);
        } else {
            long latitude = Double.doubleToRawLongBits(location.getLatitude());
            long longitude = Double.doubleToRawLongBits(location.getLongitude());
            editor.putLong(SETTING_LAST_LATITUDE, latitude);
            editor.putLong(SETTING_LAST_LONGITUDE, longitude);
        }
        editor.apply();
    }
    //endregion

    //region Fragment Backstack
    public static String getFragmentBackstack() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getString(SETTING_FRAGMENT_BACKSTACK, NOT_AVAILABLE);
    }


    public static void setFragmentBackstack(String fragmentBackstack) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putString(SETTING_FRAGMENT_BACKSTACK, fragmentBackstack);
        editor.apply();
    }

    public static int getFragmentBackstackMaxSize() {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        return savedState.getInt(SETTING_FRAGMENT_BACKSTACK_MAX_SIZE, STARTING_BACKSTACK_SIZE);
    }

    public static void setFragmentBackstackMaxSizeOrder(int maxBackstackSize) {
        SharedPreferences savedState =
                mContext.getSharedPreferences(A1_GROCERY_LIST_SAVED_STATES, 0);
        SharedPreferences.Editor editor = savedState.edit();
        editor.putInt(SETTING_FRAGMENT_BACKSTACK_MAX_SIZE, maxBackstackSize);
        editor.apply();
    }
    //endregion
}
