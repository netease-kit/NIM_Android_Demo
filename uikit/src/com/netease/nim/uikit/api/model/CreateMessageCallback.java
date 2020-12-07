package com.netease.nim.uikit.api.model;


import com.netease.nimlib.sdk.msg.model.IMMessage;

public interface CreateMessageCallback {
    //不支持的消息类型
    int FAILED_CODE_NOT_SUPPORT = 1;

    void onFinished(IMMessage message);

    void onFailed(int code);

    void onException(Throwable exception);
}