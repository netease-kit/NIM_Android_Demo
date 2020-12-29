package com.netease.yunxin.nertc.nertcvideocall.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo;
import com.netease.nimlib.app.AppForegroundWatcherCompat;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.yunxin.nertc.nertcvideocall.R;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.UIService;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.nertcvideocall.bean.CustomInfo;
import com.netease.yunxin.nertc.nertcvideocall.utils.Utils;

public class CallService extends Service {
    private static final int NOTIFICATION_ID = 1024;

    private static final int INCOMING_CALL_NOTIFY_ID = 1025;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && AppForegroundWatcherCompat.isBackground()) {
                buildIncomingCallingNotification(invitedEvent);
                notificationManager.notify(INCOMING_CALL_NOTIFY_ID, incomingCallNotification);
            } else {
                //直接呼起
                Intent intent = initIntent(invitedEvent);
                if (intent != null) {
                    startActivity(intent);
                }
            }

        }


        @Override
        public void onUserEnter(long uid,String accId) {

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
        public void onCameraAvailable(long userId, boolean isVideoAvailable) {

        }

        @Override
        public void onAudioAvailable(long userId, boolean isVideoAvailable) {

        }

        @Override
        public void onUserNetworkQuality(NERtcNetworkQualityInfo[] stats) {

        }

        @Override
        public void onCallTypeChange(ChannelType type) {

        }

        @Override
        public void timeOut() {

        }

    };

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
            if (customInfo.callType == Utils.ONE_TO_ONE_CALL) {
                Intent intent = new Intent();

                if (invitedEvent.getChannelBaseInfo().getType() == ChannelType.VIDEO) {
                    intent.setClass(CallService.this, uiService.getOneToOneVideoChat());
                    intent.putExtra(CallParams.INVENT_CHANNEL_TYPE, ChannelType.VIDEO.getValue());
                } else if (invitedEvent.getChannelBaseInfo().getType() == ChannelType.AUDIO) {
                    intent.setClass(CallService.this, uiService.getOneToOneAudioChat());
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
            } else if (customInfo.callType == Utils.GROUP_CALL) {
                Intent intentTeam = new Intent();
                customInfo.callUserList.add(invitedEvent.getFromAccountId());
                intentTeam.setClass(CallService.this, uiService.getGroupVideoChat());
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
            Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
            intent.setAction("notification_cancelled");
            intent.putExtra(CallParams.INVENT_REQUEST_ID, invitedEvent.getRequestId());
            intent.putExtra(CallParams.INVENT_CHANNEL_ID, invitedEvent.getChannelBaseInfo().getChannelId());
            intent.putExtra(CallParams.INVENT_FROM_ACCOUNT_ID, invitedEvent.getFromAccountId());
            PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            return pendingIntentCancel;

        }
        return null;
    }

    private void buildIncomingCallingNotification(InvitedEvent invitedEvent) {
        String displayName = invitedEvent.getRequestId();
        Intent notifyIntent = initIntent(invitedEvent);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, incomingCallChannel);
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
        if (ServiceUtils.isServiceRunning(CallService.class)) {
            return;
        }
        Intent starter = new Intent(context, CallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(starter);
        } else {
            context.startService(starter);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取服务通知
        Notification notification = createForegroundNotification();
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initNERTCCall();
    }

    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "NERTC Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(uiService.getNotificationSmallIcon());
        //通知标题
        builder.setContentTitle(getString(R.string.app_name));
        //通知内容
        builder.setContentText("正在运行中");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());

        //创建通知并返回
        return builder.build();
    }

    private void initNERTCCall() {
        nertcVideoCall = NERTCVideoCall.sharedInstance();
        nertcVideoCall.addServiceDelegate(callingDelegate);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nertcVideoCall != null) {
            nertcVideoCall.removeDelegate(callingDelegate);
        }

        NERTCVideoCall.destroySharedInstance();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
