package com.netease.nim.demo.contact.protocol;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.common.http.NimHttpClient;
import com.netease.nim.demo.config.DemoServers;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通讯录数据获取协议的实现
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public class ContactHttpClient implements IContactHttpProtocol {
    private static final String TAG = "ContactHttpClient";

    private static ContactHttpClient instance;

    public static synchronized ContactHttpClient getInstance() {
        if (instance == null) {
            instance = new ContactHttpClient();
        }

        return instance;
    }

    private ContactHttpClient() {
        NimHttpClient.getInstance().init();
    }

    /**
     * *********************************** token ******************************************
     */
    private static final String HTTP_CONFIG = "HTTP_CONFIG";
    private static final String HTTP_CONFIG_KEY_TOKEN = "TOKEN";
    private static final String HTTP_CONFIG_KEY_TOKEN_EXPIRES = "TOKEN_EXPIRES";
    private static final String HTTP_CONFIG_KEY_TOKEN_UPDATE_TIME_SECOND = "TOKEN_UPDATE_TIME_SECOND";

    private String secret;
    private String token;
    private long expires;
    private long updateTime;

    private String getSecret() {
        return Preferences.getUserToken();
    }

    private String getToken() {
        if (token == null) {
            SharedPreferences sp = DemoCache.getContext().getSharedPreferences(HTTP_CONFIG, Context.MODE_PRIVATE);
            token = sp.getString(HTTP_CONFIG_KEY_TOKEN, null);
            expires = sp.getLong(HTTP_CONFIG_KEY_TOKEN_EXPIRES, 0);
            updateTime = sp.getLong(HTTP_CONFIG_KEY_TOKEN_UPDATE_TIME_SECOND, 0);
        }

        return token;
    }

    private boolean isTokenValid() {
        getToken();

        if (TextUtils.isEmpty(token) || expires == 0 || updateTime == 0
                || TimeUtil.currentTimeSecond() - updateTime >= expires) {
            LogUtil.e(TAG, "local check token invalid, expires = " + expires + ", updateTime = " + updateTime + ", " +
                    "delta = " + (TimeUtil.currentTimeSecond() - updateTime));

            return false;
        }

        return true;
    }

    public void getTokenOnLogin() {
        secret = getSecret();
        if (TextUtils.isEmpty(secret)) {
            return;
        }

        getHttpToken(DemoCache.getAccount(), secret, null);

        LogUtil.i(TAG, "get token on login");
    }

    public void saveToken(String token, long expires) {
        SharedPreferences sp = DemoCache.getContext().getSharedPreferences(HTTP_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(HTTP_CONFIG_KEY_TOKEN, token);
        editor.putLong(HTTP_CONFIG_KEY_TOKEN_EXPIRES, expires);
        long currentTime = TimeUtil.currentTimeSecond();
        editor.putLong(HTTP_CONFIG_KEY_TOKEN_UPDATE_TIME_SECOND, currentTime);
        editor.commit();

        this.token = token;
        this.expires = expires;
        this.updateTime = currentTime;

        LogUtil.i(TAG, "save token =" + token + ", expires =" + expires + ", updateTime =" + updateTime);
    }

    public void resetToken() {
        LogUtil.i(TAG, "reset token");

        token = null;
        expires = 0;
        updateTime = 0;
        saveToken(token, 0);
    }

    /**
     * ******************************* common method ************************************
     */
    private void execute(final boolean checkToken, final String url, final Map<String, String> headers, final String body,
                         final NimHttpClient.NimHttpCallback callback) {
        Log.i(TAG, "execute url =" + url);
        if (!checkToken || isTokenValid()) {
            // request directly
            NimHttpClient.getInstance().execute(url, addFixedHeader(checkToken, headers), body, callback);
        } else {
            // fetch token
            if (TextUtils.isEmpty(secret)) {
                secret = getSecret();
            }
            getHttpToken(DemoCache.getAccount(), secret,
                    new IContactHttpCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            NimHttpClient.getInstance().execute(url, addFixedHeader(checkToken, headers), body, callback);
                        }

                        @Override
                        public void onFailed(int code, String errorMsg) {
                            LogUtil.e(TAG, "getToken failed, error:" + errorMsg);
                            callback.onResponse(null, 400, errorMsg);
                        }
                    });
        }
    }

    private final Map<String, String> addFixedHeader(boolean addToken, final Map<String, String> headers) {
        Map<String, String> res = new ArrayMap<>();
        if (headers == null || !headers.containsKey(HEADER_CONTENT_TYPE)) {
            res.put(HEADER_CONTENT_TYPE, "application/json");
        }

        if (addToken) {
            String token = getToken();
            if (!TextUtils.isEmpty(token)) {
                res.put(HEADER_KEY_ACCESS_TOKEN, token);
            }
        }

        if (headers != null && !headers.isEmpty()) {
            res.putAll(headers);
        }

        return res;
    }

    /**
     * *********************************** IContactHttpProtocol ******************************************
     */

    @Override
    public void getHttpToken(String account, String secret, final IContactHttpCallback<String>
            callback) {
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(secret)) {
            callback.onFailed(400, "fetch token account is null");
            return;
        }

        LogUtil.i(TAG, "fetch token, " + account + "/" + secret + "/" + REQUEST_VALUE_CLIENT_TYPE);

        String url = DemoServers.apiServer() + API_NAME_GET_TOKEN;

        JSONObject obj = new JSONObject();
        obj.put(REQUEST_KEY_USER_ID, account);
        obj.put(REQUEST_KEY_SECRET, secret);
        obj.put(REQUEST_KEY_CLIENT_TYPE, REQUEST_VALUE_CLIENT_TYPE);

        execute(false, url, null, obj.toJSONString(), new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get http token failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        JSONObject msg = resObj.getJSONObject(RESULT_KEY_MSG);
                        String token = msg.getString(RESULT_KEY_ACCESS_TOKEN);
                        long expires = msg.getLong(RESULT_KEY_EXPIRE);
                        saveToken(token, expires); // 通讯录token保存到本地
                        Log.i(TAG, "get new access token :" + token);
                        if (callback != null) {
                            callback.onSuccess(token);
                        }
                    } else {
                        errorMsg = resObj.getString(RESULT_KEY_ERROR_MSG);
                        if (callback != null) {
                            callback.onFailed(resCode, errorMsg);
                        }
                        Log.e(TAG, "getHttpToken error:" + errorMsg);
                    }
                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onFailed(-1, e.getMessage());
                    }
                }

            }
        });
    }

    @Override
    public void getUserInfo(List<String> accounts, final IContactHttpCallback<List<User>> callback) {
        String url = DemoServers.apiServer() + API_NAME_GET_USER_INFO;

        JSONArray jsonArray = new JSONArray();
        for (String account : accounts) {
            JSONObject obj = new JSONObject();
            obj.put(REQUEST_KEY_UID, account.toLowerCase());
            jsonArray.add(obj);
        }
        String accountString = jsonArray.toString();

        JSONObject reqObj = new JSONObject();
        reqObj.put(REQUEST_KEY_UID, accountString);

        execute(true, url, null, accountString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    callback.onFailed(code, errorMsg);
                    return;
                }

                onResponseGetUserInfo(response, callback);
            }
        });
    }

    @Override
    public void getUserInfo(String account, final IContactHttpCallback<User> callback) {
        List<String> accounts = new ArrayList<>(1);
        accounts.add(account);
        getUserInfo(accounts, new IContactHttpCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> buddies) {
                if (!buddies.isEmpty()) {
                    callback.onSuccess(buddies.get(0));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                callback.onFailed(code, errorMsg);
            }
        });
    }

    private void onResponseGetUserInfo(String response, final IContactHttpCallback<List<User>> callback) {
        try {
            JSONObject resObj = JSONObject.parseObject(response);
            int resCode = resObj.getIntValue(RESULT_KEY_RES);
            if (resCode == RESULT_CODE_SUCCESS) {
                JSONObject msg = resObj.getJSONObject(RESULT_KEY_MSG);
                int total = msg.getIntValue(RESULT_KEY_TOTAL);
                JSONArray list = msg.getJSONArray(RESULT_KEY_LIST);
                List<User> buddies = new ArrayList<>();
                for (int i = 0; i < Math.min(total, list.size()); i++) {
                    JSONObject o = list.getJSONObject(i);
                    String account = o.getString(RESULT_KEY_UID);
                    String name = o.getString(RESULT_KEY_NAME);
                    String icon = o.getString(RESULT_KEY_ICON);
                    int iconVal = TextUtils.isEmpty(icon) ? 0 : Integer.valueOf(icon);
                    buddies.add(new User(account, name, iconVal));
                }
                callback.onSuccess(buddies);
            } else {
                String errorMsg = resObj.getString(RESULT_KEY_ERROR_MSG);
                if (resCode == 400 && errorMsg.contains("token")) {
                    resetToken();
                } else if (resCode == 401) {
                    onTokenInvalid(resCode); // token失效
                }

                callback.onFailed(resCode, errorMsg);
            }
        } catch (JSONException e) {
            callback.onFailed(-1, e.getMessage());
        }
    }

    private void onTokenInvalid(int code) {
        LogUtil.e(TAG, "request failed as token invalid");
        if (code != 401) {
            return;
        }

        resetToken();
        getTokenOnLogin();
    }

    @Override
    public void register(String account, String nickName, String password, final IContactHttpCallback<Void> callback) {
        String url = DemoServers.apiServer() + API_NAME_REGISTER;
        password = MD5.getStringMD5(password);
        try {
            nickName = URLEncoder.encode(nickName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        headers.put(HEADER_KEY_APP_KEY, appKey);

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_NICK_NAME).append("=").append(nickName).append("&")
                .append(REQUEST_PASSWORD).append("=").append(password);
        String bodyString = body.toString();

        StringBuilder log = new StringBuilder();
        log.append("register request, url = ").append(url).append(", header = ").append(appKey).append("; body = ")
                .append(bodyString);
        Log.i(TAG, log.toString());

        execute(false, url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    private String readAppKey() {
        try {
            ApplicationInfo appInfo = DemoCache.getContext().getPackageManager()
                    .getApplicationInfo(DemoCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
