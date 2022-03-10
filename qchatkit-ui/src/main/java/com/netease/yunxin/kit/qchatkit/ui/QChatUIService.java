package com.netease.yunxin.kit.qchatkit.ui;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.netease.yunxin.kit.qchatkit.ui.utils.ScreenUtil;
import com.netease.yunxin.kit.qchatkit.QChatService;

@Keep
public class QChatUIService extends QChatService {

    @NonNull
    @Override
    public String getServiceName() {
        return "QChatUIService";
    }

    @NonNull
    @Override
    public QChatService create(@NonNull Context context) {
        ScreenUtil.init(context);
        return this;
    }
}
