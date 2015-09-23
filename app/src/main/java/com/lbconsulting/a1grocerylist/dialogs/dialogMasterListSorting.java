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
 * A dialog where the user selects how to sort the Master List
 */
public class dialogMasterListSorting extends DialogFragment {

    private RadioButton rbAlphabetical;
    private RadioButton rbFavoritesFirst;
    private RadioButton rbDateUpdated;
    private RadioButton rbManual;
    private RadioButton rbReverseAlphabetical;
    private RadioButton rbSelectedFirst;

    private AlertDialog mAlertDialog;

    public dialogMasterListSorting() {
        // Empty constructor required for DialogFragment
    }


    public static dialogMasterListSorting newInstance() {
        MyLog.i("dialogMasterListSorting", "newInstance");
        return new dialogMasterListSorting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("dialogMasterListSorting", "onCreate");
           }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyLog.i("dialogMasterListSorting", "onActivityCreated");
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
        MyLog.i("dialogMasterListSorting", "onCreateDialog");
        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_master_list_sorting, null, false);

        // find the dialog's views
        rbAlphabetical = (RadioButton) view.findViewById(R.id.rbAlphabetical);
        rbReverseAlphabetical = (RadioButton) view.findViewById(R.id.rbReverseAlphabetical);
        rbFavoritesFirst = (RadioButton) view.findViewById(R.id.rbFavoritesFirst);
        rbDateUpdated = (RadioButton) view.findViewById(R.id.rbDateUpdated);
        rbManual = (RadioButton) view.findViewById(R.id.rbManual);
        rbSelectedFirst = (RadioButton) view.findViewById(R.id.rbSelectedFirst);

        // TODO: Enable FavoritesFirst, DateUpdated, Manual, and SelectedFirst Sorting
        rbFavoritesFirst.setEnabled(false);
        rbDateUpdated.setEnabled(false);
        rbManual.setEnabled(false);
        rbSelectedFirst.setEnabled(false);

        switch (MySettings.getMasterListSortOrder()){
            case MySettings.SORT_ALPHABETICAL:
                rbAlphabetical.setChecked(true);
                break;
            case MySettings.SORT_REVERSE_ALPHABETICAL:
                rbReverseAlphabetical.setChecked(true);
                break;
            case MySettings.SORT_FAVORITES_FIRST:
                rbFavoritesFirst.setChecked(true);
                break;
            case MySettings.SORT_DATE_UPDATED:
                rbDateUpdated.setChecked(true);
                break;
            case MySettings.SORT_MANUALLY:
                rbManual.setChecked(true);
                break;
            case MySettings.SORT_SELECTED_FIRST:
                rbSelectedFirst.setChecked(true);
                break;
        }

        // build the dialog
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.sortListDialog_title))
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.btnOk_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (rbAlphabetical.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_ALPHABETICAL);
                                } else if (rbReverseAlphabetical.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_REVERSE_ALPHABETICAL);
                                }else if (rbFavoritesFirst.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_FAVORITES_FIRST);
                                }else if (rbDateUpdated.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_DATE_UPDATED);
                                }else if (rbManual.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_MANUALLY);
                                }else if (rbSelectedFirst.isChecked()) {
                                    MySettings.setMasterListSortOrder(MySettings.SORT_SELECTED_FIRST);
                                }

                                EventBus.getDefault().post(new MyEvents.updateUI(null));
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
