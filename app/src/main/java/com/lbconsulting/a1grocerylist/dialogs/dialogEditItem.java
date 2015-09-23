package com.lbconsulting.a1grocerylist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.adapters.GroupsSpinnerArrayAdapter;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.Item;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A dialog where the user can edit the item's name, note, and group
 */
public class dialogEditItem extends DialogFragment {

    private static final String ARG_ITEM_ID = "argItemID";
    private static final String ARG_DIALOG_TITLE = "argDialogTitle";

    private EditText txtItemName;
    private EditText txtItemNote;
    private Spinner spnGroup;
    private CheckBox ckIsFavorite;

    private String mInitialItemName;
    private Item mItem;
    private String mDialogTitle = "";

    private AlertDialog mAlertDialog;
    private AlertDialog mYesNoAlertDialog;

    public dialogEditItem() {
        // Empty constructor required for DialogFragment
    }


    public static dialogEditItem newInstance(String itemID, String dialogTitle) {
        MyLog.i("dialogEditItem", "newInstance: itemID = " + itemID);
        dialogEditItem fragment = new dialogEditItem();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemID);
        args.putString(ARG_DIALOG_TITLE, dialogTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogEditItem", "onCreate");
        Bundle args = getArguments();
        if (args.containsKey(ARG_ITEM_ID)) {
            String itemID = args.getString(ARG_ITEM_ID);
            mItem = Item.getItem(itemID);
            mDialogTitle = args.getString(ARG_DIALOG_TITLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("dialogEditItem", "onActivityCreated");
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnOK = mAlertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnOK.setTextSize(18);

                Button btnCancel = mAlertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnCancel.setTextSize(18);

                Button btnDelete = mAlertDialog.getButton(Dialog.BUTTON_NEUTRAL);
                btnDelete.setTextSize(18);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MyLog.i("dialogEditItem", "onCreateDialog");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_item, null, false);

        // find the dialog's views
        txtItemName = (EditText) view.findViewById(R.id.txtItemName);
        txtItemNote = (EditText) view.findViewById(R.id.txtItemNote);
        spnGroup = (Spinner) view.findViewById(R.id.spnGroup);
        ckIsFavorite = (CheckBox) view.findViewById(R.id.ckIsFavorite);

        // get the item
        Group itemGroup = null;
        if (mItem != null) {
            txtItemName.setText(mItem.getItemName());
            mInitialItemName = mItem.getItemName();
            txtItemNote.setText(mItem.getItemNote());
            ckIsFavorite.setChecked(mItem.isFavorite());
            itemGroup = mItem.getGroup();
        } else {
            MyLog.e("dialogEditItem", "onCreateDialog: No Item available!");
        }

        // fill spnGroup
        List<Group> groups = Group.getAllGroups();
        GroupsSpinnerArrayAdapter adapter = new GroupsSpinnerArrayAdapter(getActivity(), groups);
        spnGroup.setAdapter(adapter);
        if (itemGroup != null) {
            int startingPosition = getGroupStartingPosition(itemGroup, groups);
            spnGroup.setSelection(startingPosition);
        }

        // build the dialog
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mDialogTitle)
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.btnOk_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String proposedItemName = txtItemName.getText().toString().trim();

                                if (proposedItemName.isEmpty()) {
                                    // Error, the proposed name is empty! Set the text box to the
                                    // original initial Item name.
                                    txtItemName.setText(mInitialItemName);
                                    // Inform the user of the error.
                                    String title = "Error Updating Item Name";
                                    String msg = "Unable to update the Item's name because the proposed name is empty!";
                                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                                    return;
                                }

                                // verify that there is no other item with the same proposed item name
                                if (!Item.itemExists(proposedItemName)) {
                                    // The Item does not exist in the table
                                    // so update the item's name
                                    mItem.setItemName(proposedItemName);

                                } else if (proposedItemName.toLowerCase().equals(mInitialItemName.toLowerCase())) {
                                    // The item name already exists in the items table.
                                    // However, its letters are the same so there is only a
                                    // difference letter capitalization so update the Item's name
                                    mItem.setItemName(proposedItemName);

                                } else {
                                    // Error, the proposed name is in use! Set the text box to the
                                    // original initial Item name.
                                    txtItemName.setText(mInitialItemName);
                                    // Inform the user of the error.
                                    String title = "Error Updating Item Name";
                                    String msg = "Unable to update the Item's name because the proposed name \""
                                            + proposedItemName + "\" is already in use.";
                                    EventBus.getDefault().post(new MyEvents.showOkDialog(title, msg));
                                    return;
                                }

                                // update the item's note
                                mItem.setItemNote(txtItemNote.getText().toString().trim());

                                // update the item's group
                                Group selectedGroup = (Group) spnGroup.getSelectedItem();
                                mItem.setGroup(selectedGroup);

                                // update the item's Favorite
                                mItem.setFavorite(ckIsFavorite.isChecked());

                                // indicate that the item is dirty
                                mItem.setItemDirty(true);

                                // update the UI
                                EventBus.getDefault().post(new MyEvents.updateUI(mItem.getItemID()));
                                dismiss();
                            }
                        }
                )

                .setNeutralButton(getActivity().getString(R.string.btnDelete_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                AlertDialog.Builder yesNoDialog = new AlertDialog.Builder(getActivity());
                                // set dialog title and message
                                yesNoDialog
                                        .setTitle("Delete \"" + mItem.getItemName() + "\" ?")
                                        .setCancelable(true)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                mItem.deleteEventually();
                                                EventBus.getDefault().post(new MyEvents.updateUI(null));
                                                dismiss();
                                            }
                                        })

                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dismiss();
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

        return mAlertDialog;
    }


    private int getGroupStartingPosition(Group soughtGroup, List<Group> groups) {
        int position = 0;
        boolean found = false;
        for (Group group : groups) {
            if (group.getGroupID().equals(soughtGroup.getGroupID())) {
                found = true;
                break;
            } else {
                position++;
            }
        }

        if (!found) {
            position = -1;
        }
        return position;
    }
}
