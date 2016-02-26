package com.ouj.library.net.response;

import java.io.Serializable;

/**
 * Created by liqi on 2016-2-22.
 */
public class BaseResponse<T> implements Serializable {
    public int code;
    public int result;
    public String msg;
    public T data;
}
