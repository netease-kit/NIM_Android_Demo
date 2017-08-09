package com.netease.nim.demo.redpacket;


import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.session.module.ModuleProxy;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;


/**
 * 发送领取了红包的消息
 */
public class NIMOpenRpCallback {
    private String sendUserAccount;
    private String sessionId;
    private SessionTypeEnum sessionType;
    private ModuleProxy proxy;

    public NIMOpenRpCallback(String sendUserAccount, String sessionId, SessionTypeEnum sessionType, ModuleProxy proxy) {
        this.sendUserAccount = sendUserAccount;
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.proxy = proxy;
    }

    public void sendMessage(String openAccount, String envelopeId, boolean getDone) {
        if (proxy == null) {
            return;
        }

        IMMessage imMessage;
        final NimUserInfo selfInfo = NimUserInfoCache.getInstance().getUserInfo(NimUIKit.getAccount());
        if (selfInfo == null) {
            return;
        }
        RedPacketOpenedAttachment redPacketOpenedMessage;
        if (openAccount.equals(sendUserAccount)) {
            redPacketOpenedMessage = RedPacketOpenedAttachment.obtain(selfInfo.getAccount(), selfInfo.getAccount(), envelopeId, getDone);
        } else {
            redPacketOpenedMessage = RedPacketOpenedAttachment.obtain(sendUserAccount, selfInfo.getAccount(), envelopeId, getDone);
        }

        String content = redPacketOpenedMessage.getDesc(sessionType, sessionId);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        config.enablePush = false;
        config.enableUnreadCount = false;
        imMessage = MessageBuilder.createCustomMessage(sessionId, sessionType, content, redPacketOpenedMessage, config);
        proxy.sendMessage(imMessage);
    }
}
