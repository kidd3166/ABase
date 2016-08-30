package com.ouj.library.helper;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eyeem.recyclerviewtools.LoadMoreOnScrollListener;
import com.eyeem.recyclerviewtools.adapter.WrapAdapter;
import com.ouj.library.R;
import com.ouj.library.net.response.Page;
import com.ouj.library.net.response.PageResponse;
import com.ouj.library.net.response.ResponseItems;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by liqi on 2016-2-22.
 */
public class RefreshPtrHelper<T extends PageResponse> {

    protected PtrFrameLayout mPtrFrameLayout;
    protected RecyclerView mRecyclerView;
    protected TextView mfooterTips;
    protected ProgressBar mfooterProgress;

    protected Listener mListener;
    protected DataStore mDataStore;
    protected int currentPage, resultCount;
    protected boolean hasMore, loadMore, autoLoadMore = true, isRefresh;
    protected boolean autoRefresh = true;
    protected boolean mWrapAdapter = true, mFooter = true;

    public static interface Listener {
        public void onRefresh(String page, boolean pullToRefresh);
    }

    public static interface DataStore {
        public void setItems(List items, boolean isRefresh);

        public int getCount();

        public void clear();

    }

    public RefreshPtrHelper(PtrFrameLayout mPtrFrameLayout, RecyclerView mRecyclerView, DataStore dataStore, Listener listener) {
        this.mPtrFrameLayout = mPtrFrameLayout;
        this.mRecyclerView = mRecyclerView;
        this.mDataStore = dataStore;
        this.mListener = listener;
    }

    public void destroy() {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
        mDataStore.clear();
    }

    public void attach() {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException("RecyclerView Adapter is null");

        if (mWrapAdapter) {
            WrapAdapter wrapAdapter = null;
            boolean resetAdapter = false;
            if (adapter instanceof WrapAdapter) {
                wrapAdapter = (WrapAdapter) adapter;
            } else {
                wrapAdapter = new WrapAdapter(adapter);
                resetAdapter = true;
            }

            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                gridLayoutManager.setSpanSizeLookup(
                        wrapAdapter.createSpanSizeLookup(gridLayoutManager.getSpanCount()));
            }

            if (mFooter) {
                View footer = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.base__view_recycler_view_footer, mRecyclerView, false);
                mfooterTips = (TextView) footer.findViewById(R.id.footer_tips);
                mfooterProgress = (ProgressBar) footer.findViewById(R.id.footer_progress);
                wrapAdapter.addFooter(footer);
            }

            if (resetAdapter) {
                mRecyclerView.setAdapter(wrapAdapter);
            }

            final LoadMoreOnScrollListener.Listener loadMoreListener = new LoadMoreOnScrollListener.Listener() {
                @Override
                public void onLoadMore(RecyclerView recyclerView) {
                    if (hasMore) {
                        if (!loadMore && autoLoadMore) {
                            loadMore();
                        }
                    }
                }
            };
            mRecyclerView.clearOnScrollListeners();
            mRecyclerView.addOnScrollListener(new LoadMoreOnScrollListener(loadMoreListener));
            if (mfooterTips != null)
                mfooterTips.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMoreListener.onLoadMore(mRecyclerView);
                    }
                });
        }

        if (mPtrFrameLayout != null) {
            mPtrFrameLayout.setPtrHandler(new PtrHandler() {
                @Override
                public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                    return RefreshPtrHelper.this.checkCanDoRefresh(frame, content, header);
                }

                @Override
                public void onRefreshBegin(PtrFrameLayout frame) {
                    onRefresh();
                }
            });
            if (autoRefresh)
                mPtrFrameLayout.autoRefresh(true);
        } else {
            if (autoRefresh) {
                onRefresh();
            }
        }
    }

    public void onRefresh() {
        onRefreshPrepare();
        if (mfooterTips != null)
            mfooterTips.setText("");
        if (mfooterProgress != null)
            mfooterProgress.setVisibility(View.INVISIBLE);
        isRefresh = true;
        onRefresh(true);
    }

    public void loadMore() {
        loadMore = true;
        isRefresh = false;
        if (mfooterTips != null)
            mfooterTips.setText("");
        if (mfooterProgress != null)
            mfooterProgress.setVisibility(View.VISIBLE);
        onRefresh(false);
    }

    public int getCount() {
        int itemCount = 0;
        RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
        if (adapter != null) {
            if (adapter instanceof WrapAdapter) {
                itemCount = ((WrapAdapter) adapter).getWrappedCount();
            } else {
                itemCount = adapter.getItemCount();
            }
        }
        return itemCount;
    }

    public PtrFrameLayout getPtrFrameLayout() {
        return mPtrFrameLayout;
    }

    public void handleResponse(T response) {
        if (response != null) {
            Page page = response.page;
            if (page != null) {
                resultCount = page.maxResults;
                currentPage = page.currentPage;
            }
            this.hasMore = response.hasMore();
            handleItems(response);
        }
    }

    protected void handleItems(ResponseItems responseItems) {
        if (responseItems == null)
            return;
        if (mRecyclerView == null)
            return;
        RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            return;
        List items = responseItems.getItems();
        int originalItemCount = mDataStore.getCount();
        if (items != null && !items.isEmpty()) {
            mDataStore.setItems(items, this.isRefresh);
            if (this.isRefresh)
                originalItemCount = 0;
            if (originalItemCount == 0) {
                adapter.notifyDataSetChanged();
            } else {
//                if (adapter instanceof WrapAdapter) {
//                    originalItemCount += ((WrapAdapter) adapter).getHeaderCount();
//                }
                adapter.notifyItemRangeInserted(originalItemCount, mDataStore.getCount() - originalItemCount);
            }
        } else {
            if (this.isRefresh) {
                mDataStore.setItems(new ArrayList(), this.isRefresh);
                adapter.notifyDataSetChanged();
            } else {
                if (originalItemCount == 0) {
                    mDataStore.setItems(new ArrayList(), this.isRefresh);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    protected boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
    }

    protected void handleNoMore() {
        if (mfooterTips != null) {
            if (!this.hasMore) {
                if(mDataStore != null){
                    int count = mDataStore.getCount();
                    if(count == 0){
                        mfooterTips.setText(R.string.sfl_default_placeholder_empty);
                    }else {
                        mfooterTips.setText(R.string.default_footer_end);
//                        mfooterTips.setText("");
                    }
                }else {
                    mfooterTips.setText("");
                }
            }else
                mfooterTips.setText("");
        }
        if (mfooterProgress != null)
            mfooterProgress.setVisibility(View.INVISIBLE);
        this.loadMore = false;
    }

    protected void onRefreshPrepare() {
        reset();
    }

    protected void onRefresh(boolean pullToRefresh) {
        int page = currentPage;
        if (!pullToRefresh)
            page = currentPage + 1;
        if (mListener != null)
            mListener.onRefresh(String.valueOf(page), pullToRefresh);
    }

    public void reset(){
        hasMore = false;
        loadMore = false;
        autoLoadMore = true;
        currentPage = 0;
    }

    public void refreshComplete(){
        loadMore = false;
        if(mPtrFrameLayout != null)
            mPtrFrameLayout.refreshComplete();
        handleNoMore();
    }

    public void setAutoLoadMore(boolean autoLoad) {
        this.autoLoadMore = autoLoad;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public void setWrapAdapter(boolean wrapAdapter) {
        this.mWrapAdapter = wrapAdapter;
    }

    public void setFooter(boolean footer) {
        this.mFooter = footer;
        this.mWrapAdapter = true;
    }

}
