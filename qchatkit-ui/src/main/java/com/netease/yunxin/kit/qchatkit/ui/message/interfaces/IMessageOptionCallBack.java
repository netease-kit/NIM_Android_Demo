package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;

public interface IMessageOptionCallBack {
    /**
     * this message have been read
     */
    void onRead(QChatMessageInfo message);

    /**
     * resend on failed message
     */
    void reSend(QChatMessageInfo message);

    /**
     * copy a message success
     */
    void onCopy(QChatMessageInfo message);
}
