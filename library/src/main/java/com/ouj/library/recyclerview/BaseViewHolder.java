package com.ouj.library.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liqi on 2016-5-13.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final OnViewClickListener mListener;

    public BaseViewHolder(View itemView, OnViewClickListener mListener) {
        super(itemView);
        this.mListener = mListener;
    }

    @Override
    public void onClick(View v) {
        if (this.mListener != null)
            this.mListener.onViewClick(v, getAdapterPosition());
    }
}
