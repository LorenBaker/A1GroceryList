package com.lbconsulting.a1grocerylist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.classes.MySettings;

import de.greenrobot.event.EventBus;

/**
 * A dialog where the user selects how to sort the Shopping List
 */
public class dialogShoppingListSorting extends DialogFragment {

    private RadioButton rbAlphabetical;
    private RadioButton rbReverseAlphabetical;
    private RadioButton rbByGroup;

    private AlertDialog mAlertDialog;

    public dialogShoppingListSorting() {
        // Empty constructor required for DialogFragment
    }


    public static dialogShoppingListSorting newInstance() {
        MyLog.i("dialogShoppingListSorting", "newInstance");
        return new dialogShoppingListSorting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogShoppingListSorting", "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("dialogShoppingListSorting", "onActivityCreated");
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnOK = mAlertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnOK.setTextSize(18);

                Button btnCancel = mAlertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnCancel.setTextSize(18);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MyLog.i("dialogShoppingListSorting", "onCreateDialog");
        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_shopping_list_sorting, null, false);

        // find the dialog's views
        rbAlphabetical = (RadioButton) view.findViewById(R.id.rbAlphabetical);
        rbReverseAlphabetical = (RadioButton) view.findViewById(R.id.rbReverseAlphabetical);
        rbByGroup = (RadioButton) view.findViewById(R.id.rbByGroup);

        // set the initial radio button
        switch (MySettings.getShoppingListSortOrder()) {
            case MySettings.SORT_ALPHABETICAL:
                rbAlphabetical.setChecked(true);
                break;
            case MySettings.SORT_REVERSE_ALPHABETICAL:
                rbReverseAlphabetical.setChecked(true);
                break;

            case MySettings.SORT_BY_GROUP:
                rbByGroup.setChecked(true);
                break;
        }

        // build the dialog
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.sortListDialog_title))
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.btnOk_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // set the appropriate shopping list sort order
                                if (rbAlphabetical.isChecked()) {
                                    MySettings.setShoppingListSortOrder(MySettings.SORT_ALPHABETICAL);
                                } else if (rbReverseAlphabetical.isChecked()) {
                                    MySettings.setShoppingListSortOrder(MySettings.SORT_REVERSE_ALPHABETICAL);
                                } else if (rbByGroup.isChecked()) {
                                    MySettings.setShoppingListSortOrder(MySettings.SORT_BY_GROUP);
                                }

                                switch (MySettings.getActiveFragmentID()) {
                                    case MySettings.FRAG_SHOPPING_LIST:
                                        switch (MySettings.getShoppingListSortOrder()) {
                                            case MySettings.SORT_ALPHABETICAL:
                                            case MySettings.SORT_REVERSE_ALPHABETICAL:
                                                EventBus.getDefault().post(new MyEvents.updateUI(null));
                                                break;

                                            case MySettings.SORT_BY_GROUP:
                                                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_SHOPPING_LIST_BY_GROUP));
                                                break;
                                        }
                                        break;

                                    case MySettings.FRAG_SHOPPING_LIST_BY_GROUP:
                                        switch (MySettings.getShoppingListSortOrder()) {
                                            case MySettings.SORT_ALPHABETICAL:
                                            case MySettings.SORT_REVERSE_ALPHABETICAL:
                                                EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_SHOPPING_LIST));
                                                break;

                                            case MySettings.SORT_BY_GROUP:
                                                // do nothing
                                               // EventBus.getDefault().post(new MyEvents.showFragment(MySettings.FRAG_SHOPPING_LIST_BY_GROUP));
                                                break;
                                        }
                                        break;
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

        return mAlertDialog;
    }


}
