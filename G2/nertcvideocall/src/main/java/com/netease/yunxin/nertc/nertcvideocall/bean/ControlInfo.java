/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.bean;

/**
 * 自定义控制消息
 */
public class ControlInfo {

    /**
     * 1,代表通话过程使用，2，音视频切换使用
     */
    public int cid;

    /**
     * 音视频类型{@link com.netease.nimlib.sdk.avsignalling.constant.ChannelType}
     */
    public int type;

    public ControlInfo(int cid) {
        this.cid = cid;
    }

    public ControlInfo(int cid, int type) {
        this.cid = cid;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ControlInfo{" +
                "cid=" + cid +
                ", type=" + type +
                '}';
    }
}
