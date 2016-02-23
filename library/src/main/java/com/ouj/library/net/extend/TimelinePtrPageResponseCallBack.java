package com.ouj.library.net.extend;

import com.ouj.library.helper.TimelinePtrHelper;
import com.ouj.library.net.response.TimelineResponse;
import com.ouj.library.widget.StatefulLayout;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class TimelinePtrPageResponseCallBack<T extends TimelineResponse> extends ResponseCallback<TimelineResponse> {

    private TimelinePtrHelper ptrHelper;
    private StatefulLayout statefulLayout;

    public TimelinePtrPageResponseCallBack(TimelinePtrHelper ptrHelper, StatefulLayout statefulLayout) {
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
    public void onResponse(int code, TimelineResponse response) {
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
