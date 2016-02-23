package com.ouj.library.net;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.ParameterizedType;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class ResponseGenericCallback<T> extends ResponseStringCallback {

    private Class<?> cl;

    public ResponseGenericCallback() {
        this.cl = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public abstract void onResponseEntity(T response);

    @Override
    public void onResponse(String response) {
        try {
            onResponseEntity((T) JSON.parseObject(response, cl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
