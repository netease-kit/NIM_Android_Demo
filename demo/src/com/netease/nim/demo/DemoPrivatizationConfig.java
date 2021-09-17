package com.netease.nim.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.nimlib.push.net.lbs.IPVersion;
import com.netease.nimlib.push.packet.asymmetric.AsymmetricType;
import com.netease.nimlib.push.packet.symmetry.SymmetryType;
import com.netease.nimlib.sdk.NimHandshakeType;
import com.netease.nimlib.sdk.ServerAddresses;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

/**
 * 网易云信私有化配置项
 */
public class DemoPrivatizationConfig {

    /// BASIC
    private static final String KEY_APP_KEY = "appkey";

    private static final String KEY_MODULE = "module";

    private static final String KEY_VERSION = "version";

    private static final String KEY_HAND_SHAKE_TYPE = "hand_shake_type";

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

    // 握手协议(国密)
    private static final String KEY_DEDICATED_CLUSTE_FLAG = "dedicated_cluste_flag";
    private static final String KEY_NEGO_KEY_NECA = "nego_key_neca";
    private static final String KEY_NEGO_KEY_ENCA_KEY_VERSION = "nego_key_enca_key_version";
    private static final String KEY_NEGO_KEY_ENCA_KEY_PARTA = "nego_key_enca_key_parta";
    private static final String KEY_NEGO_KEY_ENCA_KEY_PARTB = "nego_key_enca_key_partb";
    private static final String KEY_COMM_ENCA = "comm_enca";

    // IM IPv6
    private static final String KEY_LINK_IPV6 = "link_ipv6";
    private static final String KEY_IP_PROTOCOL_VERSION = "ip_protocol_version";
    private static final String KEY_PROBE_IPV4_URL = "probe_ipv4_url";
    private static final String KEY_PROBE_IPV6_URL = "probe_ipv6_url";


    private static final String SHARE_NAME = "nim_demo_private_config";

    private static final String KEY_CONFIG_ENABLE = "private_config_enable";

    private static final String KEY_CONFIG_JSON = "private_config_json";


    private static final String KEY_CHAT_ROOM_LIST_URL = "chatroomDemoListUrl";


    private static final String BUCKET_NAME_PLACE_HOLDER = "{bucket}";

    private static final String OBJECT_PLACE_HOLDER = "{object}";


    private static final String CONFIG_URL = "config_private_url";

    private static final String YSF_DEFALUT_URL_LABEL = "ysf_defalut_url_label";

    private static final String YSF_DA_URL_LABEL = "ysf_da_url_label";

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

    public static void saveYsfDefaultUrl(Context context, String ysfDefaultUrl) {
        getSP(context).edit().putString(YSF_DEFALUT_URL_LABEL, ysfDefaultUrl).apply();
    }

    public static void saveYsfDaUrl(Context context, String ysfDaUrl) {
        getSP(context).edit().putString(YSF_DA_URL_LABEL, ysfDaUrl).apply();
    }

    public static String getYsfDefalutUrlLabel(Context context) {
        return getSP(context).getString(YSF_DEFALUT_URL_LABEL, null);
    }

    public static String getYsfDaUrlLabel(Context context) {
        return getSP(context).getString(YSF_DA_URL_LABEL, null);
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

    public static ServerAddresses checkConfigAndModifyConfig(String config) {
        JSONObject jsonObject = parse(config);
        return parseAddresses(jsonObject);
    }

    private static ServerAddresses parseAddresses(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        ServerAddresses addresses = new ServerAddresses();
        addresses.handshakeType = NimHandshakeType.value(jsonObject.optInt(KEY_HAND_SHAKE_TYPE, NimHandshakeType.V1.getValue()));
        addresses.module = jsonObject.optString(KEY_MODULE);
        addresses.publicKeyVersion = jsonObject.optInt(KEY_VERSION, 0);
        addresses.lbs = jsonObject.optString(KEY_LBS);
        addresses.defaultLink = jsonObject.optString(KEY_LINK);
        addresses.nosUploadLbs = jsonObject.optString(KEY_NOS_LBS);
        addresses.nosUploadDefaultLink = jsonObject.optString(KEY_NOS_UPLOADER);
        addresses.nosUpload = jsonObject.optString(KEY_NOS_UPLOADER_HOST);
        addresses.nosSupportHttps = jsonObject.optBoolean(KEY_HTTPS_ENABLED, false);
        addresses.nosDownloadUrlFormat = jsonObject.optString(KEY_NOS_DOWNLOADER);
        addresses.nosDownload = jsonObject.optString(KEY_NOS_ACCELERATE_HOST);
        addresses.nosAccess = jsonObject.optString(KEY_NOS_ACCELERATE);
        addresses.ntServerAddress = jsonObject.optString(KEY_NT_SERVER);
        addresses.dedicatedClusteFlag = jsonObject.optInt(KEY_DEDICATED_CLUSTE_FLAG);
        addresses.negoKeyNeca = AsymmetricType.value(jsonObject.optInt(KEY_NEGO_KEY_NECA, AsymmetricType.RSA.getValue()));
        addresses.negoKeyEncaKeyVersion = jsonObject.optInt(KEY_NEGO_KEY_ENCA_KEY_VERSION);
        addresses.negoKeyEncaKeyParta = jsonObject.optString(KEY_NEGO_KEY_ENCA_KEY_PARTA);
        addresses.negoKeyEncaKeyPartb = jsonObject.optString(KEY_NEGO_KEY_ENCA_KEY_PARTB);
        addresses.commEnca = SymmetryType.value(jsonObject.optInt(KEY_COMM_ENCA, SymmetryType.RC4.getValue()));
        addresses.linkIpv6 = jsonObject.optString(KEY_LINK_IPV6);
        addresses.ipProtocolVersion = IPVersion.value(jsonObject.optInt(KEY_IP_PROTOCOL_VERSION, IPVersion.IPV4.getValue()));
        addresses.probeIpv4Url = jsonObject.optString(KEY_PROBE_IPV4_URL);
        addresses.probeIpv6Url = jsonObject.optString(KEY_PROBE_IPV6_URL);
        appKey = jsonObject.optString(KEY_APP_KEY);
        autoAdjust(addresses);
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

    /**
     * 自动调整字段，避免去改其他地方的逻辑
     */
    private static void autoAdjust(@NonNull ServerAddresses addresses) {
        addresses.module = TextUtils.isEmpty(addresses.module) ? null : addresses.module;
        addresses.lbs = TextUtils.isEmpty(addresses.lbs) ? null : addresses.lbs;
        addresses.defaultLink = TextUtils.isEmpty(addresses.defaultLink) ? null : addresses.defaultLink;
        addresses.nosUploadLbs = TextUtils.isEmpty(addresses.nosUploadLbs) ? null : addresses.nosUploadLbs;
        addresses.nosUploadDefaultLink = TextUtils.isEmpty(addresses.nosUploadDefaultLink) ? null : addresses.nosUploadDefaultLink;
        addresses.nosUpload = TextUtils.isEmpty(addresses.nosUpload) ? null : addresses.nosUpload;
        addresses.nosDownloadUrlFormat = TextUtils.isEmpty(addresses.nosDownloadUrlFormat) ? null : addresses.nosDownloadUrlFormat;
        addresses.nosDownload = TextUtils.isEmpty(addresses.nosDownload) ? null : addresses.nosDownload;
        addresses.nosAccess = TextUtils.isEmpty(addresses.nosAccess) ? null : addresses.nosAccess;
        addresses.ntServerAddress = TextUtils.isEmpty(addresses.ntServerAddress) ? null : addresses.ntServerAddress;
        addresses.negoKeyEncaKeyParta = TextUtils.isEmpty(addresses.negoKeyEncaKeyParta) ? null : addresses.negoKeyEncaKeyParta;
        addresses.negoKeyEncaKeyPartb = TextUtils.isEmpty(addresses.negoKeyEncaKeyPartb) ? null : addresses.negoKeyEncaKeyPartb;
        addresses.linkIpv6 = TextUtils.isEmpty(addresses.linkIpv6) ? null : addresses.linkIpv6;
        addresses.probeIpv4Url = TextUtils.isEmpty(addresses.probeIpv4Url) ? null : addresses.probeIpv4Url;
        addresses.probeIpv6Url = TextUtils.isEmpty(addresses.probeIpv6Url) ? null : addresses.probeIpv6Url;
        appKey = TextUtils.isEmpty(appKey) ? null : appKey;
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
        return !TextUtils.isEmpty(format) && format.contains(BUCKET_NAME_PLACE_HOLDER) && format.contains(
                OBJECT_PLACE_HOLDER);
    }
}
