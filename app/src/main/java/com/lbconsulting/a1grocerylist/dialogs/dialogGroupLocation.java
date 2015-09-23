package com.lbconsulting.a1grocerylist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Location;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A dialog where the user can select a store's group location
 */
public class dialogGroupLocation extends DialogFragment {

    private static final String ARG_STORE_ID = "argStoreID";
    private static final String ARG_GROUP_ID = "argGroupID";
    private ListView lvGroupLocation;
    private AlertDialog mAlertDialog;
    private Store mStore;
    private Group mGroup;

    public dialogGroupLocation() {
        // Empty constructor required for DialogFragment
    }


    public static dialogGroupLocation newInstance(String storeID, String groupID) {
        MyLog.i("dialogGroupLocation", "newInstance");
        dialogGroupLocation fragment = new dialogGroupLocation();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_ID, storeID);
        args.putString(ARG_GROUP_ID, groupID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogGroupLocation", "onCreate");

        String storeID = getArguments().getString(ARG_STORE_ID);
        String groupID = getArguments().getString(ARG_GROUP_ID);

        mStore = Store.getStore(storeID);
        mGroup = Group.getGroup(groupID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("dialogGroupLocation", "onActivityCreated");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MyLog.i("dialogGroupLocation", "onCreateDialog");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_group_location, null, false);
        lvGroupLocation = (ListView) view.findViewById(R.id.lvGroupLocation);
        List<Location> locationsList = Location.getAllLocations();
        final ArrayAdapter<Location> adapter = new ArrayAdapter<Location>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, locationsList);
        lvGroupLocation.setAdapter(adapter);
        lvGroupLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location location = (Location) adapter.getItem(position);
                StoreMapEntry.setGroupLocation(getActivity(), mStore, mGroup, location);
                EventBus.getDefault().post(new MyEvents.updateUI(null));
                dismiss();
            }
        });

        String dialogTitle = "Select \"" + mGroup.getGroupName() + "\" Location";


        // build the dialog
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitle)
                .setView(view)
                .create();

        return mAlertDialog;
    }

}
