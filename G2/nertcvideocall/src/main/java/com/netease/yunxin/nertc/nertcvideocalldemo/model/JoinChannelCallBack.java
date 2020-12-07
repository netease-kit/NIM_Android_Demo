package com.netease.yunxin.nertc.nertcvideocalldemo.model;

import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;

/**
 * 加入Channel的回调
 */
public interface JoinChannelCallBack {
    void onJoinChannel(ChannelFullInfo channelFullInfo);

    void onJoinFail(String msg,int code);
}
