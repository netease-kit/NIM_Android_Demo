package com.netease.nim.demo.config;

import com.netease.nimlib.sdk.sf.InnerConfigs;

public class ExtraOptions {

    public static void provide() {
        InnerConfigs.setUserState(0); // SF在线状态初始化
    }
}
