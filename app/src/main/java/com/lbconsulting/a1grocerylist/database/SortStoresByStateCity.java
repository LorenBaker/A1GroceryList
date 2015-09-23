package com.lbconsulting.a1grocerylist.database;

import java.util.Comparator;

public class SortStoresByStateCity implements Comparator<Store> {

    @Override
    public int compare(Store store1, Store store2) {
        // sort by state
        int result = store1.getState().compareToIgnoreCase(store2.getState());

        // then sort by city
        if (result == 0) {
            result = store1.getCity().compareToIgnoreCase(store2.getCity());
        }

        // then sort by store chain
        if (result == 0) {
            result = store1.getStoreChain().getStoreChainName().compareToIgnoreCase(store2.getStoreChain().getStoreChainName());
        }

        // then sort by store regional name
        if (result == 0) {
            result = store1.getStoreRegionalName().compareToIgnoreCase(store2.getStoreRegionalName());
        }

        return result;
    }
}
