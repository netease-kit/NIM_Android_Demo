package com.netease.nim.avchatkit.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.nimlib.sdk.avchat.model.AVChatServerAddresses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 网易云信音视频私有化配置项
 */
public class AVPrivatizationConfig {

    private static final String KEY_NRTC_SERVER = "nrtc_server";
    private static final String KEY_NRTC_ROOMSERVER = "nrtc_roomserver";
    private static final String KEY_KIBANA_SERVER = "kibana_server";
    private static final String KEY_STATISTIC_SERVER = "statistic_server";
    private static final String KEY_NETDETECT_SERVER = "netdetect_server";
    private static final String KEY_COMPAT_SERVER = "compat_server";


    private static final String SHARE_NAME = "nim_demo_private_config";
    private static final String KEY_CONFIG_ENABLE = "private_config_enable";
    private static final String KEY_CONFIG_JSON = "private_config_json";


    public static AVChatServerAddresses getServerAddresses(Context context) {

        /**
         *  KEY_CONFIG_JSON 来源于IM demo 的私有化配置 ， 参考DemoPrivatizationConfig
         */
        String configStr = getSP(context).getString(KEY_CONFIG_JSON, null);
        if (TextUtils.isEmpty(configStr)) {
            return null;
        }

        if (isPrivateDisable(context)) {
            return null;
        }
        AVChatServerAddresses rtcServerAddresses = null;
        try {
            JSONObject jsonObject = new JSONObject(configStr);
            rtcServerAddresses = new AVChatServerAddresses();
//            rtcServerAddresses.channelServer = jsonObject.optString(KEY_NRTC_SERVER,null);
            rtcServerAddresses.roomServer = jsonObject.optString(KEY_NRTC_ROOMSERVER, null);
            rtcServerAddresses.statisticsServer = jsonObject.optString(KEY_KIBANA_SERVER, null);
            rtcServerAddresses.functionServer = jsonObject.optString(KEY_STATISTIC_SERVER, null);
            rtcServerAddresses.netDetectServer = jsonObject.optString(KEY_NETDETECT_SERVER, null);
            rtcServerAddresses.compatServer = jsonObject.optString(KEY_COMPAT_SERVER, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rtcServerAddresses;
    }


    private static boolean isPrivateDisable(Context context) {
        return !getSP(context).getBoolean(KEY_CONFIG_ENABLE, false);
    }


    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
    }

}
