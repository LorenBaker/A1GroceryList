package com.lbconsulting.a1grocerylist.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.MasterItemsListAdapter;
import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.dialogs.dialogMasterListSorting;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows selected grocery items
 */
public class fragMasterItemsList extends Fragment implements View.OnClickListener {

    private EditText txtItemName;
    private EditText txtItemNote;
    private ListView lvItemsListView;

    private ParseQueryAdapter<Item> mItemsListAdapter;
    private ParseQueryAdapter.QueryFactory<Item> mFactory;

    private String mMoveToItemID = null;


    public static fragMasterItemsList newInstance() {
        MyLog.i("fragMasterItemsList", "newInstance");
        return new fragMasterItemsList();
    }

    public fragMasterItemsList() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MyLog.i("fragMasterItemsList", "onAttach");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("fragMasterItemsList", "onCreate");
        EventBus.getDefault().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragMasterItemsList", "onCreateView");
        View rootView = inflater.inflate(R.layout.frag_master_items_list, container, false);

        txtItemName = (EditText) rootView.findViewById(R.id.txtItemName);

        txtItemNote = (EditText) rootView.findViewById(R.id.txtItemNote);
        Button btnAddToMasterList = (Button) rootView.findViewById(R.id.btnAddToMasterList);
        Button btnClearEditText = (Button) rootView.findViewById(R.id.btnClearEditText);

        btnAddToMasterList.setOnClickListener(this);
        btnClearEditText.setOnClickListener(this);

        lvItemsListView = (ListView) rootView.findViewById(R.id.lvItemsListView);
        lvItemsListView.setItemsCanFocus(true);

        // Set up the Parse query to use in the adapter
        mFactory = new ParseQueryAdapter.QueryFactory<Item>() {
            public ParseQuery<Item> create() {
                ParseQuery<Item> query = Item.getQuery();
                if (MySettings.showFavorites()) {
                    query.whereEqualTo(Item.IS_FAVORITE, true);
                }

                switch (MySettings.getMasterListSortOrder()) {
                    // TODO: implement manual sort order
                    case MySettings.SORT_ALPHABETICAL:
                        query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
                        break;

                    case MySettings.SORT_REVERSE_ALPHABETICAL:
                        query.orderByDescending(Item.ITEM_NAME_LOWERCASE);
                        break;

                    case MySettings.SORT_SELECTED_FIRST:
                        // query.orderByDescending(Item.IS_SELECTED);
                        query.orderByDescending(Item.IS_SELECTED).addAscendingOrder(Item.ITEM_NAME_LOWERCASE);
                        break;

                    case MySettings.SORT_FAVORITES_FIRST:
                        query.orderByDescending(Item.IS_FAVORITE);
                        query.orderByAscending(Item.ITEM_NAME_LOWERCASE);
                        break;

                    case MySettings.SORT_DATE_UPDATED:
                        query.orderByDescending(Item.DATE_UPDATED);
                        break;

                    case MySettings.SORT_MANUALLY:
                        query.orderByAscending(Item.SORT_KEY);
                        break;

                }

                if (!txtItemName.getText().toString().isEmpty()) {
                    query.whereContains(Item.ITEM_NAME_LOWERCASE, txtItemName.getText().toString().toLowerCase());
                }

                query.fromLocalDatastore();
                return query;
            }
        };

        // Set up the ListView adapter
        mItemsListAdapter = new MasterItemsListAdapter(getActivity(), mFactory);
        mItemsListAdapter.setObjectsPerPage(MySettings.QUERY_LIMIT_ITEMS);
        mItemsListAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Item>() {
            public void onLoading() {
                // Trigger "loading" UI progress bar
            }

            @Override
            public void onLoaded(List<Item> list, Exception e) {
                // Execute any post-loading logic, hide "loading" progress bar
                MyLog.i("fragMasterItemsList", "Loaded " + list.size() + " Items.");
                if (mMoveToItemID != null && !mMoveToItemID.isEmpty()) {
                    int position = 0;
                    Item item;
                    for (int i = 0; i < mItemsListAdapter.getCount(); i++) {
                        item = mItemsListAdapter.getItem(i);
                        if (item.getItemID().equals(mMoveToItemID)) {
                            position = i;
                            break;
                        }
                    }

                    lvItemsListView.setSelection(position);
                }
            }
        });
        lvItemsListView.setAdapter(mItemsListAdapter);

        // setup txtItemName Listeners
        txtItemName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean result = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            addItemToMasterList();
                            result = true;
                        default:
                            break;
                    }
                }
                return result;
            }
        });

        txtItemName.addTextChangedListener(new TextWatcher() {
            // filter master list as the user inputs text
            @Override
            public void afterTextChanged(Editable s) {
                updateUI(null);
                // mItemsArrayAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }
        });


        return rootView;
    }

    private void addItemToMasterList() {
        String proposedItemName = txtItemName.getText().toString().trim();
        String proposedItemNote = txtItemNote.getText().toString().trim();
        // check that the proposed item name is not empty
        if (proposedItemName.isEmpty()) {
            // Error, the proposed name is empty!
            // Inform the user of the error.
            String title = "Error Adding Item";
            String msg = "Unable to add the Item name because its proposed name is empty!";
            EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
            return;
        }

        // verify that there is no other item with the same proposed item name
        if (!Item.itemExists(proposedItemName)) {
            // The Item does not exist in the table
            // so add it to the master list
            createNewItem(proposedItemName, proposedItemNote, null);

        } else if (proposedItemName.toLowerCase().equals(Item.getFoundExistingItem().getItemName().toLowerCase())) {
            // The item name already exists in the items table.
            // However, its letters are the same so there is only a
            // difference letter capitalization so update the Item's name and note
            createNewItem(proposedItemName, proposedItemNote, Item.getFoundExistingItem());

        } else {
            // Error, the proposed name is in use!
            // Inform the user of the error.
            String title = "Error Adding Item";
            String msg = "Unable to add the Item because it is already in the master items list.";
            EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
        }
    }

    private void createNewItem(String itemName, String itemNote, Item newItem) {
        boolean creatingNewItem = false;
        if (newItem == null) {
            newItem = new Item();
            creatingNewItem = true;

        }
        newItem.setAuthor(ParseUser.getCurrentUser());
        newItem.setItemName(itemName);
        newItem.setItemNote(itemNote);
        newItem.setChecked(false);
        if (MySettings.showFavorites()) {
            newItem.setFavorite(true);
        } else {
            newItem.setFavorite(false);
        }
        newItem.setSelected(true);
        newItem.setStruckOut(false);
        newItem.setBarcodeNumber("");
        newItem.setBarcodeFormat("");
        ParseACL itemACL = new ParseACL(ParseUser.getCurrentUser());
        newItem.setACL(itemACL);

        if (creatingNewItem) {
            try {
                newItem.setGroup(Group.getDefaultGroup());
                newItem.setSortKey(Item.getNextSortKey());
                if (A1Utils.isNetworkAvailable(getActivity())) {
                    // the network is available
                    newItem.setItemDirty(false);
                    newItem.pin();
                    newItem.saveInBackground();

                } else {
                    // the network is not available
                    newItem.setUuidString();
                    newItem.setItemDirty(true);
                    newItem.pin();
                }


            } catch (ParseException e) {
                MyLog.e("fragMasterItemsList", "createNewItem: ParseException" + e.getMessage());
            }
        }

        txtItemName.setText("");
        txtItemNote.setText("");
        updateUI(newItem.getItemID());
    }

    public void onEvent(MyEvents.updateUI event) {
        updateUI(event.getItemID());
    }

    private void updateUI(String itemID) {
        mMoveToItemID = itemID;
        mItemsListAdapter.loadObjects();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragMasterItemsList", "onActivityCreated");
        EventBus.getDefault().post(new MyEvents.setActionBarTitle("Master Items List"));
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyLog.i("fragMasterItemsList", "onSaveInstanceState");
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
        MyLog.i("fragMasterItemsList", "onViewStateRestored");
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragMasterItemsList", "onResume");
        MySettings.setActiveFragmentID(MySettings.FRAG_MASTER_ITEMS_LIST);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragMasterItemsList", "onPause");
        // EventBus.getDefault().post(new MyEvents.uploadDirtyObjects());
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.i("fragMasterItemsList", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragMasterItemsList", "onDestroy");
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        MyLog.i("fragMasterItemsList", "onDetach");
    }

    //region Options Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_master_list, menu);
        MyLog.i("fragMasterItemsList", "onCreateOptionsMenu: menu_frag_master_list");

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MyLog.i("fragMasterItemsList", "onPrepareOptionsMenu: menu_frag_master_list");
        MenuItem showFavorites = menu.findItem(R.id.action_show_favorites);
        MenuItem showAllItems = menu.findItem(R.id.action_show_all_items);
        if (MySettings.showFavorites()) {
            showFavorites.setVisible(false);
            showAllItems.setVisible(true);
        } else {
            showFavorites.setVisible(true);
            showAllItems.setVisible(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyLog.i("fragMasterItemsList", "onOptionsItemSelected: menu itemID = " + item.getItemId());
        switch (item.getItemId()) {

            case R.id.action_show_favorites:
                MySettings.setShowFavorites(true);
                updateUI(null);
                getActivity().invalidateOptionsMenu();

                return true;

            case R.id.action_show_all_items:
                MySettings.setShowFavorites(false);
                updateUI(null);
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.action_select_all_items:
                Item.selectAllItems(getActivity());
                updateUI(null);
                // EventBus.getDefault().post(new MyEvents.uploadDirtyObjects());
                return true;

            case R.id.action_select_all_favorites:
                Item.selectAllFavorites(getActivity());
                updateUI(null);
                EventBus.getDefault().post(new MyEvents.uploadDirtyObjects());
                return true;

            case R.id.action_deselect_all_items:
                Item.deselectAllItems(getActivity());
                updateUI(null);
                //EventBus.getDefault().post(new MyEvents.uploadDirtyObjects());
                return true;

            case R.id.action_show_master_list_sort_dialog:
                showMasterListSortDialog();
                return true;

            case R.id.action_set_manual_sort_order:
                Toast.makeText(getActivity(), "action_set_manual_sort_order", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_cull_items:
                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_CULL_ITEMS));
                return true;

            case R.id.action_manage_groups:
                //EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_ITEMS_BY_GROUP));
                Toast.makeText(getActivity(), "action_manage_groups", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_manage_item_location:
                Toast.makeText(getActivity(), "action_manage_item_location", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_scan_barcodes:
                Toast.makeText(getActivity(), "action_scan_barcodes", Toast.LENGTH_SHORT).show();
                //launchScannerActivity();
                return true;

            case R.id.action_show_scanned_items:
                Toast.makeText(getActivity(), "action_show_scanned_items", Toast.LENGTH_SHORT).show();
                //EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_PRODUCTS_LIST));
                return true;

            case android.R.id.home:
                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_SHOPPING_LIST));
                return true;

            default:
                // Not implemented here
                return false;
        }
    }

    private void showMasterListSortDialog() {
        FragmentManager fm = getFragmentManager();
        dialogMasterListSorting dialog = dialogMasterListSorting.newInstance();
        dialog.show(fm, "dialogMasterListSorting");
    }
    //endregion


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddToMasterList:
                //Toast.makeText(getActivity(), "btnAddToMasterList.click", Toast.LENGTH_SHORT).show();
                addItemToMasterList();
                // hide the soft input keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtItemName.getWindowToken(), 0);
                break;

            case R.id.btnClearEditText:
                // Toast.makeText(getActivity(), "btnClearEditText.click", Toast.LENGTH_SHORT).show();
                clearEditText();
                break;
        }
    }

    private void clearEditText() {
        String itemNote = txtItemNote.getText().toString().trim();
        if (!itemNote.isEmpty()) {
            // the item has a note ... so clear it
            txtItemNote.setText("");
        } else {
            // the item has no note ... so clear the item
            txtItemName.setText("");
        }
    }
}
