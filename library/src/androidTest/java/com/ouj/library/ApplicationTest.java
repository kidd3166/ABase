package com.ouj.library;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.ouj.library.net.OKHttp;
import com.ouj.library.net.extend.ResponseCallback;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        OKHttp.init(getContext(), null, null, true, 30000, 50);
    }

    public void test() throws Exception {
        Request request = new Request.Builder().url("http://test.api.hikeep.oxzj.net/course/all.do").build();
        new OKHttp.Builder(this).cacheType(OKHttp.CacheType.ONLY_NETWORK).build().execute(request);
        new OKHttp.Builder(this).build().enqueue(request, new ResponseCallback<String>() {
            @Override
            public void onResponse(int code, String response) {

                Log.d("d", response.toString() + "");
            }

            @Override
            public void onResponseError(int code, String message) {

            }

        });
    }
}