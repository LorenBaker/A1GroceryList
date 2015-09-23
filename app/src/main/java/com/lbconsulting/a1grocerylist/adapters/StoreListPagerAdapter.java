package com.lbconsulting.a1grocerylist.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.fragments.fragStoreListByAisle;

import java.util.List;


//FragmentStatePagerAdapter
//FragmentPagerAdapter
public class StoreListPagerAdapter extends FragmentPagerAdapter {
    private List<Store> mStoreList;

    public StoreListPagerAdapter(FragmentManager fm) {
        super(fm);
        loadData();
    }

    public void loadData() {
        this.mStoreList = Store.getAllStores();
        MyLog.i("StoreListPagerAdapter", "Loaded " + mStoreList.size() + " Stores.");
    }

    @Override
    public int getItemPosition(Object object) {
        // The following line makes the pager adapter refresh its page
        // fragments upon implementing notifyDataSetChanged.
        return POSITION_NONE;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        MyLog.i("StoreListPagerAdapter", "notifyDataSetChanged");
    }

    @Override
    public Fragment getItem(int position) {
        Fragment newStoreListFragment = null;
        if (mStoreList != null && mStoreList.size() > 0) {
            Store store = mStoreList.get(position);
            String storeID = store.getStoreID();
            newStoreListFragment = fragStoreListByAisle.newInstance(storeID);
        }
        return newStoreListFragment;
    }

    public Store getStore(int position) {
        Store store = null;
        if (mStoreList != null && mStoreList.size() > 0) {
            store = mStoreList.get(position);
        }
        return store;
    }

    public String getStoreID(int position) {
        String storeID = null;
        if (mStoreList != null && mStoreList.size() > 0) {
            Store store = mStoreList.get(position);
            storeID = store.getStoreID();
        }
        return storeID;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mStoreList != null && mStoreList.size() > 0) {
            count = mStoreList.size();
        }
        return count;
    }


    public int findStoreIDPosition(String soughtStoreID) {
        // this method searches the stores array for the sought storeID
        // if the store is not found, it returns 0.
        int position = 0;
        boolean storeFound = false;
        if (mStoreList != null && mStoreList.size() > 0) {
            for (Store store : mStoreList) {
                if (store.getStoreID().equals(soughtStoreID)) {
                    storeFound = true;
                    break;
                } else {
                    position++;
                }
            }
            if (!storeFound) {
                position = 0;
            }
        }
        return position;
    }
}
