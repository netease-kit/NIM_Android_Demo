package com.netease.yunxin.nertc.nertcvideocall.push;

import android.util.Log;

import com.huawei.hms.push.RemoteMessage;
import com.netease.nimlib.sdk.mixpush.HWPushMessageService;

public class SignallingHuaweiPushMessageService extends HWPushMessageService {

    private static final String TAG = "SignallingPushService";

    public void onNewToken(String token) {
        Log.i(TAG, " onNewToken, token=" + token);
    }

    /**
     * 透传消息， 需要用户自己弹出通知
     *
     * @param remoteMessage
     */
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, " onMessageReceived");
    }

    public void onMessageSent(String s) {
        Log.i(TAG, " onMessageSent");
    }

    public void onDeletedMessages() {
        Log.i(TAG, " onDeletedMessages");
    }

    public void onSendError(String var1, Exception var2) {
        Log.e(TAG, " onSendError, " + var1, var2);
    }
}

