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
