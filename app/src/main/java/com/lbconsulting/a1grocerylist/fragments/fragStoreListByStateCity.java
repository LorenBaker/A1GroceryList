package com.lbconsulting.a1grocerylist.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.StoreListByStateCityArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.loaders.StoresBySateCityLoader;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows the shopping list sorted by Group.
 */
public class fragStoreListByStateCity extends Fragment implements LoaderManager.LoaderCallbacks<List<Store>> {

    // TODO: pin checked stores and related store maps
    private static final String STORE_LIST_TITLE = "All Stores by: State/City/Name";
    private StoreListByStateCityArrayAdapter mAdapter;

    // ItemsByGroupLoader
    private LoaderManager.LoaderCallbacks<List<Store>> mLoaderCallbacks;
    private LoaderManager mLoaderManager = null;
    private final int STORES_LOADER_ID = 3;
    private StoresBySateCityLoader mStoresByStateCityLoader;

    public fragStoreListByStateCity() {
        // Required empty public constructor
    }

    public static fragStoreListByStateCity newInstance() {
        MyLog.i("fragStoreListByStateCity", "newInstance");
        return new fragStoreListByStateCity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragStoreListByStateCity", "onCreate");
        EventBus.getDefault().register(this);
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI();
    }

    private void updateUI() {
        if (mStoresByStateCityLoader != null) {
            mStoresByStateCityLoader.updateUI();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragStoreListByStateCity", "onCreateView: ");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_store_list_by_group, container, false);
        ListView lvStores = (ListView) rootView.findViewById(R.id.lvStoreItems);
        lvStores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.toggleChecked(view, position);
            }
        });

        // set the adapter
        mAdapter = new StoreListByStateCityArrayAdapter(getActivity());
        lvStores.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragStoreListByStateCity", "onActivityCreated: ");

        getActivity().invalidateOptionsMenu();
        EventBus.getDefault().post(new MyEvents.setActionBarTitle(STORE_LIST_TITLE));
        MySettings.setActiveFragmentID(MySettings.FRAG_SHOW_ALL_STORES);

        // setup the Items Loader
        mLoaderCallbacks = this;
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(STORES_LOADER_ID, null, mLoaderCallbacks);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        MyLog.i("fragStoreListByStateCity", "onSaveInstanceState: ");
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragStoreListByStateCity", "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragStoreListByStateCity", "onPause: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragStoreListByStateCity", "onDestroy: ");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Loader<List<Store>> onCreateLoader(int id, Bundle args) {
        MyLog.i("fragStoreListByStateCity", "onCreateLoader");
        mStoresByStateCityLoader = new StoresBySateCityLoader(getActivity());
        return mStoresByStateCityLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Store>> loader, List<Store> data) {
        MyLog.i("fragStoreListByStateCity", "onLoadFinished");
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Store>> loader) {
        MyLog.i("fragStoreListByStateCity", "onLoaderReset");
        mAdapter.setData(null);
    }
}
