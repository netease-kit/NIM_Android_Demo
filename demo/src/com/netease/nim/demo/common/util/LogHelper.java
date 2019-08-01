package com.netease.nim.demo.common.util;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.common.log.ILogUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;

/**
 * Created by winnie on 2017/12/21.
 */

public class LogHelper {

    public static void init() {
        AVChatKit.setiLogUtil(new ILogUtil() {
            @Override
            public void ui(String msg) {
                LogUtil.ui(msg);
            }

            @Override
            public void e(String tag, String msg) {
                LogUtil.e(tag, msg);
            }

            @Override
            public void i(String tag, String msg) {
                LogUtil.i(tag, msg);
            }

            @Override
            public void d(String tag, String msg) {
                LogUtil.d(tag, msg);
            }
        });
    }
}
