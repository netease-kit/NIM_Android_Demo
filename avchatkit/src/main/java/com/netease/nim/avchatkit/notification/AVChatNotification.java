package com.netease.nim.avchatkit.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.R;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.constant.AVChatExtras;

/**
 * 音视频聊天通知栏
 * Created by huangjun on 2015/5/14.
 */
public class AVChatNotification {

    private Context context;

    private NotificationManager notificationManager;
    private Notification callingNotification;
    private Notification missCallNotification;
    private String account;
    private String displayName;
    private static final int CALLING_NOTIFY_ID = 111;
    private static final int MISS_CALL_NOTIFY_ID = 112;

    public AVChatNotification(Context context) {
        this.context = context;
    }

    public void init(String account, String displayName) {
        this.account = account;
        this.displayName = displayName;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        AVChatNotificationChannelCompat26.createNIMMessageNotificationChannel(context);
    }

    private void buildCallingNotification() {
        if (callingNotification == null) {
            Intent localIntent = new Intent();
            localIntent.setClass(context, AVChatActivity.class);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            String tickerText = String.format(context.getString(R.string.avchat_notification), displayName);
            int iconId = AVChatKit.getAvChatOptions().notificationIconRes;

            PendingIntent pendingIntent = PendingIntent.getActivity(context, CALLING_NOTIFY_ID, localIntent, PendingIntent
                    .FLAG_UPDATE_CURRENT);
            callingNotification = makeNotification(pendingIntent, context.getString(R.string.avchat_call), tickerText, tickerText,
                    iconId, false, false);
        }
    }

    private void buildMissCallNotification() {
        if (missCallNotification == null) {
            Intent notifyIntent = new Intent(context, AVChatKit.getAvChatOptions().entranceActivity);
            notifyIntent.putExtra(AVChatExtras.EXTRA_ACCOUNT, account);
            notifyIntent.putExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION, true);

            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifyIntent.setAction(Intent.ACTION_VIEW);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, CALLING_NOTIFY_ID, notifyIntent, PendingIntent
                    .FLAG_UPDATE_CURRENT);

            String title = context.getString(R.string.avchat_no_pickup_call);
            String tickerText = displayName + ": 【网络通话】";
            int iconId = R.drawable.avchat_no_pickup;

            missCallNotification = makeNotification(pendingIntent, title, tickerText, tickerText, iconId, true, true);
        }
    }

    private Notification makeNotification(PendingIntent pendingIntent, String title, String content, String tickerText,
                                          int iconId, boolean ring, boolean vibrate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AVChatNotificationChannelCompat26.getNIMChannelId(context));
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

        return builder.build();
    }

    public void activeCallingNotification(boolean active) {
        if (notificationManager != null) {
            if (active) {
                buildCallingNotification();
                notificationManager.notify(CALLING_NOTIFY_ID, callingNotification);
                AVChatKit.getNotifications().put(CALLING_NOTIFY_ID, callingNotification);
            } else {
                notificationManager.cancel(CALLING_NOTIFY_ID);
                AVChatKit.getNotifications().remove(CALLING_NOTIFY_ID);
            }
        }
    }

    public void activeMissCallNotification(boolean active) {
        if (notificationManager != null) {
            if (active) {
                buildMissCallNotification();
                notificationManager.notify(MISS_CALL_NOTIFY_ID, missCallNotification);
                AVChatKit.getNotifications().put(MISS_CALL_NOTIFY_ID, callingNotification);
            } else {
                notificationManager.cancel(MISS_CALL_NOTIFY_ID);
                AVChatKit.getNotifications().remove(MISS_CALL_NOTIFY_ID);
            }
        }
    }
}
