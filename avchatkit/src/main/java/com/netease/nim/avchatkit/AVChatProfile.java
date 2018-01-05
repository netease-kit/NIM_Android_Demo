package com.netease.nim.avchatkit;

import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.common.Handlers;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

/**
 * Created by huangjun on 2015/5/12.
 */
public class AVChatProfile {

    private final String TAG = "AVChatProfile";

    private boolean isAVChatting = false; // 是否正在音视频通话

    public static AVChatProfile getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isAVChatting() {
        return isAVChatting;
    }

    public void setAVChatting(boolean chating) {
        isAVChatting = chating;
    }

    private static class InstanceHolder {
        public final static AVChatProfile instance = new AVChatProfile();
    }

    public void launchActivity(final AVChatData data, final String displayName, final int source) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 启动，如果 task正在启动，则稍等一下
                if (!AVChatKit.isMainTaskLaunching()) {

                    AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
                } else {
                    launchActivity(data, displayName, source);
                }
            }
        };
        Handlers.sharedHandler(AVChatKit.getContext()).postDelayed(runnable, 200);
    }
}