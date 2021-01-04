package com.netease.nim.demo.mixpush;

import com.huawei.hms.push.RemoteMessage;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nimlib.sdk.mixpush.HWPushMessageService;

public class DemoHwPushMessageService extends HWPushMessageService {

    private static final String TAG = "DemoHwPushMessageService";

    public void onNewToken(String token) {
        NimLog.i(TAG, " onNewToken, token=" + token);
    }

    /**
     * 透传消息， 需要用户自己弹出通知
     *
     * @param remoteMessage
     */
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NimLog.i(TAG, " onMessageReceived");
    }

    public void onMessageSent(String s) {
        NimLog.i(TAG, " onMessageSent");
    }

    public void onDeletedMessages() {
        NimLog.i(TAG, " onDeletedMessages");
    }

    public void onSendError(String var1, Exception var2) {
        NimLog.e(TAG, " onSendError, " + var1, var2);
    }
}
