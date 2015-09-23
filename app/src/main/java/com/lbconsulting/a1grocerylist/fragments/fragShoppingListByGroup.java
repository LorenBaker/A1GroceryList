package com.lbconsulting.a1grocerylist.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.ShoppingListByGroupArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.loaders.ItemsByGroupLoader;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows the shopping list sorted by Group.
 */
public class fragShoppingListByGroup extends Fragment implements LoaderManager.LoaderCallbacks<List<Item>> {


    private static final String SHOPPING_LIST_TITLE = "Shopping List";
    private ListView lvStoreItems;
    private ShoppingListByGroupArrayAdapter mAdapter;

    // ItemsByGroupLoader
    private LoaderManager.LoaderCallbacks<List<Item>> mLoaderCallbacks;
    private LoaderManager mLoaderManager = null;
    private final int ITEMS_LOADER_ID = 2;
    private ItemsByGroupLoader mItemsByGroupLoader;

    public fragShoppingListByGroup() {
        // Required empty public constructor
    }

    public static fragShoppingListByGroup newInstance() {
        MyLog.i("fragShoppingListByGroup", "newInstance");
        return new fragShoppingListByGroup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragShoppingListByGroup", "onCreate");
        EventBus.getDefault().register(this);
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI();
    }

    private void updateUI() {
        if (mItemsByGroupLoader != null) {
            mItemsByGroupLoader.updateUI();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragShoppingListByGroup", "onCreateView: ");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_store_list_by_group, container, false);
        lvStoreItems = (ListView) rootView.findViewById(R.id.lvStoreItems);

        // set the adapter
        mAdapter = new ShoppingListByGroupArrayAdapter(getActivity());
        lvStoreItems.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragShoppingListByGroup", "onActivityCreated: ");

        getActivity().invalidateOptionsMenu();
        EventBus.getDefault().post(new MyEvents.setActionBarTitle(SHOPPING_LIST_TITLE));

        // setup the Items Loader
        mLoaderCallbacks = this;
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(ITEMS_LOADER_ID, null, mLoaderCallbacks);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        MyLog.i("fragShoppingListByGroup", "onSaveInstanceState: ");
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
        MyLog.i("fragShoppingListByGroup", "onResume: ");
        MySettings.setActiveFragmentID(MySettings.FRAG_SHOPPING_LIST_BY_GROUP);
        MySettings.setShoppingListSortOrder(MySettings.SORT_BY_GROUP);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragShoppingListByGroup", "onPause: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragShoppingListByGroup", "onDestroy: ");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Loader<List<Item>> onCreateLoader(int id, Bundle args) {
        MyLog.i("fragShoppingListByGroup", "onCreateLoader");
        mItemsByGroupLoader = new ItemsByGroupLoader(getActivity());
        return mItemsByGroupLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Item>> loader, List<Item> data) {
        MyLog.i("fragShoppingListByGroup", "onLoadFinished");
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Item>> loader) {
        MyLog.i("fragShoppingListByGroup", "onLoaderReset");
        mAdapter.setData(null);
    }
}
