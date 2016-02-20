package com.ouj.library.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.SparseArray;

import de.greenrobot.event.EventBus;

/**
 * Created by liqi on 2016-2-20.
 */
public class PermissionHelper {

    private SparseArray<Runnable> allowablePermissionRunnables;
    private SparseArray<Runnable> disallowablePermissionRunnables;

    public PermissionHelper() {
        allowablePermissionRunnables = new SparseArray<>();
        disallowablePermissionRunnables = new SparseArray<>();
    }

    public void requestPermission(final Activity context, int id, Runnable allowableRunnable, Runnable disallowableRunnable, final Runnable requestPermissionsRunnable, final String... permissions) {
        if (permissions == null || permissions.length == 0)
            return;

        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            String permission = null;
            for (String _permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, _permission) != PackageManager.PERMISSION_GRANTED) {
                    permission = _permission;
                    break;
                }
            }
            if (!TextUtils.isEmpty(permission)) {
                allowablePermissionRunnables.put(id, allowableRunnable);
                if (disallowableRunnable != null) {
                    disallowablePermissionRunnables.put(id, disallowableRunnable);
                }

                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    new AlertDialog.Builder(context)
                            .setMessage("授权后才能使用该功能")
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissionsRunnable.run();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                } else {
                    requestPermissionsRunnable.run();
                }
            } else {
                allowableRunnable.run();
            }
        } else {
            allowableRunnable.run();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (allowablePermissionRunnables != null) {
                Runnable allowRun = allowablePermissionRunnables.get(requestCode);
                if (allowRun != null)
                    allowRun.run();
            }
        } else {
            if (disallowablePermissionRunnables != null) {
                Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
                if (disallowRun != null)
                    disallowRun.run();
            }
        }
    }

    public void onDestroy() {
        if (allowablePermissionRunnables != null)
            allowablePermissionRunnables.clear();
        if (disallowablePermissionRunnables != null)
            disallowablePermissionRunnables.clear();
        allowablePermissionRunnables = null;
        disallowablePermissionRunnables = null;
    }
}
