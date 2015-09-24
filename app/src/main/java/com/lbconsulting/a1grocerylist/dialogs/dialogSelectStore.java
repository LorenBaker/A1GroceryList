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
import com.lbconsulting.a1grocerylist.classes.MySettings;
import com.lbconsulting.a1grocerylist.database.Store;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A dialog where the user can select a store
 */
public class dialogSelectStore extends DialogFragment {

    private ListView lvStores;
    private AlertDialog mAlertDialog;

    public dialogSelectStore() {
        // Empty constructor required for DialogFragment
    }


    public static dialogSelectStore newInstance() {
        MyLog.i("dialogGroupLocation", "newInstance");
        return new dialogSelectStore();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogGroupLocation", "onCreate");
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
        View view = inflater.inflate(R.layout.dialog_select_store, null, false);
        lvStores = (ListView) view.findViewById(R.id.lvStores);
        List<Store> storeList = Store.getAllStores();
        final ArrayAdapter<Store> adapter = new ArrayAdapter<Store>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, storeList);
        lvStores.setAdapter(adapter);
        lvStores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Store store = (Store) adapter.getItem(position);
                MySettings.setStoreIDtoMapID(store.getStoreID());
                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_MAP_STORE));
                dismiss();
            }
        });

        String dialogTitle = "Select Store To Map";


        // build the dialog
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitle)
                .setView(view)
                .create();

        return mAlertDialog;
    }

}
