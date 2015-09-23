package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.ParseQueryAdapter;

import de.greenrobot.event.EventBus;

/**
 * This adapter shows the master items list
 */
public class ShoppingListAdapter extends ParseQueryAdapter<Item> {

    Context mContext;
    LayoutInflater mInflater;

    public ShoppingListAdapter(Context context,
                               QueryFactory<Item> queryFactory) {
        super(context, queryFactory);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void setObjectsPerPage(int objectsPerPage) {
        super.setObjectsPerPage(objectsPerPage);
    }

    @Override
    public Item getItem(int index) {
        return super.getItem(index);
    }

    @Override
    public void loadObjects() {
        super.loadObjects();
        MyLog.i("ShoppingListAdapter", "loadObjects");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        MyLog.i("ShoppingListAdapter", "notifyDataSetChanged");
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        MyLog.i("ShoppingListAdapter", "notifyDataSetInvalidated");
    }

    @Override
    public void addOnQueryLoadListener(OnQueryLoadListener<Item> listener) {
        super.addOnQueryLoadListener(listener);
        MyLog.i("ShoppingListAdapter", "addOnQueryLoadListener");
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getItemView(Item item, View view, ViewGroup parent) {
        ShoppingListViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.row_shopping_list, parent, false);
            holder = new ShoppingListViewHolder();
            holder.tvSelectedItem = (TextView) view.findViewById(R.id.tvSelectedItem);
            view.setTag(holder);
        } else {
            holder = (ShoppingListViewHolder) view.getTag();
        }

        TextView tvSelectedItem = holder.tvSelectedItem;
        String selectedItemText = item.getItemName();
        if(!item.getItemNote().isEmpty()){
            selectedItemText = selectedItemText+" (" +item.getItemNote()+")";
        }
        tvSelectedItem.setText(selectedItemText);

        if (item.isStruckOut()) {
            setStrikeOut(tvSelectedItem);
        } else {
            setNoStrikeOut(tvSelectedItem);
        }

        // save the item so it can be retrieved later
        tvSelectedItem.setTag(item);

        tvSelectedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                final Item item = (Item) v.getTag();

                // toggle the item's struckOut attribute
                Item.setStruckOut(mContext,item,!item.isStruckOut());

                // save the now dirty item to the local datastore
                //item.pinInBackground();

                // set the TextView's attributes to either strikeout or normal as appropriate
                if(item.isStruckOut()){
                    setStrikeOut(tv);
                } else{
                    setNoStrikeOut(tv);
                }
            }
        });

        tvSelectedItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Item item = (Item) v.getTag();
                MyLog.i("ShoppingListAdapter", "tvSelectedItem OnLongClick: Item " + item.getItemName());
                EventBus.getDefault().post(new MyEvents.showEditItemDialog(item.getItemID()));
                return true;
            }
        });

        return view;
    }

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

}
class ShoppingListViewHolder {
    TextView tvSelectedItem;
}

