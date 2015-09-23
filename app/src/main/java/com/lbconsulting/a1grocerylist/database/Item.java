package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds item data
 */

import android.content.Context;

import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ParseClassName("Item")
public class Item extends ParseObject {
    public static final String AUTHOR = "author";
    public static final String ITEM_NAME = "itemName";
    public static final String ITEM_NAME_LOWERCASE = "itemNameLowercase";
    public static final String ITEM_NOTE = "itemNote";
    public static final String GROUP = "group";
    public static final String IS_CHECKED = "isChecked";
    public static final String IS_FAVORITE = "isFavorite";
    public static final String IS_SELECTED = "isSelected";
    public static final String IS_STRUCK_OUT = "isStruckOut";
    public static final String SORT_KEY = "sortKey";
    public static final String IS_ITEM_DIRTY = "isItemDirty";
    public static final String BARCODE_NUMBER = "barcodeNumber";
    public static final String BARCODE_FORMAT = "barcodeFormat";
    public static final String DATE_UPDATED = "updatedAt";

    private static int mLargestSortKey;

    public static int getNextSortKey() {
        mLargestSortKey++;
        return mLargestSortKey;
    }

    public static void setLargestSortKey(int sortKey) {
        mLargestSortKey = sortKey;
    }

    private static Item mFoundExistingItem = null;

    public static Item getFoundExistingItem() {
        return mFoundExistingItem;
    }

    public Item() {
        // A default constructor is required.
    }


    public String getItemID() {
        String id = getObjectId();

        if (id == null || id.isEmpty()) {
            id = getUuidString();
        }

        return id;
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR);
    }

    public void setAuthor(ParseUser currentUser) {
        put(AUTHOR, currentUser);
    }

    public String getItemNameLowercase() {
        return getString(ITEM_NAME_LOWERCASE);
    }

    public void setItemNameLowercase(String itemNameLowercase) {
        put(ITEM_NAME_LOWERCASE, itemNameLowercase);
    }

    public String getItemName() {
        return getString(ITEM_NAME);
    }

    public void setItemName(String itemName) {
        put(ITEM_NAME, itemName);
        put(ITEM_NAME_LOWERCASE, itemName.toLowerCase());
    }

    public String getItemNote() {
        return getString(ITEM_NOTE);
    }

    public void setItemNote(String itemNote) {
        put(ITEM_NOTE, itemNote);
    }

    public Group getGroup() {
        return (Group) get(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }







    public boolean isChecked() {
        return getBoolean(IS_CHECKED);
    }

    public void setChecked(boolean isChecked) {
        put(IS_CHECKED, isChecked);
    }

    public boolean isFavorite() {
        return getBoolean(IS_FAVORITE);
    }

    public void setFavorite(boolean isFavorite) {
        put(IS_FAVORITE, isFavorite);
    }

    public int getSortKey() {
        return getInt(SORT_KEY);
    }

    public void setSortKey(int manualSortOrder) {
        put(SORT_KEY, manualSortOrder);
    }

    public String getBarcodeNumber() {
        return getString(BARCODE_NUMBER);
    }

    public void setBarcodeNumber(String barcodeNumber) {
        put(BARCODE_NUMBER, barcodeNumber);
    }

    public String getBarcodeFormat() {
        return getString(BARCODE_FORMAT);
    }

    public void setBarcodeFormat(String barcodeFormat) {
        put(BARCODE_FORMAT, barcodeFormat);
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public boolean isItemDirty() {
        return getBoolean(IS_ITEM_DIRTY);
    }

    public void setItemDirty(boolean itemDirty) {
        put(IS_ITEM_DIRTY, itemDirty);
    }


    public static ParseQuery<Item> getQuery() {
        return ParseQuery.getQuery(Item.class);
    }

    @Override
    public String toString() {
        return getItemName();
    }

    public static Item getItem(String itemID) {
        Item item = null;
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo("objectId", itemID);
            query.include(GROUP);
            query.fromLocalDatastore();
            item = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("Item", "getItem: ParseException: " + e.getMessage());
        }
        return item;
    }

    public static boolean itemExists(String proposedItemName) {
        boolean itemFound = false;
        List<Item> allItems = getAllMasterListItems();

        if (allItems.size() > 0) {
            for (Item item : allItems) {
                if (item.getItemName().toLowerCase().equals(proposedItemName.toLowerCase())) {
                    itemFound = true;
                    mFoundExistingItem = item;
                    break;
                }
            }
        }
        return itemFound;
    }

    public static List<Item> getAllMasterListItems() {
        List<Item> allItems = new ArrayList<>();
        try {
            ParseQuery<Item> query = getQuery();
            query.fromLocalDatastore();
            if (MySettings.showFavorites()) {
                query.whereEqualTo(Item.IS_FAVORITE, true);
            }

            switch (MySettings.getMasterListSortOrder()) {
                // TODO: implement manual sort order
                case MySettings.SORT_REVERSE_ALPHABETICAL:
                    query.orderByDescending(Item.ITEM_NAME_LOWERCASE);
                    break;

                default:
                    //MySettings.SORT_ALPHABETICAL
                    query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
                    break;
            }

            allItems = query.find();

        } catch (ParseException e) {
            MyLog.e("Item", "getAllMasterListItems: ParseException: " + e.getMessage());
        }
        return allItems;
    }


    //region Select/Deselect Methods

    public boolean isSelected() {
        return getBoolean(IS_SELECTED);
    }

    public void setSelected(boolean isSelected) {
        put(IS_SELECTED, isSelected);
    }

    public static void deselectStruckOutItems(Context context) {
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(IS_STRUCK_OUT, true);
            query.fromLocalDatastore();
            final List<Item> items = query.find();

            if (items != null && items.size() > 0) {
                if (A1Utils.isNetworkAvailable(context)) {
                    List<String> itemIDs = new ArrayList<String>();
                    for (Item item : items) {
                        item.setSelected(false);
                        item.setStruckOut(false);
                        itemIDs.add(item.getItemID());
                    }
                    MyLog.i("Item", "deselectStruckOutItems: Starting selection of " + itemIDs.size() + " Items.");
                    final HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("itemIDs", itemIDs);
                    ParseCloud.callFunctionInBackground("deselectStruckOutItems", params, new FunctionCallback<Integer>() {

                        @Override
                        public void done(final Integer numberOfSelectedItems, ParseException e) {
                            if (e == null) {
                                // Success. Items deselect StruckOut Items in Parse cloud
                                MyLog.i("Item", "deselectStruckOutItems: Deselected " + numberOfSelectedItems + " StruckOut Items.");

                            } else {
                                MyLog.e("Item", "deselectStruckOutItems: Error: " + e.getMessage());
                                // set items dirty
                                for (Item item : items) {
                                    item.setItemDirty(true);
                                }
                            }
                        }
                    });

                } else {
                    for (Item item : items) {
                        item.setSelected(false);
                        item.setStruckOut(false);
                        item.setItemDirty(true);
                    }
                }
            }

        } catch (ParseException e) {
            MyLog.e("Item", "deselectStruckOutItems: ParseException: " + e.getMessage());
        }
    }

    public static void selectAllFavorites(Context context) {
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(IS_FAVORITE, true);
            query.whereEqualTo(IS_SELECTED, false);
            query.fromLocalDatastore();
            final List<Item> items = query.find();

            syncIsSelectedTrue(context, items);

        } catch (ParseException e) {
            MyLog.e("Item", "selectAllFavorites: ParseException: " + e.getMessage());
        }
    }

    public static void selectAllItems(Context context) {
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(IS_SELECTED, false);
            query.fromLocalDatastore();
            final List<Item> items = query.find();

            syncIsSelectedTrue(context, items);

        } catch (ParseException e) {
            MyLog.e("Item", "selectAllItems: ParseException: " + e.getMessage());
        }
    }

    public static void setSelected(Context context, Item item, boolean isSelected) {
        List<Item> items = new ArrayList<>();
        items.add(item);

        if (isSelected) {
            syncIsSelectedTrue(context, items);
        } else {
            syncIsSelectedFalse(context, items);
        }
    }
    
    private static void syncIsSelectedTrue(Context context, final List<Item> items) {
        if (items != null && items.size() > 0) {
            if (A1Utils.isNetworkAvailable(context)) {
                List<String> itemIDs = new ArrayList<String>();
                for (Item item : items) {
                    item.setSelected(true);
                    itemIDs.add(item.getItemID());
                }
                MyLog.i("Item", "syncIsSelectedTrue: Starting selection of " + itemIDs.size() + " Items.");
                final HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("itemIDs", itemIDs);
                ParseCloud.callFunctionInBackground("syncIsSelectedTrue", params, new FunctionCallback<Integer>() {

                    @Override
                    public void done(final Integer numberOfSelectedItems, ParseException e) {
                        if (e == null) {
                            // Success. Items selected in Parse cloud
                            MyLog.i("Item", "syncIsSelectedTrue: Selected " + numberOfSelectedItems + " Items.");

                        } else {
                            MyLog.e("Item", "syncIsSelectedTrue: Error: " + e.getMessage());
                            // set items dirty
                            for (Item item : items) {
                                item.setItemDirty(true);
                            }
                        }
                    }
                });

            } else {
                for (Item item : items) {
                    item.setSelected(true);
                    item.setItemDirty(true);
                }
            }
        }
    }

    public static void deselectAllItems(Context context) {
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(IS_SELECTED, true);
            query.fromLocalDatastore();
            final List<Item> items = query.find();

            syncIsSelectedFalse(context, items);


        } catch (ParseException e) {
            MyLog.e("Item", "deselectAllItems: ParseException: " + e.getMessage());
        }
    }

    private static void syncIsSelectedFalse(Context context, final List<Item> items) {

        if (items != null && items.size() > 0) {
            if (A1Utils.isNetworkAvailable(context)) {
                List<String> itemIDs = new ArrayList<String>();
                for (Item item : items) {
                    item.setSelected(false);
                    itemIDs.add(item.getItemID());
                }
                MyLog.i("Item", "syncIsSelectedFalse: Starting deselection of " + itemIDs.size() + " Items.");
                final HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("itemIDs", itemIDs);
                ParseCloud.callFunctionInBackground("syncIsSelectedFalse", params, new FunctionCallback<Integer>() {

                    @Override
                    public void done(final Integer numberOfSelectedItems, ParseException e) {
                        if (e == null) {
                            // Success. Items deselected in Parse cloud
                            MyLog.i("Item", "syncIsSelectedFalse: Deselected " + numberOfSelectedItems + " Items.");

                        } else {
                            MyLog.e("Item", "syncIsSelectedFalse: Error: " + e.getMessage());
                            // set items dirty
                            for (Item item : items) {
                                item.setItemDirty(true);
                            }
                        }
                    }
                });

            } else {
                for (Item item : items) {
                    item.setSelected(false);
                    item.setItemDirty(true);
                }
            }
        }
    }

    public static List<Item> getSelectedItems() {
        List<Item> selectedItems = null;
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(Item.IS_SELECTED, true);
            query.include(GROUP);
            query.fromLocalDatastore();
            selectedItems = query.find();
        } catch (ParseException e) {
            MyLog.e("Item", "getSelectedItems: ParseException" + e.getMessage());
        }

        return selectedItems;
    }



    //endregion


    //region Struck Out Methods

    public boolean isStruckOut() {
        return getBoolean(IS_STRUCK_OUT);
    }

    public void setStruckOut(boolean isStruckOut) {
        put(IS_STRUCK_OUT, isStruckOut);
    }
    private static void syncIsStruckOutTrue(Context context, final List<Item> items) {
        if (items != null && items.size() > 0) {
            if (A1Utils.isNetworkAvailable(context)) {
                List<String> itemIDs = new ArrayList<String>();
                for (Item item : items) {
                    item.setStruckOut(true);
                    itemIDs.add(item.getItemID());
                }
                MyLog.i("Item", "syncIsStruckOutTrue: Starting deselection of " + itemIDs.size() + " Items.");
                final HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("itemIDs", itemIDs);
                ParseCloud.callFunctionInBackground("syncIsStruckOutTrue", params, new FunctionCallback<Integer>() {

                    @Override
                    public void done(final Integer numberItems, ParseException e) {
                        if (e == null) {
                            // Success. 
                            MyLog.i("Item", "syncIsStruckOutTrue: Set strikeout on " + numberItems + " Items.");

                        } else {
                            MyLog.e("Item", "syncIsStruckOutTrue: Error: " + e.getMessage());
                            // set items dirty
                            for (Item item : items) {
                                item.setItemDirty(true);
                            }
                        }
                    }
                });

            } else {
                for (Item item : items) {
                    item.setStruckOut(true);
                    item.setItemDirty(true);
                }
            }
        }
    }

    private static void syncIsStruckOutFalse(Context context, final List<Item> items) {
        if (items != null && items.size() > 0) {
            if (A1Utils.isNetworkAvailable(context)) {
                List<String> itemIDs = new ArrayList<String>();
                for (Item item : items) {
                    item.setStruckOut(false);
                    itemIDs.add(item.getItemID());
                }
                MyLog.i("Item", "syncIsStruckOutFalse: Starting deselection of " + itemIDs.size() + " Items.");
                final HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("itemIDs", itemIDs);
                ParseCloud.callFunctionInBackground("syncIsStruckOutFalse", params, new FunctionCallback<Integer>() {

                    @Override
                    public void done(final Integer numberItems, ParseException e) {
                        if (e == null) {
                            // Success. 
                            MyLog.i("Item", "syncIsStruckOutFalse: Removed strikeout from " + numberItems + " Items.");

                        } else {
                            MyLog.e("Item", "syncIsStruckOutFalse: Error: " + e.getMessage());
                            // set items dirty
                            for (Item item : items) {
                                item.setItemDirty(true);
                            }
                        }
                    }
                });

            } else {
                for (Item item : items) {
                    item.setStruckOut(false);
                    item.setItemDirty(true);
                }
            }
        }
    }

    public static void setStruckOut(Context context, Item item, boolean isStruckOut) {
        List<Item> items = new ArrayList<>();
        items.add(item);

        if (isStruckOut) {
            syncIsStruckOutTrue(context, items);
        } else {
            syncIsStruckOutFalse(context, items);
        }
    }
    //endregion
    

    public String getGroupID() {
        return getGroup().getGroupID();
    }

    public String getItemNameWithNote() {
        String itemNameWithNote = getItemName();
        if (!getItemNote().isEmpty()) {
            itemNameWithNote = itemNameWithNote + " (" + getItemNote() + "0";
        }
        return itemNameWithNote;
    }

    public static List<Item> getCheckedItems() {
        List<Item> checkedItems = null;
        try {
            ParseQuery<Item> query = getQuery();
            query.whereEqualTo(Item.IS_CHECKED, true);
            query.fromLocalDatastore();
//            query.fromPin(MySettings.GROUP_NAME_ITEMS);
            checkedItems = query.find();
        } catch (ParseException e) {
            MyLog.e("Item", "getCheckedItems: ParseException" + e.getMessage());
        }

        return checkedItems;
    }

    public static List<Item> getOldItems(Date earlierDate) {
        List<Item> oldItems = null;
        try {
            ParseQuery<Item> query = getQuery();
            query.whereLessThan("updatedAt", earlierDate);
            query.fromLocalDatastore();
            oldItems = query.find();
        } catch (ParseException e) {
            MyLog.e("Item", "getOldItems: ParseException" + e.getMessage());
        }

        return oldItems;
    }

    public static void unPinAll() {
        try {
            List<Item> allItems =getAllMasterListItems();
            ParseObject.unpinAll(allItems);
        } catch (ParseException e) {
            MyLog.e("Item", "unPinAll: ParseException" + e.getMessage());
        }
    }

    public static List<Item> getUncheckedItems() {
        List<Item> allUncheckedItems = new ArrayList<>();
        try {
            ParseQuery<Item> query = getQuery();
            query.fromLocalDatastore();
            query.whereEqualTo(IS_CHECKED,false);
            allUncheckedItems = query.find();

        } catch (ParseException e) {
            MyLog.e("Item", "getUncheckedItems: ParseException: " + e.getMessage());
        }

        return allUncheckedItems;
    }
}


