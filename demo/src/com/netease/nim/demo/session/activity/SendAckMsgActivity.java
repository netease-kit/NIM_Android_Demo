package com.netease.nim.demo.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;

/**
 * 发送已读回执消息界面
 * Created by winnie on 2018/3/14.
 */

public class SendAckMsgActivity extends UI {
    private static final String EXTRA_SESSIONID = "session_id";
    public static final String EXTRA_CONTENT = "extra_content";

    private String sessionId;

    private EditText msgEdit;

    public static void startActivity(Context context, String sessionId, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SESSIONID, sessionId);
        intent.setClass(context, SendAckMsgActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_ack_msg_layout);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.send_ack_msg;
        options.navigateId = R.drawable.actionbar_dark_back_icon;
        setToolBar(R.id.toolbar, options);

        sessionId = getIntent().getStringExtra(EXTRA_SESSIONID);

        msgEdit = findView(R.id.ack_msg_edit_text);
        Button btn = findView(R.id.send_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAckMsg(msgEdit.getText().toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideInput(SendAckMsgActivity.this, msgEdit);
    }

    private void sendAckMsg(String msg) {
        hideInput(SendAckMsgActivity.this, msgEdit);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONTENT, msg);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private static void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
