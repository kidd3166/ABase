package com.ouj.library;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ouj.library.event.OnForegroundEvent;
import com.ouj.library.net.OKHttp;
import com.ouj.library.permission.PermissionHelper;

import de.greenrobot.event.EventBus;

/**
 * Created by liqi on 2016-2-20.
 */
public class BaseFragment extends Fragment {

    private PermissionHelper permissionHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(willRetainInstance());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        OKHttp.cancelTag(this);
        super.onDestroy();
        if (permissionHelper != null)
            permissionHelper.onDestroy();
    }

    public void onEventMainThread(OnForegroundEvent event) {

    }

    protected boolean willRetainInstance(){
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionHelper != null)
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected final void requestPermission(final int id, Runnable allowableRunnable, Runnable disallowableRunnable, final String... permissions) {
        permissionHelper = new PermissionHelper();
        permissionHelper.requestPermission(getActivity(), id, allowableRunnable, disallowableRunnable, new Runnable() {
            @Override
            public void run() {
                requestPermissions(permissions, id);
            }
        }, permissions);

    }
}
