package com.ouj.library.webview;

import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class KLPluginManager {
    private HashMap<String, KLPlugin> pluginMap;
    public KLWebView mWebview;

    public KLPluginManager(KLWebView webview) {
        pluginMap = new HashMap<String, KLPlugin>();
        mWebview = webview;
    }

    public KLPlugin getPlugin(String name) {
        KLPlugin plugin = pluginMap.get(name);
        if (plugin == null) {
            try {
                @SuppressWarnings("unchecked")
                Class<KLPlugin> classObj = (Class<KLPlugin>) Class.forName(name);
                Constructor<KLPlugin> con = classObj.getConstructor(KLWebView.class);

                plugin = con.newInstance(mWebview);
                pluginMap.put(name, plugin);
            } catch (Exception e) {
                // 插件初始化失败则跳过
                e.printStackTrace();
            }
        }

        return plugin;
    }

    private void onPluginAction(String funcName) {
        onPluginAction(funcName, new Class<?>[0], new Object[0]);
    }

    private void onPluginAction(String funcName, Class<?>[] paramClasses, Object... params) {

        if (pluginMap != null) {
            Iterator<Entry<String, KLPlugin>> iter = pluginMap.entrySet().iterator();
            Method method;

            while (iter.hasNext()) {
                Entry<String, KLPlugin> entry = (Entry<String, KLPlugin>) iter.next();
                KLPlugin plugin = entry.getValue();

                try {
                    method = plugin.getClass().getMethod(funcName, paramClasses);
                    method.invoke(plugin, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        Class<?>[] classes = { Bundle.class };
        onPluginAction("onCreate", classes, savedInstanceState);
    }

    public void onStart() {
        onPluginAction("onStart");
    }
    
    public void onPause() {
        onPluginAction("onPause");
    }
    
    public void onResume() {
        onPluginAction("onResume");
    }

    public void onStop() {
        onPluginAction("onStop");
    }

    public void onDestroy() {
        if (pluginMap != null) {
            onPluginAction("onDestroy");

            pluginMap.clear();
            pluginMap = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Class<?>[] classes = { int.class, int.class, Intent.class };
        onPluginAction("onActivityResult", classes, requestCode, resultCode, intent);
    }

    public void onSaveInstanceState(Bundle outState) {
        Class<?>[] classes = { Bundle.class };
        onPluginAction("onSaveInstanceState", classes, outState);
    }
    
}
