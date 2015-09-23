package com.lbconsulting.a1grocerylist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.StoreChain;

import de.greenrobot.event.EventBus;

/**
 * A dialog where the user can add a new store chain
 */
public class dialogNewStoreChain extends DialogFragment {

    private EditText txtNewStoreChainName;
    private AlertDialog mNewStoreChainDialog;

    public dialogNewStoreChain() {
        // Empty constructor required for DialogFragment
    }


    public static dialogNewStoreChain newInstance() {
        MyLog.i("dialogNewStoreChain", "newInstance");
        return new dialogNewStoreChain();
//        dialogNewStoreChain fragment = new dialogNewStoreChain();
//        Bundle args = new Bundle();
//        args.putString(ARG_ITEM_ID, itemID);
//        args.putString(ARG_DIALOG_TITLE, dialogTitle);
//        fragment.setArguments(args);
//        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogNewStoreChain", "onCreate");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("dialogNewStoreChain", "onActivityCreated");
        mNewStoreChainDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnCreate = mNewStoreChainDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnCreate.setTextSize(18);

                Button btnCancel = mNewStoreChainDialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnCancel.setTextSize(18);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MyLog.i("dialogNewStoreChain", "onCreateDialog");
        final String title = "Error Creating Store Chain Name";

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_store_chain, null, false);

        // find the dialog's views
        txtNewStoreChainName = (EditText) view.findViewById(R.id.txtNewStoreChainName);

        // build the dialog
        mNewStoreChainDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Create New Store Chain")
                .setView(view)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String proposedStoreChainName = txtNewStoreChainName.getText().toString().trim();

                                if (proposedStoreChainName.isEmpty()) {
                                    // Error, the proposed name is empty!
                                    // Inform the user of the error.
                                    String msg = "Unable to create the new store chain name. The proposed name is empty!";
                                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                                    return;
                                }

                                // verify that there is no store chain name the same proposed item name
                                if (!StoreChain.storeChainExists(proposedStoreChainName)) {
                                    // The Chain Name does not exist in the table
                                    // so create it
                                    StoreChain.createNewStoreChain(proposedStoreChainName);


                                } else {
                                    // Error, the proposed name is in use!
                                    // Inform the user of the error.
                                    String msg = "Unable to create the new store chain name because the proposed name \""
                                            + proposedStoreChainName + "\" is already in use.";
                                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                                    return;
                                }

                                dismiss();
                            }
                        }
                )

                .setNegativeButton(getActivity().getString(R.string.btnCancel_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();

        return mNewStoreChainDialog;
    }

}
