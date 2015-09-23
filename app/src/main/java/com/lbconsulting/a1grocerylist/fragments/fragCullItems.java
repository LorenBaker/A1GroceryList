package com.lbconsulting.a1grocerylist.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.CullItemsListAdapter;
import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows selected grocery items
 */
public class fragCullItems extends Fragment {

    private ListView lvItemsListView;

    private ParseQueryAdapter<Item> mItemsListAdapter;
    private ParseQueryAdapter.QueryFactory<Item> mFactory;

    private AlertDialog mYesNoAlertDialog;

    public static fragCullItems newInstance() {
        MyLog.i("fragCullItems", "newInstance");
        return new fragCullItems();
    }

    public fragCullItems() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MyLog.i("fragCullItems", "onAttach");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragCullItems", "onCreate");
        EventBus.getDefault().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragCullItems", "onCreateView");
        View rootView = inflater.inflate(R.layout.frag_cull_items, container, false);

        lvItemsListView = (ListView) rootView.findViewById(R.id.lvItemsListView);
        lvItemsListView.setItemsCanFocus(true);

        // Set up the Parse query to use in the adapter
        mFactory = new ParseQueryAdapter.QueryFactory<Item>() {
            public ParseQuery<Item> create() {
                ParseQuery<Item> query = Item.getQuery();
                query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
                query.fromLocalDatastore();
                return query;
            }
        };

        // Set up the ListView adapter
        mItemsListAdapter = new CullItemsListAdapter(getActivity(), mFactory);
        mItemsListAdapter.setObjectsPerPage(MySettings.QUERY_LIMIT_ITEMS);
        mItemsListAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Item>() {

            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List<Item> list, Exception e) {
                MyLog.i("fragCullItems", "Loaded " + list.size() + " Items.");
            }
        });
        lvItemsListView.setAdapter(mItemsListAdapter);

        return rootView;
    }


    public void onEvent(MyEvents.updateUI event) {
        updateUI();
    }

    private void updateUI() {
        mItemsListAdapter.loadObjects();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragCullItems", "onActivityCreated");
        EventBus.getDefault().post(new MyEvents.setActionBarTitle("Cull Items"));
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyLog.i("fragCullItems", "onSaveInstanceState");
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
        MyLog.i("fragCullItems", "onViewStateRestored");
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragCullItems", "onResume");
        MySettings.setActiveFragmentID(MySettings.FRAG_CULL_ITEMS);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragCullItems", "onPause");
        clearCheckedItems();
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.i("fragCullItems", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragCullItems", "onDestroy");
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        MyLog.i("fragCullItems", "onDetach");
    }

    //region Options Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_cull_items, menu);
        MyLog.i("fragCullItems", "onCreateOptionsMenu: menu_frag_master_list");

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MyLog.i("fragCullItems", "onPrepareOptionsMenu: menu_frag_master_list");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete_checked_items:
                deleteCheckedItems();
                break;

            case R.id.action_clear_all_check_marks:
                clearCheckedItems();
                break;

            case R.id.action_check_all:
                checkAllItems();
                break;

            case R.id.action_check_90_days:
                Date date90DaysAgo = getEarlierDate(90);
                checkEarlierDates(date90DaysAgo);
                break;

            case R.id.action_check_180_days:
                Date date180DaysAgo = getEarlierDate(180);
                checkEarlierDates(date180DaysAgo);
                break;

            case R.id.action_check_365_days:
                Date date365DaysAgo = getEarlierDate(365);
                checkEarlierDates(date365DaysAgo);
                break;

            case android.R.id.home:
                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_MASTER_ITEMS_LIST));
                return true;

            default:
                // Not implemented here
                return false;
        }

        return true;
    }

    private void checkAllItems() {
        final List<Item> uncheckedItems = Item.getUncheckedItems();
        for (Item item : uncheckedItems) {
            item.setChecked(true);
        }
        EventBus.getDefault().post(new MyEvents.updateUI(null));
    }

    private void checkEarlierDates(Date earlierDate) {
        final List<Item> oldItems = Item.getOldItems(earlierDate);
        if (oldItems == null) {
            return;
        }

        if (oldItems.size() > 0) {
            for (Item item : oldItems) {
                item.setChecked(true);
                item.setItemDirty(true);
            }

            EventBus.getDefault().post(new MyEvents.updateUI(null));
        }
    }

    private Date getEarlierDate(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now today's date.
        c.add(Calendar.DAY_OF_MONTH, -days); // subtract days
        return c.getTime();
    }

    private void clearCheckedItems() {
        final List<Item> checkedItems = Item.getCheckedItems();
        if (checkedItems == null) {
            return;
        }

        if (checkedItems.size() > 0) {
            for (Item item : checkedItems) {
                item.setChecked(false);
                item.setItemDirty(true);
            }

            EventBus.getDefault().post(new MyEvents.updateUI(null));
        }
    }

    private void deleteCheckedItems() {
        final List<Item> checkedItems = Item.getCheckedItems();
        if (checkedItems == null) {
            return;
        }

        if (checkedItems.size() > 0) {
            AlertDialog.Builder yesNoDialog = new AlertDialog.Builder(getActivity());
            // set dialog title and message
            yesNoDialog
                    // TODO: Implement plurals
                    .setTitle("Delete " + checkedItems.size() + " Items ?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {

                            if (A1Utils.isNetworkAvailable(getActivity())) {
                                dialog.dismiss();
                                // delete items using Parse Cloud batch delete
                                List<String> itemIDs = new ArrayList<String>();
                                for (Item item : checkedItems) {
                                    itemIDs.add(item.getItemID());
                                }
                                MyLog.i("fragCullItems", "Starting deletion of " + itemIDs.size() + " Items.");
                                final HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put("itemIDs", itemIDs);
                                ParseCloud.callFunctionInBackground("itemsBatchDelete", params, new FunctionCallback<Integer>() {

                                    @Override
                                    public void done(final Integer numberOfItemsDeleted, ParseException e) {
                                        if (e == null) {
                                            // Success. Items deleted in Parse cloud
                                            ParseObject.unpinAllInBackground(checkedItems, new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        MyLog.i("fragCullItems", "unpinAllInBackground success: Deleted " + numberOfItemsDeleted + " Items.");
                                                        EventBus.getDefault().post(new MyEvents.updateUI(null));
                                                    } else {
                                                        MyLog.e("fragCullItems", "unpinAllInBackground failure: ParseException: " + e.getMessage());
                                                    }
                                                }
                                            });

                                        } else {
                                            MyLog.e("fragCullItems", "ERROR: " + e.getMessage());
                                        }
                                    }
                                });


                            } else {
                                // delete items Eventually
                                for (Item item : checkedItems) {
                                    item.deleteEventually();
                                }
                                EventBus.getDefault().post(new MyEvents.updateUI(null));
                                dialog.dismiss();
                            }


                        }
                    })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            // create alert dialog
            mYesNoAlertDialog = yesNoDialog.create();

            mYesNoAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button btnYes = mYesNoAlertDialog.getButton(Dialog.BUTTON_POSITIVE);
                    btnYes.setTextSize(18);

                    Button btnNo = mYesNoAlertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNo.setTextSize(18);
                }
            });

            // show it
            mYesNoAlertDialog.show();
        } else {
            String title = "Delete Items";
            String msg = "Unable to delete Items. No Items are checked.";
            EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
        }
    }


    //endregion


}
