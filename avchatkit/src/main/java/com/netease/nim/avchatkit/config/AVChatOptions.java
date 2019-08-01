package com.netease.nim.avchatkit.config;

import android.app.Activity;
import android.content.Context;

/**
 * 音视频初始化配置
 * Created by winnie on 2017/12/7.
 */

public class AVChatOptions {

    /**
     * 通知入口
     */
    public Class<? extends Activity> entranceActivity;

    /**
     * 通知栏icon
     */
    public int notificationIconRes;

    /**
     * 被踢出时，调用的方法
     */
    public void logout(Context context){

    }
}
