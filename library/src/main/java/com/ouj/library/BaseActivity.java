package com.ouj.library;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;

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
        dismissProgressDialog();
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

    protected void showProgressDialog(int stringRes) {
        showProgressDialog(getString(stringRes));
    }

    protected void showProgressDialog(final String message) {
        AppCompatDialogFragment dialog = new AppCompatDialogFragment() {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.dismiss();
                progressDialog.setMessage(message);
                return progressDialog;
            }
        };
        dialog.show(getSupportFragmentManager(), "progressDialog");
    }

    protected void dismissProgressDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragment_dialog");
        if (fragment != null) {
            DialogFragment df = (DialogFragment) fragment;
            df.dismiss();
        }
    }
}
