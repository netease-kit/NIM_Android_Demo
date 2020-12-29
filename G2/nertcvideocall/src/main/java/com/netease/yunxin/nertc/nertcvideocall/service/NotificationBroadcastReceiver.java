package com.netease.yunxin.nertc.nertcvideocall.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

/**
 * 删除通知时实现拒绝功能
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private String inventRequestId;
    private String inventChannelId;
    private String inventFromAccountId;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        inventRequestId = intent.getStringExtra(CallParams.INVENT_REQUEST_ID);
        inventChannelId = intent.getStringExtra(CallParams.INVENT_CHANNEL_ID);
        inventFromAccountId = intent.getStringExtra(CallParams.INVENT_FROM_ACCOUNT_ID);

        if (action.equals("notification_cancelled")) {
            //处理滑动清除和点击删除事件
            rejectInvited();
        }
    }

    private void rejectInvited() {
        NERTCVideoCall nertcVideoCall = NERTCVideoCall.sharedInstance();
        if (!TextUtils.isEmpty(inventRequestId) && !TextUtils.isEmpty(inventChannelId) && !TextUtils.isEmpty(inventFromAccountId)) {
            InviteParamBuilder invitedParam = new InviteParamBuilder(inventChannelId, inventFromAccountId, inventRequestId);
            nertcVideoCall.reject(invitedParam, null);
        }
    }
}
