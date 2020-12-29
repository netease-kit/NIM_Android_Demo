package com.netease.yunxin.nertc.nertcvideocall.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netease.nimlib.sdk.mixpush.MixPushMessageHandler;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

import java.util.Map;

public class SignalingPushHandler implements MixPushMessageHandler {

    private static final String TAG = "SignalingPushHandler";

    private static boolean isSignalingPush(Map<String, String> payload) {
        if (payload == null) {
            return false;
        }

        for (String key : CallParams.CallParamKeys) {
            if (!payload.containsKey(key)) {
                Log.i(TAG, "isSignalingPush !containsKey:" + key);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onNotificationClicked(Context context, Map<String, String> payload) {
        Log.d("G2", "onNotificationMessageClicked payload:" + payload);

        if (!isSignalingPush(payload)) {
            Log.i("G2", "onNotificationMessageClicked isSignalingPush:" + false);
            return false;
        }
        Log.d("G2", "onNotificationMessageClicked isSignalingPush:" + true);

        Intent intent = new Intent();
        for (String key : payload.keySet()) {
            Object valueObj = payload.get(key);
            intent.putExtra(key, valueObj == null ? null : valueObj.toString());
        }

        Intent welcomeIntent = new Intent();
        welcomeIntent.setClassName(context, "com.netease.nim.demo.main.activity.WelcomeActivity");
        welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        welcomeIntent.putExtra(CallParams.INVENT_NOTIFICATION_FLAG, true);
        welcomeIntent.putExtra(CallParams.INVENT_NOTIFICATION_EXTRA, intent.getExtras());
        context.startActivity(welcomeIntent);

        return true;
    }

    @Override
    public boolean cleanMixPushNotifications(int pushType) {
        return false;
    }
}
