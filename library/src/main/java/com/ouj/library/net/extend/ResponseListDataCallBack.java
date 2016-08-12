package com.ouj.library.net.extend;

import com.ouj.library.helper.RefreshPtrHelper;
import com.ouj.library.util.NetworkUtils;
import com.ouj.library.widget.StatefulLayout;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class ResponseListDataCallBack<T extends RefreshPtrHelper.DataStore> extends ResponseCallback<T> {

    private StatefulLayout statefulLayout;
    private int count;

    public ResponseListDataCallBack(StatefulLayout statefulLayout) {
        this.statefulLayout = statefulLayout;
    }

    @Override
    public void onStart() {
        if (statefulLayout.getState() != StatefulLayout.State.CONTENT) {
            statefulLayout.showProgress();
        }
    }

    @Override
    public void onResponse(int code, T response) {
        this.count = response.getCount();
    }

    @Override
    public void onFinish() {
        int itemCount = this.count;
        if (itemCount == 0) {
            if (NetworkUtils.isAvailable())
                statefulLayout.showEmpty();
            else
                statefulLayout.showOffline();
        } else {
            statefulLayout.showContent();
        }
    }
}
