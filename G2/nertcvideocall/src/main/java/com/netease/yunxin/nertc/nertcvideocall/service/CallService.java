package com.netease.yunxin.nertc.nertcvideocall.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.GsonUtils;
import com.netease.nimlib.app.AppForegroundWatcherCompat;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.nertcvideocall.bean.CustomInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.UIService;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

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

    private NERTCVideoCall nertcVideoCall;
    private NERTCCallingDelegate callingDelegate = new NERTCCallingDelegate() {

        @Override
        public void onError(int errorCode, String errorMsg, boolean needFinish) {
            if (needFinish) {
                cancelNotification();
            }
        }

        @Override
        public void onInvited(InvitedEvent invitedEvent) {
            ALog.d("NERTCVideoCallImpl","onInvited!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && AppForegroundWatcherCompat.isBackground()) {
                buildIncomingCallingNotification(invitedEvent);
                notificationManager.notify(INCOMING_CALL_NOTIFY_ID, incomingCallNotification);
            } else {
                //直接呼起
                Intent intent = initIntent(invitedEvent);
                if (intent != null) {
                    mContext.startActivity(intent);
                    ALog.d("NERTCVideoCallImpl","start new call In!");
                }
            }

        }


        @Override
        public void onUserEnter(String accId) {

        }

        @Override
        public void onCallEnd(String userId) {

        }

        @Override
        public void onUserLeave(String accountId) {

        }

        @Override
        public void onUserDisconnect(String userId) {

        }


        @Override
        public void onRejectByUserId(String userId) {

        }


        @Override
        public void onUserBusy(String userId) {

        }

        @Override
        public void onCancelByUserId(String userId) {
            cancelNotification();
        }


        @Override
        public void onCameraAvailable(String userId, boolean isVideoAvailable) {

        }

        @Override
        public void onAudioAvailable(String userId, boolean isVideoAvailable) {

        }

        @Override
        public void onDisconnect(int res) {
            cancelNotification();
        }

        @Override
        public void onUserNetworkQuality(Entry<String, Integer>[] stats) {

        }

        @Override
        public void onCallTypeChange(ChannelType type) {

        }

        @Override
        public void timeOut() {

        }

        @Override
        public void onFirstVideoFrameDecoded(String userId, int width, int height) {

        }

    };

    private CallService(Context context){
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
    private Intent initIntent(InvitedEvent invitedEvent) {
        CustomInfo customInfo = GsonUtils.fromJson(invitedEvent.getCustomInfo(), CustomInfo.class);
        if (customInfo != null && uiService != null) {
            if (customInfo.callType == CallParams.CallType.P2P) {
                Intent intent = new Intent();

                if (invitedEvent.getChannelBaseInfo().getType() == ChannelType.VIDEO) {
                    intent.setClass(mContext, uiService.getOneToOneVideoChat());
                    intent.putExtra(CallParams.INVENT_CHANNEL_TYPE, ChannelType.VIDEO.getValue());
                } else if (invitedEvent.getChannelBaseInfo().getType() == ChannelType.AUDIO) {
                    intent.setClass(mContext, uiService.getOneToOneAudioChat());
                    intent.putExtra(CallParams.INVENT_CHANNEL_TYPE, ChannelType.AUDIO.getValue());
                } else {
                    return null;
                }

                intent.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.getRequestId());
                intent.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.getChannelBaseInfo().getChannelId());
                intent.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.getFromAccountId());
                intent.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return intent;
            } else if (customInfo.callType == CallParams.CallType.TEAM) {
                Intent intentTeam = new Intent();
                customInfo.callUserList.add(invitedEvent.getFromAccountId());
                intentTeam.setClass(mContext, uiService.getGroupVideoChat());
                intentTeam.putExtra(CallParams.INVENT_USER_IDS, customInfo.callUserList);
                intentTeam.putExtra(CallParams.TEAM_CHAT_GROUP_ID, customInfo.groupID);
                intentTeam.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.getRequestId());
                intentTeam.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.getChannelBaseInfo().getChannelId());
                intentTeam.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.getFromAccountId());
                intentTeam.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                intentTeam.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return intentTeam;
            }
        }
        return null;
    }

    /**
     * 删除通知的intent
     *
     * @param invitedEvent
     * @return
     */
    private PendingIntent getDeleteIntent(InvitedEvent invitedEvent) {
        CustomInfo customInfo = GsonUtils.fromJson(invitedEvent.getCustomInfo(), CustomInfo.class);
        if (customInfo != null && uiService != null) {
            Intent intent = new Intent(mContext, NotificationBroadcastReceiver.class);
            intent.setAction("notification_cancelled");
            intent.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.getRequestId());
            intent.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.getChannelBaseInfo().getChannelId());
            intent.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.getFromAccountId());
            PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(mContext, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            return pendingIntentCancel;

        }
        return null;
    }

    private void buildIncomingCallingNotification(InvitedEvent invitedEvent) {
        String displayName = invitedEvent.getRequestId();
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
        if(instance == null){
            instance = new CallService(context);
        }
    }

    public static void stop() {
        if(instance == null){
            return;
        }
        instance.destroy();
        instance = null;
    }

    private void destroy(){
        if(nertcVideoCall != null ){
            if(callingDelegate != null){
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
