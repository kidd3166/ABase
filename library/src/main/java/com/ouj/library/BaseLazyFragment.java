package com.ouj.library;

/**
 * Created by liqi on 2016-4-8.
 */
public abstract class BaseLazyFragment extends BaseFragment {

    private boolean isLazyLoad;

    protected abstract void onLazyLoad();

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            if (!isLazyLoad) {
                isLazyLoad = true;
                onLazyLoad();
            }
        }
    }
}
