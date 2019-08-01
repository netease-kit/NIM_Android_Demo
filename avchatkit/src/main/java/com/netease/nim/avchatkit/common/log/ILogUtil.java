package com.netease.nim.avchatkit.common.log;

/**
 * Created by winnie on 2017/12/21.
 */

public interface ILogUtil {
    void ui(String msg);

    void e(String tag, String msg);

    void i(String tag, String msg);

    void d(String tag, String msg);
}
