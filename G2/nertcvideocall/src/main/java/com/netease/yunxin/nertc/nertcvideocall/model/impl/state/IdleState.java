/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

/**
 * 空状态
 */
public class IdleState extends CallState {


    public IdleState(NERTCVideoCallImpl videoCall) {
        super(videoCall);
        status = CallState.STATE_IDLE;
    }

    @Override
    public void onInvited() {
        videoCall.setCurrentState(videoCall.getInvitedState());
    }

    @Override
    public void callOut() {
        videoCall.setCurrentState(videoCall.getCallOutState());
    }

    @Override
    public void dialog() {

    }


    @Override
    public void release() {

    }
}
