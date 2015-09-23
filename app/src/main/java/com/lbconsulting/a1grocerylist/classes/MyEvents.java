package com.lbconsulting.a1grocerylist.classes;

/**
 * EventBus events.
 */
public class MyEvents {

    public static class syncA1GroceryListData {
        final int mAction;

        public syncA1GroceryListData(int action) {
            mAction = action;
        }

        public int getAction() {
            return mAction;
        }
    }

    public static class updateUI {
        final String mItemID;

        public updateUI(String itemID) {
            this.mItemID = itemID;
        }

        public String getItemID() {
            return mItemID;
        }
    }

    public static class updateStoreChainSpinner {
        public updateStoreChainSpinner() {
        }
    }

    public static class updateSeparatorText {
        public updateSeparatorText() {
        }
    }

    public static class selectedStore {
        String mSelectedStoreID;

        public selectedStore(String selectedStoreID) {
            mSelectedStoreID = selectedStoreID;
        }

        public String getSelectedStoreID() {
            return mSelectedStoreID;
        }
    }

    public static class updateStoreListUI {
        String mStoreName;
        String mItemID;

        public updateStoreListUI(String storeName, String itemID) {
            mStoreName = storeName;
            mItemID = itemID;
        }

        public String getStoreName() {
            return mStoreName;
        }

        public String getItemID() {
            return mItemID;
        }
    }

    public static class updateStoreName {
        String mStoreName;
        String mStoreID;

        public updateStoreName(String storeName, String storeID) {
            mStoreName = storeName;
            mStoreID = storeID;
        }

        public String getStoreName() {
            return mStoreName;
        }

        public String getStoreID() {
            return mStoreID;
        }
    }

    public static class setActionBarTitle {
        final String mTitle;

        public setActionBarTitle(String title) {
            mTitle = title;
        }

        public String getTitle() {
            return mTitle;
        }
    }

    public static class showOkDialog {
        final String mTitle;
        final String mMessage;

        public showOkDialog(String title, String message) {
            mTitle = title;
            mMessage = message;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getMessage() {
            return mMessage;
        }
    }

    public static class showToast {
        final String mMessage;

        public showToast(String message) {
            mMessage = message;
        }

        public String getMessage() {
            return mMessage;
        }
    }

    public static class uploadDirtyObjects {
        public uploadDirtyObjects() {
        }
    }

    public static class showEditItemDialog {
        String mItemID;

        public showEditItemDialog(String itemID) {
            mItemID = itemID;
        }

        public String getItemID() {
            return mItemID;
        }
    }

    public static class showFragment {
        int mFragmentID;

        public showFragment(int fragmentID) {
            mFragmentID = fragmentID;
        }

        public int getFragmentID() {
            return mFragmentID;
        }

    }

    public static class setBackgroundColor {

        int mBackgroundColor;

        public setBackgroundColor(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }

        public int getBackgroundColor() {
            return mBackgroundColor;
        }
    }
}
