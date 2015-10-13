package com.netease.nim.demo.main.helper;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.LoginSyncDataStatusObserver;
import com.netease.nim.demo.NimUserInfoCache;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.cache.BitmapCache;

/**
 * 注销帮助类
 * Created by huangjun on 2015/10/8.
 */
public class LogoutHelper {
    public static void logout() {
        // 清理缓存&注销监听&清除状态
        NimUserInfoCache.getInstance().clear();
        BitmapCache.getInstance().clear();
        NimUIKit.clearCache();
        DemoCache.clear();
        LoginSyncDataStatusObserver.getInstance().reset();
    }
}
