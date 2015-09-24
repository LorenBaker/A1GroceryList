package com.lbconsulting.a1grocerylist.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.MapStoreArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.lbconsulting.a1grocerylist.dialogs.dialogGroupLocation;
import com.lbconsulting.a1grocerylist.loaders.MapStoreLoader;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that allows the user to map a store.
 */
public class fragMapStore extends Fragment implements LoaderManager.LoaderCallbacks<List<StoreMapEntry>> {

    private static final String ARG_STORE_ID = "storeID";
    private MapStoreArrayAdapter mAdapter;
    private String mStoreID;
    private Store mStore;

    private MapStoreLoader mMapStoreLoader;

    public fragMapStore() {
        // Required empty public constructor
    }

    public static fragMapStore newInstance(String storeID) {
        MyLog.i("fragMapStore", "newInstance");
        fragMapStore fragment = new fragMapStore();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_ID, storeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragMapStore", "onCreate");
        mStoreID = getArguments().getString(ARG_STORE_ID);
        mStore = Store.getStore(mStoreID);
        EventBus.getDefault().register(this);
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI();
    }

    private void updateUI() {
        if (mMapStoreLoader != null) {
            mMapStoreLoader.updateUI();
        }
    }

    public void onEvent(MyEvents.updateSeparatorText event) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragMapStore", "onCreateView: ");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_store_list_by_group, container, false);
        ListView lvGroups = (ListView) rootView.findViewById(R.id.lvStoreItems);
        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final StoreMapEntry clickedEntry = mAdapter.getItem(position);

                if (clickedEntry != null) {
                    FragmentManager fm = getFragmentManager();
                    dialogGroupLocation dialog = dialogGroupLocation.newInstance(
                            clickedEntry.getStore().getStoreID(), clickedEntry.getGroup().getGroupID());
                    dialog.show(fm, "dialogGroupLocation");
                } else {
                    MyLog.e("MapStoreArrayAdapter", "onClick: tvStoreSeparator store or group is null!");
                }

            }
        });

        // set the adapter
        mAdapter = new MapStoreArrayAdapter(getActivity());
        lvGroups.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragMapStore", "onActivityCreated: ");

        if (savedInstanceState != null) {
            mStoreID = savedInstanceState.getString(ARG_STORE_ID);
            mStore = Store.getStore(mStoreID);
        }

        EventBus.getDefault().post(new MyEvents.setBackgroundColor(MySettings.BACKGROUND_COLOR_OPAL));

        // setup the Items Loader
        final int STORE_MAP_ENTRY_LOADER_ID = 4;
        getLoaderManager().initLoader(STORE_MAP_ENTRY_LOADER_ID, null, this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        MyLog.i("fragMapStore", "onSaveInstanceState: ");
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

        outState.putString(ARG_STORE_ID, mStoreID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragMapStore", "onResume: ");
        MySettings.setActiveFragmentID(MySettings.FRAG_MAP_STORE);
        if (mStore != null) {
            EventBus.getDefault().post(new MyEvents.setActionBarTitle(mStore.getStoreChainAndRegionalName()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragMapStore", "onPause: ");
        MySettings.setStoreIDtoMapID(MySettings.NOT_AVAILABLE);
        EventBus.getDefault().post(new MyEvents.setBackgroundColor(MySettings.BACKGROUND_COLOR_GENOA));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragMapStore", "onDestroy: ");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Loader<List<StoreMapEntry>> onCreateLoader(int id, Bundle args) {
        MyLog.i("fragMapStore", "onCreateLoader");
        mMapStoreLoader = new MapStoreLoader(getActivity(), mStoreID);
        return mMapStoreLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<StoreMapEntry>> loader, List<StoreMapEntry> data) {
        MyLog.i("fragMapStore", "onLoadFinished");
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<StoreMapEntry>> loader) {
        MyLog.i("fragMapStore", "onLoaderReset");
        mAdapter.setData(null);
    }
}
