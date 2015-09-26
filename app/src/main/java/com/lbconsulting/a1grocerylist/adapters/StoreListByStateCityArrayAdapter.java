package com.lbconsulting.a1grocerylist.adapters;

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

    private final Context mContext;

    public StoreListByStateCityArrayAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
        MyLog.i("StoreListByStateCityArrayAdapter", "Initialized.");
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

    private int getStorePosition(String soughtItemID) {
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
        Store store = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_store_list_by_state_city, parent, false);
            holder = new StoreViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (StoreViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        holder.ckBoxStoreName.setText(store.getStoreChainAndRegionalName());
        holder.ckBoxStoreName.setChecked(store.isChecked());

        if (holder.ckBoxStoreName.isChecked()) {
            setChecked(holder);
        } else {
            setUnChecked(holder);
        }

        String address = store.getAddress1();
        address += System.getProperty("line.separator") + store.getCity()
                + " ," + store.getState() + " " + store.getZip();
        holder.tvStoreAddress.setText(address);

        if (okToShowStoreStateSeparator(position)) {
            holder.tvStoreSeparator.setText(store.getState());
            holder.tvStoreSeparator.setVisibility(View.VISIBLE);
        } else {
            holder.tvStoreSeparator.setVisibility(View.GONE);
        }

        // save the item so it can be retrieved later
        holder.ckBoxStoreName.setTag(store);

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

    public void toggleChecked(View view, int position) {
        Store store = getItem(position);
        boolean isChecked = !store.isChecked();
        store.setChecked(isChecked);
        StoreViewHolder holder = new StoreViewHolder(view);
        if (isChecked) {
            setChecked(holder);
        } else {
            setUnChecked(holder);
        }
    }
}


class StoreViewHolder {

    public final CheckBox ckBoxStoreName;
    public final TextView tvStoreAddress;
    public final TextView tvStoreSeparator;

    public StoreViewHolder(View rootView) {
        ckBoxStoreName = (CheckBox) rootView.findViewById(R.id.ckBoxStoreName);
        tvStoreAddress = (TextView) rootView.findViewById(R.id.tvStoreAddress);
        tvStoreSeparator = (TextView) rootView.findViewById(R.id.tvItemsSeparator);
    }


}
