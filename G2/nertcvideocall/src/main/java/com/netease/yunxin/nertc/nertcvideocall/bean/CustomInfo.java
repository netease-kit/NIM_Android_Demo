package com.netease.yunxin.nertc.nertcvideocall.bean;

import com.netease.yunxin.nertc.nertcvideocall.utils.Utils;

import java.util.ArrayList;

public class CustomInfo {
    /**
     * 呼叫类型{@link Utils}
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

    public CustomInfo(int callType, ArrayList<String> callUserList, String groupID) {
        this.callType = callType;
        this.callUserList = callUserList;
        this.groupID = groupID;
    }
}
