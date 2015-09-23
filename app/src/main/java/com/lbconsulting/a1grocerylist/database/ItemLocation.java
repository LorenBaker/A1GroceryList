package com.lbconsulting.a1grocerylist.database;

import android.content.Context;

import java.util.List;

/**
 * This class holds Items and Item Location data.
 */
public class ItemLocation {

    private Item mItem;
    private List<StoreMapEntry> mStoreMap;
    private Location mLocation;

    public ItemLocation(Item item, Store store, List<StoreMapEntry> storeMap) {
        this.mItem = item;
        this.mStoreMap = storeMap;
        String ItemGroupID = item.getGroup().getGroupID();
        this.mLocation = findItemLocation(ItemGroupID);
        //this.mLocation = StoreMapEntry.getGroupLocation(store, item.getGroup());
    }

    private Location findItemLocation(String soughtGroupID) {
        Location location = null;

        for (StoreMapEntry storeMapEntry : mStoreMap) {
            String storeMapEntryGroupID = storeMapEntry.getGroup().getGroupID();
            if (storeMapEntryGroupID.equals(soughtGroupID)) {
                location = storeMapEntry.getLocation();
                break;
            }
        }

        return location;
    }

    public Item getItem() {
        return mItem;
    }

    public String getItemName() {
        return mItem.getItemName();
    }

    public String getGroupName() {
        return mItem.getGroup().getGroupName();
    }

    public String getLocationName() {
        return mLocation.getLocationName();
    }

    public String getItemNameWithNote() {
        String itemName = mItem.getItemName();
        if (!mItem.getItemNote().isEmpty()) {
            itemName = itemName + " (" + mItem.getItemNote() + ")";
        }
        return itemName;
    }

    public int getLocationSortKey() {
        return mLocation.getSortKey();
    }

    public String getItemID() {
        return mItem.getItemID();
    }

    public static void toggleStrikeout(Context context, ItemLocation item) {
//        mItem.setStruckOut(!mItem.isStruckOut());
        Item.setStruckOut(context, item.getItem(), !item.isStruckOut());
    }

    public String getGroupID() {
        return mItem.getGroup().getGroupID();
    }

    public String getLocationID() {
        return mLocation.getLocationID();
    }

    public boolean isStruckOut() {
        return mItem.isStruckOut();
    }

    public void setStruckOut(boolean struckOut) {
        mItem.setStruckOut(struckOut);
    }

    public void setItemDirty(boolean isDirty) {
        mItem.setItemDirty(isDirty);
    }

   }

