/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;

/**
 * 话单生成的回调
 */
public interface CallOrderListener {

    /**
     * 被用户取消。
     *
     * @param channelType 房间类型。1 表示语音通话，2 表示视频通话。
     * @param accountId   用户的 IM accId。
     * @param callType    0 表示 1对1通话；1 表示多人通话。
     */
    void onCanceled(ChannelType channelType, String accountId, int callType);

    /**
     * 被用户拒绝。
     *
     * @param channelType 通话类型。1 表示语音通话，2 表示视频通话。
     * @param accountId   用户的 IM accId。
     * @param callType    0 表示 1对1通话；1 表示多人通话。
     */
    void onReject(ChannelType channelType, String accountId, int callType);

    /**
     * 超时。
     *
     * @param channelType 通话类型。1 表示语音通话，2 表示视频通话。
     * @param accountId   用户的 IM accId。
     * @param callType    0 表示 1对1通话；1 表示多人通话。
     */
    void onTimeout(ChannelType channelType, String accountId, int callType);

    /**
     * 用户忙。
     *
     * @param channelType 通话类型。1 表示语音通话，2 表示视频通话。
     * @param accountId   用户的 IM accId。
     * @param callType    0 表示 1对1通话；1 表示多人通话。
     */
    void onBusy(ChannelType channelType, String accountId, int callType);


}
