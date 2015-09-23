package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.ItemLocation;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * An ArrayAdapter for displaying a store list.
 */
public class StoreListArrayAdapter extends ArrayAdapter<ItemLocation> {

    private Context mContext;
    private String mStoreName;

    public StoreListArrayAdapter(Context context, String storeName) {
        super(context, 0);
        this.mContext = context;
        this.mStoreName = storeName;
        MyLog.i("StoreListArrayAdapter", "Initialized for " + storeName);
    }

    public void setData(List<ItemLocation> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    @Override
    public int getPosition(ItemLocation soughtItem) {
        return getItemPosition(soughtItem.getItemID());
    }

    public int getItemPosition(String soughtItemID) {
        int position;
        boolean found = false;

        ItemLocation item;
        for (position = 0; position < getCount(); position++) {
            item = getItem(position);
            if (item.getItemID().equals(soughtItemID)) {
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
        StoreListViewHolder holder;

        // Get the data item for this position
        ItemLocation item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_store_list_item, parent, false);
            holder = new StoreListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (StoreListViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        holder.tvSelectedItem.setText(item.getItemNameWithNote());
        if (okToShowAisleSeparator(position)) {
            holder.tvItemsSeparator.setText(item.getLocationName());
            holder.tvItemsSeparator.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemsSeparator.setVisibility(View.GONE);
        }

        if (item.isStruckOut()) {
            setStrikeOut(holder.tvSelectedItem);
        } else {
            setNoStrikeOut(holder.tvSelectedItem);
        }

        // save the item so it can be retrieved later
        holder.tvSelectedItem.setTag(item);

        holder.tvSelectedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                final ItemLocation clickedItem = (ItemLocation) v.getTag();

                // toggle the item's struckOut attribute
                ItemLocation.toggleStrikeout(mContext, clickedItem);

                // set the TextView's attributes to either strikeout or normal as appropriate
                if (clickedItem.isStruckOut()) {
                    setStrikeOut(tv);
                } else {
                    setNoStrikeOut(tv);
                }
                EventBus.getDefault().post(new MyEvents.updateStoreListUI(mStoreName, clickedItem.getItem().getItemID()));
            }
        });

        holder.tvSelectedItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ItemLocation longClickedItem = (ItemLocation) v.getTag();
                // MyLog.i("ShoppingListAdapter", "tvSelectedItem OnLongClick: Item " + longClickedItem.getItemName());
                EventBus.getDefault().post(new MyEvents.showEditItemDialog(longClickedItem.getItemID()));
                return true;
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    public void setStrikeOut(TextView tv) {
        tv.setTypeface(null, Typeface.ITALIC);
        tv.setTextColor(mContext.getResources().getColor(R.color.black));
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void setNoStrikeOut(TextView tv) {
        tv.setTypeface(null, Typeface.NORMAL);
        tv.setTextColor(mContext.getResources().getColor(R.color.white));
        tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private boolean okToShowAisleSeparator(int position) {
        if (position == 0) {
            // This is the top of the list ... show separator
            return true;
        } else {
            int previousPosition = position - 1;
            String currentPositionLocationID = getItem(position).getLocationID();
            String previousPositionLocationID = getItem(previousPosition).getLocationID();
            return !currentPositionLocationID.equals(previousPositionLocationID);
        }
    }
}

class StoreListViewHolder {
    public LinearLayout rowLinearLayout;
    public TextView tvSelectedItem;
    public TextView tvItemsSeparator;

    public StoreListViewHolder(View base) {
        rowLinearLayout = (LinearLayout) base.findViewById(R.id.rowLinearLayout);
        tvSelectedItem = (TextView) base.findViewById(R.id.tvSelectedItem);
        tvItemsSeparator = (TextView) base.findViewById(R.id.tvItemsSeparator);
    }
}