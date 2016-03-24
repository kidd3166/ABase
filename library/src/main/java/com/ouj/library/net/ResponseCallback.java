package com.ouj.library.net;

import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseCallback<T> implements Callback {

    public void onStart() {

    }

    public void onFinish() {
    }

    public abstract void onResponse(T response) throws Exception;

    public abstract T parseNetworkResponse(Response response) throws Exception;
}
