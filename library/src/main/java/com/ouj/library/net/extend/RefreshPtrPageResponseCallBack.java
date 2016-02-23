package com.ouj.library.net.extend;

import com.ouj.library.helper.RefreshPtrHelper;
import com.ouj.library.net.response.PageResponse;
import com.ouj.library.widget.StatefulLayout;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class RefreshPtrPageResponseCallBack<T extends PageResponse> extends ResponseCallback<PageResponse> {

    private RefreshPtrHelper ptrHelper;
    private StatefulLayout statefulLayout;

    public RefreshPtrPageResponseCallBack(RefreshPtrHelper ptrHelper, StatefulLayout statefulLayout) {
        this.ptrHelper = ptrHelper;
        this.statefulLayout = statefulLayout;
    }

    @Override
    public void onStart() {
        int itemCount = ptrHelper.getCount();
        if (itemCount == 0) {
            statefulLayout.showProgress();
        }
    }

    @Override
    public void onResponse(int code, PageResponse response) {
        ptrHelper.handleResponse(response);
    }

    @Override
    public void onFinish() {
        int itemCount = ptrHelper.getCount();
        if (itemCount == 0) {
            statefulLayout.showEmpty();
        } else {
            statefulLayout.showContent();
        }
        ptrHelper.getPtrFrameLayout().refreshComplete();
    }
}
