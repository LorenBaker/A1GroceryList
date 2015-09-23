package com.lbconsulting.a1grocerylist.database;

import java.util.Comparator;

public class SortStoreMap implements Comparator<StoreMapEntry> {

    @Override
    public int compare(StoreMapEntry entry1, StoreMapEntry entry2) {
        return entry1.getGroup().getGroupName().compareToIgnoreCase( entry2.getGroup().getGroupName());
    }
}
