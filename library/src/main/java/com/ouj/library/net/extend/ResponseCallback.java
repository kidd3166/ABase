package com.ouj.library.net.extend;

import com.alibaba.fastjson.JSON;
import com.ouj.library.net.ResponseStringCallback;
import com.ouj.library.net.response.BaseResponse;

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

    }

    @Override
    public void onResponse(String responseStr) throws Exception {
        BaseResponse response = JSON.parseObject(responseStr, BaseResponse.class);
        if (response.result == 1) {
            Object o = response.data;
            if (o != null)
                onResponse(response.code, (T) JSON.parseObject(response.data.toString(), this.cl));
            else
                onResponse(response.code, null);
        } else {
            onResponseError(response.code, response.msg);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }
}
