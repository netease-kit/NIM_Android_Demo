/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

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
