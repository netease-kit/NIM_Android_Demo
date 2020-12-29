package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.lava.nertc.sdk.NERtcOption;

import org.jetbrains.annotations.NotNull;

/**
 * 初始化options
 */
public class VideoCallOptions {
    //rtc Option
    private NERtcOption rtcOption;

    //UI 注入
    private UIService uiService;

    private UserInfoInitCallBack userInfoInitCallBack;

    public VideoCallOptions(NERtcOption rtcOption, @NotNull UIService uiService, UserInfoInitCallBack userInfoInitCallBack) {
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
