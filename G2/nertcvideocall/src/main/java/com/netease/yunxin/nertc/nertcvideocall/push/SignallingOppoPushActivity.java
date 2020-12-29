package com.netease.yunxin.nertc.nertcvideocall.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

public class SignallingOppoPushActivity extends Activity {
    private static final String TAG = "OppoPushActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OppoPushActivity onCreate");
        parseIntent();
        finish();
    }

    void parseIntent() {
        if (getIntent() == null) {
            return;
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Intent intent = new Intent();

            for (String key : bundle.keySet()) {
                Object valueObj = bundle.get(key);
                intent.putExtra(key, valueObj == null ? null : valueObj.toString());
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
