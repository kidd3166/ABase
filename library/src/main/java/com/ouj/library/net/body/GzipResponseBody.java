package com.ouj.library.net.body;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

/**
 * Created by liqi on 2016-2-23.
 */
public class GzipResponseBody extends ResponseBody {
    private final ResponseBody responseBody;

    public GzipResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(new GzipSource(responseBody.source()));
    }

}
