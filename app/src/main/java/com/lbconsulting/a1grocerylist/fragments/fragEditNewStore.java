package com.lbconsulting.a1grocerylist.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.StoreChainSpinnerArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.A1Utils;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreChain;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;
import com.lbconsulting.a1grocerylist.dialogs.dialogNewStoreChain;
import com.parse.FunctionCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A fragment that shows the store detail
 */
public class fragEditNewStore extends Fragment implements View.OnClickListener {

    public static final String NOT_AVAILABLE = "notAvailable";
    private static final String ARG_STORE_ID = "argStoreID";
    private static final String ARG_IS_STORE_DIRTY = "argIsStoreDirty";
    private StoreChain mStoreChain;
    private Store mStore;
    private String mStoreName;
    private boolean mIsNewStore;
    private boolean mIsStoreDirty;

    private final TextWatcher storeChangedWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            mIsStoreDirty = true;
        }
    };

    private final TextWatcher storeNameChangedWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (mStore != null && mStore.getStoreChain() != null) {
                mStoreName = mStore.getStoreChain().getStoreChainName() + " " + txtStoreRegionalName.getText().toString();
            }
            mIsStoreDirty = true;
        }
    };

    private Spinner spnStoreChains;
    private Button btnGetDeviceLocation;
    private Button btnGetGeoPoint;
    private EditText txtStoreRegionalName;

    private EditText txtAddress1;
    private EditText txtAddress2;
    private EditText txtCity;
    private EditText txtState;
    private EditText txtZip;
    private EditText txtCountry;
    private EditText txtLatitude;
    private EditText txtLongitude;
    private EditText txtWebsiteURL;
    private EditText txtPhoneNumber;

    private RelativeLayout progressBar;
    private StoreChainSpinnerArrayAdapter mAdapter;

    public fragEditNewStore() {
        // Required empty public constructor
    }

    public static fragEditNewStore newInstance(String storeID) {
        MyLog.i("fragEditNewStore", "newInstance: StoreID = " + storeID);
        fragEditNewStore fragment = new fragEditNewStore();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_ID, storeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        mIsNewStore = MySettings.IsNewStore();

        String storeID;
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_STORE_ID)) {
            storeID = savedInstanceState.getString(ARG_STORE_ID);
            mIsStoreDirty = savedInstanceState.getBoolean(ARG_IS_STORE_DIRTY);
        } else {
            storeID = getArguments().getString(ARG_STORE_ID);
        }

        if (mIsNewStore) {
            mStore = new Store();
            mStoreName = "New Store";
            mIsStoreDirty = true;
            EventBus.getDefault().post(new MyEvents.setActionBarTitle("New Store"));
        } else {
            mStore = Store.getStore(storeID);
            if (mStore != null) {
                mStoreName = mStore.getStoreChainAndRegionalName();
            }
            EventBus.getDefault().post(new MyEvents.setActionBarTitle("Edit Store"));
        }
        MyLog.i("fragEditNewStore", "onCreate: " + mStoreName);

        setHasOptionsMenu(true);
    }

    public void onEvent(MyEvents.updateUI event) {
        // syncA1GroceryListData complete
        progressBar.setVisibility(View.GONE);
    }

    public void onEvent(MyEvents.updateStoreChainSpinner event) {
        List<StoreChain> storeChains = StoreChain.getAllStoreChains();
        mAdapter.clear();
        mAdapter.addAll(storeChains);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.i("fragEditNewStore", "onCreateView: " + mStoreName);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_edit_new_store, container, false);

        spnStoreChains = (Spinner) rootView.findViewById(R.id.spnStoreChains);
        List<StoreChain> storeChains = StoreChain.getAllStoreChains();
        mAdapter = new StoreChainSpinnerArrayAdapter(getActivity(), storeChains);
        spnStoreChains.setAdapter(mAdapter);
        spnStoreChains.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStoreChain = mAdapter.getStoreChain(position);
                mIsStoreDirty = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (!mIsNewStore) {
            spnStoreChains.setSelection(mAdapter.getPosition(mStore.getStoreChain()));
        } else {
            spnStoreChains.setSelection(0);
        }

        btnGetDeviceLocation = (Button) rootView.findViewById(R.id.btnGetDeviceLocation);
        btnGetGeoPoint = (Button) rootView.findViewById(R.id.btnGetGeoPoint);

        btnGetDeviceLocation.setOnClickListener(this);
        btnGetGeoPoint.setOnClickListener(this);

        txtStoreRegionalName = (EditText) rootView.findViewById(R.id.txtStoreRegionalName);
        txtAddress1 = (EditText) rootView.findViewById(R.id.txtAddress1);
        txtAddress2 = (EditText) rootView.findViewById(R.id.txtAddress2);
        txtCity = (EditText) rootView.findViewById(R.id.txtCity);
        txtState = (EditText) rootView.findViewById(R.id.txtState);
        txtZip = (EditText) rootView.findViewById(R.id.txtZip);
        txtCountry = (EditText) rootView.findViewById(R.id.txtCountry);
        txtLatitude = (EditText) rootView.findViewById(R.id.txtLatitude);
        txtLongitude = (EditText) rootView.findViewById(R.id.txtLongitude);
        txtWebsiteURL = (EditText) rootView.findViewById(R.id.txtWebsiteURL);
        txtPhoneNumber = (EditText) rootView.findViewById(R.id.txtPhoneNumber);

        progressBar = (RelativeLayout) rootView.findViewById(R.id.progressBar);

        if (!mIsNewStore) {
            txtStoreRegionalName.setText(mStore.getStoreRegionalName());
            txtAddress1.setText(mStore.getAddress1());
            txtAddress2.setText(mStore.getAddress2());
            txtCity.setText(mStore.getCity());
            txtState.setText(mStore.getState());
            txtZip.setText(mStore.getZip());
            txtCountry.setText(mStore.getCountry());
            txtLatitude.setText(Double.toString(mStore.getStoreLatitude()));
            txtLongitude.setText(Double.toString(mStore.getStoreLongitude()));
            txtWebsiteURL.setText(mStore.getWebsiteUrl());
            txtPhoneNumber.setText(mStore.getPhoneNumber());
        }

        txtStoreRegionalName.addTextChangedListener(storeNameChangedWatcher);
        txtAddress1.addTextChangedListener(storeChangedWatcher);
        txtAddress2.addTextChangedListener(storeChangedWatcher);
        txtCity.addTextChangedListener(storeChangedWatcher);
        txtState.addTextChangedListener(storeChangedWatcher);
        txtZip.addTextChangedListener(storeChangedWatcher);
        txtCountry.addTextChangedListener(storeChangedWatcher);
        txtLatitude.addTextChangedListener(storeChangedWatcher);
        txtLongitude.addTextChangedListener(storeChangedWatcher);
        txtWebsiteURL.addTextChangedListener(storeChangedWatcher);
        txtPhoneNumber.addTextChangedListener(storeChangedWatcher);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("fragEditNewStore", "onActivityCreated: " + mStoreName);
        MySettings.setActiveFragmentID(MySettings.FRAG_EDIT_NEW_STORE);

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        MyLog.i("fragEditNewStore", "onSaveInstanceState: " + mStoreName);
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
        if (mStore != null) {
            outState.putString(ARG_STORE_ID, mStore.getStoreID());
        }
        outState.putBoolean(ARG_IS_STORE_DIRTY, mIsStoreDirty);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        MyLog.i("fragEditNewStore", "onViewStateRestored: " + mStoreName);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("fragEditNewStore", "onResume: " + mStoreName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.i("fragEditNewStore", "onPause: " + mStoreName);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        }

    }

    private void showSaveDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set dialog title and message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveStore();
                        dialog.cancel();
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_STORE_LIST_BY_AISLE));
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnYes = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                Button btnNo = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnYes.setTextSize(18);
                btnNo.setTextSize(18);
            }
        });

        // show it
        alertDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.i("fragEditNewStore", "onDestroyView: " + mStoreName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i("fragEditNewStore", "onDestroy: " + mStoreName);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MyLog.i("fragEditNewStore", "onDetach: " + mStoreName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetDeviceLocation:
                if (A1Utils.isNetworkAvailable(getActivity())) {
                    // TODO: check if a location has been found ... if not, wait
                    android.location.Location location = MySettings.getLastLocation();
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    getAddress(latitude, longitude);
                } else {
                    String title = "Unable TO Get Device Location";
                    String msg = "Network is not available.";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                }
                break;

            case R.id.btnGetGeoPoint:
                if (A1Utils.isNetworkAvailable(getActivity())) {
                    getGeoPoint();
                } else {
                    String title = "Unable TO Get GeoPoint";
                    String msg = "Network is not available.";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                }
                break;
        }
    }

    private void getAddress(final String latitude, final String longitude) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("latitude", latitude);
        params.put("longitude", longitude);

        ParseCloud.callFunctionInBackground("getAddress", params, new FunctionCallback<HashMap<String, String>>() {

            @Override
            public void done(final HashMap<String, String> address, ParseException e) {
                if (e == null) {
                    // Success. Found address
                    txtAddress1.setText(address.get(("address1")));
                    txtAddress2.setText("");
                    txtCity.setText(address.get(("city")));
                    txtState.setText(address.get(("state")));
                    txtZip.setText(address.get(("zip")));
                    txtCountry.setText(address.get(("country")));
                    txtLatitude.setText(latitude);
                    txtLongitude.setText(longitude);

                } else {
                    EventBus.getDefault().post(new MyEvents.showOkDialog("Failure", e.getMessage()));
                    MyLog.e("fragEditNewStore", "getAddress: Error: " + e.getMessage());
                }
            }
        });
    }

    private void getGeoPoint() {

        final HashMap<String, String> params = new HashMap<>();
        params.put("address1", txtAddress1.getText().toString().trim());
        params.put("address2", txtAddress2.getText().toString().trim());
        params.put("city", txtCity.getText().toString().trim());
        params.put("state", txtState.getText().toString().trim());
        params.put("zip", txtZip.getText().toString().trim());
        params.put("country", txtCountry.getText().toString().trim());

        ParseCloud.callFunctionInBackground("getLatitudeAndLongitude", params, new FunctionCallback<ParseGeoPoint>() {

            @Override
            public void done(final ParseGeoPoint geoPoint, ParseException e) {
                if (e == null) {
                    // Success. Found geoPoint
                    txtLatitude.setText(String.valueOf(geoPoint.getLatitude()));
                    txtLongitude.setText(String.valueOf(geoPoint.getLongitude()));
                } else {
                    EventBus.getDefault().post(new MyEvents.showOkDialog("Failure", e.getMessage()));
                    MyLog.e("fragEditNewStore", "getGeoPoint: Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_edit_new_store, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save:
                saveStore();
                break;

            case R.id.action_place:
                // TODO: Show store on map
                Toast.makeText(getActivity(), "action_place", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_new_store_chain:
                showNewStoreChainDialog(getActivity());
                break;

            case android.R.id.home:
                if (mIsStoreDirty) {
                    String title = "Save Store?";
                    String message = "Store details have changed. Do you want to save them?";
                    showSaveDialog(getActivity(), title, message);
                } else {
                    EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_STORE_LIST_BY_AISLE));
                }
                return true;

            default:
                // Not implemented here
                return false;
        }

        return true;
    }

    private void showNewStoreChainDialog(Context context) {
        FragmentManager fm = getFragmentManager();
        dialogNewStoreChain dialog = dialogNewStoreChain.newInstance();
        dialog.show(fm, "dialogNewStoreChain");
    }

    private void saveStore() {

        if (mIsStoreDirty) {
            final String title = "Unable to Save Store";
            final String titleFail = "FAILED To Save New Store";
            String errorMessage;

            // check for non empty Store Regional Name
            String storeRegionalName = txtStoreRegionalName.getText().toString().trim();
            if (storeRegionalName.isEmpty()) {
                errorMessage = "Store Regional Name cannot be empty!";
                EventBus.getDefault().post(new MyEvents.showOkDialog(title, errorMessage));
                return;
            }

            // check for valid latitude and longitude
            String latitudeStr = txtLatitude.getText().toString().trim();
            String longitudeStr = txtLongitude.getText().toString().trim();
            double latitude;
            double longitude;

            if (latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
                // invalid latitude or longitude
                errorMessage = "Latitude and Longitude fields cannot be empty!";
                EventBus.getDefault().post(new MyEvents.showOkDialog(title, errorMessage));
                return;
            } else {
                latitude = Double.parseDouble(latitudeStr);
                longitude = Double.parseDouble(longitudeStr);
                if (!A1Utils.isValidLatitudeAndLongitude(latitude, longitude)) {
                    // invalid latitude or longitude
                    errorMessage = "Latitude (+-90) and Longitude(+-180) fields are not valid.";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, errorMessage));
                    return;
                }
            }
            mStore.setStoreRegionalName(storeRegionalName);
            mStore.setAddress1(txtAddress1.getText().toString().trim());
            mStore.setAddress2(txtAddress2.getText().toString().trim());
            mStore.setCity(txtCity.getText().toString().trim());
            mStore.setState(txtState.getText().toString().trim());
            mStore.setZip(txtZip.getText().toString().trim());
            mStore.setCountry(txtCountry.getText().toString().trim());
            mStore.setStoreGeoPoint(latitude, longitude);
            mStore.setWebsiteUrl(txtWebsiteURL.getText().toString().trim());
            mStore.setPhoneNumber(txtPhoneNumber.getText().toString().trim());
            mStore.setStoreDirty(false);
            mStore.setChecked(false);
            mStore.setStoreChain(mStoreChain);
            mStore.setAuthor(ParseUser.getCurrentUser());
            ParseACL storeACL = new ParseACL(ParseUser.getCurrentUser());
            storeACL.setPublicReadAccess(true);
            storeACL.setPublicWriteAccess(true);
            mStore.setACL(storeACL);

            if (mIsNewStore) {
                if (A1Utils.isNetworkAvailable(getActivity())) {

                    progressBar.setVisibility(View.VISIBLE);
                    mStore.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // Saved successfully.
                                String id = mStore.getObjectId();
                                MySettings.setActiveStoreID(id);
                                mIsStoreDirty = false;
                                MyLog.i("fragEditNewStore", "Successfully saved: " + mStore.getStoreChainAndRegionalName());
                                EventBus.getDefault().post(new MyEvents.syncA1GroceryListData(MySettings.ACTION_UPLOAD_AND_DOWNLOAD));


                            } else {
                                // The save failed.
                                progressBar.setVisibility(View.GONE);
                                String msg = "Save new store FAILED.";
                                EventBus.getDefault().post(new MyEvents.showOkDialog(titleFail, msg));
                                MyLog.e("fragEditNewStore", msg + " StoreID: " + mStore.getStoreID());
                            }
                        }
                    });

                } else {
                    // the network is not available
                    progressBar.setVisibility(View.GONE);
                    errorMessage = "No internet network available to save the new store!";
                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, errorMessage));
                    return;
                }

            } else {
                mStore.saveEventually();
                mIsStoreDirty = false;
            }


        }
    }

    private void downloadStoreMap(Store store) {
        try {
            ParseQuery query = StoreMapEntry.getQuery();
            query.whereEqualTo(StoreMapEntry.STORE, store);
            List<StoreMapEntry> storeMap = query.find();
            if (storeMap != null && storeMap.size() > 0) {
                MyLog.i("fragEditNewStore", "downloadStoreMap: Found " + storeMap.size() + " store map entries for the new store.");
                ParseObject.pinAll(storeMap);
            }
        } catch (ParseException e) {
            MyLog.e("fragEditNewStore", "downloadStoreMap: ParseException" + e.getMessage());
        }

    }
}
