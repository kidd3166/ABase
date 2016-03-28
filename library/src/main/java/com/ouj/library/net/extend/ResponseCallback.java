package com.ouj.library.net.extend;

import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ouj.library.BaseApplication;
import com.ouj.library.net.ResponseStringCallback;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Call;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseCallback<T> extends ResponseStringCallback {

    private Class<?> cl;

    public ResponseCallback() {
        this.cl = (Class<?>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public abstract void onResponse(int code, T response) throws Exception;

    public void onResponseError(int code, String message) throws Exception {
        if (code != 0) {
            Toast.makeText(BaseApplication.app, BaseApplication.APP_DEBUG ? String.format("%d: %s", code, message) : message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(String responseStr) throws Exception {
        JSONObject jsonObject = new JSONObject(responseStr);
        int code = jsonObject.optInt("code", 0);
        if (jsonObject.optInt("result", 0) == 1) {
            if (jsonObject.has("data")) {
                String data = jsonObject.optString("data");
                if (!TextUtils.isEmpty(data))
                    onResponse(code, (T) JSON.parseObject(data, this.cl));
                else
                    onResponse(code, null);
            } else {
                onResponse(code, null);
            }
        } else {
            onResponseError(code, jsonObject.optString("msg"));
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }
}
