/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;

import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.yunxin.app.im.main.MainActivity;

import java.io.IOException;

/**
 * Nim SDK config info
 */

class NimSDKOptionConfig {
    public static final String NOTIFY_SOUND_KEY = "android.resource://com.netease.nim.demo/raw/msg";
    public static final int LED_ON_MS = 1000;
    public static final int LED_OFF_MS = 1500;

    static SDKOptions getSDKOptions(Context context,String appKey) {
        SDKOptions options = new SDKOptions();
        options.appKey = appKey;
        initStatusBarNotificationConfig(options);
        options.sdkStorageRootPath = getAppCacheDir(context) + "/nim"; //
        options.preloadAttach = true;
        options.sessionReadAck = true;
        options.animatedImageThumbnailEnabled = true;
        options.asyncInitSDK = true;
        options.reducedIM = false;
        options.checkManifestConfig = false;
        options.enableTeamMsgAck = true;
        options.enableFcs = false;
        options.shouldConsiderRevokedMessageUnreadCount = true;
        return options;
    }

    private static void initStatusBarNotificationConfig(SDKOptions options) {
        // load status bar config
        StatusBarNotificationConfig config = loadStatusBarNotificationConfig();
        options.statusBarNotificationConfig = config;
    }

    // config StatusBarNotificationConfig
    private static StatusBarNotificationConfig loadStatusBarNotificationConfig() {
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MainActivity.class;
        config.notificationColor = Color.GRAY;
        config.notificationSound = NOTIFY_SOUND_KEY;
        config.notificationFolded = true;
        config.downTimeEnableNotification = true;
        config.ledARGB = Color.GREEN;
        config.ledOnMs = LED_ON_MS;
        config.ledOffMs = LED_OFF_MS;
        config.showBadge = true;
        return config;
    }

    /**
     * config app image/voice/file/log directory
     */
    static String getAppCacheDir(Context context) {
        String storageRootPath = null;
        try {
            if (context.getExternalCacheDir() != null) {
                storageRootPath = context.getExternalCacheDir().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            storageRootPath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName();
        }
        return storageRootPath;
    }
}
