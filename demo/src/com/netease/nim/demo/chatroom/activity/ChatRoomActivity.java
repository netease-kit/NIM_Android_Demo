package com.netease.nim.demo.chatroom.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.demo.R;
import com.netease.nim.demo.chatroom.fragment.ChatRoomFragment;
import com.netease.nim.uikit.business.chatroom.fragment.ChatRoomMessageFragment;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;

/**
 * 聊天室
 * Created by hzxuwen on 2015/12/14.
 */
public class ChatRoomActivity extends UI {
    private final static String EXTRA_ROOM_ID = "ROOM_ID";
    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    /**
     * 聊天室基本信息
     */
    private String roomId;
    private ChatRoomInfo roomInfo;
    private boolean hasEnterSuccess = false; // 是否已经成功登录聊天室
    private ChatRoomFragment fragment;

    /**
     * 子页面
     */
    private ChatRoomMessageFragment messageFragment;
    private AbortableFuture<EnterChatRoomResultData> enterRequest;

    public static void start(Context context, String roomId) {
        Intent intent = new Intent();
        intent.setClass(context, ChatRoomActivity.class);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room_activity);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        // 注册监听
        registerObservers(true);

        // 登录聊天室
        enterRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    @Override
    public void onBackPressed() {
        if (messageFragment == null || !messageFragment.onBackPressed()) {
            super.onBackPressed();
        }

        logoutChatRoom();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (messageFragment != null) {
            messageFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void enterRoom() {
        DialogMaker.showProgressDialog(this, null, "", true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (enterRequest != null) {
                    enterRequest.abort();
                    onLoginDone();
                    finish();
                }
            }
        }).setCanceledOnTouchOutside(false);
        hasEnterSuccess = false;
        EnterChatRoomData data = new EnterChatRoomData(roomId);

        enterRequest = NIMClient.getService(ChatRoomService.class).enterChatRoomEx(data, 1);
        enterRequest.setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData result) {
                onLoginDone();
                roomInfo = result.getRoomInfo();
                NimUIKit.enterChatRoomSuccess(result, false);
                initChatRoomFragment();
                initMessageFragment();
                hasEnterSuccess = true;
            }

            @Override
            public void onFailed(int code) {
                onLoginDone();
                if (code == ResponseCode.RES_CHATROOM_BLACKLIST) {
                    ToastHelper.showToast(ChatRoomActivity.this, "你已被拉入黑名单，不能再进入");
                } else if (code == ResponseCode.RES_ENONEXIST) {
                    ToastHelper.showToast(ChatRoomActivity.this, "聊天室不存在");
                } else {
                    ToastHelper.showToast(ChatRoomActivity.this, "enter chat room failed, code=" + code);
                }
                finish();
            }

            @Override
            public void onException(Throwable exception) {
                onLoginDone();
                ToastHelper.showToast(ChatRoomActivity.this, "enter chat room exception, e=" + exception.getMessage());
                finish();
            }
        });
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeOnlineStatus(onlineStatus, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeKickOutEvent(kickOutObserver, register);
    }

    private void logoutChatRoom() {
        NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
        onExitedChatRoom();
    }

    public void onExitedChatRoom() {
        NimUIKit.exitedChatRoom(roomId);
        finish();
    }

    Observer<ChatRoomStatusChangeData> onlineStatus = new Observer<ChatRoomStatusChangeData>() {
        @Override
        public void onEvent(ChatRoomStatusChangeData chatRoomStatusChangeData) {
            if (!chatRoomStatusChangeData.roomId.equals(roomId)) {
                return;
            }
            if (chatRoomStatusChangeData.status == StatusCode.CONNECTING) {
                DialogMaker.updateLoadingMessage("连接中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINING) {
                DialogMaker.updateLoadingMessage("登录中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINED) {
                if (fragment != null) {
                    fragment.updateOnlineStatus(true);
                }
            } else if (chatRoomStatusChangeData.status == StatusCode.UNLOGIN) {
                if (fragment != null) {
                    fragment.updateOnlineStatus(false);
                }

                // 登录成功后，断网重连交给云信SDK，如果重连失败，可以查询具体失败的原因
                if (hasEnterSuccess) {
                    int code = NIMClient.getService(ChatRoomService.class).getEnterErrorCode(roomId);
                    ToastHelper.showToast(ChatRoomActivity.this, "getEnterErrorCode=" + code);
                    LogUtil.d(TAG, "chat room enter error code:" + code);
                }
            } else if (chatRoomStatusChangeData.status == StatusCode.NET_BROKEN) {
                if (fragment != null) {
                    fragment.updateOnlineStatus(false);
                }
                ToastHelper.showToast(ChatRoomActivity.this, R.string.net_broken);
            }

            LogUtil.i(TAG, "chat room online status changed to " + chatRoomStatusChangeData.status.name());
        }
    };

    Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {
        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            ToastHelper.showToast(ChatRoomActivity.this, "被踢出聊天室，原因:" + chatRoomKickOutEvent.getReason());
            onExitedChatRoom();
        }
    };

    private void initChatRoomFragment() {
        fragment = (ChatRoomFragment) getSupportFragmentManager().findFragmentById(R.id.chat_rooms_fragment);
        if (fragment != null) {
            fragment.updateView();
        } else {
            // 如果Fragment还未Create完成，延迟初始化
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initChatRoomFragment();
                }
            }, 50);
        }
    }

    private void initMessageFragment() {
        messageFragment = (ChatRoomMessageFragment) getSupportFragmentManager().findFragmentById(R.id.chat_room_message_fragment);
        if (messageFragment != null) {
            messageFragment.init(roomId);
        } else {
            // 如果Fragment还未Create完成，延迟初始化
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initMessageFragment();
                }
            }, 50);
        }
    }

    private void onLoginDone() {
        enterRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    public ChatRoomInfo getRoomInfo() {
        return roomInfo;
    }
}
