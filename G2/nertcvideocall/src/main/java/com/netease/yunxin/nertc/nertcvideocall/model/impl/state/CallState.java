package com.netease.yunxin.nertc.nertcvideocall.model.impl.state;

import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

/**
 * 状态机
 */
public abstract class CallState {
    public static final int STATE_IDLE = 0;//状态空闲
    public static final int STATE_INVITED = 1;//被邀请了
    public static final int STATE_CALL_OUT = 2;//正在呼叫别人
    public static final int STATE_DIALOG = 3;//通话中

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
