package com.ouj.library.adapter;

import android.support.v7.widget.RecyclerView;

import com.ouj.library.helper.RefreshPtrHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liqi on 2016-4-1.
 */
public abstract class RefreshAdapter<E, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements RefreshPtrHelper.DataStore {

    public final ArrayList<E> items = new ArrayList<>();

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public void setItems(List list, boolean b) {
        if (items == null)
            return;
        if (b)
            items.clear();
        if (list != null && !list.isEmpty())
            items.addAll(list);
    }

    @Override
    public void clear() {
        if (items == null)
            return;
        items.clear();
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }
}
