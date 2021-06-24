/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

public class CalloutState extends CallState {

    public CalloutState(NERTCVideoCallImpl videoCall) {
        super(videoCall);
        status = CallState.STATE_CALL_OUT;
    }

    @Override
    public void onInvited() {

    }

    @Override
    public void callOut() {

    }

    @Override
    public void dialog() {
        videoCall.setCurrentState(videoCall.getDialogState());
    }


    @Override
    public void release() {
        videoCall.setCurrentState(videoCall.getIdleState());
    }
}
