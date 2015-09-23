package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds StoreChain data
 */

import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

@ParseClassName("StoreChain")
public class StoreChain extends ParseObject {
    public static final String AUTHOR = "author";
    public static final String STORE_CHAIN_NAME = "storeChainName";
    public static final String IS_CHECKED = "isChecked";
    public static final String IS_DEFAULT = "isDefault";
    public static final String IS_DIRTY = "isDirty";
    public static final String SORT_KEY = "sortKey";


    public StoreChain() {
        // A default constructor is required.
    }


    public String getStoreChainID() {
        return getObjectId();
    }

    public long getSortKey() {
        return getLong(SORT_KEY);
    }

    public void setSortKey(long sortKey) {
        put(SORT_KEY, sortKey);
    }

    public String getStoreChainName() {
        return getString(STORE_CHAIN_NAME);
    }

    public void setStoreChainName(String storeChainName) {
        put(STORE_CHAIN_NAME, storeChainName);
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

    public boolean isStoreChainDirty() {
        return getBoolean(IS_DIRTY);
    }

    public void setStoreChainDirty(boolean storeChainDirty) {
        put(IS_DIRTY, storeChainDirty);
    }

    public boolean isDefault() {
        return getBoolean(IS_DEFAULT);
    }

    public void setDefault(boolean defaultStoreChain) {
        put(IS_DEFAULT, defaultStoreChain);
    }

    public static ParseQuery<StoreChain> getQuery() {
        return ParseQuery.getQuery(StoreChain.class);
    }


    @Override
    public String toString() {
        return getStoreChainName();
    }

    public static StoreChain getStoreChain(String storeChainID) {
        StoreChain group = null;
        try {
            ParseQuery<StoreChain> query = getQuery();
            query.whereEqualTo("objectId", storeChainID);
            query.fromLocalDatastore();
//            query.fromPin(MySettings.GROUP_NAME_STORE_CHAINS);
            group = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("StoreChain", "getStoreChain: ParseException: " + e.getMessage());
        }
        return group;
    }


    public static List<StoreChain> getAllStoreChains() {
        List<StoreChain> storeChains = new ArrayList<>();
        try {
            ParseQuery<StoreChain> query = getQuery();
            query.orderByAscending(STORE_CHAIN_NAME);
            query.fromLocalDatastore();
            storeChains = query.find();
        } catch (ParseException e) {
            MyLog.e("StoreChain", "getAllStoreChains: ParseException: " + e.getMessage());
        }

        return storeChains;
    }

    public static void unPinAll() {
        try {
            List<StoreChain> allStoreChains = getAllStoreChains();
            ParseObject.unpinAll(allStoreChains);
        } catch (ParseException e) {
            MyLog.e("StoreChain", "unPinAll: ParseException" + e.getMessage());
        }
    }

    public static boolean storeChainExists(String proposedStoreChainName) {
        boolean itemFound = false;
        List<StoreChain> storeChains = getAllStoreChains();

        if (storeChains.size() > 0) {
            for (StoreChain storeChain : storeChains) {
                if (storeChain.getStoreChainName().toLowerCase().equals(proposedStoreChainName.toLowerCase())) {
                    itemFound = true;
                    break;
                }
            }
        }
        return itemFound;
    }

    public static void createNewStoreChain(String proposedStoreChainName) {
        final StoreChain newStoreChain = new StoreChain();

        newStoreChain.setAuthor(ParseUser.getCurrentUser());
        newStoreChain.setStoreChainName(proposedStoreChainName);
        newStoreChain.setChecked(false);
        newStoreChain.setDefault(false);
        newStoreChain.setStoreChainDirty(false);
        newStoreChain.setSortKey(-1);

        ParseACL storeChainACL = new ParseACL(ParseUser.getCurrentUser());
        storeChainACL.setPublicReadAccess(true);
        storeChainACL.setPublicWriteAccess(true);
        newStoreChain.setACL(storeChainACL);
        newStoreChain.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                newStoreChain.saveEventually();
                EventBus.getDefault().post(new MyEvents.updateStoreChainSpinner());
            }
        });


    }
}


