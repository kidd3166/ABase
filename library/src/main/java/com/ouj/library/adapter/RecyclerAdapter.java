package com.ouj.library.adapter;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class RecyclerAdapter<DATA, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected ArrayList<DATA> items;
}
