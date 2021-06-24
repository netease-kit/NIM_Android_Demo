/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public interface UIService {
    /**
     * 获取一对一音频通话时启动的 activity 页面 class
     */
    Class<? extends Activity> getOneToOneAudioChat();

    /**
     * 获取一对一视频通话时启动的 activity 页面 class
     */
    Class<? extends Activity> getOneToOneVideoChat();

    /**
     * 获取群组通话时启动的 activity 页面 class
     */
    Class<? extends Activity> getGroupVideoChat();

    /**
     * 获取呼叫组件产生呼叫推送时的本地图标资源 id
     */
    int getNotificationIcon();

    /**
     *
     * 获取呼叫组件产生呼叫推送时的本地图标资源 id（小图），目前已经废弃返回 0
     */
    @Deprecated
    int getNotificationSmallIcon();

    /**
     * 群组通话邀请他人时触发联系人的选择列表
     *
     * @param context         上下文
     * @param groupId         群组id
     * @param excludeUserList 列表中已选择用户，不需要进行选择
     * @param requestCode     联系人选择通过 {@link Activity#startActivityForResult(Intent, int)} 实现，对应的请求码
     */
    void startContactSelector(Context context, String groupId, List<String> excludeUserList, int requestCode);

}
