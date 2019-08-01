package com.netease.nim.avchatkit.common.log;

import android.util.Log;

import com.netease.nim.avchatkit.AVChatKit;

/**
 * Created by winnie on 2017/12/19.
 */

public class LogUtil {

    public static void ui(String msg) {
        if (AVChatKit.getiLogUtil() == null) {
            Log.i("ui", msg);
        } else {
            AVChatKit.getiLogUtil().ui(msg);
        }
    }

    public static void e(String tag, String msg) {
        if (AVChatKit.getiLogUtil() == null) {
            Log.e(tag, msg);
        } else {
            AVChatKit.getiLogUtil().e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (AVChatKit.getiLogUtil() == null) {
            Log.i(tag, msg);
        } else {
            AVChatKit.getiLogUtil().i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (AVChatKit.getiLogUtil() == null) {
            Log.d(tag, msg);
        } else {
            AVChatKit.getiLogUtil().d(tag, msg);
        }
    }
}
