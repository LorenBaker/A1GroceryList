package com.lbconsulting.a1grocerylist.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Store;

import java.util.List;

/**
 * An ArrayAdapter for displaying a shopping list sorted by Group.
 */
public class StoreListByStateCityArrayAdapter extends ArrayAdapter<Store> {

    private final Activity mActivity;
    private Context mContext;
    private Store mStore;

    public StoreListByStateCityArrayAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
        mActivity = (Activity) context;
        MyLog.i("ShoppingListByGroupArrayAdapter", "Initialized.");
    }

    public void setData(List<Store> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    @Override
    public int getPosition(Store soughtStore) {
        return getStorePosition(soughtStore.getStoreID());
    }

    public int getStorePosition(String soughtItemID) {
        int position;
        boolean found = false;

        Store store;
        for (position = 0; position < getCount(); position++) {
            store = getItem(position);
            if (store.getStoreID().equals(soughtItemID)) {
                found = true;
                break;
            }
        }

        if (!found) {
            position = 0;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StoreViewHolder holder;
        // Get the data item for this position
        mStore = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_store_list_by_state_city, parent, false);
            holder = new StoreViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (StoreViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        holder.ckBoxStoreName.setText(mStore.getStoreChainAndRegionalName());
        holder.ckBoxStoreName.setChecked(mStore.isChecked());

        if (holder.ckBoxStoreName.isChecked()) {
            setChecked(holder);
        } else {
            setUnChecked(holder);
        }

        String address = mStore.getAddress1();
        address += System.getProperty("line.separator") + mStore.getCity()
                + " ," + mStore.getState() + " " + mStore.getZip();
        holder.tvStoreAddress.setText(address);

        if (okToShowStoreStateSeparator(position)) {
            holder.tvStoreSeparator.setText(mStore.getState());
            holder.tvStoreSeparator.setVisibility(View.VISIBLE);
        } else {
            holder.tvStoreSeparator.setVisibility(View.GONE);
        }

        // save the item so it can be retrieved later
        holder.ckBoxStoreName.setTag(mStore);

//        holder.ckBoxStoreName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CheckBox ckBox = (CheckBox) v;
//                final Store clickedItem = (Store) v.getTag();
//                if (ckBox.isChecked()) {
//                    setChecked(ckBox);
//                } else {
//                    setUnChecked(ckBox);
//                }
//            }
//        });
//
//        holder.tvSelectedItem.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mIsMappingStore) {
//                    return true;
//                }
//                Item longClickedItem = (Item) v.getTag();
//                MyLog.i("ShoppingListByGroupArrayAdapter", "tvSelectedItem OnLongClick: Item " + longClickedItem.getItemName());
//                EventBus.getDefault().post(new MyEvents.showEditItemDialog(longClickedItem.getItemID()));
//                return true;
//            }
//        });
//
//        holder.tvItemsSeparator.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mIsMappingStore) {
//                    return;
//                }
//                // show locations dialog
//                Group group = (Group) v.getTag();
//
//                if (group != null && mStore != null) {
//                    FragmentManager fm = mActivity.getFragmentManager();
//                    dialogGroupLocation dialog = dialogGroupLocation.newInstance(mStore.getStoreID(), group.getGroupID());
//                    dialog.show(fm, "dialogGroupLocation");
//                } else {
//                    MyLog.e("ShoppingListByGroupArrayAdapter", "onClick: tvStoreSeparator store or group is null!");
//                }
//
//            }
//        });

        // Return the completed view to render on screen
        return convertView;
    }

    private boolean okToShowStoreStateSeparator(int position) {
        if (position == 0) {
            // This is the top of the list ... show separator
            return true;
        } else {
            int previousPosition = position - 1;
            String currentPositionState = getItem(position).getState();
            String previousPositionState = getItem(previousPosition).getState();
            return !currentPositionState.equals(previousPositionState);
        }
    }


    private void setChecked(StoreViewHolder holder) {
        holder.ckBoxStoreName.setChecked(true);
        holder.ckBoxStoreName.setTextColor(mContext.getResources().getColor(R.color.white));
        holder.ckBoxStoreName.setTypeface(null, Typeface.NORMAL);

        holder.tvStoreAddress.setTextColor(mContext.getResources().getColor(R.color.greyLight1));
        holder.tvStoreAddress.setTypeface(null, Typeface.ITALIC);
    }

    private void setUnChecked(StoreViewHolder holder) {
        holder.ckBoxStoreName.setChecked(false);
        holder.ckBoxStoreName.setTextColor(mContext.getResources().getColor(R.color.black));
        holder.ckBoxStoreName.setTypeface(null, Typeface.ITALIC);

        holder.tvStoreAddress.setTextColor(mContext.getResources().getColor(R.color.black));
        holder.tvStoreAddress.setTypeface(null, Typeface.ITALIC);
    }

    public void toggleChecked(View view, int position){
        Store store = getItem(position);
        boolean isChecked = !store.isChecked();
        store.setChecked(isChecked);
        StoreViewHolder holder = new StoreViewHolder(view);
        if(isChecked){
            setChecked(holder);
        }else{
            setUnChecked(holder);
        }
    }
}



class StoreViewHolder {

    public  CheckBox ckBoxStoreName;
    public  TextView tvStoreAddress;
    public TextView tvStoreSeparator;

    public StoreViewHolder(View rootView) {
        ckBoxStoreName = (CheckBox) rootView.findViewById(R.id.ckBoxStoreName);
        tvStoreAddress = (TextView) rootView.findViewById(R.id.tvStoreAddress);
        tvStoreSeparator = (TextView) rootView.findViewById(R.id.tvItemsSeparator);
    }


}
