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

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by liqi on 2016-2-22.
 */
public class RefreshPtrHelper {

    protected PtrFrameLayout mPtrFrameLayout;
    protected RecyclerView mRecyclerView;
    protected TextView mfooterTips;
    protected ProgressBar mfooterProgress;

    protected int currentPage, resultCount;
    protected boolean hasMore, loadMore, autoLoad = true, isRefresh;
    protected Listener mListener;
    protected DataStore mDataStore;

    public static interface Listener {
        public void onRefresh(String page, boolean pullToRefresh);
    }

    public static interface DataStore {
        public List getData();
    }

    public RefreshPtrHelper(PtrFrameLayout mPtrFrameLayout, RecyclerView mRecyclerView, DataStore dataStore, Listener listener) {
        this.mPtrFrameLayout = mPtrFrameLayout;
        this.mRecyclerView = mRecyclerView;
        this.mDataStore = dataStore;
        this.mListener = listener;
        attach();
    }

    public void destroy() {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
        List items = mDataStore.getData();
        if (items != null)
            items.clear();
    }

    public void attach() {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            throw new IllegalStateException("RecyclerView Adapter is null");

        WrapAdapter wrapAdapter = null;
        boolean resetAdapter = false;
        if (adapter instanceof WrapAdapter) {
            wrapAdapter = (WrapAdapter) adapter;
        } else {
            wrapAdapter = new WrapAdapter(adapter);
            resetAdapter = true;
        }

        View footer = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.base__view_recycler_view_footer, mRecyclerView, false);
        mfooterTips = (TextView) footer.findViewById(R.id.footer_tips);
        mfooterProgress = (ProgressBar) footer.findViewById(R.id.footer_progress);
        wrapAdapter.addFooter(footer);

        if (resetAdapter) {
            mRecyclerView.setAdapter(wrapAdapter);
        }

        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(
                    wrapAdapter.createSpanSizeLookup(gridLayoutManager.getSpanCount()));
        }

        final LoadMoreOnScrollListener.Listener loadMoreListener = new LoadMoreOnScrollListener.Listener() {
            @Override
            public void onLoadMore(RecyclerView recyclerView) {
                if (hasMore) {
                    if (!loadMore && autoLoad) {
                        loadMore = true;
                        isRefresh = false;
                        mfooterTips.setText("");
                        mfooterProgress.setVisibility(View.VISIBLE);
                        onRefresh(false);
                    }
                }
            }
        };
        mRecyclerView.addOnScrollListener(new LoadMoreOnScrollListener(loadMoreListener));
        mfooterTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreListener.onLoadMore(mRecyclerView);
            }
        });

        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                onRefreshPrepare();
                mfooterTips.setText("");
                mfooterProgress.setVisibility(View.INVISIBLE);
                isRefresh = true;
                onRefresh(true);
            }
        });
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

    public void handleResponse(PageResponse response) {
        if (response != null) {
            Page page = response.page;
            if (page != null) {
                resultCount = page.maxResults;
                currentPage = page.currentPage;
            }
            this.hasMore = response.hasMore();
            handleItems(response);
        }
        handleNoMore();
    }

    protected void handleItems(ResponseItems responseItems) {
        if (responseItems == null)
            return;

        List items = mDataStore.getData();
        if (items == null)
            return;
        if (this.isRefresh)
            items.clear();
        int originalItemCount = items.size();
        List _items = responseItems.getItems();
        if (_items != null && !_items.isEmpty()) {
            items.addAll(_items);
            RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
            if (adapter != null) {
                if (originalItemCount == 0) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyItemRangeInserted(originalItemCount, items.size() - 1);
                }
            }
        }
    }

    protected void handleNoMore() {
        if (!this.hasMore)
            mfooterTips.setText("没了~");
        else
            mfooterTips.setText("");
        mfooterProgress.setVisibility(View.INVISIBLE);
    }

    protected void onRefreshPrepare() {
        hasMore = false;
        loadMore = false;
        autoLoad = true;
        currentPage = 0;
    }

    protected void onRefresh(boolean pullToRefresh) {
        int page = currentPage;
        if (!pullToRefresh)
            page = currentPage + 1;
        mListener.onRefresh(String.valueOf(page), pullToRefresh);
    }
}
