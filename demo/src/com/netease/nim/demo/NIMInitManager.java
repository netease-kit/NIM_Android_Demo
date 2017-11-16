package com.netease.nim.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.netease.nim.demo.avchat.AVChatProfile;
import com.netease.nim.demo.avchat.activity.AVChatActivity;
import com.netease.nim.demo.avchat.receiver.PhoneCallStateObserver;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.event.OnlineStateEventManager;
import com.netease.nim.demo.rts.activity.RTSActivity;
import com.netease.nim.demo.team.TeamAVChatHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.BroadcastMessage;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;

import java.util.Map;

/**
 * Created by hzchenkang on 2017/9/26.
 * 用于初始化时，注册全局的广播、云信观察者等等云信相关业务
 */

public class NIMInitManager {

    private static final String TAG = "NIMInitManager";

    private NIMInitManager() {
    }

    private static class InstanceHolder {
        static NIMInitManager receivers = new NIMInitManager();
    }

    public static NIMInitManager getInstance() {
        return InstanceHolder.receivers;
    }

    public void init(boolean register) {
        // 注册通知消息过滤器
        registerIMMessageFilter();

        // 注册语言变化监听广播
        registerLocaleReceiver(register);

        // 注册全局云信sdk 观察者
        registerGlobalObservers(register);

        // 初始化在线状态事件
        OnlineStateEventManager.init();
    }

    private void registerGlobalObservers(boolean register) {
        // 注册网络通话来电
        registerAVChatIncomingCallObserver(register);

        // 注册白板会话
        registerRTSIncomingObserver(register);

        // 注册云信全员广播
        registerBroadcastMessages(register);
    }

    /**
     * 注册音视频来电观察者
     *
     * @param register
     */
    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                String extra = data.getExtra();
                Log.e("Extra", "Extra Message->" + extra);
                if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
                        || AVChatProfile.getInstance().isAVChatting()
                        || TeamAVChatHelper.sharedInstance().isTeamAVChatting()
                        || AVChatManager.getInstance().getCurrentChatId() != 0) {
                    LogUtil.i(TAG, "reject incoming call data =" + data.toString() + " as local phone is not idle");
                    AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);
                    return;
                }
                // 有网络来电打开AVChatActivity
                AVChatProfile.getInstance().setAVChatting(true);
                AVChatProfile.getInstance().launchActivity(data, AVChatActivity.FROM_BROADCASTRECEIVER);
            }
        }, register);
    }

    /**
     * 注册白板来电观察者
     *
     * @param register
     */
    private void registerRTSIncomingObserver(boolean register) {
        RTSManager.getInstance().observeIncomingSession(new Observer<RTSData>() {
            @Override
            public void onEvent(RTSData rtsData) {
                RTSActivity.incomingSession(DemoCache.getContext(), rtsData, RTSActivity.FROM_BROADCAST_RECEIVER);
            }
        }, register);
    }

    private void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            DemoCache.getContext().registerReceiver(localeReceiver, filter);
        } else {
            DemoCache.getContext().unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };

    private void updateLocale() {
        Context context = DemoCache.getContext();
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = context.getString(R.string.nim_status_bar_multi_messages_incoming);
        strings.status_bar_image_message = context.getString(R.string.nim_status_bar_image_message);
        strings.status_bar_audio_message = context.getString(R.string.nim_status_bar_audio_message);
        strings.status_bar_custom_message = context.getString(R.string.nim_status_bar_custom_message);
        strings.status_bar_file_message = context.getString(R.string.nim_status_bar_file_message);
        strings.status_bar_location_message = context.getString(R.string.nim_status_bar_location_message);
        strings.status_bar_notification_message = context.getString(R.string.nim_status_bar_notification_message);
        strings.status_bar_ticker_text = context.getString(R.string.nim_status_bar_ticker_text);
        strings.status_bar_unsupported_message = context.getString(R.string.nim_status_bar_unsupported_message);
        strings.status_bar_video_message = context.getString(R.string.nim_status_bar_video_message);
        strings.status_bar_hidden_message_content = context.getString(R.string.nim_status_bar_hidden_msg_content);
        NIMClient.updateStrings(strings);
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    private void registerIMMessageFilter() {
        NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (UserPreferences.getMsgIgnore() && message.getAttachment() != null) {
                    if (message.getAttachment() instanceof UpdateTeamAttachment) {
                        UpdateTeamAttachment attachment = (UpdateTeamAttachment) message.getAttachment();
                        for (Map.Entry<TeamFieldEnum, Object> field : attachment.getUpdatedFields().entrySet()) {
                            if (field.getKey() == TeamFieldEnum.ICON) {
                                return true;
                            }
                        }
                    } else if (message.getAttachment() instanceof AVChatAttachment) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * 注册云信全服广播接收器
     *
     * @param register
     */
    private void registerBroadcastMessages(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeBroadcastMessage(new Observer<BroadcastMessage>() {
            @Override
            public void onEvent(BroadcastMessage broadcastMessage) {
                Toast.makeText(DemoCache.getContext(), broadcastMessage.getContent(), Toast.LENGTH_SHORT).show();
            }
        }, register);
    }

}
