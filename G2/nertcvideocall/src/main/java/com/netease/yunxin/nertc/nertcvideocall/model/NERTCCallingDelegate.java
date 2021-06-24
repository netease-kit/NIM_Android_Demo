/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;

public interface NERTCCallingDelegate {

    /**
     * 错误回调。
     *
     * @note 如果 needFinish 为 true，表示 SDK 遇到不可恢复的严重错误，请及时退出 UI。
     *
     * @param errorCode  错误码。
     * @param errorMsg   错误信息。
     * @param needFinish UI 层是否需要退出。true 表示严重错误，需要退出 UI。
     */
    void onError(int errorCode, String errorMsg, boolean needFinish);

    /**
     * 被邀请通话回调。
     *
     * @param invitedInfo 邀请参数
     */
    void onInvited(InvitedInfo invitedInfo);

    /**
     * 用户进入通话回调。
     *
     * 如果用户接受呼叫邀请，则本端会触发此回调。
     *
     * @param userId 进入通话的用户 ID。
     */
    void onUserEnter(String userId);


    /**
     * 通话结束回调。
     *
     * 如果有用户同意离开通话，那么本端会收到此回调。
     *
     * @param userId 离开通话的用户 ID。
     */
    void onCallEnd(String userId);

    /**
     * 用户离开时回调。
     *
     * @param userId 离开通话的用户 ID。
     */
    void onUserLeave(String userId);

    /**
     * 用户断开连接。
     *
     * @param userId 断开连接的用户 ID。
     */
    void onUserDisconnect(String userId);

    /**
     * 拒绝通话。
     *
     * @param userId 拒绝通话的用户 ID。
     */
    void onRejectByUserId(String userId);


    /**
     * 邀请方忙线。
     *
     * @param userId 忙线用户 ID。
     */
    void onUserBusy(String userId);

    /**
     * 作为被邀请方会收到，收到该回调说明本次通话被取消了。
     */
    void onCancelByUserId(String userId);


    /**
     * 远端用户开启或关闭了摄像头。
     *
     * @param userId           远端用户 ID。
     * @param isVideoAvailable true:远端用户打开摄像头；false:远端用户关闭摄像头。
     */
    void onCameraAvailable(String userId, boolean isVideoAvailable);

    /**
     * 远端用户是否开启视频流采集
     *
     * @param userId    远端用户id
     * @param isMuted   true:关闭，false:开启
     */
    void onVideoMuted(String userId, boolean isMuted);

    /**
     * 远端用户是否开启音频流采集
     * @param userId    远端用户id
     * @param isMuted   true:关闭，false:开启
     */
    void onAudioMuted(String userId, boolean isMuted);

    /**
     * 当前用户加入音视频的回调
     *
     * @param accId         用户 id
     * @param uid           用户用于加入 rtc 房间的 uid
     * @param channelName   用户加入 rtc 房间的通道名称
     * @param rtcChannelId  rtc 房间通道 id
     */
    void onJoinChannel(String accId, long uid, String channelName, long rtcChannelId);

    /**
     * 远端用户开启或关闭了麦克风。
     *
     * @param userId           远端用户 ID。
     * @param isAudioAvailable true:远端用户打开摄像头；false:远端用户关闭摄像头。
     */
    void onAudioAvailable(String userId, boolean isAudioAvailable);

    /**
     * 音视频异常断开连接。
     *
     * @param res 断开原因。
     */
    void onDisconnect(int res);

    /**
     * 网络状态回调。
     *
     * @param stats 网络状态。
     */
    void onUserNetworkQuality(Entry<String, Integer>[] stats);

    /**
     * 通话类型改变。
     *
     * @param type 通话类型。{@link ChannelType#AUDIO}音频通话，{@link ChannelType#VIDEO}视频通话
     */
    void onCallTypeChange(ChannelType type);

    /**
     * 呼叫超时。
     */
    void timeOut();

    /**
     * 已解码远端首帧回调。
     *
     * @param userId 远端用户id。
     * @param width 首帧视频宽，单位为 px。
     * @param height 首帧视频高，单位为 px。
     */
    void onFirstVideoFrameDecoded(String userId, int width, int height);

}
