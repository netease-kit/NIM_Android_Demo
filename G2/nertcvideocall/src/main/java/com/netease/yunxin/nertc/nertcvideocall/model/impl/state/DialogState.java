/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

public class DialogState extends CallState {

    public DialogState(NERTCVideoCallImpl videoCall) {
        super(videoCall);
        status = CallState.STATE_DIALOG;
    }

    @Override
    public void onInvited() {

    }

    @Override
    public void callOut() {

    }

    @Override
    public void dialog() {

    }


    @Override
    public void release() {
        videoCall.setCurrentState(videoCall.getIdleState());
    }
}
