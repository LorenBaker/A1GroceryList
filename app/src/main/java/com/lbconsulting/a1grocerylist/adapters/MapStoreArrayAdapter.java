package com.lbconsulting.a1grocerylist.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Group;
import com.lbconsulting.a1grocerylist.database.StoreMapEntry;

import java.util.List;

/**
 * An ArrayAdapter for displaying a store's groups to map.
 */
public class MapStoreArrayAdapter extends ArrayAdapter<StoreMapEntry> {

    private final Activity mActivity;
    private Context mContext;
//    private Store mStore;
    private List<StoreMapEntry> mStoreMap;
//    private Group mGroup;

    public MapStoreArrayAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
        mActivity = (Activity) context;
//        this.mStore = null;
        MyLog.i("MapStoreArrayAdapter", "Initialized.");
    }

    public void setData(List<StoreMapEntry> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

//    public void setStore(String storeID) {
//        mStore = Store.getStore(storeID);
//    }

    @Override
    public int getPosition(StoreMapEntry soughtStoreMapEntry) {
        return getStoreMapEntryPosition(soughtStoreMapEntry.getStoreMapEntryID());
    }

    public int getStoreMapEntryPosition(String soughtStoreMapEntryID) {
        int position;
        boolean found = false;

        StoreMapEntry entry;
        for (position = 0; position < getCount(); position++) {
            entry = getItem(position);
            if (entry.getStoreMapEntryID().equals(soughtStoreMapEntryID)) {
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
        MapStoreHolder holder;
        StoreMapEntry entry = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_map_store, parent, false);
            holder = new MapStoreHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MapStoreHolder) convertView.getTag();
        }

        String group = entry.getGroup().getGroupName();
        String location = entry.getLocation().getLocationName();
        holder.tvGroup.setText(group);
        holder.tvGroup.setTag(entry);

        holder.tvLocation.setText(location);
        if(location.startsWith("[")){
            holder.tvLocation.setTextColor(mContext.getResources().getColor(R.color.yellow));
        }else{
            holder.tvLocation.setTextColor(mContext.getResources().getColor(R.color.white));
        }

//        holder.tvGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                TextView tv = (TextView) v.findViewById(R.id.tvGroup);
//                final StoreMapEntry clickedEntry = (StoreMapEntry) tv.getTag();
//                if (clickedEntry != null) {
//                    FragmentManager fm = mActivity.getFragmentManager();
//                    dialogGroupLocation dialog = dialogGroupLocation.newInstance(
//                            clickedEntry.getStore().getStoreID(), clickedEntry.getGroup().getGroupID());
//                    dialog.show(fm, "dialogGroupLocation");
//                } else {
//                    MyLog.e("MapStoreArrayAdapter", "onClick: tvStoreSeparator store or group is null!");
//                }
//            }
//        });
//
//        holder.tvLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        // Return the completed view to render on screen
        return convertView;
    }


    private String getItemLocation(Group group) {
        String groupID = group.getGroupID();
        String itemLocation = "";
        if (mStoreMap != null) {
            for (StoreMapEntry entry : mStoreMap) {
                if (entry.getGroup().getGroupID().equals(groupID)) {
                    itemLocation = entry.getLocation().getLocationName();
                    break;
                }
            }
        }
        return itemLocation;
    }

}

class MapStoreHolder {
    public TextView tvGroup;
    public TextView tvLocation;

    public MapStoreHolder(View base) {
        tvGroup = (TextView) base.findViewById(R.id.tvGroup);
        tvLocation = (TextView) base.findViewById(R.id.tvLocation);
    }
}

