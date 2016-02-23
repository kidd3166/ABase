package com.ouj.library;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import com.ouj.library.event.ActivityEvent;
import com.ouj.library.event.OnForegroundEvent;
import com.ouj.library.net.OKHttp;
import com.ouj.library.permission.PermissionHelper;

import de.greenrobot.event.EventBus;

/**
 * Created by liqi on 2016-2-20.
 */
public class BaseActivity extends AppCompatActivity {

    private PermissionHelper permissionHelper;

    public void onEvent(ActivityEvent event) {

    }

    public void onEventMainThread(OnForegroundEvent event) {

    }

    public BaseActivity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        OKHttp.cancelTag(this);
        super.onDestroy();
        if (permissionHelper != null)
            permissionHelper.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionHelper != null)
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected final void requestPermission(final int id, Runnable allowableRunnable, Runnable disallowableRunnable, final String... permissions) {
        permissionHelper = new PermissionHelper();
        permissionHelper.requestPermission(this, id, allowableRunnable, disallowableRunnable, new Runnable() {
            @Override
            public void run() {
                ActivityCompat.requestPermissions(BaseActivity.this, permissions, id);
            }
        }, permissions);
    }

}
