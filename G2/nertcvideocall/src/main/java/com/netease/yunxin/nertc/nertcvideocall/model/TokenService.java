/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.RequestCallback;

/**
 *获取token的服务
 */
public interface TokenService {

    /**
    * 获取 Token 的服务。
    * 
    * @param uid      用户 rtcID（用于加入 rtc 房间）。
    * @param callback callback 回调。
    */
    void getToken(long uid, RequestCallback<String> callback);
}
