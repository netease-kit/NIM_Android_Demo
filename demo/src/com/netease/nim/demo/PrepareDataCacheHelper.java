package com.netease.nim.demo;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.cache.BitmapCache;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.session.emoji.StickerManager;

/**
 * 构建缓存数据帮助类
 * Created by huangjun on 2015/9/25.
 */
public class PrepareDataCacheHelper {
    private static final String TAG = PrepareDataCacheHelper.class.getSimpleName();

    /**
     * 本地缓存构建(异步)
     */
    public static void buildDataCache() {
        NimSingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // clear
                NimUserInfoCache.getInstance().clear();
                NimUIKit.clearCache();

                // build user/friend/team data cache
                NimUIKit.buildCache();
                NimUserInfoCache.getInstance().buildCache();

                // build sticker data
                BitmapCache.getInstance().init();
                StickerManager.getInstance().init();

                LogUtil.i(TAG, "build data cache completed");
            }
        });
    }
}
