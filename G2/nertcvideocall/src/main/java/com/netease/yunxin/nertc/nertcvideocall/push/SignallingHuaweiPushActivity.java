package com.netease.yunxin.nertc.nertcvideocall.push;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

import java.util.Set;

public class SignallingHuaweiPushActivity extends Activity {
    private static final String TAG = "MixPushActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "HuaweiPushActivity onCreate");
        parseIntent();
        finish();
    }

    void parseIntent() {
        if (getIntent() == null) {
            return;
        }

        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d(TAG, "HuaweiPushActivity uri:" + uri.toString());

            Intent intent = new Intent();

            Set<String> parameterSet = uri.getQueryParameterNames();
            for (String p : parameterSet) {
                String value = uri.getQueryParameter(p);
                if (TextUtils.isEmpty(value)) {
                    continue;
                }

                intent.putExtra(p, value);
            }

            Intent welcomeIntent = new Intent();
            welcomeIntent.setClassName(this, "com.netease.nim.demo.main.activity.WelcomeActivity");
            welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            welcomeIntent.putExtra(CallParams.INVENT_NOTIFICATION_FLAG, true);
            welcomeIntent.putExtra(CallParams.INVENT_NOTIFICATION_EXTRA, intent.getExtras());
            startActivity(welcomeIntent);
        }
    }
}