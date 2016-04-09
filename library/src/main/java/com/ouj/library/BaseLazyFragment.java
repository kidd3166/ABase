package com.ouj.library;

/**
 * Created by liqi on 2016-4-8.
 */
public abstract class BaseLazyFragment extends BaseFragment {

    private boolean isLazyLoad;

    protected abstract void onLazyLoad();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!isLazyLoad) {
                isLazyLoad = true;
                onLazyLoad();
            }
        }
    }
}
