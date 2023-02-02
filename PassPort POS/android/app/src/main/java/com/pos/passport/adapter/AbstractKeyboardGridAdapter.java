package com.pos.passport.adapter;

import android.widget.BaseAdapter;

import com.pos.passport.interfaces.KeyboardGridInterface;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Kareem on 6/3/2016.
 */
public abstract class AbstractKeyboardGridAdapter extends BaseAdapter implements KeyboardGridInterface{

    public static final int INVALID_ID = -1;

    private int nextStableId = 0;

    private HashMap<Object, Integer> mIdMap = new HashMap<Object, Integer>();

    /**
     * Adapter must have stable id
     *
     * @return
     */
    @Override
    public final boolean hasStableIds() {
        return true;
    }

    /**
     * creates stable id for object
     *
     * @param item
     */
    protected void addStableId(Object item) {
        mIdMap.put(item, nextStableId++);
    }

    /**
     * create stable ids for list
     *
     * @param items
     */
    protected void addAllStableId(List<?> items) {
        for (Object item : items) {
            addStableId(item);
        }
    }

    /**
     * get id for position
     *
     * @param position
     * @return
     */
    @Override
    public final long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Object item = getItem(position);
        return mIdMap.get(item);
    }

    /**
     * clear stable id map
     * should called when clear adapter data;
     */
    protected void clearStableIdMap() {
        mIdMap.clear();
    }

    /**
     * remove stable id for <code>item</code>. Should called on remove data item from adapter
     *
     * @param item
     */
    protected void removeStableID(Object item) {
        mIdMap.remove(item);
    }
}
