package com.netease.yunxin.kit.qchatkit.ui.message.model;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;

public class ChannelMemberStatusBean extends QChatBaseBean {

    public QChatServerMemberInfo channelMember;
    public boolean onlineStatus = false;

    public ChannelMemberStatusBean(QChatServerMemberInfo member) {
        this.channelMember = member;
        this.viewType = QChatViewType.CHANNEL_MEMBER_VIEW_TYPE;
    }

    public void setOnlineStatus() {
        onlineStatus = true;
    }

    public void setOffLineStatus() {
        onlineStatus = false;
    }
}

