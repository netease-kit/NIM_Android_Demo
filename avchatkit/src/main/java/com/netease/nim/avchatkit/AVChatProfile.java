package com.netease.nim.avchatkit;

import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.common.Handlers;
import com.netease.nim.avchatkit.notification.AVChatNotification;
import com.netease.nimlib.app.AppForegroundWatcherCompat;
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

    @Nullable
    private AVChatData backgroundIncomingCallData;
    @Nullable
    private AVChatNotification backgroundIncomingCallNotification;

    public boolean isBackgroundIncomingCall(String account) {
        if (backgroundIncomingCallData == null) {
            return false;
        }

        return TextUtils.equals(backgroundIncomingCallData.getAccount(), account);
    }

    private static class InstanceHolder {
        public final static AVChatProfile instance = new AVChatProfile();
    }

    public void launchIncomingCall(final AVChatData data, final String displayName, final int source) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && AppForegroundWatcherCompat.isBackground()) {
                    backgroundIncomingCallData = data;

                    backgroundIncomingCallNotification = new AVChatNotification(AVChatKit.getContext());
                    backgroundIncomingCallNotification.init(data.getAccount(), displayName);
                    backgroundIncomingCallNotification.activeIncomingCallNotification(true, backgroundIncomingCallData);
                } else {
                    // 启动，如果 task正在启动，则稍等一下
                    if (AVChatKit.isMainTaskLaunching()) {
                        launchIncomingCall(data, displayName, source);
                    } else {
                        launchActivityTimeout();
                        AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
                    }}
            }
        };
        Handlers.sharedHandler(AVChatKit.getContext()).postDelayed(runnable, 200);
    }

    public void removeBackgroundIncomingCall(boolean activeMissCall) {
        backgroundIncomingCallData = null;

        if (backgroundIncomingCallNotification != null) {
            backgroundIncomingCallNotification.activeIncomingCallNotification(false, null);
            if (activeMissCall) {
                backgroundIncomingCallNotification.activeMissCallNotification(true);
            }

            backgroundIncomingCallNotification = null;
        }
    }

    public void activityLaunched() {
        Handler handler = Handlers.sharedHandler(AVChatKit.getContext());
        handler.removeCallbacks(launchTimeout);

        removeBackgroundIncomingCall(false);
    }

    // 有些设备（比如OPPO、VIVO）默认不允许从后台broadcast receiver启动activity
    // 增加启动activity超时机制
    private void launchActivityTimeout() {
        Handler handler = Handlers.sharedHandler(AVChatKit.getContext());
        handler.removeCallbacks(launchTimeout);
        handler.postDelayed(launchTimeout, 3000);
    }

    private Runnable launchTimeout = new Runnable() {
        @Override
        public void run() {
            // 如果未成功启动，就恢复av chatting -> false
            setAVChatting(false);
        }
    };
}