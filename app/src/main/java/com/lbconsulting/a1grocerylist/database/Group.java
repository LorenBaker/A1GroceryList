package com.lbconsulting.a1grocerylist.database;

/**
 * a ParseObject that holds Group data
 */

import com.lbconsulting.a1grocerylist.classes.MyLog;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Group")
public class Group extends ParseObject {
    //private static final String GROUP_ID = "objectId";
    public static final String AUTHOR = "author";
    public static final String GROUP_NAME = "groupName";
    public static final String IS_CHECKED = "isChecked";
    public static final String IS_DEFAULT = "isDefault";
    public static final String IS_DIRTY = "isDirty";
    public static final String SORT_KEY = "sortKey";


    public Group() {
        // A default constructor is required.
    }

    public void setGroup(String groupName, long sortKey) {
        groupName = groupName.trim();
        if (!groupName.isEmpty()) {
            setGroupName(groupName);
            setSortKey(sortKey);
            setAuthor(ParseUser.getCurrentUser());
        }
    }

    public String getGroupID() {
        return getObjectId();
    }

    public long getSortKey() {
        return getLong(SORT_KEY);
    }

    public void setSortKey(long sortKey) {
        put(SORT_KEY, sortKey);
    }

    public String getGroupName() {
        return getString(GROUP_NAME);
    }

    public void setGroupName(String groupName) {
        put(GROUP_NAME, groupName);
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR);
    }

    public void setAuthor(ParseUser currentUser) {
        put(AUTHOR, currentUser);
    }

    public boolean isChecked() {
        return getBoolean(IS_CHECKED);
    }

    public void setChecked(boolean isChecked) {
        put(IS_CHECKED, isChecked);
    }

    public boolean isGroupDirty() {
        return getBoolean(IS_DIRTY);
    }

    public void setGroupDirty(boolean groupDirty) {
        put(IS_DIRTY, groupDirty);
    }

    public boolean isDefault() {
        return getBoolean(IS_DEFAULT);
    }

    public void setDefault(boolean defaultGroup) {
        put(IS_DEFAULT, defaultGroup);
    }

    public static ParseQuery<Group> getQuery() {
        return ParseQuery.getQuery(Group.class);
    }

    @Override
    public String toString() {
        return getGroupName();
    }

    public static Group getGroup(String objectId) {
        Group group = null;
        try {
            ParseQuery<Group> query = getQuery();
            query.whereEqualTo("objectId", objectId);
            query.fromLocalDatastore();
            group = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("Group", "getGroup: ParseException: " + e.getMessage());
        }
        return group;
    }

    public static List<Group> getAllGroups() {
        List<Group> allGroups = new ArrayList<>();
        try {
            ParseQuery<Group> query = getQuery();
            query.orderByAscending(SORT_KEY);
            query.fromLocalDatastore();
            allGroups = query.find();
        } catch (ParseException e) {
            MyLog.e("Group", "getAllGroups: ParseException: " + e.getMessage());
        }
        return allGroups;
    }

    public static List<Group> getAllGroupsWithoutDefault() {
        List<Group> allGroups = new ArrayList<>();
        try {
            ParseQuery<Group> query = getQuery();
            query.whereNotEqualTo(GROUP_NAME,"[No Group]");
            query.orderByAscending(GROUP_NAME);
            query.fromLocalDatastore();
            allGroups = query.find();
        } catch (ParseException e) {
            MyLog.e("Group", "getAllGroups: ParseException: " + e.getMessage());
        }
        return allGroups;
    }

    public static Group getDefaultGroup() {
        Group group = null;
        try {
            ParseQuery<Group> query = getQuery();
            query.whereEqualTo(IS_DEFAULT, true);
            query.fromLocalDatastore();
            group = query.getFirst();
        } catch (ParseException e) {
            MyLog.e("Group", "getDefaultGroup: ParseException: " + e.getMessage());
        }
        return group;
    }


    public static void unPinAll() {
        try {
            List<Group> allGroups =getAllGroups();
            ParseObject.unpinAll(allGroups);
        } catch (ParseException e) {
            MyLog.e("Group", "unPinAll: ParseException" + e.getMessage());
        }
    }

}


