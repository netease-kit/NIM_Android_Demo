/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;

/**
 * 加入房间的回调。
 */
public interface JoinChannelCallBack {
    /**
     * 加入房间成功回调。
     *
     * @param channelFullInfo 房间信息。
     */
    void onJoinChannel(ChannelFullInfo channelFullInfo);

    /**
     * 加入房间失败回调。
     *
     * @param msg  错误信息。
     * @param code 错误码。
     */
    void onJoinFail(String msg, int code);
}
