/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.utils;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.nertcvideocall.bean.EventParam;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * 事件埋点上报
 */
public final class EventReporter {
    private static final String TAG = "EventReporter";

    private static final String EVENT_REPORT_URL = "https://statistic.live.126.net/statics/report/callkit/action";

    private static final ExecutorService executors = Executors.newSingleThreadExecutor();

    public static final String EVENT_CALL = "call";

    public static final String EVENT_ACCEPT = "accept";

    public static final String EVENT_CANCEL = "cancel";

    public static final String EVENT_REJECT = "reject";

    public static final String EVENT_HANGUP = "hangup";

    public static final String EVENT_TIMEOUT = "timeout";

    /**
     * 上报事件埋点
     *
     * @param event 具体事件类型
     * @param param 事件参数
     */
    public static void reportP2PEvent(String event, EventParam param) {
        executors.submit(() -> usePostParams(event, param, EVENT_REPORT_URL));
    }

    /**
     * 网络请求
     *
     * @param id         事件id
     * @param param      事件详细参数
     * @param requestUrl 上报url
     */
    private static void usePostParams(String id, EventParam param, String requestUrl) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            //设置链接超时时间
            connection.setConnectTimeout(5000);
            //设置读取超时时间
            connection.setReadTimeout(5000);
            //设置请求方法
            connection.setRequestMethod("POST");
            //添加Header
            connection.setRequestProperty("Connection", "keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //接受输入流
            connection.setDoInput(true);

            //有请求数据时，必须开启此项！
            connection.setDoOutput(true);
            //POST不支持缓存
            connection.setUseCaches(false);
            connection.connect();

            //传输'请求数据'
            JSONObject object = new JSONObject();
            object.putOpt("id", id);
            object.putOpt("accid", param.accid);
            object.putOpt("date", System.currentTimeMillis());
            object.putOpt("appKey", param.appKey);
            object.putOpt("platform", param.platform);
            object.putOpt("version", param.version);
            String body = object.toString();
            ALog.d(TAG, "========> request：" + body);
            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(body);
            writer.flush();
            writer.close();

            StringBuilder result = new StringBuilder();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                ALog.d(TAG, "<======== response：successful：" + result);
            } else {
                ALog.d(TAG, "<======== response：failed：" + connection.getResponseCode() + "; " + connection.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            close(writer);
            close(reader);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

