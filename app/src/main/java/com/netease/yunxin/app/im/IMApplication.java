/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.BasicInfo;


public class IMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate(); //config nim sdk
        SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
        NIMClient.init(this, null, options);

        initALog(this);
    }

    private void initALog(Context context) {
        ALog.logFirst(new BasicInfo.Builder()
                .packageName(context)
                .imVersion(NIMClient.getSDKVersion())
                .deviceId(context)
                .platform("Android")
                .name("XKit", true)
                .build());
    }
}
