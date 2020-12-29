package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;

/**
 * 话单生成的回调
 */
public interface CallOrderListener {

    /**
     * 被用户取消
     *
     * @param channelType 通话类型 1 音频 2 视频
     * @param accountId   用户im accId
     * @param callType    0, 1对1通话 1，多人通话
     */
    void onCanceled(ChannelType channelType, String accountId, int callType);

    /**
     * 被用户拒绝
     *
     * @param channelType 通话类型 1 音频 2 视频
     * @param accountId   用户im accId
     * @param callType    0, 1对1通话 1，多人通话
     */
    void onReject(ChannelType channelType, String accountId, int callType);

    /**
     * 超时
     *
     * @param channelType 通话类型 1 音频 2 视频
     * @param accountId   用户im accId
     * @param callType    0, 1对1通话 1，多人通话
     */
    void onTimeout(ChannelType channelType, String accountId, int callType);

    /**
     * 用户忙
     *
     * @param channelType 通话类型 1 音频 2 视频
     * @param accountId   用户im accId
     * @param callType    0, 1对1通话 1，多人通话
     */
    void onBusy(ChannelType channelType, String accountId, int callType);


}
