package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.netease.nim.demo.mixpush.DemoMixPushMessageHandler;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;

import java.util.HashMap;
import java.util.Set;

public class MixPushActivity extends Activity {
    private static final String TAG = "MixPushActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NimLog.i(TAG, "mix_push onCreate");
        parseIntent();
        finish();
    }

    void parseIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Uri uri = intent.getData();
        Bundle bundle = intent.getExtras();
        HashMap<String, String> map;
        if (uri != null) {
            Set<String> parameterSet = uri.getQueryParameterNames();
            map = new HashMap<>((parameterSet.size() << 2) / 3 + 1);
            String value;
            for (String p : parameterSet) {
                value = uri.getQueryParameter(p);
                if (value == null) {
                    continue;
                }
                map.put(p, value);
            }
        } else if (bundle != null) {
            map = new HashMap<>((bundle.size() << 2) / 3 + 1);
            for (String key : bundle.keySet()) {
                Object valueObj = bundle.get(key);
                map.put(key, valueObj == null ? null : valueObj.toString());
            }
        } else {
            map = new HashMap<>(0);
        }
        new DemoMixPushMessageHandler().onNotificationClicked(this.getApplicationContext(), map);
    }
}
