/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.bean;

import java.util.ArrayList;

/**
 * <p>
 * 收到呼叫请求时信息
 * <p>
 */
public class InvitedInfo {
    /**
     * 呼叫发起人
     */
    public final String invitor;

    /**
     * 房间中所有人的ID（不包含invitor）
     */
    public final ArrayList<String> userIds;

    /**
     * 信令 channelId
     */
    public final String channelId;

    /**
     * 呼叫类型 详见{@link com.netease.yunxin.nertc.nertcvideocall.utils.CallParams.CallType}
     */
    public final int callType;

    /**
     * 透传groupCall传入的groupID，1to1则为null
     */
    public final String groupId;

    /**
     * 音频/视频 详见 {@link com.netease.nimlib.sdk.avsignalling.constant.ChannelType}
     */
    public final int channelType;

    /**
     * 用户额外在呼叫时传入信息
     */
    public final String attachment;

    /**
     * 通话请求id
     */
    public final String requestId;

    public InvitedInfo(String invitor, String requestId, String channelId, ArrayList<String> userIds, int callType, String groupId, int channelType, String attachment) {
        this.invitor = invitor;
        this.channelId = channelId;
        this.requestId = requestId;
        this.userIds = userIds;
        this.callType = callType;
        this.groupId = groupId;
        this.channelType = channelType;
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "InvitedInfo{" +
                "invitor='" + invitor + '\'' +
                ", userIds=" + userIds +
                ", channelId='" + channelId + '\'' +
                ", callType=" + callType +
                ", groupId='" + groupId + '\'' +
                ", channelType=" + channelType +
                ", attachment='" + attachment + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
