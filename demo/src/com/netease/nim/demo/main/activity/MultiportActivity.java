package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.OnlineClient;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hzxuwen on 2015/7/8.
 */
public class MultiportActivity extends TActionBarActivity implements View.OnClickListener{
    private final static String EXTRA_DATA = "EXTRA_DATA";

    private View computerLayout;
    private TextView computerLogout;
    private View webLayout;
    private TextView webLogout;
    private View line1;
    private View line2;

    private List<OnlineClient> onlineClients;

    public static void startActivity(Context context, List<OnlineClient> onlineClients) {
        Intent intent = new Intent();
        intent.setClass(context, MultiportActivity.class);
        intent.putExtra(EXTRA_DATA, (Serializable)onlineClients);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiport_activity);
        setTitle(R.string.multiport_manager);

        findViews();
        setListener();
        parseIntent();
    }

    private void findViews() {
        computerLayout = findViewById(R.id.computer_version_layout);
        computerLogout = (TextView) findViewById(R.id.computer_logout);
        webLayout = findViewById(R.id.web_version_layout);
        webLogout = (TextView) findViewById(R.id.web_logout);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
    }

    private void setListener() {
        computerLogout.setOnClickListener(this);
        webLogout.setOnClickListener(this);
    }

    private void parseIntent() {
        onlineClients = (List<OnlineClient>)getIntent().getSerializableExtra(EXTRA_DATA);
        updateView();
    }

    private void updateView() {
        for(OnlineClient client : onlineClients) {
            switch (client.getClientType()) {
                case ClientType.Windows:
                    computerLayout.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.VISIBLE);
                    break;
                case ClientType.Web:
                    webLayout.setVisibility(View.VISIBLE);
                    line2.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        OnlineClient client = null;
        final boolean finished = onlineClients.size() == 1;
        switch (v.getId()) {
            case R.id.computer_logout:
                for (OnlineClient c : onlineClients) {
                    if (c.getClientType() == ClientType.Windows) {
                        client = c;
                    }
                }
                kickOtherOut(client, computerLayout, line1, finished);
                break;
            case R.id.web_logout:
                for (OnlineClient c : onlineClients) {
                    if (c.getClientType() == ClientType.Web) {
                        client = c;
                    }
                }
                kickOtherOut(client, webLayout, line2, finished);
                break;
            default:
                break;
        }
    }

    private void kickOtherOut(OnlineClient client, final View layout, final View line, final boolean finished) {
        NIMClient.getService(AuthService.class).kickOtherClient(client).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                hideLayout(layout, line, finished);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    private void hideLayout(View layout, View line, boolean finished) {
        layout.setVisibility(View.GONE);
        line.setVisibility(View.GONE);
        if(finished) {
            finish();
        }
    }
}
