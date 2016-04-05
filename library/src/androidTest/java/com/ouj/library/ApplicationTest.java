package com.ouj.library;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.ouj.library.helper.TimelinePtrHelper;
import com.ouj.library.module.AppVersion;
import com.ouj.library.net.OKHttp;
import com.ouj.library.net.extend.ResponseCallback;
import com.ouj.library.net.extend.ResponsePageCallBack;
import com.ouj.library.net.response.TimelineResponse;
import com.ouj.library.util.ToastUtils;

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
        Request request = new Request.Builder().url("http://test.api.hikeep.oxzj.net/dlapi/course/15/test_15_1458028099324.json?&ver=1&os=2&channel=null&uid=100006&token=722ff301-16cf-4bea-987a-1cff4815233b").build();
        new OKHttp.Builder(this).cacheType(OKHttp.CacheType.ONLY_NETWORK).build().enqueue(request, new ResponseCallback<String>() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("d", response.toString() + "");
            }

            @Override
            public void onResponseError(int code, String message) {

            }

        });
    }
    public void test1() throws Exception {
//        Request request = new Request.Builder().url(" http://test.api.hikeep.oxzj.net/post/getNewList.do?&ver=1&os=2&channel=ouj&uid=100004&token=f18935a1-d485-410c-8405-c8b4ce87a288").build();
//        new OKHttp.Builder(this).cacheType(OKHttp.CacheType.ONLY_NETWORK).build().enqueue(request, new ResponsePageCallBack<Time, TimelinePtrHelper>(null, null) {
//
//
//        });
    }
    public void testUpload() throws Exception {
//        AppVersion appVersion = new AppVersion(getApplication());
    }
}