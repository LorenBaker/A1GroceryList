package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds item data
 */

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Initial_Items")
public class Initial_Items extends ParseObject {
    //public static final String ITEM_ID = "objectId";
    public static final String ITEM_NAME = "itemName";
    public static final String GROUP = "group";

    public Initial_Items() {
        // A default constructor is required.
    }


    public String getItemID() {
        return getObjectId();
    }

    public String getItemName() {
        return getString(ITEM_NAME);
    }

    public void setItemName(String itemName) {
        put(ITEM_NAME, itemName);
    }


    public Group getGroup() {
        return (Group) get(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }


    public static ParseQuery<Initial_Items> getQuery() {
        return ParseQuery.getQuery(Initial_Items.class);
    }

    @Override
    public String toString() {
        return getItemName();
    }

}


