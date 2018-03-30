package com.netease.nim.rtskit.common.log;

import android.util.Log;

import com.netease.nim.rtskit.RTSKit;

/**
 * Created by winnie on 2017/12/19.
 */

public class LogUtil {

    public static void ui(String msg) {
        if (RTSKit.getiLogUtil() == null) {
            Log.i("ui", msg);
        } else {
            RTSKit.getiLogUtil().ui(msg);
        }
    }

    public static void e(String tag, String msg) {
        if (RTSKit.getiLogUtil() == null) {
            Log.e(tag, msg);
        } else {
            RTSKit.getiLogUtil().e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (RTSKit.getiLogUtil() == null) {
            Log.i(tag, msg);
        } else {
            RTSKit.getiLogUtil().i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (RTSKit.getiLogUtil() == null) {
            Log.d(tag, msg);
        } else {
            RTSKit.getiLogUtil().d(tag, msg);
        }
    }
}
