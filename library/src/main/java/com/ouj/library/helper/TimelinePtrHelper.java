package com.ouj.library.helper;

import android.support.v7.widget.RecyclerView;

import com.ouj.library.net.response.TimelineResponse;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by liqi on 2016-2-22.
 */
public class TimelinePtrHelper<T> extends RefreshPtrHelper {

    private String timeline = null;

    public TimelinePtrHelper(PtrFrameLayout mPtrFrameLayout, RecyclerView mRecyclerView, DataStore dataStore, Listener listener) {
        super(mPtrFrameLayout, mRecyclerView, dataStore, listener);
    }

    @Override
    protected void onRefreshPrepare() {
        super.onRefreshPrepare();
        timeline = null;
    }

    @Override
    protected void onRefresh(boolean pullToRefresh) {
        mListener.onRefresh(timeline, pullToRefresh);
    }

    public void handleResponse(TimelineResponse response) {
        if (response != null) {
            this.timeline = response.timeline;
            this.hasMore = response.hasMore();
            handleItems(response);
        }
        handleNoMore();
    }
}
