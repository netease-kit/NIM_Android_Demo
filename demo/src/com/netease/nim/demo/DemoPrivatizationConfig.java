package com.netease.nim.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.nimlib.sdk.ServerAddresses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 网易云信私有化配置项
 */
public class DemoPrivatizationConfig {

    /// BASIC
    private static final String KEY_APP_KEY = "appkey";
    private static final String KEY_MODULE = "module";
    private static final String KEY_VERSION = "version";

    /// MAIN LINK
    private static final String KEY_LBS = "lbs";
    private static final String KEY_LINK = "link";

    /// NOS UPLOAD
    private static final String KEY_HTTPS_ENABLED = "https_enabled";
    private static final String KEY_NOS_LBS = "nos_lbs";
    private static final String KEY_NOS_UPLOADER = "nos_uploader";
    private static final String KEY_NOS_UPLOADER_HOST = "nos_uploader_host";

    /// NOS DOWNLOAD
    private static final String KEY_NOS_DOWNLOADER = "nos_downloader";
    private static final String KEY_NOS_ACCELERATE = "nos_accelerate";
    private static final String KEY_NOS_ACCELERATE_HOST = "nos_accelerate_host";

    /// SERVER
    private static final String KEY_NT_SERVER = "nt_server";


    private static final String SHARE_NAME = "nim_demo_private_config";
    private static final String KEY_CONFIG_ENABLE = "private_config_enable";
    private static final String KEY_CONFIG_JSON = "private_config_json";


    private static final String KEY_CHAT_ROOM_LIST_URL = "chatroomDemoListUrl";


    private static final String BUCKET_NAME_PLACE_HOLDER = "{bucket}";
    private static final String OBJECT_PLACE_HOLDER = "{object}";


    private static final String CONFIG_URL = "config_private_url";

    private static String appKey;

    public static String getAppKey(Context context) {
        if (isPrivateDisable(context)) {
            return null;
        }
        if (appKey != null) {
            return appKey;
        }

        JSONObject jsonObject = getConfig(context);
        if (jsonObject == null) {
            return null;
        }
        try {
            appKey = jsonObject.getString(KEY_APP_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return appKey;
    }


    public static ServerAddresses getServerAddresses(Context context) {
        if (isPrivateDisable(context)) {
            return null;
        }
        return parseAddresses(getConfig(context));
    }


    public static void updateConfig(String config, Context context) {
        if (TextUtils.isEmpty(config)) {
            return;
        }
        getSP(context).edit().putString(KEY_CONFIG_JSON, config).apply();
    }


    public static String getChatListRoomUrl(Context context) {
        return getConfig(context).optString(KEY_CHAT_ROOM_LIST_URL);
    }

    public static String getConfigUrl(Context context) {
        return getSP(context).getString(CONFIG_URL, null);
    }

    public static void saveConfigUrl(Context context, String url) {
        getSP(context).edit().putString(CONFIG_URL, url).apply();
    }

    public static void enablePrivateConfig(boolean enable, Context context) {
        getSP(context).edit().putBoolean(KEY_CONFIG_ENABLE, enable).apply();
    }

    public static boolean isPrivateDisable(Context context) {
        return !getSP(context).getBoolean(KEY_CONFIG_ENABLE, false);
    }

    public static JSONObject getConfig(Context context) {
        String configStr = getSP(context).getString(KEY_CONFIG_JSON, null);
        if (TextUtils.isEmpty(configStr)) {
            return null;
        }
        return parse(configStr);
    }

    public static ServerAddresses checkConfig(String config) {
        return parseAddresses(parse(config));
    }

    private static ServerAddresses parseAddresses(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        ServerAddresses addresses = new ServerAddresses();
        try {
            addresses.publicKey = jsonObject.getString(KEY_MODULE);
            addresses.publicKeyVersion = jsonObject.getInt(KEY_VERSION);
            addresses.lbs = jsonObject.getString(KEY_LBS);
            addresses.defaultLink = jsonObject.getString(KEY_LINK);
            addresses.nosUploadLbs = jsonObject.getString(KEY_NOS_LBS);
            addresses.nosUploadDefaultLink = jsonObject.getString(KEY_NOS_UPLOADER);
            addresses.nosUpload = jsonObject.getString(KEY_NOS_UPLOADER_HOST);
            addresses.nosSupportHttps = jsonObject.getBoolean(KEY_HTTPS_ENABLED);
            addresses.nosDownloadUrlFormat = jsonObject.getString(KEY_NOS_DOWNLOADER);
            addresses.nosDownload = jsonObject.getString(KEY_NOS_ACCELERATE_HOST);
            addresses.nosAccess = jsonObject.getString(KEY_NOS_ACCELERATE);
            addresses.ntServerAddress = jsonObject.getString(KEY_NT_SERVER);
            appKey = jsonObject.getString(KEY_APP_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        checkValid(addresses);
        return addresses;
    }

    private static void checkValid(ServerAddresses addresses) {

        if (TextUtils.isEmpty(addresses.lbs)) {
            throw new IllegalArgumentException("ServerAddresses lbs is null");
        }

        if (TextUtils.isEmpty(addresses.nosUploadLbs)) {
            throw new IllegalArgumentException("ServerAddresses nosUploadLbs is null");
        }

        if (TextUtils.isEmpty(addresses.defaultLink)) {
            throw new IllegalArgumentException("ServerAddresses  defaultLink is null");
        }

        if (TextUtils.isEmpty(addresses.nosUploadDefaultLink)) {
            throw new IllegalArgumentException("ServerAddresses nosUploadDefaultLink is null");
        }

        if (TextUtils.isEmpty(addresses.nosDownloadUrlFormat)) {
            throw new IllegalArgumentException("ServerAddresses nosDownloadUrlFormat is null");
        }

        if (!checkFormatValid(addresses.nosDownloadUrlFormat)) {
            throw new IllegalArgumentException("ServerAddresses nosDownloadUrlFormat is illegal");
        }

        if (addresses.nosSupportHttps && TextUtils.isEmpty(addresses.nosUpload)) {
            throw new IllegalArgumentException("ServerAddresses nosSupportHttps is true , but  nosUpload is null");
        }
    }

    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
    }

    private static JSONObject parse(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }
    }


    private static boolean checkFormatValid(String format) {
        return !TextUtils.isEmpty(format) && format.contains(BUCKET_NAME_PLACE_HOLDER) && format.contains(OBJECT_PLACE_HOLDER);
    }
}
