package com.netease.nim.uikit.api.wrapper;

import android.util.Log;

import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.RevokeType;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;

/**
 * 云信消息撤回观察者
 */

public class NimMessageRevokeObserver implements Observer<RevokeMsgNotification> {
    private static final String TAG = "NimMsgRevokeObserver";

    @Override
    public void onEvent(RevokeMsgNotification notification) {
        if (notification == null || notification.getMessage() == null) {
            return;
        }
        Log.i(TAG, String.format("notification type=%s, postscript=%s", notification.getNotificationType(), notification.getCustomInfo()));
        if (notification.getRevokeType() == RevokeType.P2P_ONE_WAY_DELETE_MSG ||
            notification.getRevokeType() == RevokeType.TEAM_ONE_WAY_DELETE_MSG) {
            return;
        }
        MessageHelper.getInstance().onRevokeMessage(notification.getMessage(), notification.getRevokeAccount());
    }
}
