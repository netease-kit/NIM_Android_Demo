package com.netease.nim.uikit.business.session.module;

import com.netease.nim.uikit.api.model.CreateMessageCallback;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

public interface IMultiRetweetMsgCreator {
    void create(List<IMMessage> msgList, boolean shouldEncrypt,  CreateMessageCallback callback);
}
