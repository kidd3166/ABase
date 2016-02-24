package com.ouj.library.net.extend;

import com.ouj.library.net.ResponseGenericCallback;
import com.ouj.library.net.response.BaseResponse;

/**
 * Created by liqi on 2016-1-30.
 */
public abstract class ResponseCallback<T> extends ResponseGenericCallback<BaseResponse<T>> {

    public abstract void onResponse(int code, T response);

    public abstract void onResponseError(int code, String message);

    @Override
    public void onResponseEntity(BaseResponse<T> response) {
        if (response.ret == 1) {
            onResponse(response.code, response.data);
        } else {
            onResponseError(response.code, response.msg);
        }
    }

}
