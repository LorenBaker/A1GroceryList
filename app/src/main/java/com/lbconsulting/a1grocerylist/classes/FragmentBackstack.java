package com.lbconsulting.a1grocerylist.classes;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * A class that holds the fragment ID stack
 */
public class FragmentBackstack {

    private Stack<Integer> mStack = new Stack<>();

    public FragmentBackstack() {
        fillStack();
    }

    public int pop() {
        int previousFragmentID = 0;

        try {
            mStack.pop();
            if (!mStack.empty()) {
                previousFragmentID = mStack.peek();
            }

            if (isEmpty()) {
                mStack.clear();
            }
        } catch (EmptyStackException e) {
            MyLog.e("FragmentBackstack", "pop: EmptyStackException" + e.getMessage());
        }
        return previousFragmentID;
    }

    public void push(int fragmentID) {
        if (mStack.empty()) {
            mStack.push(fragmentID);
        } else {
            // the stack has fragment ID ...
            // so make sure that you're not repeating the top most fragment ID
            if (fragmentID != mStack.peek()) {
                mStack.push(fragmentID);
            }
        }
    }

    public void save() {
        if (mStack.empty()) {
            MySettings.setFragmentBackstack(MySettings.NOT_AVAILABLE);
        } else {
            Object[] items = mStack.toArray();
            String backstackString = Arrays.toString(items);
            MySettings.setFragmentBackstack(backstackString);
        }
    }

    private void fillStack() {
        String backstackString = MySettings.getFragmentBackstack();
        MyLog.i("FragmentBackstack", "fillStack: " + backstackString);
        if (backstackString.equals(MySettings.NOT_AVAILABLE)) {
            return;
        }

        String[] backstackArray = backstackString.split("[\\[\\]]")[1].split(", ");
        int start = backstackArray.length - MySettings.getFragmentBackstackMaxSize();
        if (start < 0) {
            start = 0;
        }

        int entry;
        for (int i = start; i < backstackArray.length; i++) {
            entry = Integer.valueOf(backstackArray[i]);
            mStack.push(entry);
        }
//        for (String entry : backstackArray) {
//            mStack.push(Integer.valueOf(entry));
//        }

        Object[] items = mStack.toArray();
        String restoredBackstackString = Arrays.toString(items);
        MyLog.d("FragmentBackstack", "Restored:  " + restoredBackstackString);

    }

    public boolean isEmpty() {
        boolean empty = true;
        if (mStack.size() > 1) {
            empty = false;
        }
        return empty;
    }

    public void clear() {
        mStack.clear();
    }

    public int size() {
        int size = 0;
        if (mStack.size() > 1) {
            size = mStack.size() - 1;
        }
        return size;
    }
}
