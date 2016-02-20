package com.ouj.library.net;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseStringCallback extends ResponseCallback {

    private String responseData;

    public abstract void onResponse(String response);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
