package com.lbconsulting.a1grocerylist.database;

import java.util.Comparator;

public class SortItemsByGroup implements Comparator<Item> {

    @Override
    public int compare(Item item1, Item item2) {
        int result = item1.getGroup().getGroupName().compareToIgnoreCase(item2.getGroup().getGroupName());
        if (result == 0) {
            result = item1.getItemName().compareToIgnoreCase(item2.getItemName());
        }
        return result;
    }
}
