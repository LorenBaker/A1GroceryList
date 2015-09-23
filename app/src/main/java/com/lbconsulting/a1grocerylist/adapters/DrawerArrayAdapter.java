package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;

/**
 * An array adapter for showing the app's navigation drawer list view
 */
public class DrawerArrayAdapter extends ArrayAdapter<String> {
    private Context mContext;

    public DrawerArrayAdapter(Context context, String[] drawerItemTitles) {
        super(context, 0, drawerItemTitles);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String drawerTitle = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_simple_list, parent, false);
        }
        // Lookup view for data population
        TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
        // Populate the data into the template view using the data object
        tvText.setText(drawerTitle);
                // Return the completed view to render on screen
        return convertView;
    }
}
