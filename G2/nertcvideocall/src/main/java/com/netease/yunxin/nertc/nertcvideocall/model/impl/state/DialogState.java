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
