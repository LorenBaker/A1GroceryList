package com.lbconsulting.a1grocerylist.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.lbconsulting.a1grocerylist.R;
import com.lbconsulting.a1grocerylist.classes.MyEvents;
import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.lbconsulting.a1grocerylist.database.Item;
import com.parse.ParseQueryAdapter;

import de.greenrobot.event.EventBus;

/**
 * This adapter shows the master items list
 */
public class MasterItemsListAdapter extends ParseQueryAdapter<Item> {

    Context mContext;
    LayoutInflater mInflater;

    public MasterItemsListAdapter(Context context,
                                  ParseQueryAdapter.QueryFactory<Item> queryFactory) {
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
        MyLog.i("MasterItemsListAdapter", "loadObjects");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        MyLog.i("MasterItemsListAdapter", "notifyDataSetChanged");
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        MyLog.i("MasterItemsListAdapter", "notifyDataSetInvalidated");
    }

    @Override
    public void addOnQueryLoadListener(OnQueryLoadListener<Item> listener) {
        super.addOnQueryLoadListener(listener);
        MyLog.i("MasterItemsListAdapter", "addOnQueryLoadListener");
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getItemView(Item item, View view, ViewGroup parent) {
        MasterItemViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.row_master_list_item_list, parent, false);
            holder = new MasterItemViewHolder();
            holder.ckBox = (CheckBox) view.findViewById(R.id.ckBox);
            holder.btnFavorite = (ImageButton) view.findViewById(R.id.btnFavorite);
            view.setTag(holder);
        } else {
            holder = (MasterItemViewHolder) view.getTag();
        }

        CheckBox ckBox = holder.ckBox;
        ImageButton btnFavorite = holder.btnFavorite;

        ckBox.setText(item.getItemName());
        ckBox.setChecked(item.isSelected());
        if (item.isSelected()) {
            setSelected(ckBox);
        } else {
            setDeselect(ckBox);
        }

        if (item.isFavorite()) {
            setAsFavorite(btnFavorite);
        } else {
            setAsNotFavorite(btnFavorite);
        }

        // save the item so it can be retrieved later
        ckBox.setTag(item);
        btnFavorite.setTag(item);

        ckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                final Item item = (Item) v.getTag();
                // toggle the item's selection attribute
                Item.setSelected(mContext, item, !item.isSelected());

                // set the TextView's attributes to either Selected or Normal as appropriate
                if (item.isSelected()) {
                    setSelected(checkBox);
                } else {
                    setDeselect(checkBox);
                }

            }
        });

        ckBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Item item = (Item) v.getTag();
                MyLog.i("MasterItemsListAdapter", "ckBox OnLongClick: Item " + item.getItemName());
                EventBus.getDefault().post(new MyEvents.showEditItemDialog(item.getItemID()));
                return true;
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton btn = (ImageButton) v;
                final Item item = (Item) v.getTag();
                MyLog.i("MasterItemsListAdapter", "btnFavorite onClick: Item " + item.getItemName());
                // toggle the item's favorite attribute
                item.setFavorite(!item.isFavorite());
                item.setItemDirty(true);

                // save the now dirty item to the local datastore
                item.pinInBackground();

                // set the ImageButton's attributes to either Favorite or NotFavorite as appropriate
                if (item.isFavorite()) {
                    setAsFavorite(btn);
                } else {
                    setAsNotFavorite(btn);
                }

            }
        });


        return view;
    }

    private void setAsFavorite(ImageButton btnFavorite) {
        btnFavorite.setImageResource(R.drawable.ic_action_favorite_light);
    }

    private void setAsNotFavorite(ImageButton btnFavorite) {
        btnFavorite.setImageResource(R.drawable.ic_action_favorite_dark);
    }

    private void setSelected(CheckBox ckBox) {
        ckBox.setTextColor(mContext.getResources().getColor(R.color.white));
        ckBox.setTypeface(null, Typeface.NORMAL);
    }

    private void setDeselect(CheckBox ckBox) {
        ckBox.setTextColor(mContext.getResources().getColor(R.color.black));
        ckBox.setTypeface(null, Typeface.ITALIC);
    }

}

class MasterItemViewHolder {
    CheckBox ckBox;
    ImageButton btnFavorite;
}