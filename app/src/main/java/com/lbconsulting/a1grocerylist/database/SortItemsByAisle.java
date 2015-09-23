package com.lbconsulting.a1grocerylist.database;

import java.util.Comparator;

public class SortItemsByAisle implements Comparator<ItemLocation> {

    @Override
    public int compare(ItemLocation item1, ItemLocation item2) {
        int result = item1.getLocationSortKey() - item2.getLocationSortKey();
        if (result == 0) {
            result = item1.getItemName().compareToIgnoreCase(item2.getItemName());
        }
        return result;
    }
}
