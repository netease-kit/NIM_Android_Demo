package com.netease.yunxin.nertc.nertcvideocall.utils;

/**
 * 话单类型
 */
public interface NrtcCallStatus {
    int NrtcCallStatusComplete = 1;
    int NrtcCallStatusCanceled = 2;
    int NrtcCallStatusRejected = 3;
    int NrtcCallStatusTimeout = 4;
    int NrtcCallStatusBusy = 5;
}
