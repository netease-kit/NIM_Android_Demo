package com.netease.nim.demo.chatroom.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.netease.nim.demo.R;
import com.netease.nim.demo.chatroom.constants.Extras;
import com.netease.nim.demo.chatroom.constants.EnterMode;
import com.netease.nim.demo.chatroom.fragment.ChatRoomListFragment;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.string.MD5;

/**
 * Created by hzsunyj on 2019-09-02.
 */
public class ChatRoomIndependentActivity extends UI {

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ChatRoomIndependentActivity.class);
        context.startActivity(intent);
    }

    private EditText appKeyET, accountET, pwdET;

    private String lastAppKey, lastAccount, lastPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_rooms_independent);
        findViews();
        setViewsListener();
    }

    private void findViews() {
        appKeyET = findView(R.id.independent);
        accountET = findView(R.id.account);
        pwdET = findView(R.id.pwd);
        appKeyET.setText("a24e6c8a956a128bd50bdffe69b405ff");
    }

    private void setViewsListener() {
        findView(R.id.room_list).setOnClickListener(v -> {
            String appKey = appKeyET.getText().toString();
            if (TextUtils.isEmpty(appKey)) {
                ToastHelper.showToast(ChatRoomIndependentActivity.this,
                                      getString(R.string.appkey_cannot_empty));
                return;
            }
            String value = accountET.getText().toString();
            String password = pwdET.getText().toString();
            if (TextUtils.equals(appKey, lastAppKey) && TextUtils.equals(lastAccount, value) &&
                TextUtils.equals(lastPwd, password)) {
                return;
            } else {
                lastAccount = value;
                lastAppKey = appKey;
                lastPwd = password;
            }
            ChatRoomListFragment fragment = new ChatRoomListFragment();
            fragment.setContainerId(R.id.message_fragment_container);
            Bundle args = new Bundle();
            args.putInt(Extras.MODE, EnterMode.INDEPENDENT);
            args.putString(Extras.APP_KEY, appKey);
            args.putString(Extras.ACCOUNT, value);
            password = MD5.getStringMD5(password);
            args.putString(Extras.PWD, password);
            fragment.setArguments(args);
            switchContent(fragment);
        });
    }
}
