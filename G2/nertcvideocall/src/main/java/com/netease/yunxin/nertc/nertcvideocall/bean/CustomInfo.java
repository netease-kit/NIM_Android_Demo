/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.bean;


import com.google.gson.annotations.SerializedName;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;

import java.util.ArrayList;

import static com.netease.yunxin.nertc.nertcvideocall.utils.CallParams.CHANNEL_NAME_SEPARATOR;

public class CustomInfo {

    /**
     * 呼叫类型{@link com.netease.yunxin.nertc.nertcvideocall.utils.CallParams}
     */
    public int callType;

    /**
     * 房间所有用户Id，不包含自己
     */
    public ArrayList<String> callUserList;


    /**
     * 群呼Id
     */
    public String groupId;

    /**
     * 用于加入音视频房间
     */
    public String channelName;

    /**
     * 呼叫组件版本号 形如 1.1.0
     */
    public String version;

    /**
     * 用户自定义信息字段
     */
    @SerializedName("_attachment")
    public String extraInfo;

    public CustomInfo(int callType, ArrayList<String> callUserList, String groupId, String channelId, String uid, String version, String extraInfo) {
        this.callType = callType;
        this.callUserList = callUserList;
        this.groupId = groupId;
        this.channelName = channelId
                + CHANNEL_NAME_SEPARATOR
                + callType
                + CHANNEL_NAME_SEPARATOR
                + (callType == CallParams.CallType.P2P ? uid : groupId);
        this.version = version;
        this.extraInfo = extraInfo;
    }

    public CustomInfo(String version){
        this.version = version;
    }
}
