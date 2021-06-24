/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

/**
 * 用户信息初始化回调
 */
public interface UserInfoInitCallBack {
    void onUserLoginToIm(String imAccId, String imToken);
}
