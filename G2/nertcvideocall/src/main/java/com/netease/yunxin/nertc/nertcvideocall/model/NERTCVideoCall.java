/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model;

import android.content.Context;

import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;

import java.util.ArrayList;

public abstract class NERTCVideoCall {
    protected NERTCVideoCall() {
    }

    public static NERTCVideoCall sharedInstance() {
        return NERTCVideoCallImpl.sharedInstance();
    }

    public static void destroySharedInstance() {
        NERTCVideoCallImpl.destroySharedInstance();
    }

    /**
     * 组件内部使用，不建议外部使用
     *
     * @return Android 上下文
     */
    public abstract Context getContext();

    /**
     * 添加 service delegate。
     *
     * 组件内部设置服务回调，不建议外部使用
     *
     * @param delegate 回调。
     */
    public void addServiceDelegate(NERTCCallingDelegate delegate) {

    }

    /**
     * 获取当前组件的通话状态。
     *
     * 详细信息请查看 {@link CallState}。
     */
    public abstract int getCurrentState();
    /**
     * 初始化，需要且仅能调用一次。
     *
     * @param context context
     * @param appKey  网易云信应用的 AppKey，请在控制台中获取。
     * @param option  初始化选项。
     */
    public abstract void setupAppKey(Context context, String appKey, VideoCallOptions option);

    /**
     * 用户若已经在 app 内实现了 IM 登录/登出逻辑，则不必依赖此接口完成调用。
     *
     * 登录组件。
     *
     * 使用实例前需要登录，建议在初始化后登录。
     *
     * @param imAccount IM 账号。
     * @param imToken   IM Token。
     * @param callback  登录回调。
     */
    public abstract void login(String imAccount, String imToken, RequestCallback<LoginInfo> callback);

    /**
     * 用户若已经在 app 内实现了 IM 登录/登出逻辑，则不必依赖此接口完成调用。
     *
     * 登出组件。
     */
    public abstract void logout();

    /**
     * 增加事件回调。
     *
     * 您可以通过 NERTCCallingDelegate 获得呼叫组件的各种状态通知。
     *
     * @param delegate 上层可以通过回调监听事件。
     */
    public abstract void addDelegate(NERTCCallingDelegate delegate);

    /**
     * 移除回调接口。
     *
     * @param delegate 需要移除的监听器。
     */
    public abstract void removeDelegate(NERTCCallingDelegate delegate);


    /**
     * 设置获取 Token 的服务。
     *
     * @note 无论为安全模式下必须设置，非安全模式下可以直接返回 null 做为结果。
     *
     * @param tokenService 获取 Token 的服务。
     */
    public abstract void setTokenService(TokenService tokenService);

    /**
     * 设置远端的视频画布。
     *
     * @param videoRender 视频画布。
     * @param userId         远端用户 ID。
     */
    public abstract void setupRemoteView(NERtcVideoView videoRender, String userId);

    /**
     * 设置本端的视频画布。
     *
     * @param videoRender 视频画布。
     */
    public abstract void setupLocalView(NERtcVideoView videoRender);

    /**
     * 静音指定用户。
     *
     * @param mute   是否静音。
     * @param userId 被静音的用户 ID。
     */
    public abstract void setAudioMute(boolean mute, String userId);

    /**
     * 视频通话中转为音频通话。(目前仅支持视频通话转音频通话)
     *
     * 被操作方会收到 {@link NERTCCallingDelegate#onCallTypeChange(ChannelType)} 的回调。
     *
     * @param type     通话类型。可设置为 {@link ChannelType#AUDIO} 表示语音通话。
     * @param callback callback 回调。
     */
    public abstract void switchCallType(ChannelType type, RequestCallback<Void> callback);

    /**
     * 单人通话邀请。
     *
     * 调用此方法邀请被叫加入通话。被叫方会收到 {@link NERTCCallingDelegate#onInvited} 的回调。
     *
     * @param userId              被叫方用户 ID。
     * @param selfUserId          主叫方用户 ID。
     * @param type                通话类型。1 表示语音通话，2 表示视频通话。
     * @param extraInfo           自定义扩展字段，在 onInvite 接口回调
     * @param joinChannelCallBack 加入房间回调。
     */
    public abstract void call(String userId, String selfUserId, ChannelType type,String extraInfo, JoinChannelCallBack joinChannelCallBack);

    /**
     * IM 群组邀请通话。
     *
     * IM 群组邀请通话，被邀请方会收到 {@link NERTCCallingDelegate#onInvited} 的回调。
     *
     * @param callUserIds         被叫方 ID 列表。
     * @param selfUserId          主叫方用户 ID。
     * @param groupId             群 ID。
     * @param type                通话类型。1 表示语音通话，2 表示视频通话。
     * @param joinChannelCallBack 加入房间回调。
     */
    public abstract void groupCall(ArrayList<String> callUserIds, String groupId, String selfUserId, ChannelType type, String extraInfo, JoinChannelCallBack joinChannelCallBack);

    /**
     * 群组通话中邀请其他人加入。
     *
     * 如果当前处于通话中，可以继续调用该函数继续邀请他人进入通话。
     *
     * @param callUserIds         被叫方 ID 列表。
     * @param totalUserIds        所有 ID 列表。
     * @param groupId             群 ID。
     * @param selfUserId          主叫方用户 ID。
     * @param joinChannelCallBack 加入房间回调。
     */
    public abstract void groupInvite(ArrayList<String> callUserIds, ArrayList<String> totalUserIds, String groupId, String selfUserId, String extraInfo, JoinChannelCallBack joinChannelCallBack);

    /**
     * 接受当前通话。
     *
     * 当您作为被邀请方收到 {@link NERTCCallingDelegate#onInvited} 的回调时，可以调用该函数接听来电。
     *
     * @param invitedParam        邀请信息。
     * @param selfAccId           自己的accid。
     * @param joinChannelCallBack 加入房间回调。
     */
    public abstract void accept(InviteParamBuilder invitedParam, String selfAccId, JoinChannelCallBack joinChannelCallBack);

    /**
     * 拒绝当前通话。
     *
     * 当您作为被邀请方收到 {@link NERTCCallingDelegate#onInvited} 的回调时，可以调用该函数拒绝来电。
     *
     * @param inviteParam 邀请信息。详细信息请参考 {@link InviteParamBuilder}。
     */
    public abstract void reject(InviteParamBuilder inviteParam, RequestCallback<Void> callback);

    /**
     * 挂断当前通话。
     *
     * 当您处于通话中，可以调用该函数结束通话，即离开房间并关闭房间。
     *
     * @note 通话发起者拥有此权限，并可以将此权限授权给被叫。
     *
     * @param channelId 需要结束通话的音视频房间 ID。如果为 null 则关闭当前通话，如果为实际值则关闭对应通话，如果值和当前通话不一致则不做关闭处理。
     * @param callback  callback 回调。
     */
    public abstract void hangup(String channelId, RequestCallback<Void> callback);

    /**
     * 通话过程中离开房间。
     *
     * 调用该函数在通话过程中离开房间，但房间不会关闭，且通话会继续进行。
     *
     * @note 多人通话时如果未有其他用户加入，底层也会调用取消的逻辑。
     *
     * @param callback  callback 回调。
     */
    public abstract void leave(RequestCallback<Void> callback);

    /**
     * 取消一对一呼叫。
     *
     * 当您处于一对一呼叫中，可以调用该函数取消呼叫。
     *
     * @note 多人通话时建议直接调用 {@link NERTCVideoCall#leave(RequestCallback)}。
     *
     * @param callback  callback 回调。
     */
    public abstract void cancel(RequestCallback<Void> callback);

    /**
     * 开启摄像头。
     *
     * 处于通话中的用户会收到 {@link NERTCCallingDelegate#onCameraAvailable(String, boolean)} 回调。
     *
     * @param enable 是否开启摄像头。
     */
    public abstract void enableLocalVideo(boolean enable);

    /**
     * 切换前后摄像头。
     */
    public abstract void switchCamera();

    /**
     * 静音本地音频采集。
     *
     * @param isMute true 表示麦克风关闭，false 表示麦克风打开。
     */
    public abstract void muteLocalAudio(boolean isMute);

    /**
     * 开启/关闭视频采集
     * @param isMute    true:视频采集关闭 false:视频采集打开
     */
    public abstract void muteLocalVideo(boolean isMute);

    /**
     * 设置呼叫超时时间。在呼叫/被叫前设置生效，可在调用{@link #setupAppKey(Context, String, VideoCallOptions)}时调用
     * 默认时长两分钟。
     *
     * @param timeOut 呼叫超时时长，最长两分钟，单位为毫秒（ms）。
     */
    public abstract void setTimeOut(long timeOut);
}
