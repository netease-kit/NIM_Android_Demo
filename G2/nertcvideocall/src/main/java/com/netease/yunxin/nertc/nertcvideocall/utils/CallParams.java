package com.netease.yunxin.nertc.nertcvideocall.utils;

public interface CallParams {
    interface CallType {
        int P2P = 0;
        int TEAM = 1;
    }

    String INVENT_NOTIFICATION_FLAG = "com.netease.yunxin.nertc.nertcvideocall.notification.flag";
    String INVENT_NOTIFICATION_EXTRA = "com.netease.yunxin.nertc.nertcvideocall.notification.extra";

    String INVENT_CALL_RECEIVED = "invent_call_received";

    String INVENT_CALL_TYPE = "invent_call_type";
    String INVENT_CHANNEL_TYPE = "invent_channel_type";
    String INVENT_CHANNEL_ID = "invent_channelId";
    String INVENT_REQUEST_ID = "invent_requestId";
    String INVENT_FROM_ACCOUNT_ID = "invent_fromAccountId";
    String INVENT_USER_IDS = "invent_userIds";
    String TEAM_CHAT_GROUP_ID = "team_chat_group_id";

    String CHANNEL_NAME_SEPARATOR = "|";

    String[] CallParamKeys = new String[]{INVENT_CALL_TYPE, INVENT_CHANNEL_TYPE, INVENT_CHANNEL_ID, INVENT_REQUEST_ID, INVENT_FROM_ACCOUNT_ID, INVENT_USER_IDS, TEAM_CHAT_GROUP_ID};
}
