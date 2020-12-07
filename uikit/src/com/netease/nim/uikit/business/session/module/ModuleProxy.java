package com.netease.nim.uikit.business.session.module;

import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 会话窗口提供给子模块的代理接口。
 */
public interface ModuleProxy {
    /**
     * 发送消息
     *
     * @param msg 被发送的消息
     */
    boolean sendMessage(IMMessage msg);

    /**
     * 消息输入区展开时候的处理
     */
    void onInputPanelExpand();

    /**
     * 应当收起输入区
     */
    void shouldCollapseInputPanel();

    /**
     * 是否正在录音
     *
     * @return 是否正在录音
     */
    boolean isLongClickEnabled();

    void onItemFooterClick(IMMessage message);

    /**
     * 用户进行回复操作
     *
     * @param replyMsg 被回复的消息
     */
    void onReplyMessage(IMMessage replyMsg);
}
