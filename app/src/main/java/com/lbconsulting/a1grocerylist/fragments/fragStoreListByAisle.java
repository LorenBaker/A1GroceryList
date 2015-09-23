package com.lbconsulting.a1grocerylist.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.StoreListArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.database.ItemLocation;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.lbconsulting.a1grocerylist.loaders.ItemsByAisleLoader;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows selected grocery items
 */
public class fragStoreListByAisle extends Fragment implements LoaderManager.LoaderCallbacks<List<ItemLocation>> {

    private static final String ARG_STORE_ID = "argStoreID";
    private TextView tvStoreTitle;
    private ListView lvStoreItems;
    private StoreListArrayAdapter mAdapter;
    private Store mStore;
    private String mStoreName;
    private List<StoreMapEntry> mStoreMap;

    // ItemsByAisleLoader
    private LoaderManager.LoaderCallbacks<List<ItemLocation>> mLoaderCallbacks;
    private LoaderManager mLoaderManager = null;
    private final int ITEMS_LOADER_ID = 1;
    private ItemsByAisleLoader mItemsByAisleLoader;

    public fragStoreListByAisle() {
        // Required empty public constructor
    }

    public static fragStoreListByAisle newInstance(String StoreID) {
        MyLog.i("fragStoreListByAisle", "newInstance: StoreID = " + StoreID);
        fragStoreListByAisle fragment = new fragStoreListByAisle();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_ID, StoreID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String storeID;
        if (savedInstanceState != null) {
            storeID = savedInstanceState.getString(ARG_STORE_ID);
        } else {
            storeID = getArguments().getString(ARG_STORE_ID);
        }
        MyLog.i("fragStoreListByAisle", "onCreate: storeID = " + storeID);

        EventBus.getDefault().register(this);
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI();
    }

    private void updateUI() {
        if (mItemsByAisleLoader != null) {
            mItemsByAisleLoader.updateUI();
        }
    }

    public void onEvent(MyEvents.updateStoreListUI event) {
        // if the store names are the same, you're in the fragment
        // that initiated the need to update the row ... so do nothing
        if (lvStoreItems != null && mAdapter != null && !event.getStoreName().equals(mStoreName)) {
            int itemPosition = mAdapter.getItemPosition(event.getItemID());
            Item item = Item.getItem(event.getItemID());
            int firstVisiblePosition = lvStoreItems.getFirstVisiblePosition();
            int lastVisiblePosition = lvStoreItems.getLastVisiblePosition();

            if (itemPosition < firstVisiblePosition || itemPosition > lastVisiblePosition) {
                // the item is not visible ... so do nothing
                return;
            }

            // the item is visible in the list view
            View view = lvStoreItems.getChildAt(itemPosition - firstVisiblePosition);
            if (view != null) {
                TextView tvSelectedItem = (TextView) view.findViewById(R.id.tvSelectedItem);
                if (tvSelectedItem != null && item != null) {
                    if (item.isStruckOut()) {
                        mAdapter.setStrikeOut((tvSelectedItem));
                    } else {
                        mAdapter.setNoStrikeOut((tvSelectedItem));
                    }
                }
            }
        }
    }

    public void onEvent(MyEvents.updateStoreName event) {
        if (mStore.getStoreID().equals(event.getStoreID())) {
            mStoreName = event.getStoreName();
            tvStoreTitle.setText(mStoreName);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_store_list, container, false);

        tvStoreTitle = (TextView) rootView.findViewById(R.id.tvStoreTitle);
        lvStoreItems = (ListView) rootView.findViewById(R.id.lvStoreItems);

        String storeID;
        if (savedInstanceState != null) {
            storeID = savedInstanceState.getString(ARG_STORE_ID);
        } else {
            storeID = getArguments().getString(ARG_STORE_ID);
        }
        mStore = Store.getStore(storeID);
        if(mStore!=null) {
            mStoreName = mStore.getStoreChainAndRegionalName();
            mStoreMap = StoreMapEntry.getStoreMap(mStore);
        }else{
            MyLog.e("fragStoreListByAisle", "onCreateView; Unable to find Store with ID = " + storeID);
            mStoreName="Unknown Store Name";
        }
        MyLog.i("fragStoreListByAisle", "onCreateView: " + mStoreName);
        tvStoreTitle.setText(mStoreName);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MyLog.i("fragStoreListByAisle", "onActivityCreated: " + mStoreName);

        // set the adapter
        mAdapter = new StoreListArrayAdapter(getActivity(), mStoreName);
        lvStoreItems.setAdapter(mAdapter);

        // setup the Items Loader
        mLoaderCallbacks = this;
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(ITEMS_LOADER_ID, null, mLoaderCallbacks);

        // TODO: Do we need to invalidateOptionsMenu ?
        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        MyLog.i("fragStoreListByAisle", "onSaveInstanceState: " + mStoreName);
        /*
        Called to ask the fragment to save its current dynamic state,
        so it can later be reconstructed in a new instance if its process is restarted.
        If a new instance of the fragment later needs to be created, the data you place
        in the Bundle here will be available in the Bundle given to onCreate(Bundle),
        onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).

        Note however: this method may be called at any time before onDestroy().

        There are many situations where a fragment may be mostly torn down (such as when placed on
        the back stack with no UI showing), but its state will not be saved until its owning
        activity actually needs to save its state.
        */

        outState.putString(ARG_STORE_ID, mStore.getStoreID());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragStoreListByAisle", "onResume: " + mStoreName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragStoreListByAisle", "onPause: " + mStoreName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragStoreListByAisle", "onDestroy: " + mStoreName);
        EventBus.getDefault().unregister(this);
        if (mItemsByAisleLoader != null) {
            mItemsByAisleLoader = null;
        }
        if(mAdapter!=null){
            mAdapter=null;
        }
    }

    @Override
    public Loader<List<ItemLocation>> onCreateLoader(int id, Bundle args) {
        MyLog.i("fragStoreListByAisle", "onCreateLoader: " + mStoreName);
        mItemsByAisleLoader = new ItemsByAisleLoader(getActivity(), mStore, mStoreMap);
        return mItemsByAisleLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<ItemLocation>> loader, List<ItemLocation> data) {
        MyLog.i("fragStoreListByAisle", "onLoadFinished: " + mStoreName);
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<ItemLocation>> loader) {
        MyLog.i("fragStoreListByAisle", "onLoaderReset: " + mStoreName);
        mAdapter.setData(null);
    }
}
