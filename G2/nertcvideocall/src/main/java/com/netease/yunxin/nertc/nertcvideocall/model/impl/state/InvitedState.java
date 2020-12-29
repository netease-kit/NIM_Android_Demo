package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

/**
 * 被邀请
 */
public class InvitedState extends CallState {

    public InvitedState(NERTCVideoCallImpl videoCall) {
        super(videoCall);
        status = STATE_INVITED;
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
