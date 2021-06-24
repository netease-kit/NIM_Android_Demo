/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.utils;

public interface CallParams {
    interface CallType {

        /**
         * 一对一通话
         */
        int P2P = 0;

        /**
         * 群组通话
         */
        int TEAM = 1;
    }

    String INVENT_NOTIFICATION_FLAG = "com.netease.yunxin.nertc.nertcvideocall.notification.flag";

    /**
     * 标记此页面启动是否做为被叫方
     */
    String INVENT_CALL_RECEIVED = "invent_call_received";
    /**
     * 呼叫类型，为一对一通话或群组通话 详见{@link CallType}
     */
    String INVENT_CALL_TYPE = "invent_call_type";
    /**
     * 呼叫通话类型，详见{@link com.netease.nimlib.sdk.avsignalling.constant.ChannelType}
     */
    String INVENT_CHANNEL_TYPE = "invent_channel_type";
    /**
     * 标记每次通话的 channel Id
     */
    String INVENT_CHANNEL_ID = "invent_channelId";
    /**
     * 标记本次邀请的请求 Id
     */
    String INVENT_REQUEST_ID = "invent_requestId";
    /**
     * 主叫用户的 IM 账号 Id
     */
    String INVENT_FROM_ACCOUNT_ID = "invent_fromAccountId";
    /**
     * 群组呼叫时所有被邀请用户的 IM 账号 Id 列表
     */
    String INVENT_USER_IDS = "invent_userIds";
    /**
     * 群组呼叫时标记群组通话的 Id
     */
    String TEAM_CHAT_GROUP_ID = "team_chat_group_id";

    /**
     * 加入 NERtc 房间时房间名称内部分割符
     */
    String CHANNEL_NAME_SEPARATOR = "|";
}
