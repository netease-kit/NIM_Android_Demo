/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.RequestCallback;

import androidx.annotation.NonNull;


/**
 * 初始化options
 */
public class VideoCallOptions {
    //rtc Option
    public final NERtcOption rtcOption;

    //UI 注入
    public final UIService uiService;

    public final UserInfoInitCallBack userInfoInitCallBack;

    /**
     * VideoCallOptions 构造函数
     *
     * @param rtcOption            NERtc sdk 初始化配置详细参考 {@link NERtcOption}
     * @param uiService            用户呼叫/被叫时展示的页面设置入口
     * @param userInfoInitCallBack 通过组件进行登录 IM sdk 时传入，登录成功回调。
     *                             也可以设置为 null，依赖
     *                             {@link NERTCVideoCall#login(String, String, RequestCallback)}接口的回调。
     */
    public VideoCallOptions(NERtcOption rtcOption, @NonNull UIService uiService, UserInfoInitCallBack userInfoInitCallBack) {
        this.rtcOption = rtcOption;
        this.uiService = uiService;
        this.userInfoInitCallBack = userInfoInitCallBack;
    }

    public NERtcOption getRtcOption() {
        return rtcOption;
    }

    public UIService getUiService() {
        return uiService;
    }

    public UserInfoInitCallBack getUserInfoInitCallBack() {
        return userInfoInitCallBack;
    }
}
