package com.ouj.library.net.extend;

import com.ouj.library.helper.RefreshPtrHelper;
import com.ouj.library.net.response.PageResponse;
import com.ouj.library.widget.StatefulLayout;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class ResponsePageCallBack<T extends PageResponse, R extends RefreshPtrHelper> extends ResponseCallback<T> {

    private R ptrHelper;
    private StatefulLayout statefulLayout;

    public ResponsePageCallBack(R ptrHelper, StatefulLayout statefulLayout) {
        this.ptrHelper = ptrHelper;
        this.statefulLayout = statefulLayout;
    }

    @Override
    public void onStart() {
        if (ptrHelper != null) {
            int itemCount = ptrHelper.getCount();
            if (itemCount == 0) {
                statefulLayout.showProgress();
            }
        }
    }

    @Override
    public void onResponse(int code, T response) {
        if (ptrHelper != null) {
            ptrHelper.handleResponse(response);
        }
    }

    @Override
    public void onFinish() {
        if (ptrHelper != null) {
            int itemCount = ptrHelper.getCount();
            if (itemCount == 0) {
                statefulLayout.showEmpty();
            } else {
                statefulLayout.showContent();
            }
            if (ptrHelper.getPtrFrameLayout() != null)
                ptrHelper.getPtrFrameLayout().refreshComplete();
        }
    }
}
