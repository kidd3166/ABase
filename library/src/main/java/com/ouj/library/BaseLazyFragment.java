package com.ouj.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by liqi on 2016-4-8.
 */
public abstract class BaseLazyFragment extends BaseFragment {

    private boolean isLazyLoad;
    private boolean isViewCreated;

    protected abstract void onLazyLoad();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isViewCreated && !isLazyLoad) {
                isLazyLoad = true;
                onLazyLoad();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.isViewCreated = true;
    }
}
