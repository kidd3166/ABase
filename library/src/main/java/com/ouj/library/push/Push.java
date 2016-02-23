package com.ouj.library.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ouj.library.util.DeviceUtils;

public class Push {

    public  static boolean DEBUG  = true;
	public final static String PREF = "PUSH";

    /**
     * 开启服务
     * @param context
     */
    public static void start(Context context) {
        if(Push.isEnable(context)) {
            Intent intent = new Intent(PushService.ACTION_START);
            intent.putExtra("appId", Push.getAppId(context));
            context.startService(intent);
        }
    }

    public static void start(Context context, boolean enable) {
        Push.setEnable(context, enable);
        if(Push.isEnable(context)) {
            Intent intent = new Intent(PushService.ACTION_START);
            intent.putExtra("appId", Push.getAppId(context));
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        stop(context, false);
    }

    /**
     * 关闭服务
     * @param context
     * @param disable 是否启动了关闭推送服务的开关，关闭后下次将忽略启动
     */
    public static void stop(Context context, boolean disable) {
        if (disable) {
            Push.setEnable(context, false);
        }
        Intent intent = new Intent(PushService.ACTION_STOP);
        intent.putExtra("appId", Push.getAppId(context));
        context.startService(intent);
    }

	public static void init(Context context, String appId, String server) {
		if (context == null)
			return;

		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		preferences.edit().putString("appId", appId).putString("server", server).commit();
        Push.setEnable(context, true);
    }

	public static void setPushId(Context context, String id) {
		if (context == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		preferences.edit().putString("push_id", id).commit();
	}

	public static String getPushId(Context context) {
		if (context == null)
			return null;
		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		return preferences.getString("push_id", DeviceUtils.uniqueId(context));
	}

	public static String getAppId(Context context) {
		if (context == null)
			return null;
		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		return preferences.getString("appId", null);
	}

	public static String getServer(Context context) {
		if (context == null)
			return null;
		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		return preferences.getString("server", "ws://push.ouj.com");
	}

	public static boolean isEnable(Context context) {
		if (context == null)
			return false;
		SharedPreferences preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		return preferences.getBoolean("enable", true);
	}

	public static void setEnable(Context context, boolean enable) {
		context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("enable", enable).commit();
	}
}
