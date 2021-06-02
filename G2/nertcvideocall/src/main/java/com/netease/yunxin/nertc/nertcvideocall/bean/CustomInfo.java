package com.netease.yunxin.nertc.nertcvideocall.bean;


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
    public String groupID;

    /**
     * 用于加入音视频房间
     */
    public String channelName;

    /**
     * 呼叫组件版本号 形如 1.1.0
     */
    public String version;

    public CustomInfo(int callType, ArrayList<String> callUserList, String groupID,String channelId,String uid,String version) {
        this.callType = callType;
        this.callUserList = callUserList;
        this.groupID = groupID;
        this.channelName = channelId
                + CHANNEL_NAME_SEPARATOR
                + callType
                + CHANNEL_NAME_SEPARATOR
                + (callType == CallParams.CallType.P2P ? uid : groupID);
        this.version = version;
    }

    public CustomInfo(String version){
        this.version = version;
    }
}
