package com.netease.nim.demo.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

public class RedPacketOpenedAttachment extends CustomAttachment {
    private String sendAccount; //发送红包ID
    private String openAccount; //打开红包ID
    private String redPacketId;     //红包ID
    private boolean isGetDone;    //是否被领完

    private static final String KEY_SEND = "sendPacketId";
    private static final String KEY_OPEN = "openPacketId";
    private static final String KEY_RP_ID = "redPacketId";
    private static final String KEY_DONE = "isGetDone";

    public RedPacketOpenedAttachment() {
        super(CustomAttachmentType.OpenedRedPacket);
    }

    public String getSendNickName(SessionTypeEnum sessionTypeEnum, String targetId) {
        if (DemoCache.getAccount().equals(sendAccount) && DemoCache.getAccount().equals(openAccount)) {
            return "自己";
        }
        return getDisplayName(sessionTypeEnum, targetId, sendAccount);
    }

    public String getOpenNickName(SessionTypeEnum sessionTypeEnum, String targetId) {
        return getDisplayName(sessionTypeEnum, targetId, openAccount);
    }

    // 我发的红包或者是我打开的红包
    public boolean belongTo(String account) {
        if (openAccount == null || sendAccount == null || account == null) {
            return false;
        }
        return openAccount.equals(account) || sendAccount.equals(account);
    }

    private String getDisplayName(SessionTypeEnum sessionTypeEnum, String targetId, String account) {
        if (sessionTypeEnum == SessionTypeEnum.Team) {
            return TeamHelper.getTeamMemberDisplayNameYou(targetId, account);
        } else if (sessionTypeEnum == SessionTypeEnum.P2P) {
            return UserInfoHelper.getUserDisplayNameEx(account, "你");
        } else {
            return "";
        }
    }

    public String getDesc(SessionTypeEnum sessionTypeEnum, String targetId) {
        String sender = getSendNickName(sessionTypeEnum, targetId);
        String opened = getOpenNickName(sessionTypeEnum, targetId);
        return String.format("%s领取了%s的红包", opened, sender);
    }

    public String getSendAccount() {
        return sendAccount;
    }

    private void setSendAccount(String sendAccount) {
        this.sendAccount = sendAccount;
    }

    public String getOpenAccount() {
        return openAccount;
    }

    private void setOpenAccount(String openAccount) {
        this.openAccount = openAccount;
    }

    public String getRedPacketId() {
        return redPacketId;
    }

    private void setRedPacketId(String redPacketId) {
        this.redPacketId = redPacketId;
    }

    public boolean isRpGetDone() {
        return isGetDone;
    }

    private void setIsGetDone(boolean isGetDone) {
        this.isGetDone = isGetDone;
    }

    @Override
    protected void parseData(JSONObject data) {
        sendAccount = data.getString(KEY_SEND);
        openAccount = data.getString(KEY_OPEN);
        redPacketId = data.getString(KEY_RP_ID);
        isGetDone = data.getBoolean(KEY_DONE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject jsonObj = new JSONObject();

        jsonObj.put(KEY_SEND, sendAccount);
        jsonObj.put(KEY_OPEN, openAccount);
        jsonObj.put(KEY_RP_ID, redPacketId);
        jsonObj.put(KEY_DONE, isGetDone);

        return jsonObj;
    }

    public static RedPacketOpenedAttachment obtain(String sendPacketId, String openPacketId, String packetId, boolean isGetDone) {
        RedPacketOpenedAttachment model = new RedPacketOpenedAttachment();
        model.setRedPacketId(packetId);
        model.setSendAccount(sendPacketId);
        model.setOpenAccount(openPacketId);
        model.setIsGetDone(isGetDone);
        return model;
    }
}
