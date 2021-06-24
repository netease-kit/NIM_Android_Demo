/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

/**
 * 组件内部错误码
 */
public interface CallErrorCode {
    /**
     * 组件内部状态错误。
     */
    int STATUS_ERROR = 2000;

    /**
     * 其他端已经接受呼叫。
     */
    int OTHER_CLIENT_ACCEPT = 2001;

    /**
     * 其他端拒绝呼叫。
     */
    int OTHER_CLIENT_REJECT = 2002;

    /**
     * RTC 断开连接。
     */
    int NERTC_DISCONNECT = 2011;

    /**
     * 请求 Token 失败。
     */
    int LOAD_TOKEN_ERROR = 2021;

    /**
     * UID 和 accid 对应错误。
     */
    int UID_ACCID_ERROR = 2031;

    /**
     * 使用错误。
     */
    int COMMON_ERROR = -1;
}
