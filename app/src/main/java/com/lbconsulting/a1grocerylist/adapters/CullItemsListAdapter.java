package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.ParseQueryAdapter;

import de.greenrobot.event.EventBus;

/**
 * This adapter shows items for the cull items fragment
 */
public class CullItemsListAdapter extends ParseQueryAdapter<Item> {

    Context mContext;
    LayoutInflater mInflater;

    public CullItemsListAdapter(Context context,
                                QueryFactory<Item> queryFactory) {
        super(context, queryFactory);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public void loadObjects() {
        super.loadObjects();
        MyLog.i("CullItemsListAdapter", "loadObjects");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        MyLog.i("CullItemsListAdapter", "notifyDataSetChanged");
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        MyLog.i("CullItemsListAdapter", "notifyDataSetInvalidated");
    }


    @Override
    public View getItemView(Item item, View view, ViewGroup parent) {
        CullItemsViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.row_cull_items_list, parent, false);
            holder = new CullItemsViewHolder();
            holder.ckBox = (CheckBox) view.findViewById(R.id.ckBox);
            view.setTag(holder);
        } else {
            holder = (CullItemsViewHolder) view.getTag();
        }

        CheckBox ckBox = holder.ckBox;

        ckBox.setText(item.getItemName());
        ckBox.setChecked(item.isChecked());
        if (item.isChecked()) {
            setChecked(ckBox);
        } else {
            setUnChecked(ckBox);
        }

        // save the item so it can be retrieved later
        ckBox.setTag(item);

        ckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                final Item item = (Item) v.getTag();
                // toggle the item's checked attribute
                // TODO: create cloud code??
                item.setChecked(!item.isChecked());
                //item.setItemDirty(true);

                // save the now dirty item to the local datastore
                //item.pinInBackground();

                // set the TextView's attributes to either Selected or Normal as appropriate
                if(item.isChecked()){
                    setChecked(checkBox);
                }else{
                    setUnChecked(checkBox);
                }

            }
        });

        ckBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Item item = (Item) v.getTag();
                MyLog.i("CullItemsListAdapter", "ckBox OnLongClick: Item " + item.getItemName());
                EventBus.getDefault().post(new MyEvents.showEditItemDialog(item.getItemID()));
                return true;
            }
        });

        return view;
    }


    private void setChecked(CheckBox ckBox) {
        ckBox.setTextColor(mContext.getResources().getColor(R.color.white));
        ckBox.setTypeface(null, Typeface.NORMAL);
    }

    private void setUnChecked(CheckBox ckBox) {
        ckBox.setTextColor(mContext.getResources().getColor(R.color.black));
        ckBox.setTypeface(null, Typeface.ITALIC);
    }

}

class CullItemsViewHolder {
    CheckBox ckBox;
}