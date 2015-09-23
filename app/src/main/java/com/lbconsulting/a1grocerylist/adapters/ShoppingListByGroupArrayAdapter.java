package com.lbconsulting.a1grocerylist.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Item;
import com.lbconsulting.a1grocerylist.database.Store;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * An ArrayAdapter for displaying a shopping list sorted by Group.
 */
public class ShoppingListByGroupArrayAdapter extends ArrayAdapter<Item> {

    private final Activity mActivity;
    private Context mContext;
    //    private boolean mIsMappingStore;
    private Store mStore;
    private List<StoreMapEntry> mStoreMap;
    private Item mItem;
//    private boolean mShowItems = true;

    public ShoppingListByGroupArrayAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
        mActivity = (Activity) context;
//        this.mIsMappingStore = false;
        this.mStore = null;
        MyLog.i("ShoppingListByGroupArrayAdapter", "Initialized.");
    }

    public void setData(List<Item> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

//    public void setShowItems(boolean showItems) {
//        mShowItems = showItems;
//    }

    @Override
    public int getPosition(Item soughtItem) {
        return getItemPosition(soughtItem.getItemID());
    }

    public int getItemPosition(String soughtItemID) {
        int position;
        boolean found = false;

        Item item;
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
        mItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_store_list_item, parent, false);
            holder = new StoreListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (StoreListViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
//        holder.rowLinearLayout.setVisibility(View.VISIBLE);
//        holder.rowLinearLayout.setPadding(10, 10, 10, 10);
//        boolean showSeparator = okToShowGroupSeparator(position);
        displayItem(holder, true);
        displaySeparator(holder, okToShowGroupSeparator(position));

        holder.tvSelectedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mIsMappingStore) {
//                    return;
//                }
                TextView tv = (TextView) v;
                final Item clickedItem = (Item) v.getTag();

                // toggle the item's struckOut attribute
                Item.setStruckOut(mContext, clickedItem, !clickedItem.isStruckOut());

                // set the TextView's attributes to either strikeout or normal as appropriate
                if (clickedItem.isStruckOut()) {
                    setStrikeOut(tv);
                } else {
                    setNoStrikeOut(tv);
                }
            }
        });

        holder.tvSelectedItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                if (mIsMappingStore) {
//                    return true;
//                }
                Item longClickedItem = (Item) v.getTag();
                MyLog.i("ShoppingListByGroupArrayAdapter", "tvSelectedItem OnLongClick: Item " + longClickedItem.getItemName());
                EventBus.getDefault().post(new MyEvents.showEditItemDialog(longClickedItem.getItemID()));
                return true;
            }
        });

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

    private void displaySeparator(StoreListViewHolder holder, boolean showSeparator) {
        if (showSeparator) {
            // show separator
            String separatorText = mItem.getGroup().getGroupName();
//            if (mIsMappingStore) {
//                separatorText = separatorText + " - " + getItemLocation(mItem.getGroup());
//            }
            holder.tvItemsSeparator.setText(separatorText);
            holder.tvItemsSeparator.setVisibility(View.VISIBLE);
            holder.tvItemsSeparator.setTag(mItem.getGroup());
        } else {
            // hide separator
            holder.tvItemsSeparator.setVisibility(View.GONE);
            holder.tvItemsSeparator.setTag(null);
        }

    }

    private void displayItem(StoreListViewHolder holder, boolean showItem) {

        if (showItem) {
            // show item
            holder.tvSelectedItem.setText(mItem.getItemNameWithNote());
            if (mItem.isStruckOut()) {
                setStrikeOut(holder.tvSelectedItem);
            } else {
                setNoStrikeOut(holder.tvSelectedItem);
            }
            holder.tvSelectedItem.setVisibility(View.VISIBLE);
//            holder.tvSelectedItem.setPadding(10, 1, 1, 0);
            holder.tvSelectedItem.setTag(mItem);
        } else {
            // hide item
            holder.tvSelectedItem.setVisibility(View.GONE);
//            holder.tvSelectedItem.setPadding(0, 0, 0, 0);
            holder.tvSelectedItem.setTag(null);
        }

    }

    //    public void updateSeparatorText(){
//        String separatorText = mItem.getGroup().getGroupName();
//        if (mIsMappingStore) {
//            separatorText = separatorText + " - " + getItemLocation(mItem.getGroup());
//        }
//    }
    private void setStrikeOut(TextView tv) {
        tv.setTypeface(null, Typeface.ITALIC);
        tv.setTextColor(mContext.getResources().getColor(R.color.black));
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setNoStrikeOut(TextView tv) {
        tv.setTypeface(null, Typeface.NORMAL);
        tv.setTextColor(mContext.getResources().getColor(R.color.white));
        tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }


    private boolean okToShowGroupSeparator(int position) {
        if (position == 0) {
            // This is the top of the list ... show separator
            return true;
        } else {
            int previousPosition = position - 1;
            String currentPositionGroupID = getItem(position).getGroupID();
            String previousPositionGroupID = getItem(previousPosition).getGroupID();
            return !currentPositionGroupID.equals(previousPositionGroupID);
        }
    }

//    public void setMappingMode(boolean isMappingStore, Store store) {
//        mIsMappingStore = isMappingStore;
//        mStore = store;
//        mStoreMap = null;
//        if (store != null) {
//            mStoreMap = StoreMapEntry.getStoreMap(store);
//            notifyDataSetChanged();
//        }
//    }

//    private String getItemLocation(Group group) {
//        String groupID = group.getGroupID();
//        String itemLocation = "";
//        if (mStoreMap != null) {
//            for (StoreMapEntry entry : mStoreMap) {
//                if (entry.getGroup().getGroupID().equals(groupID)) {
//                    itemLocation = entry.getLocation().getLocationName();
//                    break;
//                }
//            }
//        }
//        return itemLocation;
//    }
}


