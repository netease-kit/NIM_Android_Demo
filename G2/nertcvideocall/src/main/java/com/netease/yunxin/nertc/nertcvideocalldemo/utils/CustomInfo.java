package com.netease.yunxin.nertc.nertcvideocalldemo.utils;

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
     * 是否来自群呼
     */
    public boolean isFromGroup;

    /**
     * 群呼Id
     */
    public String groupID;

    public CustomInfo(int callType, ArrayList<String> callUserList, boolean isFromGroup, String groupID) {
        this.callType = callType;
        this.callUserList = callUserList;
        this.isFromGroup = isFromGroup;
        this.groupID = groupID;
    }
}
