package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.database.StoreChain;

import java.util.List;


public class StoreChainSpinnerArrayAdapter extends ArrayAdapter<StoreChain> {
    Context mContext;
    List<StoreChain> mStoreChains;

    public StoreChainSpinnerArrayAdapter(Context context, List<StoreChain> storeChains) {
        super(context, 0, storeChains);
        mContext = context;
        mStoreChains = storeChains;
    }

    @Override
    public int getPosition(StoreChain soughtStoreChain) {
        int position = 0;
        boolean found = false;
        for (StoreChain storeChain : mStoreChains) {
            if (storeChain.getStoreChainID().equals(soughtStoreChain.getStoreChainID())) {
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

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StoreChain storechain = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_simple_list, parent, false);
        }
        // Lookup view for data population
        TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
        // Populate the data into the template view using the data object
        tvText.setText(storechain.getStoreChainName());
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StoreChain storeChain = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_simple_list, parent, false);
        }
        // Lookup view for data population
        TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
        // Populate the data into the template view using the data object
        tvText.setText(storeChain.getStoreChainName());
        // Return the completed view to render on screen
        return convertView;
    }

    public StoreChain getStoreChain(int position) {
        return getItem(position);
    }


}