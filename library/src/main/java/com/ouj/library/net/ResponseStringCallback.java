package com.ouj.library.net;

import android.text.TextUtils;

import com.ouj.library.BaseApplication;
import com.ouj.library.BuildConfig;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseStringCallback extends ResponseCallback<String> {

    private String responseData;
    private boolean cacheResposne;

    public boolean isCacheResposne() {
        return cacheResposne;
    }

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
        String data = response.body().string();
        try {
            if (!TextUtils.isEmpty(this.responseData)) {
                if (!data.equals(this.responseData)) {
                    this.responseData = null;
                    this.cacheResposne = false;
                    onResponse(data);
                }
            } else if (CacheControl.FORCE_CACHE.toString().equals(call.request().cacheControl().toString())) {
                this.responseData = data;
                this.cacheResposne = true;
                onResponse(data);
            } else {
                this.cacheResposne = false;
                onResponse(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if(!BuildConfig.DEBUG){
                MobclickAgent.reportError(BaseApplication.app, "NETWORK RESPONSE ERROR: " + data);
            }
        }
    }
}
