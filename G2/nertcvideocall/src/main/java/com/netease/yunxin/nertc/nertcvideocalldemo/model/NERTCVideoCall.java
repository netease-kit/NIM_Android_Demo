package com.netease.yunxin.nertc.nertcvideocalldemo.model;

import android.content.Context;

import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.nertc.nertcvideocalldemo.model.impl.NERTCVideoCallImpl;

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

    public abstract Context getContext();

    /**
     * 添加service delegate
     *
     * @param delegate
     */
    public void addServiceDelegate(NERTCCallingDelegate delegate) {

    }

    /**
     * 初始化，需要且仅能调用一次
     *
     * @param context
     * @param appKey
     * @param option
     */
    public abstract void setupAppKey(Context context, String appKey, VideoCallOptions option);

    /**
     * IM 登录,使用前需要登录，最好在初始化后登录
     *
     * @param imAccount im账号
     * @param imToken   im 密码
     * @param callback  登录回调
     */
    public abstract void login(String imAccount, String imToken, RequestCallback<LoginInfo> callback);

    /**
     * 登出
     */
    public abstract void logout();

    /**
     * 增加回调接口
     *
     * @param delegate 上层可以通过回调监听事件
     */
    public abstract void addDelegate(NERTCCallingDelegate delegate);

    /**
     * 移除回调接口
     *
     * @param delegate 需要移除的监听器
     */
    public abstract void removeDelegate(NERTCCallingDelegate delegate);


    /**
     * 设置获取token的服务，安全模式必须设置
     *
     * @param tokenService
     */
    public abstract void setTokenService(TokenService tokenService);

    /**
     * 设置远端的视频接受播放器
     *
     * @param videoRender
     * @param uid
     */
    public abstract void setupRemoteView(NERtcVideoView videoRender, long uid);

    /**
     * 设置本端的视频接受播放器
     *
     * @param videoRender
     */
    public abstract void setupLocalView(NERtcVideoView videoRender);

    /**
     * C2C邀请通话，被邀请方会收到 {@link NERTCCallingDelegate#onInvited } 的回调
     *
     * @param userId              被邀请方
     * @param selfUserId          自己的用户Id
     * @param type                1-语音通话，2-视频通话
     * @param joinChannelCallBack channel 回调
     */
    public abstract void call(String userId, String selfUserId, ChannelType type, JoinChannelCallBack joinChannelCallBack);

    /**
     * 多人邀请通话，被邀请方会收到 {@link NERTCCallingDelegate#onInvited } 的回调
     *
     * @param callUserIds         被邀请方
     * @param selfUserId          自己的用户Id
     * @param groupId             群Id
     * @param type                1-语音通话，2-视频通话
     * @param joinChannelCallBack channel 回调
     */
    public abstract void groupCall(ArrayList<String> callUserIds, String groupId, String selfUserId, ChannelType type, JoinChannelCallBack joinChannelCallBack);

    /**
     * 当您作为被邀请方收到 {@link NERTCCallingDelegate#onInvited } 的回调时，可以调用该函数接听来电
     *
     * @param invitedParam 邀请信息
     * @param selfAccId 自己的accid
     * @param joinChannelCallBack  加入channel的回调
     */
    public abstract void accept(InviteParamBuilder invitedParam, String selfAccId, JoinChannelCallBack joinChannelCallBack);

    /**
     * 当您作为被邀请方收到 {@link NERTCCallingDelegate#onInvited } 的回调时，可以调用该函数拒绝来电
     *
     * @param inviteParam 邀请信息
     */
    public abstract void reject(InviteParamBuilder inviteParam, RequestCallback<Void> callback);

    /**
     * 当您处于通话中，可以调用该函数结束通话（离开房间并关闭房间）
     * 通话发起者拥有此权限，并可以授权给接受者是否拥有此权限
     */
    public abstract void hangup(RequestCallback<Void> callback);

    /**
     * 通话过程中离开房间，并不关闭房间
     */
    public abstract void leave(RequestCallback<Void> callback);

    /**
     * 当您处于呼叫中，可以调用该函数取消呼叫
     */
    public abstract void cancel(RequestCallback<Void> callback);

    /**
     * 您可以调用该函数开启摄像头，并渲染在指定的TXCloudVideoView中
     * 处于通话中的用户会收到 {@link NERTCCallingDelegate#onCameraAvailable(long, boolean)} 回调
     *
     * @param enable 是否开启摄像头
     */
    public abstract void enableLocalVideo(boolean enable);

    /**
     * 您可以调用该函数切换前后摄像头
     */
    public abstract void switchCamera();

    /**
     * 是否静音mic
     *
     * @param isMute true:麦克风关闭 false:麦克风打开
     */
    public abstract void muteLocalAudio(boolean isMute);

    /**
     * 设置超时，最长2分钟
     *
     * @param timeOut 超时市场，最长两分钟，单位ms
     */
    public abstract void setTimeOut(int timeOut);
}
