/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.netease.nimlib.app.AppForegroundWatcherCompat;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.UIService;
import com.netease.yunxin.nertc.nertcvideocall.model.VideoCallOptions;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

import androidx.core.app.NotificationCompat;

/**
 * 邀请消息分发
 */
public class CallService {

    private static final int INCOMING_CALL_NOTIFY_ID = 1025;

    private Context mContext;

    private static CallService instance;

    //UI相关注册
    private static UIService uiService;

    private NotificationManager notificationManager;

    private Notification incomingCallNotification;

    /**
     * 记录 app 在后台时收到的邀请信息
     */
    InvitedInfo bgInvitedInfo = null;

    private NERTCVideoCall nertcVideoCall;
    private NERTCCallingDelegate callingDelegate = new NERTCCallingDelegate() {

        @Override
        public void onError(int errorCode, String errorMsg, boolean needFinish) {
            if (needFinish) {
                cancelNotification();
            }
        }

        @Override
        public void onInvited(InvitedInfo invitedInfo) {
            ALog.dApi("CallService", new ParameterMap("onInvited").append("InvitedInfo", invitedInfo));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && AppForegroundWatcherCompat.isBackground()) {
                buildIncomingCallingNotification(invitedInfo);
                notificationManager.notify(INCOMING_CALL_NOTIFY_ID, incomingCallNotification);
                bgInvitedInfo = invitedInfo;
            } else {
                //直接呼起
                Intent intent = initIntent(invitedInfo);
                if (intent != null) {
                    mContext.startActivity(intent);
                    ALog.d("NERTCVideoCallImpl", "start new call In!");
                }
            }

        }


        @Override
        public void onUserEnter(String accId) {

        }

        @Override
        public void onCallEnd(String userId) {
            bgInvitedInfo = null;
        }

        @Override
        public void onUserLeave(String accountId) {

        }

        @Override
        public void onUserDisconnect(String userId) {
            bgInvitedInfo = null;
        }


        @Override
        public void onRejectByUserId(String userId) {
            bgInvitedInfo = null;
        }


        @Override
        public void onUserBusy(String userId) {
            bgInvitedInfo = null;
        }

        @Override
        public void onCancelByUserId(String userId) {
            cancelNotification();
            bgInvitedInfo = null;
        }


        @Override
        public void onCameraAvailable(String userId, boolean isVideoAvailable) {

        }

        @Override
        public void onVideoMuted(String userId, boolean isMuted) {

        }

        @Override
        public void onAudioMuted(String userId, boolean isMuted) {

        }

        @Override
        public void onJoinChannel(String accId, long uid, String channelName, long rtcChannelId) {
            bgInvitedInfo = null;
        }

        @Override
        public void onAudioAvailable(String userId, boolean isVideoAvailable) {

        }

        @Override
        public void onDisconnect(int res) {
            cancelNotification();
            bgInvitedInfo = null;
        }

        @Override
        public void onUserNetworkQuality(Entry<String, Integer>[] stats) {

        }

        @Override
        public void onCallTypeChange(ChannelType type) {

        }

        @Override
        public void timeOut() {
            bgInvitedInfo = null;
        }

        @Override
        public void onFirstVideoFrameDecoded(String userId, int width, int height) {

        }

    };

    private CallService(Context context) {
        this.mContext = context;
        initNERTCCall();
    }

    private void cancelNotification() {
        if (incomingCallNotification != null) {
            notificationManager.cancel(INCOMING_CALL_NOTIFY_ID);
        }
    }

    /**
     * 跳转到接通页面的intent
     *
     * @param invitedEvent
     * @return
     */
    private Intent initIntent(InvitedInfo invitedEvent) {
        if (uiService != null) {
            if (invitedEvent.callType == CallParams.CallType.P2P) {
                Intent intent = new Intent();

                if (invitedEvent.channelType == ChannelType.VIDEO.getValue()) {
                    intent.setClass(mContext, uiService.getOneToOneVideoChat());
                    intent.putExtra(CallParams.INVENT_CHANNEL_TYPE, ChannelType.VIDEO.getValue());
                } else if (invitedEvent.channelType == ChannelType.AUDIO.getValue()) {
                    intent.setClass(mContext, uiService.getOneToOneAudioChat());
                    intent.putExtra(CallParams.INVENT_CHANNEL_TYPE, ChannelType.AUDIO.getValue());
                } else {
                    return null;
                }

                intent.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.requestId);
                intent.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.channelId);
                intent.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.invitor);
                intent.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return intent;
            } else if (invitedEvent.callType == CallParams.CallType.TEAM) {
                Intent intentTeam = new Intent();
                intentTeam.setClass(mContext, uiService.getGroupVideoChat());
                intentTeam.putExtra(CallParams.INVENT_USER_IDS, invitedEvent.userIds);
                intentTeam.putExtra(CallParams.TEAM_CHAT_GROUP_ID, invitedEvent.groupId);
                intentTeam.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.requestId);
                intentTeam.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.channelId);
                intentTeam.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.invitor);
                intentTeam.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                intentTeam.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return intentTeam;
            }
        }
        return null;
    }

    /**
     * 尝试恢复被叫在后台无法展示的页面UI
     *
     * @return true 恢复成功，false 恢复失败。
     */
    public boolean tryResumeInvitedUI() {
        if (bgInvitedInfo == null || mContext == null || uiService == null) {
            ALog.d("NERTCVideoCallImpl", "bgInviteInfo, mContext or uiService is null.");
            return false;
        }


        //直接呼起
        Intent intent = initIntent(bgInvitedInfo);
        if (intent == null) {
            ALog.d("NERTCVideoCallImpl", "Intent is null.");
            return false;
        }
        mContext.startActivity(intent);
        bgInvitedInfo = null;
        ALog.d("NERTCVideoCallImpl", "start new call In!");
        return true;
    }

    /**
     * 删除通知的intent
     *
     * @param invitedEvent
     * @return
     */
    private PendingIntent getDeleteIntent(InvitedInfo invitedEvent) {
        if (uiService != null) {
            Intent intent = new Intent(mContext, NotificationBroadcastReceiver.class);
            intent.setAction("notification_cancelled");
            intent.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.requestId);
            intent.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.channelId);
            intent.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.invitor);
            PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(mContext, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            return pendingIntentCancel;

        }
        return null;
    }

    private void buildIncomingCallingNotification(InvitedInfo invitedEvent) {
        String displayName = invitedEvent.requestId;
        Intent notifyIntent = initIntent(invitedEvent);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                INCOMING_CALL_NOTIFY_ID,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "您有新的来电";
        String content = displayName + "：【网络通话】";
        String tickerText = displayName + " " + title;
        int iconId = CallService.uiService.getNotificationIcon();

        NotificationCompat.Builder incomingCallNotificationBuilder = makeIncomingCallNotificationBuilder(pendingIntent, title, content, tickerText, iconId, true, true);
        incomingCallNotificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        incomingCallNotificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL);
        incomingCallNotificationBuilder.setFullScreenIntent(pendingIntent, true);
        incomingCallNotificationBuilder.setAutoCancel(true);
        incomingCallNotificationBuilder.setDeleteIntent(getDeleteIntent(invitedEvent));
        incomingCallNotification = incomingCallNotificationBuilder.build();
    }

    private NotificationCompat.Builder makeIncomingCallNotificationBuilder(PendingIntent pendingIntent, String title, String content, String tickerText,
                                                                           int iconId, boolean ring, boolean vibrate) {

        // 唯一的通知通道的id.
        String incomingCallChannel = "incoming_call_notification_channel_id_02";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "incall_call_channel";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(incomingCallChannel, channelName, importance);
            notificationChannel.setDescription("Channel description");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, incomingCallChannel);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setSmallIcon(iconId);
        int defaults = Notification.DEFAULT_LIGHTS;
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (ring) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        builder.setDefaults(defaults);

        return builder;
    }

    public static void start(Context context, UIService uiService) {
        CallService.uiService = uiService;
        if (instance == null) {
            instance = new CallService(context);
        }
    }

    /**
     * 获取 CallService对应实例，在{@link NERTCVideoCall#setupAppKey(Context, String, VideoCallOptions)}
     * 方法执行之前调用得到结果为 null
     */
    public static CallService getInstance() {
        return instance;
    }

    public static void stop() {
        if (instance == null) {
            return;
        }
        instance.destroy();
        instance = null;
    }

    private void destroy() {
        if (nertcVideoCall != null) {
            if (callingDelegate != null) {
                nertcVideoCall.removeDelegate(callingDelegate);
            }
        }
        NERTCVideoCall.destroySharedInstance();
    }

    private void initNERTCCall() {
        nertcVideoCall = NERTCVideoCall.sharedInstance();
        nertcVideoCall.addServiceDelegate(callingDelegate);
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
