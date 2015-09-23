package com.lbconsulting.a1grocerylist.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.ShoppingListAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows selected grocery items
 */
public class fragShoppingList extends Fragment {

    private ListView lvItemsListView;

    private ParseQueryAdapter<Item> mShoppingListAdapter;
    private ParseQueryAdapter.QueryFactory<Item> mFactory;

    public static fragShoppingList newInstance() {
        MyLog.i("fragShoppingList", "newInstance");
        return new fragShoppingList();
    }

    public fragShoppingList() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MyLog.i("fragShoppingList", "onAttach");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragShoppingList", "onCreate");
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragShoppingList", "onCreateView");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_shopping_list, container, false);
        lvItemsListView = (ListView) rootView.findViewById(R.id.lvItemsListView);
        lvItemsListView.setItemsCanFocus(true);

        if (MySettings.getShoppingListSortOrder() == MySettings.SORT_BY_GROUP) {
            MySettings.setShoppingListSortOrder(MySettings.SORT_ALPHABETICAL);
        }

        // Set up the Parse query to use in the adapter
        mFactory = new ParseQueryAdapter.QueryFactory<Item>() {
            public ParseQuery<Item> create() {
                ParseQuery<Item> query = Item.getQuery();
                query.whereEqualTo(Item.IS_SELECTED, true);
                switch (MySettings.getShoppingListSortOrder()) {
                    // TODO: implement manual sort order
                    case MySettings.SORT_REVERSE_ALPHABETICAL:
                        query.orderByDescending(Item.ITEM_NAME_LOWERCASE);
                        break;

                    default:
                        //MySettings.SORT_ALPHABETICAL
                        query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
                        break;
                }
                query.fromLocalDatastore();
                return query;
            }
        };

        // Set up the ListView adapter
        mShoppingListAdapter = new ShoppingListAdapter(getActivity(), mFactory);
        mShoppingListAdapter.setObjectsPerPage(MySettings.QUERY_LIMIT_ITEMS);
        mShoppingListAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Item>() {
            @Override
            public void onLoading() {
                
            }

            @Override
            public void onLoaded(List<Item> list, Exception e) {
                MyLog.i("fragShoppingList", "Loaded " + list.size() + " Items.");
            }
        });
        lvItemsListView.setAdapter(mShoppingListAdapter);

        return rootView;
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI(event.getItemID());
    }

    private void updateUI(String itemID) {
        mShoppingListAdapter.loadObjects();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragShoppingList", "onActivityCreated");
        EventBus.getDefault().post(new MyEvents.setActionBarTitle("Shopping List"));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyLog.i("fragShoppingList", "onSaveInstanceState");
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
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        MyLog.i("fragShoppingList", "onViewStateRestored");
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragShoppingList", "onResume");
        MySettings.setActiveFragmentID(MySettings.FRAG_SHOPPING_LIST);
            }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragShoppingList", "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.i("fragShoppingList", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragShoppingList", "onDestroy");
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        MyLog.i("fragShoppingList", "onDetach");
    }

}
