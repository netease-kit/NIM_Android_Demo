/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

/**
 * 状态机
 */
public abstract class CallState {
        /**
        * 空闲状态。
        */
    public static final int STATE_IDLE = 0;
        /**
        * 被邀请加入通话。
        */    
    public static final int STATE_INVITED = 1;
        /**
        * 正在呼叫别人。
        */    
    public static final int STATE_CALL_OUT = 2;
        /**
        * 正在通话中。
        */    
    public static final int STATE_DIALOG = 3;

    protected int status;

    protected NERTCVideoCallImpl videoCall;

    public CallState(NERTCVideoCallImpl videoCall) {
        this.videoCall = videoCall;
    }

    public int getStatus() {
        return status;
    }

    public abstract void onInvited();

    public abstract void callOut();

    public abstract void dialog();


    public abstract void release();
}
