package com.ouj.library.net.extend;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ouj.library.BaseApplication;
import com.ouj.library.R;
import com.ouj.library.event.LogoutEvent;
import com.ouj.library.net.ResponseStringCallback;
import com.ouj.library.net.response.BaseResponse;
import com.ouj.library.util.UIUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseCallback<T> extends ResponseStringCallback {

    private Class<?> cl;
    private Context context;
    private Dialog progressDialog;

    public ResponseCallback() {
        this.cl = (Class<?>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public ResponseCallback(Context context) {
        this();
        this.context = context;
    }

    public abstract void onResponse(int code, T response) throws Exception;


    public String getProgressText() {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (context != null) {
            View v = LayoutInflater.from(context).inflate(R.layout.base__view_progress, null);
            TextView text = (TextView) v.findViewById(R.id.text);
            String progressText = getProgressText();
            if (!TextUtils.isEmpty(progressText)) {
                text.setText(progressText);
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
            progressDialog = new AlertDialog.Builder(context).setView(v).show();
            progressDialog.setCanceledOnTouchOutside(false);
            WindowManager.LayoutParams params =
                    progressDialog.getWindow().getAttributes();
            params.width = UIUtils.dip2px(120);
            progressDialog.getWindow().setAttributes(params);
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.progressDialog = null;
        this.context = null;
    }

    public void onResponseError(int code, String message) throws Exception {
        if (code != 0) {
            Toast.makeText(BaseApplication.app, BaseApplication.APP_DEBUG ? String.format("%d: %s", code, message) : message, Toast.LENGTH_SHORT).show();
        }
        if (code == -5) {// 登录失效
            LogoutEvent event = new LogoutEvent();
            event.message = message;
            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void onResponse(String responseStr) throws Exception {
        if (TextUtils.isEmpty(responseStr))
            return;
        BaseResponse<String> response = JSON.parseObject(responseStr, BaseResponse.class);
        if (response.result == 1) {
            if (!TextUtils.isEmpty(response.data)) {
                onResponse(response.code, (T) JSON.parseObject(response.data, this.cl));
            } else {
                onResponse(response.code, null);
            }
        } else {
            onResponseError(response.code, response.msg);
        }
        response.data = null;
        response = null;
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }
}
