package com.lbconsulting.a1grocerylist.temporary;

import android.content.Context;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Initial_Items;
import com.lbconsulting.a1grocerylist.database.Location;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreChain;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * These are helper methods that will not be included int the
 * production app
 */
public class uploadMethods {


    public static void loadInitialDataToParse(Context context) {

//        uploadInitialGroups(context);
//        uploadInitialLocations(context);
//        uploadInitialStoreChains(context);
        uploadInitialStores(context);
//        uploadInitialItems(context);
    }

    private static void uploadInitialItems(Context context) {
        // get all Groups
        List<Group> groupsList = null;
        try {
            ParseQuery<Group> groupsParseQuery = Group.getQuery();
            groupsParseQuery.addAscendingOrder(Group.SORT_KEY);
            groupsList = groupsParseQuery.find();
            MyLog.i("uploadMethods", "uploading initial items: Found " + groupsList.size() + " Groups.");
        } catch (ParseException e) {
            MyLog.e("clsParseUtils", "uploading initial items: ParseException: " + e.getMessage());
            return;
        }

        String[] groceryItems = context.getResources().getStringArray(R.array.grocery_items);
        ParseACL itemACL = new ParseACL(ParseUser.getCurrentUser());
        itemACL.setPublicReadAccess(true);
        Initial_Items initialItem;
        String itemName = "";
        for (String groceryItem : groceryItems) {
            try {
                initialItem = new Initial_Items();

                String[] item = groceryItem.split(", ");
                int groupIdInt = 0;
                itemName = item[0];
                String groupIdStringNumber = item[1];
                groupIdInt = Integer.parseInt(groupIdStringNumber) - 1;


                initialItem.setItemName(itemName);
                initialItem.setGroup(groupsList.get(groupIdInt));
                initialItem.setACL(itemACL);
                initialItem.save();
                MyLog.i("uploadMethods", "Saved Initial_Items: " + itemName);
            } catch (ParseException e) {
                MyLog.e("uploadMethods", "ParseException: Upload new Initial_Items: " + itemName);
            }
        }
    }

    private static void uploadInitialStores(Context context) {
        // NOTE: requires store groups, locations, and store chains be already created.

        Store.setSortingKey(0);

        // Albertsons
        ParseQuery storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "Albertsons");
        StoreChain storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "Albertsons" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Eastgate", "15100 SE 38TH ST", "STE 103", "BELLEVUE", "WA", "98006-1763", "US");


        // Fred Myer
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "Fred Meyer");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "Fred Meyer" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Bellevue", "2041 148TH AVE NE", "", "BELLEVUE", "WA", "98007-3788", "US");
        Store.createAndSaveStore(context, storeChainObject, "Redmond", "17667 NE 76TH ST", "", "REDMOND", "WA", "98052-4994", "US");


        // PCC
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "PCC");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "PCC" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Issaquah", "1810 12TH AVE NW", "STE A", "ISSAQUAH", "WA", "98027-8110", "US");
        Store.createAndSaveStore(context, storeChainObject, "Kirkland", "10718 NE 68TH ST", "", "KIRKLAND", "WA", "98033-7030", "US");

        // QFC
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "QFC");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "QFC" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Bel-East", "1510 145TH PL SE", "STE A", "BELLEVUE", "WA", "98007-5593", "US");
        Store.createAndSaveStore(context, storeChainObject, "Issaquah", "1540 NW GILMAN BLVD", "", "ISSAQUAH", "WA", "98027-5309", "US");
        Store.createAndSaveStore(context, storeChainObject, "Klahanie Dr", "4570 KLAHANIE DR SE", "", "ISSAQUAH", "WA", "98029-5812", "US");
        Store.createAndSaveStore(context, storeChainObject, "Factoria", "3500 FACTORIA BLVD SE", "", "BELLEVUE", "WA", "98006-5276", "US");
        Store.createAndSaveStore(context, storeChainObject, "Newcastle", "6940 COAL CREEK PKWY SE", "", "NEWCASTLE", "WA", "98059-3137", "US");

        // Safeway
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "Safeway");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "Safeway" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Factoria", "3903 FACTORIA BLVD SE", "", "BELLEVUE", "WA", "98006-6148", "US");
        Store.createAndSaveStore(context, storeChainObject, "Evergreen Village", "1645 140TH AVE NE", "STE A5", "BELLEVUE", "WA", "98005-2320", "US");
        Store.createAndSaveStore(context, storeChainObject, "Issaquah", "735 NW GILMAN BLVD", "STE B", "ISSAQUAH", "WA", "98027-8996", "US");
        Store.createAndSaveStore(context, storeChainObject, "Highlands", "1451 HIGHLANDS DR NE", "", "ISSAQUAH", "WA", "98029-6240", "US");

        //Trader Joe’s
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "Trader Joe’s");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "Trader Joe’s" + "\"");
        }

        Store.createAndSaveStore(context, storeChainObject, "Issaquah", "975 NW GILMAN BLVD", "STE A", "ISSAQUAH", "WA", "98027-5377", "US");
        Store.createAndSaveStore(context, storeChainObject, "Redmond", "15932 REDMOND WAY", "STE 101", "REDMOND", "WA", " 98052-4060", "US");
        Store.createAndSaveStore(context, storeChainObject, "Bellevue", "15563 NE 24TH ST", "", "BELLEVUE", "WA", "98007-3836", "US");

        // Whole Foods
        storeChainsQuery = StoreChain.getQuery();
        storeChainsQuery.whereEqualTo(StoreChain.STORE_CHAIN_NAME, "Whole Foods");
        storeChainObject = null;
        try {
            storeChainObject = (StoreChain) storeChainsQuery.getFirst();
        } catch (ParseException e) {
            MyLog.e("uploadMethods", "Failed to find Store Chain \"" + "Whole Foods" + "\"");
        }
        Store.createAndSaveStore(context, storeChainObject, "Bellevue", "888 116TH AVE NE", "", "BELLEVUE", "WA", "98004-4607", "US");
        Store.createAndSaveStore(context, storeChainObject, "Redmond", "17991 REDMOND WAY", "", "REDMOND", "WA", " 98052-4907", "US");
    }

    private static void uploadInitialStoreChains(Context context) {
        String[] storeChains = context.getResources().getStringArray(R.array.grocery_store_chains);
        int sortKey = 0;
        ParseACL storeChainACL = new ParseACL(ParseUser.getCurrentUser());
        storeChainACL.setPublicReadAccess(true);
        storeChainACL.setPublicWriteAccess(true);
        StoreChain storeChain;
        for (String storeChainName : storeChains) {
            try {
                storeChain = new StoreChain();
                storeChain.setStoreChainName(storeChainName);
                storeChain.setACL(storeChainACL);
                storeChain.setAuthor(ParseUser.getCurrentUser());
                storeChain.setChecked(false);
                if (storeChainName.startsWith("[")) {
                    storeChain.setDefault(true);
                } else {
                    storeChain.setDefault(false);
                }
                storeChain.setStoreChainDirty(false);
                storeChain.setSortKey(sortKey);
                storeChain.save();
                MyLog.i("uploadMethods", "Saved StoreChain: " + storeChainName);

            } catch (ParseException e) {
                MyLog.e("uploadMethods", "ParseException: Upload new StoreChain: " + storeChainName);
            }
            sortKey++;
        }
    }

    private static void uploadInitialLocations(Context context) {
        String[] initialLocations = context.getResources().getStringArray(R.array.initial_location_list);
        ArrayList<String> groupLocations = new ArrayList<>();
        for (String initialLocation : initialLocations) {
            groupLocations.add(initialLocation);
        }
        groupLocations = createInitialAisles(groupLocations);
        int sortKey = 0;
        ParseACL locationACL = new ParseACL(ParseUser.getCurrentUser());
        locationACL.setPublicReadAccess(true);
        locationACL.setPublicWriteAccess(true);
        Location location;
        for (String groupLocation : groupLocations) {
            try {
                location = new Location();
                location.setLocationName(groupLocation);
                location.setACL(locationACL);
                location.setAuthor(ParseUser.getCurrentUser());
                location.setChecked(false);
                if (groupLocation.startsWith("[")) {
                    location.setDefault(true);
                } else {
                    location.setDefault(false);
                }
                location.setLocationDirty(false);
                location.setSortKey(sortKey);
                location.save();
                MyLog.i("uploadMethods", "Saved Location: " + groupLocation);
            } catch (ParseException e) {
                MyLog.e("uploadMethods", "ParseException: Upload new Location: " + groupLocation);
            }

            sortKey++;
        }
    }

    private static void uploadInitialGroups(Context context) {
        String[] groceryGroups = context.getResources().getStringArray(R.array.grocery_groups);
        int sortKey = 0;
        Group group;
        ParseACL groupACL = new ParseACL(ParseUser.getCurrentUser());
        groupACL.setPublicReadAccess(true);

        for (String groceryGroup : groceryGroups) {
            try {
                group = new Group();
                group.setGroupName(groceryGroup);
                group.setACL(groupACL);
                group.setAuthor(ParseUser.getCurrentUser());
                group.setChecked(false);
                if (groceryGroup.startsWith("[")) {
                    group.setDefault(true);
                } else {
                    group.setDefault(false);
                }
                group.setGroupDirty(false);
                group.setSortKey(sortKey);
                group.save();
                MyLog.i("uploadMethods", "Saved Group: " + groceryGroup);

            } catch (ParseException e) {
                MyLog.e("uploadMethods", "ParseException: Upload new group: " + groceryGroup);
            }

            sortKey++;
        }
    }


    private static ArrayList<String> createInitialAisles(ArrayList<String> locations) {
        String aisleName;
        for (int i = 1; i < MySettings.INITIAL_NUMBER_OF_AISLES + 1; i++) {
            aisleName = "Aisle " + i;
            locations.add(aisleName);
        }
        return locations;
    }


}
