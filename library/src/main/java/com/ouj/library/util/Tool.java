package com.ouj.library.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.ouj.library.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Tool {

    /**
     * 发送消息
     *
     * @param handler  句柄
     * @param funcName 方法名
     * @param params   方法参数
     */
    public static void sendMessage(Handler handler, String funcName, String... params) {
        if (handler == null) {
            return;
        }

        List<Object> args = new ArrayList<Object>();
        args.add(funcName);
        for (String str : params) {
            args.add(str);
        }

        Message msg = Message.obtain();
        msg.what = args.size() - 1;
        msg.obj = args;
        handler.sendMessage(msg);
    }

    /**
     * 获取句柄
     *
     * @param className 类
     * @param instance  实例
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Handler getHandler(final Class<?> className, final Object instance) {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 反射调用消息的方法
                @SuppressWarnings("unchecked")
                ArrayList<Object> args = (ArrayList<Object>) msg.obj;
                String funcName = args.remove(0).toString();

                Class[] cs = new Class[msg.what];
                for (int i = 0; i < msg.what; i++) {
                    cs[i] = String.class;
                }

                try {
                    Method method = null;
                    method = className.getMethod(funcName, cs);
                    method.invoke(instance, args.toArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
    }


    /**
     * 获取权限
     *
     * @param permission 权限
     * @param path       路径
     */
    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installApk(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseApplication.app.startActivity(intent);
    }

}


