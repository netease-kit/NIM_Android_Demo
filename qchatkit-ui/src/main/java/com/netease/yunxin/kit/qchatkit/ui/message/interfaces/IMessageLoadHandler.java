package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;

public interface IMessageLoadHandler {

    /**
     * load more forward
     *
     * @param messageInfo first message
     * @return true as have more message to load,false as have no message to load
     */
    boolean loadMoreForward(QChatMessageInfo messageInfo);

    /**
     * load more background,should append those messages below
     *
     * @param messageInfo last message
     * @return true as have more message to load,false as have no message to load
     */
    boolean loadMoreBackground(QChatMessageInfo messageInfo);
}
