package com.netease.yunxin.nertc.nertcvideocall.utils;

import android.util.Log;

/**
 * Created by wangqiang04 on 4/19/21.
 */
public final class ALog {

    public static void dApi(String tag, ParameterMap parameterMap) {
        Log.d(tag, parameterMap.toValue());
    }

    public static void dApi(String tag, String log) {
        Log.d(tag, log);
    }

    public static void d(String tag, String log) {
        Log.d(tag, log);
    }

    public static void i(String tag, String log) {
        Log.i(tag, log);
    }

    public static void e(String tag, String log, Throwable throwable) {
        Log.e(tag, log, throwable);
    }
}
