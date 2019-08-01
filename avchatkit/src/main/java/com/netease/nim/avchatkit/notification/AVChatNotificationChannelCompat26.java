package com.netease.nim.avchatkit.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;


import java.util.Locale;

/**
 * 适配Android O版本的通知栏
 * 采用
 * <p>
 * Created by huangjun on 2017/11/8.
 */

class AVChatNotificationChannelCompat26 {

    private static final String NIM_CHANNEL_ID = "nim_avchat_tip_channel_001";
    private static String NIM_CHANNEL_NAME = "AV chat tip channel";
    private static String NIM_CHANNEL_DESC = "AV chat tip notification";

    static String getNIMChannelId(Context context) {
        /*
         * 适配关键：target 8.0+必须设置一个channel，8.0以下一定要返回null！否则通知栏弹不出
         */
        return isBuildAndTargetO(context) ? NIM_CHANNEL_ID : null;
    }

    static void createNIMMessageNotificationChannel(Context context) {
        /*
         * 适配关键：只有8.0+的机器才能创建NotificationChannel，否则会找不到类。target 8.0+才需要去创建一个channel，否则就用默认通道即null
         */
        if (!isBuildAndTargetO(context)) {
            return;
        }
        configLanguage(context);
        NotificationChannel channel;
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager != null) {
            channel = manager.getNotificationChannel(NIM_CHANNEL_ID); // 已经存在就不要再创建了，无法修改通道配置
            if (channel == null) {
                channel = buildNIMMessageChannel();
                manager.createNotificationChannel(channel);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel buildNIMMessageChannel() {
        NotificationChannel channel = new NotificationChannel(NIM_CHANNEL_ID, NIM_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(NIM_CHANNEL_DESC);
        channel.enableVibration(true);
        channel.setShowBadge(false);
        return channel;
    }

    private static boolean isBuildAndTargetO(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.O;
    }

    private static void configLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language != null && language.endsWith("zh")) {
            // default channel
            NIM_CHANNEL_NAME = "音视频聊天通知";
            NIM_CHANNEL_DESC = "音视频聊天通知";
        }
    }
}
