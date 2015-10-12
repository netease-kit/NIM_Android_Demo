package com.netease.nim.demo;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.log.LogUtil;

/**
 * 登录后准备缓存数据工具类
 * Created by huangjun on 2015/9/25.
 */
public class PrepareDataHelper {
    private static final String TAG = PrepareDataHelper.class.getSimpleName();

    public static void prepare() {
        buildDataCache();
        LogUtil.i(TAG, "prepare data completed");
    }

    private static void buildDataCache() {
        // clear
        NimUserInfoCache.getInstance().clear();
        NimUIKit.clearCache();

        // build
        NimUIKit.buildCache();
        NimUserInfoCache.getInstance().buildCache();
        LogUtil.i(TAG, "build data cache completed");
    }
}
