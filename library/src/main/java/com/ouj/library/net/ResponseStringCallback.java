package com.ouj.library.net;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseStringCallback extends ResponseCallback<String> {

    private String responseData;

    public abstract void onResponse(String response) throws Exception;

    @Override
    public String parseNetworkResponse(Response response) throws Exception {
        try {
            return response.body().string();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            String data = response.body().string();
            if (!TextUtils.isEmpty(responseData)) {
                if (!data.equals(responseData)) {
                    responseData = null;
                    onResponse(data);
                }
            } else if (CacheControl.FORCE_CACHE.toString().equals(call.request().cacheControl().toString())) {
                responseData = data;
                onResponse(data);
            } else {
                onResponse(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
