package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.nimlib.sdk.util.Entry;

public interface NERTCCallingDelegate {

    /**
     * 返回操作
     *
     * @param errorCode  错误码
     * @param errorMsg   错误信息
     * @param needFinish UI层是否需要退出（如果是致命错误，这里为true）
     */
    void onError(int errorCode, String errorMsg, boolean needFinish);

    /**
     * 被邀请通话回调
     *
     * @param invitedEvent 邀请参数
     */
    void onInvited(InvitedEvent invitedEvent);

    /**
     * 如果有用户同意进入通话频道，那么会收到此回调
     *
     * @param accId 进入通话的用户
     */
    void onUserEnter(String accId);


    /**
     * 如果有用户同意离开通话，那么会收到此回调
     *
     * @param accountId 离开通话的用户
     */
    void onCallEnd(String accountId);

    /**
     * 用户离开时回调
     * @param accountId
     */
    void onUserLeave(String accountId);

    /**
     * 用户断开连接
     * @param userId
     */
    void onUserDisconnect(String userId);

    /**
     * 拒绝通话
     *
     * @param userId 拒绝通话的用户
     */
    void onRejectByUserId(String userId);


    /**
     * 邀请方忙线
     *
     * @param userId 忙线用户
     */
    void onUserBusy(String userId);

    /**
     * 作为被邀请方会收到，收到该回调说明本次通话被取消了
     */
    void onCancelByUserId(String userId);


    /**
     * 远端用户开启/关闭了摄像头
     *
     * @param userId           远端用户ID
     * @param isVideoAvailable true:远端用户打开摄像头  false:远端用户关闭摄像头
     */
    void onCameraAvailable(String userId, boolean isVideoAvailable);

    /**
     * 远端用户开启/关闭了麦克风
     *
     * @param userId           远端用户ID
     * @param isAudioAvailable true:远端用户打开麦克风  false:远端用户关闭麦克风
     */
    void onAudioAvailable(String userId, boolean isAudioAvailable);

    /**
     * 音视频断开连接
     *
     * @param res 原因
     */
    void onDisconnect(int res);

    /**
     * 网络状态回调
     *
     * @param stats
     */
    void onUserNetworkQuality(Entry<String, Integer>[] stats);

    /**
     * 通话状态改变
     *
     * @param type
     */
    void onCallTypeChange(ChannelType type);

    /**
     * 呼叫超时
     */
    void timeOut();
}
