/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.Pair;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.lava.nertc.sdk.NERtc;
import com.netease.lava.nertc.sdk.NERtcCallback;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo;
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver;
import com.netease.lava.nertc.sdk.video.NERtcEncodeConfig;
import com.netease.lava.nertc.sdk.video.NERtcRemoteVideoStreamType;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.avsignalling.SignallingService;
import com.netease.nimlib.sdk.avsignalling.SignallingServiceObserver;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelStatus;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.constant.InviteAckStatus;
import com.netease.nimlib.sdk.avsignalling.constant.SignallingEventType;
import com.netease.nimlib.sdk.avsignalling.event.CanceledInviteEvent;
import com.netease.nimlib.sdk.avsignalling.event.ChannelCloseEvent;
import com.netease.nimlib.sdk.avsignalling.event.ChannelCommonEvent;
import com.netease.nimlib.sdk.avsignalling.event.ControlEvent;
import com.netease.nimlib.sdk.avsignalling.event.InviteAckEvent;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.nimlib.sdk.avsignalling.event.SyncChannelListEvent;
import com.netease.nimlib.sdk.avsignalling.event.UserJoinEvent;
import com.netease.nimlib.sdk.avsignalling.event.UserLeaveEvent;
import com.netease.nimlib.sdk.avsignalling.model.ChannelBaseInfo;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.nimlib.sdk.avsignalling.model.MemberInfo;
import com.netease.nimlib.sdk.avsignalling.model.SignallingPushConfig;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.BasicInfo;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.nertc.nertcvideocall.BuildConfig;
import com.netease.yunxin.nertc.nertcvideocall.bean.ControlInfo;
import com.netease.yunxin.nertc.nertcvideocall.bean.CustomInfo;
import com.netease.yunxin.nertc.nertcvideocall.bean.EventParam;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.CallErrorCode;
import com.netease.yunxin.nertc.nertcvideocall.model.CallOrderListener;
import com.netease.yunxin.nertc.nertcvideocall.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.TokenService;
import com.netease.yunxin.nertc.nertcvideocall.model.UserInfoInitCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.VideoCallOptions;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CalloutState;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.DialogState;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.IdleState;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.InvitedState;
import com.netease.yunxin.nertc.nertcvideocall.service.CallService;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.nertcvideocall.utils.EventReporter;
import com.netease.yunxin.nertc.nertcvideocall.utils.NrtcCallStatus;
import com.netease.yunxin.nertc.nertcvideocall.utils.VersionUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.netease.lava.nertc.sdk.NERtcParameters.KEY_AUTO_SUBSCRIBE_AUDIO;
import static com.netease.nimlib.sdk.avsignalling.constant.SignallingEventType.INVITE;


public class NERTCVideoCallImpl extends NERTCVideoCall {
    private static final String VERSION_1_1_0 = "1.1.0";

    private static final String CURRENT_VERSION = "1.3.0";

    private static final String LOG_TAG = "NERTCVideoCallImpl";

    private static NERTCVideoCallImpl instance;

    private NERTCInternalDelegateManager delegateManager;

    private Context mContext;

    private long selfRtcUid;

    ExecutorService tokenLoaderService;

    //****************通话状态信息start***********************

    private CallState currentState;

    private CalloutState calloutState;

    private IdleState idleState;

    private DialogState dialogState;

    private InvitedState invitedState;


    //****************通话状态信息end***********************

    //****************数据存储于标记start*******************
    private boolean haveJoinNertcChannel = false;//是否加入了NERTC的频道

    private boolean handleUserAccept = false;//是否已经处理用户接收事件

    private CopyOnWriteArrayList<InviteParamBuilder> invitedParams;//邀请别人后保留的邀请信息

    private String imChannelId;//IM渠道号

    private CallOrderListener callOrderListener;//话单回调

    private UserInfoInitCallBack userInfoInitCallBack;//用户信息初始化回调

    private ArrayList<ChannelCommonEvent> offlineEvent = new ArrayList<>();

    //单人通话时生成话单使用
    //呼叫类型
    private ChannelType callOutType;
    //被呼叫的用户ID
    private String calledUserId;
    //主动呼叫的用户ID
    private String callerUserId;
    //呼叫类型
    private int callType;

    //收到的邀请参数,reject 用到
    private InvitedEvent invitedEvent;

    //被邀请时候的信息，在用户正真加入rtc房间的时候回调
    private JoinChannelCallBack invitedChannelCallback;
    private ChannelFullInfo invitedChannelInfo;

    private TokenService tokenService;

    //****************数据存储于标记end*******************

    //************************呼叫超时start********************
    private static final int TIME_OUT_LIMITED = 2 * 60 * 1000;//呼叫超时限制

    private long timeOut = TIME_OUT_LIMITED;//呼叫超时，最长2分钟

    private CountDownTimer timer;//呼出倒计时
    //************************呼叫超时end********************

    private Map<Long, String> memberInfoMap;

    private static final String BUSY_LINE = "601";

    NERtcEx neRtc;

    private String appKey;

    private String currentChannelName;

    /**
     * 加入 rtc 房间 token
     */
    private final StateParam rtcToken = new StateParam();
    /**
     * 加入 rtc 房间名称
     */
    private final StateParam rtcChannelName = new StateParam();
    /**
     * 1v1通话中，对方版本号
     */
    private final StateParam otherVersion = new StateParam();

    /**
     * 是否可以加入rtc房间
     */
    private boolean canJoinRtc = false;

    private final EventParam param = new EventParam();

    private final LongSparseArray<List<Pair<Integer, Map<String, Object>>>> rtcActionArray = new LongSparseArray<>();


    public static synchronized NERTCVideoCall sharedInstance() {
        if (instance == null) {
            instance = new NERTCVideoCallImpl();
        }
        return instance;
    }

    public static synchronized void destroySharedInstance() {
        if (instance != null) {
            instance.destroy();
            instance = null;
        }
        ALog.flush(true);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * 信令在线消息的回调
     */
    Observer<ChannelCommonEvent> nimOnlineObserver = new Observer<ChannelCommonEvent>() {
        @Override
        public void onEvent(ChannelCommonEvent event) {
            if (event.getChannelBaseInfo().getChannelStatus() == ChannelStatus.NORMAL) {
                handleNIMEvent(event);
            } else {
                ALog.d(LOG_TAG, "this event is INVALID and cancel eventType = 0 " + event.getEventType());
            }
        }
    };

    /**
     * 信令离线消息
     */
    Observer<ArrayList<ChannelCommonEvent>> nimOfflineObserver = (Observer<ArrayList<ChannelCommonEvent>>) channelCommonEvents -> {
        if (channelCommonEvents != null && channelCommonEvents.size() > 0) {
            if (delegateManager == null || delegateManager.isEmpty()) {
                //delegate 还没有加载，暂存消息
                offlineEvent.clear();
                offlineEvent.addAll(channelCommonEvents);
            } else {
                handleOfflineEvents(channelCommonEvents);
            }
        }
    };

    /**
     * 多端同步(接受/拒绝)
     */
    Observer<InviteAckEvent> otherClientEvent = (Observer<InviteAckEvent>) ackEvent -> {
        ALog.d(LOG_TAG, "otherClientEvent :" + ackEvent.getEventType().name());
        switch (ackEvent.getEventType()) {
            case ACCEPT:
                resetState();
                delegateManager.onError(CallErrorCode.OTHER_CLIENT_ACCEPT, "已被其他端接听", true);
                break;
            case REJECT:
                resetState();
                delegateManager.onError(CallErrorCode.OTHER_CLIENT_REJECT, "已被其他端拒绝", true);
                break;
        }
    };

    /**
     * 处理离线消息
     */
    private void handleOfflineEvents(ArrayList<ChannelCommonEvent> offlineEvent) {
//        Debug.waitForDebugger();
        if (offlineEvent != null && offlineEvent.size() > 0) {
            ArrayList<ChannelCommonEvent> usefulEvent = new ArrayList<>();
            for (ChannelCommonEvent event : offlineEvent) {
                if (event.getChannelBaseInfo().getChannelStatus() == ChannelStatus.NORMAL) {
                    if (event.getEventType() == SignallingEventType.CANCEL_INVITE) {
                        String channelId = event.getChannelBaseInfo().getChannelId();
                        for (ChannelCommonEvent event1 : usefulEvent) {
                            if (TextUtils.equals(channelId, event1.getChannelBaseInfo().getChannelId())) {
                                usefulEvent.remove(event1);
                                break;
                            }
                        }
                    } else {
                        usefulEvent.add(event);
                    }
                }
            }
            for (ChannelCommonEvent commonEvent : usefulEvent) {
                handleNIMEvent(commonEvent);
            }
        }
    }

    /**
     * 处理IM信令事件
     *
     * @param event
     */
    private void handleNIMEvent(ChannelCommonEvent event) {
        SignallingEventType eventType = event.getEventType();
        ALog.d(LOG_TAG, "handle IM Event type =  " + eventType + " channelId = " + event.getChannelBaseInfo().getChannelId());
        ChannelBaseInfo baseInfo = event.getChannelBaseInfo();
        if (baseInfo == null) {
            return;
        }
        String eventChannelId = baseInfo.getChannelId();
        if ((imChannelId != null && !imChannelId.equals(eventChannelId)) && eventType != INVITE) {
            ALog.d(LOG_TAG, "handle IM Event type =  " + eventType + " filter");
            return;
        }
        switch (eventType) {
            case CLOSE:
                //信令channel被关闭
                ChannelCloseEvent channelCloseEvent = (ChannelCloseEvent) event;
                //设置imChannelId 为null 不在调用iM close
                if (TextUtils.equals(channelCloseEvent.getChannelBaseInfo().getChannelId(), imChannelId)) {
                    imChannelId = null;
                }
                leave(null);
                if (delegateManager != null) {
                    delegateManager.onCallEnd(channelCloseEvent.getFromAccountId());
                }
                break;
            case JOIN:
                UserJoinEvent userJoinEvent = (UserJoinEvent) event;
                MemberInfo memberInfo = userJoinEvent.getMemberInfo();
                updateMemberMap(memberInfo);
                long uid = memberInfo.getUid();
                List<Pair<Integer, Map<String, Object>>> actionList = rtcActionArray.get(uid);
                if (actionList != null && !actionList.isEmpty()) {
                    for (Pair<Integer, Map<String, Object>> pair : actionList) {
                        dispatchRtcAction(uid, pair.first, pair.second);
                    }
                }
                break;
            case INVITE:
                invitedEvent = (InvitedEvent) event;
                if (delegateManager != null) {
                    if (currentState.getStatus() != CallState.STATE_IDLE) { //占线，直接拒绝
                        ALog.d(LOG_TAG, "user is busy status =  " + currentState.getStatus());
                        InviteParamBuilder paramBuilder = new InviteParamBuilder(invitedEvent.getChannelBaseInfo().getChannelId(),
                                invitedEvent.getFromAccountId(), invitedEvent.getRequestId());
                        paramBuilder.customInfo(BUSY_LINE);
                        rejectInner(paramBuilder, false, null);
                        break;
                    } else {
                        canJoinRtc = false;
                        rtcChannelName.reset();
                        otherVersion.reset();
                        imChannelId = invitedEvent.getChannelBaseInfo().getChannelId();
                        param.accid = invitedEvent.getToAccountId();
                        startCount();
                        // 被叫方在接收到 invite 事件时记录 rtc channelName, version
                        CustomInfo customInfo = GsonUtils.fromJson(invitedEvent.getCustomInfo(), CustomInfo.class);
                        if (customInfo == null) {
                            delegateManager.onError(CallErrorCode.STATUS_ERROR, "When receive invite event, the customInfo is null.", true);
                            return;
                        }

                        String invitor = invitedEvent.getFromAccountId();
                        String requestId = invitedEvent.getRequestId();
                        int callType = customInfo.callType;
                        int channelType = invitedEvent.getChannelBaseInfo().getType().getValue();
                        String channelId = invitedEvent.getChannelBaseInfo().getChannelId();
                        String groupId = customInfo.groupId;
                        String attachment = customInfo.extraInfo;
                        ArrayList<String> userIds = customInfo.callUserList;
                        delegateManager.onInvited(new InvitedInfo(invitor, requestId, channelId, userIds, callType, groupId, channelType, attachment));

                        rtcChannelName.updateParam(customInfo.channelName);
                        otherVersion.updateParam(customInfo.version);
                        if (VersionUtils.compareVersion(otherVersion.param, VERSION_1_1_0) >= 0) {
                            canJoinRtc = true;
                        }
                    }
                }
                setCallType(invitedEvent);
                currentState.onInvited();
                break;
            case CANCEL_INVITE:
                CanceledInviteEvent canceledInviteEvent = (CanceledInviteEvent) event;
                ALog.d(LOG_TAG, "accept cancel signaling request Id = " + canceledInviteEvent.getRequestId());
                leave(null);
                if (delegateManager != null) {
                    delegateManager.onCancelByUserId(canceledInviteEvent.getFromAccountId());
                }
                break;
            case REJECT:
            case ACCEPT:
                otherVersion.reset();
                InviteAckEvent ackEvent = (InviteAckEvent) event;
                if (!TextUtils.equals(ackEvent.getChannelBaseInfo().getChannelId(), imChannelId)) {
                    break;
                }
                if (ackEvent.getAckStatus() == InviteAckStatus.ACCEPT && callType == CallParams.CallType.P2P) {
                    CustomInfo customInfo = GsonUtils.fromJson(ackEvent.getCustomInfo(), CustomInfo.class);
                    if (customInfo != null) {
                        otherVersion.updateParam(customInfo.version);
                    }
                    String channelId = VersionUtils.compareVersion(otherVersion.param, VERSION_1_1_0) >= 0
                            ? rtcChannelName.param : ackEvent.getChannelBaseInfo().getChannelId();
                    handleWhenUserAccept(channelId);
                } else if (ackEvent.getAckStatus() == InviteAckStatus.REJECT) {
                    if (callType == CallParams.CallType.P2P) {
                        leave(null);
                    }
                    if (TextUtils.equals(ackEvent.getCustomInfo(), BUSY_LINE)) {
                        ALog.d(LOG_TAG, "reject as busy from " + ackEvent.getFromAccountId());
                        if (callOrderListener != null && callType == CallParams.CallType.P2P) {
                            callOrderListener.onBusy(ackEvent.getChannelBaseInfo().getType(), ackEvent.getFromAccountId(), callType);
                        }
                        delegateManager.onUserBusy(ackEvent.getFromAccountId());
                    } else {
                        ALog.d(LOG_TAG, "reject by user from " + ackEvent.getFromAccountId());
                        if (callOrderListener != null && callType == CallParams.CallType.P2P) {
                            callOrderListener.onReject(ackEvent.getChannelBaseInfo().getType(), ackEvent.getFromAccountId(), callType);
                        }
                        delegateManager.onRejectByUserId(ackEvent.getFromAccountId());
                    }
                }

                break;
            case LEAVE:
                UserLeaveEvent userLeaveEvent = (UserLeaveEvent) event;
                break;
            case CONTROL:
                ControlEvent controlEvent = (ControlEvent) event;
                ControlInfo controlInfo = GsonUtils.fromJson(controlEvent.getCustomInfo(), ControlInfo.class);
                if (controlInfo != null) {
                    // 老版本
                    if (controlInfo.cid == 1 && invitedChannelInfo != null && currentState.getStatus() == CallState.STATE_INVITED) {
                        canJoinRtc = true;
                        if (rtcToken.isInit()) {
                            return;
                        }
                        int rtcResult = joinChannel(rtcToken.param, invitedChannelInfo.getChannelId());
                        if (rtcResult != 0) {
                            ToastUtils.showShort("join Rtc failed code = " + rtcResult);
                            delegateManager.onError(rtcResult, "join Rtc failed", true);
                            hangupInner();
                        }

                    } else if (controlInfo.cid == 2 && currentState.getStatus() == CallState.STATE_DIALOG) {
                        if (controlInfo.type == ChannelType.AUDIO.getValue()) {
                            NERtcEx.getInstance().enableLocalVideo(false);
                            delegateManager.onCallTypeChange(ChannelType.retrieveType(controlInfo.type));
                        } else {
                            //todo 转视频功能预留
                        }
                    }
                }
                break;
        }
    }

    /**
     * rtc 状态监控
     */
    private NERtcStatsObserver statsObserver = new NERtcStatsObserverTemp() {
        @Override
        public void onNetworkQuality(NERtcNetworkQualityInfo[] infos) {
            Entry<String, Integer>[] qualitys = new Entry[infos.length];
            for (int i = 0; i < infos.length; i++) {
                qualitys[i] = new Entry<>(memberInfoMap.get(infos[i].userId), infos[i].upStatus);
            }
            delegateManager.onUserNetworkQuality(qualitys);
        }
    };

    /**
     * 收到邀请时设置callType
     *
     * @param invitedEvent
     */
    private void setCallType(InvitedEvent invitedEvent) {
        try {
            CustomInfo customInfo = GsonUtils.fromJson(invitedEvent.getCustomInfo(), CustomInfo.class);
            callType = customInfo.callType;
        } catch (Exception e) {
            callType = CallParams.CallType.P2P;
        }
    }

    /**
     * 操作用户加入房间的消息，对于主叫方，无论对方是否为新版本都需要在此方法中做加入 rtc 房间动作，区别传过来的 channelId 不同
     *
     * @param channelId
     */
    private void handleWhenUserAccept(String channelId) {
        ALog.d(LOG_TAG, "handleWhenUserAccept handleUserAccept = " + handleUserAccept + " status = " + currentState.getStatus());
        ALog.dApi(LOG_TAG, new ParameterMap("handleWhenUserAccept")
                .append("channelId", channelId)
        );
        if (!handleUserAccept && currentState.getStatus() == CallState.STATE_CALL_OUT) {
            canJoinRtc = true;
            if (rtcToken.isInit()) {
                return;
            }
            int rtcResult = joinChannel(rtcToken.param, channelId);
            if (rtcResult != 0) {
                delegateManager.onError(rtcResult, "join rtc channel failed", true);
                resetState();
            }

            handleUserAccept = true;
        }
    }

    /**
     * 发送控制信息
     *
     * @param controlInfo
     */
    private void sendControlEvent(String channelId, String accountId, ControlInfo controlInfo, RequestCallback callback) {
        ALog.dApi(LOG_TAG, new ParameterMap("sendControlEvent")
                .append("channelId", channelId)
                .append("accountId", accountId)
                .append("controlInfo", controlInfo)
        );
        NIMClient.getService(SignallingService.class).sendControl(channelId, accountId, GsonUtils.toJson(controlInfo)).setCallback(callback);
    }

    /**
     * Nertc的回调
     */
    private final NERtcCallback rtcCallback = new NERtcCallbackExTemp() {
        @Override
        public void onJoinChannel(int result, long l, long l1) {
            ALog.d(LOG_TAG, "onJoinChannel result = " + result + " l = " + l + " l1 =" + l1);
            neRtc.enableLocalAudio(true);
            if (result != 0) {
                if (!TextUtils.isEmpty(imChannelId)) {
                    closeIMChannel(imChannelId, null);
                }
                resetState();
                if (delegateManager != null) {
                    delegateManager.onError(result, "join rtc failed", true);
                }
                return;
            }
            if (delegateManager != null) {
                String accId = memberInfoMap.get(selfRtcUid);
                if (TextUtils.isEmpty(accId) && currentState.getStatus() == CallState.STATE_CALL_OUT) {
                    accId = callerUserId;
                }
                delegateManager.onJoinChannel(accId, selfRtcUid, currentChannelName, l);
            }
            haveJoinNertcChannel = true;
            if (callType == CallParams.CallType.P2P && currentState.getStatus() == CallState.STATE_CALL_OUT &&
                    !TextUtils.isEmpty(calledUserId) && !TextUtils.isEmpty(imChannelId)
                    && VersionUtils.compareVersion(otherVersion.param, VERSION_1_1_0) < 0) {
                sendControlEvent(imChannelId, calledUserId, new ControlInfo(1), null);
            }
            if (callType == CallParams.CallType.P2P && invitedChannelCallback != null && invitedChannelInfo != null) {
                invitedChannelCallback.onJoinChannel(invitedChannelInfo);
                invitedChannelCallback = null;
                invitedChannelInfo = null;
            }
        }

        @Override
        public void onLeaveChannel(int i) {
            haveJoinNertcChannel = false;
            ALog.d(LOG_TAG, "onLeaveChannel set status idel when onleaveChannel");
            if (currentState.getStatus() == CallState.STATE_IDLE) {
                resetState();
            }
        }

        @Override
        public void onUserJoined(long l) {
            if (timer != null) {
                timer.cancel();
                ALog.d(LOG_TAG, "countdown cancel userJoined!");
            }
            handleRtcAction(l, UserRtcAction.JOIN, () -> handleUserJoinAction(l));
        }

        @Override
        public void onUserLeave(long uid, int reason) {
            Map<String, Object> map = new HashMap<>();
            map.put(RtcActionParamKeys.KEY_REASON, reason);
            handleRtcAction(uid, UserRtcAction.LEAVE, map, () -> handleUserLeave(uid, reason));
        }

        @Override
        public void onUserAudioStart(long l) {
            handleRtcAction(l, UserRtcAction.AUDIO_START, () -> handleUserAudioAction(l, true));
        }

        @Override
        public void onUserAudioStop(long l) {
            handleRtcAction(l, UserRtcAction.AUDIO_STOP, () -> handleUserAudioAction(l, false));
        }

        @Override
        public void onUserVideoStart(long l, int i) {
            Map<String, Object> map = new HashMap<>();
            map.put(RtcActionParamKeys.KEY_REASON, i);
            handleRtcAction(l, UserRtcAction.VIDEO_START, map, () -> handleUserVideoAction(l, i, true));
        }

        @Override
        public void onUserVideoMute(long l, boolean b) {
            handleRtcAction(l, UserRtcAction.VIDEO_MUTE, () -> handleUserVideoMuteAction(l, b));
        }

        @Override
        public void onUserAudioMute(long l, boolean b) {
            handleRtcAction(l, UserRtcAction.VIDEO_MUTE, () -> handleUserAudioMuteAction(l, b));
        }

        @Override
        public void onUserVideoStop(long l) {
            handleRtcAction(l, UserRtcAction.VIDEO_STOP, () -> handleUserVideoAction(l, -1, false));
        }

        @Override
        public void onFirstVideoFrameDecoded(long userId, int width, int height) {
            Map<String, Object> map = new HashMap<>();
            map.put(RtcActionParamKeys.KEY_WIDTH, width);
            map.put(RtcActionParamKeys.KEY_HEIGHT, height);
            handleRtcAction(userId, UserRtcAction.FIRST_VIDEO_FRAME_DECODED, map, () -> handleUserFirstVideoFrameDecoded(userId, width, height));
        }

        @Override
        public void onDisconnect(int i) {
            ALog.d(LOG_TAG, "onDisconnect");
            // 非 IM 指令触发导致异常终端通话需要消除当前 imChannelId
            imChannelId = null;
            resetState();
            delegateManager.onDisconnect(i);
        }
    };

    /**
     * 登录状态回调
     */
    private Observer<StatusCode> loginStatus = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode == StatusCode.KICK_BY_OTHER_CLIENT || statusCode == StatusCode.KICKOUT) {
                leaveRtcChannel(null);
                resetState();
            }
        }
    };


    private NERTCVideoCallImpl() {
        invitedParams = new CopyOnWriteArrayList<>();
        delegateManager = new NERTCInternalDelegateManager();
        memberInfoMap = new HashMap<>();
        tokenLoaderService = Executors.newSingleThreadExecutor();
        initStates();
    }

    private void updateMemberMap(MemberInfo memberInfo) {
        memberInfoMap.put(memberInfo.getUid(), memberInfo.getAccountId());
    }

    //***************状态机************
    private void initStates() {
        idleState = new IdleState(this);
        calloutState = new CalloutState(this);
        invitedState = new InvitedState(this);
        dialogState = new DialogState(this);
        currentState = idleState;
    }

    public IdleState getIdleState() {
        return idleState;
    }

    public CalloutState getCalloutState() {
        return calloutState;
    }

    public InvitedState getInvitedState() {
        return invitedState;
    }

    public DialogState getDialogState() {
        return dialogState;
    }

    public void setCurrentState(CallState currentState) {
        this.currentState = currentState;
    }
    //***************状态机************

    @Override
    public void setupAppKey(Context context, String appKey, VideoCallOptions option) {
        mContext = context;
        ALog.init(context, ALog.LEVEL_DEBUG);
        ALog.logFirst(new BasicInfo.Builder()
                .packageName(context)
                .nertcVersion(NERtc.version().versionName)
                .imVersion(NIMClient.getSDKVersion())
                .deviceId(context)
                .version(CURRENT_VERSION)
                .platform("Android")
                .name("CallKit", true)
                .gitHashCode(BuildConfig.GIT_COMMIT_HASH)
                .extra(Collections.singletonMap("From", BuildConfig.FROM))
                .build());
        param.appKey = appKey;
        param.version = CURRENT_VERSION;
        //初始化之前 destroy
        if (neRtc != null) {
            destroy();
        }

        this.appKey = appKey;

        //初始化rtc sdk
        neRtc = NERtcEx.getInstance();
        NERtcParameters parameters = new NERtcParameters();
        neRtc.setParameters(parameters); //先设置参数，后初始化
        try {
            neRtc.init(context, appKey, rtcCallback, option.getRtcOption());
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showShort("SDK初始化失败");
            return;
        }
        userInfoInitCallBack = option.getUserInfoInitCallBack();
        neRtc.setStatsObserver(statsObserver);
        NIMClient.getService(SignallingServiceObserver.class).observeOnlineNotification(nimOnlineObserver, true);
        NIMClient.getService(SignallingServiceObserver.class).observeOfflineNotification(nimOfflineObserver, true);
        NIMClient.getService(SignallingServiceObserver.class).observeOtherClientInviteAckNotification(otherClientEvent, true);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginStatus, true);
        //保存UIService
        UIServiceManager.getInstance().setUiService(option.getUiService());
        //start 一个service来接受呼入
        CallService.start(context, option.getUiService());

        setCallOrderListener(new CallOrderListener() {
                                 @Override
                                 public void onCanceled(ChannelType channelType, String accountId, int callType) {
                                     makeCallOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusCanceled, callType);
                                 }

                                 @Override
                                 public void onReject(ChannelType channelType, String accountId, int callType) {
                                     makeCallOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusRejected, callType);
                                 }

                                 @Override
                                 public void onTimeout(ChannelType channelType, String accountId, int callType) {
                                     makeCallOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusTimeout, callType);
                                 }

                                 @Override
                                 public void onBusy(ChannelType channelType, String accountId, int callType) {
                                     makeCallOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusBusy, callType);
                                 }
                             }
        );
    }

    private void makeCallOrder(ChannelType channelType, String accountId, int nrtcCallStatusBusy, int callType) {
        if (callType == 0) {
            NetCallAttachment netCallAttachment = new NetCallAttachment.NetCallAttachmentBuilder()
                    .withType(channelType.getValue())
                    .withStatus(nrtcCallStatusBusy)
                    .build();
            IMMessage message = MessageBuilder.createNrtcNetcallMessage(accountId, SessionTypeEnum.P2P, netCallAttachment);
            NIMClient.getService(MsgService.class).sendMessage(message, false);
        }
    }

    private void setCallOrderListener(CallOrderListener callOrderListener) {
        this.callOrderListener = callOrderListener;
    }

    @Override
    public void login(String imAccount, String imToken, RequestCallback<LoginInfo> callback) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                if (statusCode == StatusCode.UNLOGIN) {
                    LoginInfo loginInfo = new LoginInfo(imAccount, imToken);
                    NIMClient.getService(AuthService.class).login(loginInfo).setCallback(new RequestCallback<LoginInfo>() {
                        @Override
                        public void onSuccess(LoginInfo param) {

                        }

                        @Override
                        public void onFailed(int code) {
                            ALog.i(LOG_TAG, "login failed code:" + code);
                            callback.onFailed(code);
                        }

                        @Override
                        public void onException(Throwable exception) {
                            callback.onException(exception);
                        }
                    });
                } else if (statusCode == StatusCode.LOGINED) {
                    ALog.i(LOG_TAG, "login success");
                    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(this, false);
                    syncDevicesInfo();
                    callback.onSuccess(new LoginInfo(imAccount, imToken));
                    if (userInfoInitCallBack != null) {
                        userInfoInitCallBack.onUserLoginToIm(imAccount, imToken);
                    }
                }
            }
        }, true);
    }

    /**
     * 获取当前账号其他端登录情况
     */
    private void syncDevicesInfo() {
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(new Observer<List<OnlineClient>>() {
            @Override
            public void onEvent(List<OnlineClient> onlineClients) {
                if (onlineClients == null || onlineClients.size() == 0) {
                    leaveChannelIfNeed();
                }
                NIMClient.getService(AuthServiceObserver.class).observeOtherClients(this, false);
            }
        }, true);
    }

    /**
     * 如果已经在信令频道，leave
     */
    private void leaveChannelIfNeed() {
        NIMClient.getService(SignallingServiceObserver.class).observeSyncChannelListNotification(new Observer<ArrayList<SyncChannelListEvent>>() {
            @Override
            public void onEvent(ArrayList<SyncChannelListEvent> syncChannelListEvents) {
                if (syncChannelListEvents != null && syncChannelListEvents.size() > 0) {
                    for (SyncChannelListEvent event : syncChannelListEvents) {
                        NIMClient.getService(SignallingService.class).leave(event.getChannelFullInfo().getChannelId(), true, null);
                    }
                    NIMClient.getService(SignallingServiceObserver.class).observeSyncChannelListNotification(this, false);
                }
            }
        }, true);
    }

    @Override
    public void logout() {
        NIMClient.getService(AuthService.class).logout();
    }

    @Override
    public int getCurrentState() {
        return currentState.getStatus();
    }

    @Override
    public void addDelegate(NERTCCallingDelegate delegate) {
        delegateManager.addDelegate(delegate);
    }

    public void addServiceDelegate(NERTCCallingDelegate delegate) {
        delegateManager.addDelegate(delegate);
        //处理保存的offline 消息
        if (offlineEvent.size() > 0) {
            ALog.d(LOG_TAG, "offline event dispatch to service");
            handleOfflineEvents(offlineEvent);
            offlineEvent.clear();
        }
    }

    @Override
    public void removeDelegate(NERTCCallingDelegate delegate) {
        delegateManager.removeDelegate(delegate);
    }

    @Override
    public void setupRemoteView(NERtcVideoView videoRender, String uid) {
        if (neRtc == null) {
            return;
        }
        for (Map.Entry<Long, String> entry : memberInfoMap.entrySet()) {
            if (TextUtils.equals(uid, entry.getValue())) {
                videoRender.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
                neRtc.setupRemoteVideoCanvas(videoRender, entry.getKey());
                return;
            }
        }
        delegateManager.onError(CallErrorCode.UID_ACCID_ERROR, "can not found userId", false);

    }

    @Override
    public void setupLocalView(NERtcVideoView videoRender) {
        ALog.dApi(LOG_TAG, "setupLocalView");
        if (neRtc == null) {
            return;
        }

        neRtc.enableLocalVideo(true);
        videoRender.setZOrderMediaOverlay(true);
        videoRender.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
        neRtc.setupLocalVideoCanvas(videoRender);
    }

    @Override
    public void setAudioMute(boolean mute, String userId) {
        for (Map.Entry<Long, String> entry : memberInfoMap.entrySet()) {
            if (TextUtils.equals(userId, entry.getValue())) {
                NERtcEx.getInstance().subscribeRemoteAudioStream(entry.getKey(), !mute);
                return;
            }
        }
        delegateManager.onError(CallErrorCode.UID_ACCID_ERROR, "can not found userId", false);
    }

    @Override
    public void switchCallType(ChannelType type, RequestCallback<Void> callback) {
        if (currentState.getStatus() != CallState.STATE_DIALOG) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "switchCallType status error, status = " + currentState.getStatus(), false);
        }
        if (type == ChannelType.AUDIO) {
            sendControlEvent(imChannelId, calledUserId, new ControlInfo(2, type.getValue()), new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    NERtcEx.getInstance().enableLocalVideo(false);
                    callback.onSuccess(param);
                }

                @Override
                public void onFailed(int code) {
                    callback.onFailed(code);
                }

                @Override
                public void onException(Throwable exception) {
                    callback.onException(exception);
                }
            });
        }
    }

    @Override
    public void setTimeOut(long timeOut) {
        ALog.d(LOG_TAG, "setTimeOut timeOut = " + timeOut);
        if (timeOut < TIME_OUT_LIMITED) {
            this.timeOut = timeOut;
        }
    }

    private boolean isCurrentUser(long uid) {
        return selfRtcUid != 0 && selfRtcUid == uid;
    }

    /**
     * 启动倒计时，用于实现timeout
     */
    private void startCount() {
        if (timer != null) {
            timer.cancel();
            ALog.d(LOG_TAG, "countdown cancel start!");
            timer = null;
        }

        timer = new CountDownTimer(timeOut, 1000) {
            @Override
            public void onTick(long l) {
                if (currentState.getStatus() != CallState.STATE_CALL_OUT &&
                        currentState.getStatus() != CallState.STATE_INVITED) {
                    timer.cancel();
                    ALog.d(LOG_TAG, "countdown cancel tick!");
                }
            }

            @Override
            public void onFinish() {
                ALog.d(LOG_TAG, "countdown finish!");
                EventReporter.reportP2PEvent(EventReporter.EVENT_TIMEOUT, param);
                if (callOrderListener != null &&
                        callOutType != null && !TextUtils.isEmpty(calledUserId)) {
                    callOrderListener.onTimeout(callOutType, calledUserId, callType);
                }

                callOutType = null;
                calledUserId = "";
                if (currentState.getStatus() == CallState.STATE_CALL_OUT) {
                    cancelInner(null);
                } else if (currentState.getStatus() == CallState.STATE_INVITED) {
                    imChannelId = null;
                    hangupInner();
                }
                if (delegateManager != null) {
                    delegateManager.timeOut();
                }
            }
        };
        ALog.d(LOG_TAG, "countdown start!");
        timer.start();
    }

    @Override
    public void call(final String userId, String selfUserId, ChannelType type, String extraInfo, @NotNull JoinChannelCallBack joinChannelCallBack) {
        ALog.dApi(LOG_TAG, new ParameterMap("call")
                .append("userId", userId)
                .append("selfUserId", selfUserId)
                .append("type", type.getValue())
        );
        EventReporter.reportP2PEvent(EventReporter.EVENT_CALL, param);
        param.accid = selfUserId;
        if (currentState.getStatus() != CallState.STATE_IDLE) {
            joinChannelCallBack.onJoinFail("status Error", -1);
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "call status error: status = " + currentState.getStatus(), false);
            return;
        }
        currentState.callOut();
        startCount();//启动倒计时
        callType = CallParams.CallType.P2P;
        //保存数据，用于生成话单
        callOutType = type;
        calledUserId = userId;
        callerUserId = selfUserId;
        handleUserAccept = false;

        if (type == ChannelType.AUDIO) {
            neRtc.enableLocalVideo(false);
        }

        createIMChannelAndJoin(CallParams.CallType.P2P, null, type, selfUserId, null, userId, extraInfo, joinChannelCallBack);

    }

    @Override
    public void groupCall(ArrayList<String> userIds, String groupId, String selfUserId, ChannelType type, String extraInfo, @NotNull JoinChannelCallBack joinChannelCallBack) {
        if (currentState.getStatus() != CallState.STATE_IDLE) {
            joinChannelCallBack.onJoinFail("status Error", -1);
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "groupCall status error: status = " + currentState.getStatus(), false);
            return;
        }
        if (userIds == null || userIds.size() <= 0) {
            ToastUtils.showLong("呼出参数错误");
            return;
        }
        currentState.callOut();
        startCount();//启动倒计时
        callType = CallParams.CallType.TEAM;
        callOutType = type;
        handleUserAccept = false;
        callerUserId = selfUserId;
        //1,创建channel
        createIMChannelAndJoin(CallParams.CallType.TEAM, groupId, type, selfUserId, userIds, null, extraInfo, joinChannelCallBack);
    }

    @Override
    public void groupInvite(ArrayList<String> callUserIds, ArrayList<String> totalUserIds, String groupId, String selfUserId, String extraInfo, JoinChannelCallBack joinChannelCallBack) {
        if (callType == CallParams.CallType.P2P) {
            joinChannelCallBack.onJoinFail("p2p can not call this api", CallErrorCode.COMMON_ERROR);
            return;
        }
        if (currentState.getStatus() != CallState.STATE_DIALOG && currentState.getStatus() != CallState.STATE_CALL_OUT) {
            joinChannelCallBack.onJoinFail("current state is error statue is:" + currentState.getStatus(), CallErrorCode.STATUS_ERROR);
            return;
        }
        for (String userId : callUserIds) {
            inviteOneUserWithIM(callType, callOutType, userId, selfUserId, imChannelId, groupId, totalUserIds, extraInfo);
            joinChannelCallBack.onJoinChannel(null);
        }
    }

    /**
     * 创建IM渠道并加入
     *
     * @param callType            呼叫类型
     * @param type                通话类型
     * @param selfUserId          自己的用户ID
     * @param userIds             呼叫的用户list
     * @param callUserId          呼叫的单个用户
     * @param joinChannelCallBack 回调
     */
    private void createIMChannelAndJoin(int callType, String groupId, ChannelType type, String selfUserId,
                                        ArrayList<String> userIds, String callUserId, String extraInfo, JoinChannelCallBack joinChannelCallBack) {
        NIMClient.getService(SignallingService.class).create(type, null, null).setCallback(new RequestCallback<ChannelBaseInfo>() {
            @Override
            public void onSuccess(ChannelBaseInfo param) {
                ALog.dApi(LOG_TAG, new ParameterMap("createIMChannelAndJoin-create-onSuccess")
                        .append("param", GsonUtils.toJson(param))
                );
                //2,join channel
                if (param != null) {
                    imChannelId = param.getChannelId();
                    joinIMChannel(callType, groupId, type, param, selfUserId, userIds, callUserId, extraInfo, joinChannelCallBack);
                }
            }

            @Override
            public void onFailed(int code) {
                ALog.d(LOG_TAG, "create channel failed code = " + code);
                joinChannelCallBack.onJoinFail("create channel failed code", code);
                callFailed(code, null);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    /**
     * 加入IM的频道
     *
     * @param channelInfo
     * @param selfUserId
     */
    private void joinIMChannel(int callType, String groupId, ChannelType type, ChannelBaseInfo channelInfo, String selfUserId, ArrayList<String> userIds,
                               String callUserId, String extraInfo, JoinChannelCallBack joinChannelCallBack) {
        //2,join channel for IM
        NIMClient.getService(SignallingService.class).join(channelInfo.getChannelId(), 0, "", true).setCallback(new RequestCallback<ChannelFullInfo>() {
            @Override
            public void onSuccess(ChannelFullInfo param) {
                ALog.dApi(LOG_TAG, new ParameterMap("joinIMChannel-join-onSuccess")
                        .append("param", GsonUtils.toJson(param))
                );
                //保存Uid
                storeUid(param.getMembers(), selfUserId);

                if (callType == CallParams.CallType.TEAM) {
                    //多人通话直接加入rtc channel 然后发出邀请
                    loadToken(selfRtcUid, new RequestCallback<String>() {

                        @Override
                        public void onSuccess(String s) {
                            int rtcResult = joinChannel(s, param.getChannelId());
                            if (rtcResult == 0 && userIds != null && userIds.size() > 0) {
                                //加入rtc成功，遍历所有的用户并邀请
                                ArrayList<String> allUserIds = new ArrayList<>(userIds);
                                //多人通话模式需要循环邀请所有用户
                                for (String userId : userIds) {
                                    if (!TextUtils.isEmpty(userId)) {
                                        inviteOneUserWithIM(callType, type, userId, selfUserId, param.getChannelId(), groupId, allUserIds, extraInfo);
                                    }
                                }
                                joinChannelCallBack.onJoinChannel(param);
                            } else {
                                joinChannelCallBack.onJoinFail("join channel failed", rtcResult);
                            }
                        }

                        @Override
                        public void onFailed(int i) {
                            loadTokenError();
                        }

                        @Override
                        public void onException(Throwable throwable) {

                        }
                    });

                } else if (callType == CallParams.CallType.P2P) {
                    loadToken(selfRtcUid, new RequestCallback<String>() {
                        @Override
                        public void onSuccess(String param) {
                            ALog.dApi(LOG_TAG, new ParameterMap("joinIMChannel-loadToken-onSuccess")
                                    .append("param", param)
                            );
                            rtcToken.updateParam(param);
                            if (canJoinRtc) {
                                String channelId = VersionUtils.compareVersion(otherVersion.param, VERSION_1_1_0) >= 0 ?
                                        rtcChannelName.param : channelInfo.getChannelId();
                                int rtcResult = joinChannel(rtcToken.param, channelId);
                                ALog.dApi(LOG_TAG, new ParameterMap("joinIMChannel-loadToken-onSuccess-joinChannel"));
                                if (rtcResult != 0) {
                                    delegateManager.onError(rtcResult, "join rtc channel failed", true);
                                    resetState();
                                }
                            }
                        }

                        @Override
                        public void onFailed(int code) {
                            rtcToken.error();
                            loadTokenError();
                        }

                        @Override
                        public void onException(Throwable exception) {
                            rtcToken.error();
                        }
                    });
                    //一对一通话直接发起邀请，在对方接受邀请之后再加入channel
                    ArrayList<String> userIds = new ArrayList<>(1);
                    userIds.add(callUserId);
                    inviteOneUserWithIM(callType, type, callUserId, selfUserId, param.getChannelId(), null, userIds, extraInfo);
                    joinChannelCallBack.onJoinChannel(param);
                }


            }

            @Override
            public void onFailed(int code) {
                ALog.d(LOG_TAG, "join channel failed code = " + code);
                joinChannelCallBack.onJoinFail("join im channel failed", code);
                callFailed(code, channelInfo.getChannelId());
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    /**
     * 生成随机数座位requestID
     *
     * @return
     */
    private String getRequestId() {
        int randomInt = (int) (Math.random() * 100);
        ALog.d(LOG_TAG, "random int = " + randomInt);
        return System.currentTimeMillis() + randomInt + "_id";
    }

    /**
     * 邀请用户加入channel
     *
     * @param callType
     * @param userId
     * @param selfUid
     * @param channelId
     * @param callUsers
     */
    private void inviteOneUserWithIM(int callType, ChannelType channelType, String userId, String selfUid, String channelId, String groupId, ArrayList<String> callUsers, String extraInfo) {
        String invitedRequestId = getRequestId();
        InviteParamBuilder inviteParam = new InviteParamBuilder(channelId, userId, invitedRequestId);
        CustomInfo customInfo = new CustomInfo(callType, callUsers, groupId, channelId, String.valueOf(selfRtcUid), CURRENT_VERSION, extraInfo);
        inviteParam.customInfo(GsonUtils.toJson(customInfo));
        inviteParam.pushConfig(getPushConfig(callType, channelType, invitedRequestId, selfUid, channelId, callUsers));
        inviteParam.offlineEnabled(true);

        // 主叫方保存 rtc channelName
        rtcChannelName.updateParam(customInfo.channelName);

        ALog.d(LOG_TAG, "sendInvited channelName = " + channelId + " userId = " + userId + " requestId = " + invitedRequestId);

        NIMClient.getService(SignallingService.class).invite(inviteParam).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                //保留邀请信息，取消用
                ALog.d(LOG_TAG, "sendInvited success channelName = " + channelId + " userId = " + userId + " requestId = " + invitedRequestId);
                ALog.dApi(LOG_TAG, new ParameterMap("inviteOneUserWithIM-invite-onSuccess"));
                saveInvitedInfo(inviteParam);
            }

            @Override
            public void onFailed(int code) {
                ALog.d(LOG_TAG, "sendInvited failed channelName = " + code);
                //推送可达算成功
                if (code == ResponseCode.RES_PEER_NIM_OFFLINE || code == ResponseCode.RES_PEER_PUSH_OFFLINE) {
                    saveInvitedInfo(inviteParam);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }


    private void saveInvitedInfo(InviteParamBuilder inviteParam) {
        invitedParams.add(inviteParam);
    }

    /**
     * 获取推送配置
     *
     * @param callType
     * @param requestId
     * @param fromAccountId
     * @param imChannelId
     * @param userIds
     * @return
     */
    private SignallingPushConfig getPushConfig(int callType, ChannelType channelType, String requestId, String fromAccountId, String imChannelId, ArrayList<String> userIds) {
        String fromNickname;
        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(fromAccountId);
        if (userInfo == null) {
            fromNickname = "对方";
        } else {
            fromNickname = userInfo.getName();
        }

        String pushTitle;
        String pushContent;
        if (channelType == ChannelType.AUDIO) {
            pushTitle = "语音聊天";
            pushContent = fromNickname + "邀请你加入语音聊天";
        } else {
            pushTitle = "视频聊天";
            pushContent = fromNickname + "邀请你加入视频聊天";
        }

        return new SignallingPushConfig(true, pushTitle, pushContent);
    }

    /**
     * 呼叫失败处理
     *
     * @param code
     */
    private void callFailed(int code, String imChannelId) {
        if (delegateManager != null) {
            delegateManager.onError(code, "呼叫失败", true);
        }
        if (!TextUtils.isEmpty(imChannelId)) {
            closeIMChannel(imChannelId, null);
        }
        resetState();
    }

    /**
     * 加入视频通话频道
     *
     * @param token       null时非安全模式
     * @param channelName
     * @return 0 方法调用成功，其他失败
     */
    private int joinChannel(String token, String channelName) {
        ALog.d(LOG_TAG, "joinChannel token = " + token + " channelName = " + channelName);
        ALog.dApi(LOG_TAG, new ParameterMap("joinChannel")
                .append("token", token)
                .append("channelName", channelName)
        );
        this.currentChannelName = channelName;
        if (selfRtcUid != 0) {
            //加入rtc房间之前设置一个默认的videoConfig，清除上次通话的设置
            NERtcVideoConfig videoConfig = new NERtcVideoConfig();
            videoConfig.videoProfile = NERtcConstants.VideoProfile.HD720P;
            // 默认帧率：15,分辨率：540x960，音频scenario：语音，音频profile：kNERtcAudioProfileStandardExtend
            videoConfig.frameRate = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
            videoConfig.width = 960;
            videoConfig.height = 540;
            neRtc.setAudioProfile(NERtcConstants.AudioProfile.STANDARD_EXTEND, NERtcConstants.AudioScenario.SPEECH);
            neRtc.setLocalVideoConfig(videoConfig);
            NERtcParameters parameters = new NERtcParameters();
            parameters.set(KEY_AUTO_SUBSCRIBE_AUDIO, false);
            NERtcEx.getInstance().setParameters(parameters);
            return NERtcEx.getInstance().joinChannel(token, channelName, selfRtcUid);
        }

        return -1;
    }

    /**
     * 保存自己再rtc channel 中的uid
     *
     * @param memberInfos
     * @param selfAccid
     */
    private void storeUid(ArrayList<MemberInfo> memberInfos, String selfAccid) {
        for (MemberInfo member : memberInfos) {
            if (TextUtils.equals(member.getAccountId(), selfAccid)) {
                selfRtcUid = member.getUid();
            }
        }
    }

    @Override
    public void accept(InviteParamBuilder inviteParam, String selfAccId, JoinChannelCallBack joinChannelCallBack) {
        ALog.dApi(LOG_TAG, new ParameterMap("accept")
                .append("inviteParam", GsonUtils.toJson(inviteParam))
                .append("selfAccId", selfAccId)
        );
        if (callType == CallParams.CallType.P2P) {
            EventReporter.reportP2PEvent(EventReporter.EVENT_ACCEPT, param);
        }
        param.accid = selfAccId;
        if (currentState.getStatus() != CallState.STATE_INVITED) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "accept status error, status = " + currentState.getStatus(), false);
        }
        inviteParam.customInfo(GsonUtils.toJson(new CustomInfo(CURRENT_VERSION)));
        inviteParam.offlineEnabled(true);
        NIMClient.getService(SignallingService.class).acceptInviteAndJoin(inviteParam, 0).setCallback(
                new RequestCallbackWrapper<ChannelFullInfo>() {

                    @Override
                    public void onResult(int code, ChannelFullInfo channelFullInfo, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            ALog.d(LOG_TAG, "accept success");
                            if (channelFullInfo.getType() == ChannelType.AUDIO) {
                                neRtc.enableLocalVideo(false);
                            }
                            if (channelFullInfo.getType() == ChannelType.VIDEO) {
                                neRtc.enableLocalVideo(true);
                            }
                            imChannelId = channelFullInfo.getChannelId();
                            //加入rtc Channel
                            storeUid(channelFullInfo.getMembers(), selfAccId);
                            //保存channel 里面的meber 信息
                            for (MemberInfo memberInfo : channelFullInfo.getMembers()) {
                                updateMemberMap(memberInfo);
                            }

                            if (callType == CallParams.CallType.TEAM) {
                                loadToken(selfRtcUid, new RequestCallback<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        int rtcResult = joinChannel(s, channelFullInfo.getChannelId());
                                        if (joinChannelCallBack != null) {
                                            if (rtcResult == 0) {
                                                joinChannelCallBack.onJoinChannel(channelFullInfo);
                                            } else {
                                                joinChannelCallBack.onJoinFail("join rtc failed!", -1);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailed(int i) {
                                        if (joinChannelCallBack != null) {
                                            joinChannelCallBack.onJoinFail("join rtc failed!", -1);
                                        }
                                    }

                                    @Override
                                    public void onException(Throwable throwable) {
                                        if (joinChannelCallBack != null) {
                                            joinChannelCallBack.onJoinFail("join rtc failed!", -1);
                                        }
                                    }
                                });
                            } else if (joinChannelCallBack != null) {
                                invitedChannelCallback = joinChannelCallBack;
                                invitedChannelInfo = channelFullInfo;
                                // 直接请求token
                                rtcToken.reset();
                                // 1v1 被呼叫方 token 请求预加载
                                loadToken(selfRtcUid, new RequestCallback<String>() {
                                    @Override
                                    public void onSuccess(String param) {
                                        rtcToken.updateParam(param);

                                        if (canJoinRtc && currentState.getStatus() == CallState.STATE_INVITED) {
                                            String channelId = VersionUtils.compareVersion(otherVersion.param, VERSION_1_1_0) >= 0
                                                    ? rtcChannelName.param : invitedChannelInfo.getChannelId();
                                            int rtcResult = joinChannel(rtcToken.param, channelId);
                                            if (rtcResult != 0) {
                                                ToastUtils.showShort("join Rtc failed code = " + rtcResult);
                                                delegateManager.onError(rtcResult, "join Rtc failed", true);
                                                hangupInner();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailed(int code) {
                                        rtcToken.error();
                                        loadTokenError();
                                    }

                                    @Override
                                    public void onException(Throwable exception) {
                                        rtcToken.error();
                                    }
                                });
                            }

                        } else {
                            ALog.d(LOG_TAG, "accept failed code = " + code);
                            joinChannelCallBack.onJoinFail("accept channel failed", code);
                        }
                    }
                });
    }

    @Override
    public void reject(InviteParamBuilder inviteParam, RequestCallback<Void> callback) {
        ALog.dApi(LOG_TAG, "reject");
        if (callType == CallParams.CallType.P2P) {
            EventReporter.reportP2PEvent(EventReporter.EVENT_REJECT, param);
        }
        rejectInner(inviteParam, true, callback);
    }

    /**
     * 拒绝
     *
     * @param inviteParam
     * @param byUser
     */
    private void rejectInner(InviteParamBuilder inviteParam, boolean byUser, RequestCallback<Void> callback) {
        ALog.d(LOG_TAG, "reject by user = " + byUser);
        if (currentState.getStatus() != CallState.STATE_INVITED) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "reject status error,status = " + currentState.getStatus(), false);
        }
        inviteParam.offlineEnabled(true);
        NIMClient.getService(SignallingService.class).rejectInvite(inviteParam).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (byUser) {
                    resetState();
                }
                if (callback != null) {
                    callback.onSuccess(aVoid);
                }
            }

            @Override
            public void onFailed(int i) {
                ALog.d(LOG_TAG, "reject failed code = " + i);
                if (byUser && i != ResponseCode.RES_INVITE_HAS_ACCEPT) {//已经接受
                    resetState();
                }
                if (callback != null) {
                    callback.onFailed(i);
                }
            }

            @Override
            public void onException(Throwable throwable) {
                if (callback != null) {
                    callback.onException(throwable);
                }
                if (byUser) {
                    resetState();
                }
            }
        });

    }

    private void hangupInner(String channelId,RequestCallback<Void> callback){
        ALog.d(LOG_TAG, "hangup, channelId is " + channelId);
        if (channelId != null && !channelId.equals(imChannelId)) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "hangup status error,current channelId is" + imChannelId + ", handle channelId is " + channelId, false);
            return;
        }
        if (currentState.getStatus() == CallState.STATE_IDLE) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "hangup status error,status is idle", false);
        }
        if (!NetworkUtils.isConnected()) {
            leaveRtcChannel(callback);
            resetState();
            if (callback != null) {
                callback.onException(new IllegalStateException("Current network doesn't work."));
            }
            return;
        }

        if (!handleUserAccept && currentState.getStatus() == CallState.STATE_CALL_OUT) {
            cancelInner(wrapperCallBack(imChannelId, callback));
        } else if (currentState.getStatus() == CallState.STATE_INVITED && invitedEvent != null) {
            InviteParamBuilder paramBuilder = new InviteParamBuilder(invitedEvent.getChannelBaseInfo().getChannelId(),
                    invitedEvent.getFromAccountId(), invitedEvent.getRequestId());
            rejectInner(paramBuilder, true, wrapperCallBack(imChannelId, callback));
        } else if (callback != null) {
            if (!TextUtils.isEmpty(imChannelId)) {
                closeIMChannel(imChannelId, null);
            } else {
                callback.onFailed(-1);
            }
        }
        //离开信令的channel
        leaveRtcChannel(callback);
        resetState();
    }

    private void hangupInner() {
        hangupInner(imChannelId, null);
    }

    @Override
    public void hangup(String channelId, RequestCallback<Void> callback) {
        ALog.dApi(LOG_TAG, new ParameterMap("hangup").append("channelId", channelId));
        if (currentState.getStatus() == CallState.STATE_DIALOG && callType == CallParams.CallType.P2P) {
            EventReporter.reportP2PEvent(EventReporter.EVENT_HANGUP, param);
        }
        hangupInner(channelId,callback);
    }

    private RequestCallback<Void> wrapperCallBack(final String imChannelId, RequestCallback<Void> callback) {
        return new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                if (callback != null) {
                    callback.onSuccess(param);
                }
            }

            @Override
            public void onFailed(int code) {
                if (!TextUtils.isEmpty(imChannelId)) {
                    closeIMChannel(imChannelId, null);
                }
                if (callback != null) {
                    callback.onFailed(code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                if (!TextUtils.isEmpty(imChannelId)) {
                    closeIMChannel(imChannelId, null);
                }
                if (callback != null) {
                    callback.onException(exception);
                }
            }
        };
    }

    private void leaveRtcChannel(RequestCallback<Void> callback) {
        //离开NERtc的channel
        int rtcResult = -1;
        if (neRtc != null) {
            rtcResult = neRtc.leaveChannel();
        }

        if (rtcResult != 0 && callback != null) {
            callback.onFailed(rtcResult);
        }
    }

    @Override
    public void leave(RequestCallback<Void> callback) {
        //群呼如果未接通走取消逻辑
        if (currentState.getStatus() != CallState.STATE_DIALOG) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "leave status error,status = " + currentState.getStatus(), false);
        }
        if (callType == CallParams.CallType.TEAM && currentState.getStatus() == CallState.STATE_CALL_OUT) {
            cancelInner(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    leaveAndClear(callback);
                }

                @Override
                public void onFailed(int i) {
                    leaveAndClear(callback);
                }

                @Override
                public void onException(Throwable throwable) {
                    leaveAndClear(callback);
                }
            });
        } else {
            leaveAndClear(callback);
        }
    }

    /**
     * 离开并清除数据
     *
     * @param callback
     */
    private void leaveAndClear(RequestCallback<Void> callback) {
        singleLeave(callback);
        resetState();
    }

    /**
     * 清理重置数据
     */
    private void resetState() {
        ALog.d(LOG_TAG, "reset State!");
        invitedParams.clear();
        invitedChannelCallback = null;
        invitedChannelInfo = null;
        memberInfoMap.clear();
        callOutType = null;
        calledUserId = "";
        callerUserId = "";
        currentState.release();
        canJoinRtc = false;
        invitedEvent = null;
        rtcActionArray.clear();
        imChannelId = null;
        currentChannelName = null;
        rtcChannelName.reset();
        rtcToken.reset();
        otherVersion.reset();
        // 直接使用nertcvideoview的销毁可能失效需让sdk 来做销毁操作
        neRtc.setupLocalVideoCanvas(null);
    }

    /**
     * 用户离开
     *
     * @param callback
     */
    private void singleLeave(RequestCallback<Void> callback) {
        //离开信令的channel
        if (!TextUtils.isEmpty(imChannelId)) {
            leaveIMChannel(imChannelId, callback);
        } else if (callback != null) {
            callback.onFailed(-1);
        }

        leaveRtcChannel(callback);
    }

    private void cancelInner(RequestCallback<Void> callback){
        ALog.d(LOG_TAG, "cancel");
        if (handleUserAccept) {
            return;
        }
        if (!NetworkUtils.isConnected()) {
            resetState();
            if (callback != null) {
                callback.onException(new IllegalStateException("Current network doesn't work."));
            }
            return;
        }
        if (currentState.getStatus() != CallState.STATE_CALL_OUT) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "cancel status error,status = " + currentState.getStatus(), false);
        }
        final boolean[] needCallback = {callback != null};
        if (invitedParams != null && invitedParams.size() > 0) {
            for (InviteParamBuilder inviteParam : invitedParams) {
                ALog.d(LOG_TAG, "send cancel signaling");
                NIMClient.getService(SignallingService.class).cancelInvite(inviteParam).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ALog.d(LOG_TAG, "cancel success");
                        if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(calledUserId)) {
                            callOrderListener.onCanceled(callOutType, calledUserId, callType);
                        }

                        if (needCallback[0] && callback != null) {
                            callback.onSuccess(aVoid);
                            needCallback[0] = false;
                        }
                        if (callType == CallParams.CallType.P2P && !TextUtils.isEmpty(imChannelId)) {
                            closeIMChannel(imChannelId, null);
                        }
                        resetState();
                    }

                    @Override
                    public void onFailed(int i) {
                        if (i != ResponseCode.RES_INVITE_HAS_ACCEPT) {
                            if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(calledUserId)) {
                                callOrderListener.onCanceled(callOutType, calledUserId, callType);
                            }
                        }

                        ALog.d(LOG_TAG, "send cancel signaling failed code = " + i);
                        if (needCallback[0] && callback != null) {
                            callback.onFailed(i);
                            needCallback[0] = false;
                        }

                        if (callType == CallParams.CallType.P2P) {
                            if (i == ResponseCode.RES_INVITE_HAS_ACCEPT) {//用户已经接受
                                handleWhenUserAccept(imChannelId);
                            } else {
                                leave(null);
                            }
                        }
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        ALog.e(LOG_TAG, "send cancel signaling exception", throwable);
                        if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(calledUserId)) {
                            callOrderListener.onCanceled(callOutType, calledUserId, callType);
                        }
                        if (needCallback[0] && callback != null) {
                            callback.onException(throwable);
                            needCallback[0] = false;
                        }

                        if (callType == CallParams.CallType.P2P) {
                            leave(null);
                        }
                    }
                });
            }
        } else {
            if (needCallback[0]) {
                callback.onException(new Exception("invited params have clear"));
                needCallback[0] = false;
            }

            if (callType == CallParams.CallType.P2P) {
                //离开信令的channel
                if (!TextUtils.isEmpty(imChannelId)) {
                    closeIMChannel(imChannelId, callback);
                } else if (callback != null) {
                    callback.onFailed(-1);
                }

                leaveRtcChannel(callback);
                resetState();
            }
        }
    }

    @Override
    public void cancel(RequestCallback<Void> callback) {
        ALog.dApi(LOG_TAG,"cancel");
        if (callType == CallParams.CallType.P2P) {
            EventReporter.reportP2PEvent(EventReporter.EVENT_CANCEL, param);
        }
        cancelInner(callback);
    }

    /**
     * 离开IMChannel
     *
     * @param channelId
     */
    private void leaveIMChannel(String channelId, RequestCallback<Void> callback) {
        NIMClient.getService(SignallingService.class).leave(channelId, false, null)
                .setCallback(new RequestCallbackWrapper<Void>() {

                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            imChannelId = null;
                            if (callback != null) {
                                callback.onSuccess(result);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailed(code);
                            }
                        }

                    }
                });


    }

    /**
     * 关闭IMChannel
     *
     * @param channelId
     */
    private void closeIMChannel(String channelId, RequestCallback<Void> callback) {
        ALog.d(LOG_TAG, "closeIMChannel ");
        NIMClient.getService(SignallingService.class).close(channelId, false, null)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            ALog.d(LOG_TAG, "closeIMChannel success channelId = " + channelId);
                            imChannelId = null;
                            if (callback != null) {
                                callback.onSuccess(result);
                            }
                        } else {
                            ALog.d(LOG_TAG, "closeIMChannel failed code = " + code + "channelId" + channelId);
                            if (callback != null) {
                                callback.onFailed(code);
                            }
                        }
                    }
                });
    }

    @Override
    public void enableLocalVideo(boolean enable) {
        NERtcEx.getInstance().enableLocalVideo(enable);
    }

    @Override
    public void switchCamera() {
        NERtcEx.getInstance().switchCamera();
    }

    @Override
    public void muteLocalAudio(boolean isMute) {
        NERtcEx.getInstance().muteLocalAudioStream(isMute);
    }

    @Override
    public void muteLocalVideo(boolean isMute) {
        NERtcEx.getInstance().muteLocalVideoStream(isMute);
    }

    private void destroy() {
        NIMClient.getService(SignallingServiceObserver.class).observeOnlineNotification(nimOnlineObserver, false);
        NIMClient.getService(SignallingServiceObserver.class).observeOfflineNotification(nimOfflineObserver, false);
        NIMClient.getService(SignallingServiceObserver.class).observeOtherClientInviteAckNotification(otherClientEvent, false);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginStatus, false);
        if (neRtc != null) {
            neRtc.setStatsObserver(null);
            neRtc.release();
        }
        if (timer != null) {
            timer.cancel();
            ALog.d(LOG_TAG, "countdown cancel destroy!");
            timer = null;
        }
    }

    @Override
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    private void loadToken(final long uid, RequestCallback<String> callback) {
        ALog.dApi(LOG_TAG, new ParameterMap("loadToken")
                .append("uid", uid)
        );
        if (TextUtils.isEmpty(appKey)) {
            callback.onFailed(-1);
            return;
        }

        if (tokenService == null) {
            callback.onFailed(-2);
            return;
        }
        tokenService.getToken(uid, new RequestCallback<String>() {
            @Override
            public void onSuccess(String param) {
                ALog.dApi(LOG_TAG, new ParameterMap("loadToken-getToken-onSuccess")
                        .append("param", param)
                );
                callback.onSuccess(param);
                ALog.i(LOG_TAG, "load token success. token is " + param);
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
                ALog.i(LOG_TAG, "load token fail. code is " + code);
            }

            @Override
            public void onException(Throwable exception) {
                callback.onException(exception);
                ALog.i(LOG_TAG, "load token exception. exception is " + exception);
            }
        });
    }

    private void loadTokenError() {
        ALog.d(LOG_TAG, "request token failed ");
        if (callType == CallParams.CallType.P2P) {
            hangupInner();
            delegateManager.onError(CallErrorCode.LOAD_TOKEN_ERROR, "get token error", true);
        } else {
            leave(null);
        }
    }

    private void handleUserJoinAction(long uid) {
        if (!isCurrentUser(uid)) {
            ALog.d(LOG_TAG, "onUserJoined set status dialog");
            currentState.dialog();
            if (invitedParams != null) {
                invitedParams.clear();
            }
        }
        if (delegateManager != null && memberInfoMap.get(uid) != null) {
            ALog.dApi(LOG_TAG, new ParameterMap("onUserJoined")
                    .append("uid", uid)
            );
            delegateManager.onUserEnter(memberInfoMap.get(uid));
        }
    }

    //
    private void handleUserVideoAction(long uid, int reason, boolean start) {
        if (start) {
            ALog.d(LOG_TAG, "onUserVideoStart");
            if (!isCurrentUser(uid)) {
                NERtcEx.getInstance().subscribeRemoteVideoStream(uid, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, true);
            }
            if (delegateManager != null) {
                delegateManager.onCameraAvailable(memberInfoMap.get(uid), true);
            }
        } else {
            ALog.d(LOG_TAG, "onUserVideoStop");
            if (delegateManager != null) {
                delegateManager.onCameraAvailable(memberInfoMap.get(uid), false);
            }
        }
    }

    private void handleUserVideoMuteAction(long uid, boolean mute) {
        ALog.d(LOG_TAG, "onUserVideoMute, uid is " + uid + ", mute is " + mute);
        if (delegateManager != null) {
            delegateManager.onVideoMuted(memberInfoMap.get(uid), mute);
        }
    }

    private void handleUserAudioMuteAction(long uid, boolean mute) {
        ALog.d(LOG_TAG, "onUserAudioMute, uid is " + uid + ", mute is " + mute);
        if (delegateManager != null) {
            delegateManager.onAudioMuted(memberInfoMap.get(uid), mute);
        }
    }

    // 处理rtc 用户音频回调动作
    private void handleUserAudioAction(long uid, boolean start) {
        if (start) {
            ALog.d(LOG_TAG, "onUserAudioStart");
            if (!isCurrentUser(uid)) {
                NERtcEx.getInstance().subscribeRemoteAudioStream(uid, true);
            }
            if (delegateManager != null) {
                delegateManager.onAudioAvailable(memberInfoMap.get(uid), true);
            }
        } else {
            ALog.d(LOG_TAG, "onUserAudioStop");
            if (delegateManager != null) {
                delegateManager.onAudioAvailable(memberInfoMap.get(uid), false);
            }
        }
    }

    // 处理rtc 用户首帧回调动作
    private void handleUserFirstVideoFrameDecoded(long uid, int width, int height) {
        ALog.d(LOG_TAG, "onFirstVideoFrameDecoded");
        if (delegateManager != null) {
            delegateManager.onFirstVideoFrameDecoded(memberInfoMap.get(uid), width, height);
        }
    }

    // 处理rtc 用户离开回调动作
    private void handleUserLeave(long uid, int reason) {
        ALog.d(LOG_TAG, String.format(Locale.getDefault(), "onUserLeave uid = %d,reason = %d", uid, reason));
        if (currentState.getStatus() == CallState.STATE_DIALOG) {
            if (reason == 0) {//正常离开
                delegateManager.onUserLeave(memberInfoMap.get(uid));
            } else {//非正常离开
                delegateManager.onUserDisconnect(memberInfoMap.get(uid));
            }
            if (callType == CallParams.CallType.P2P) {
                leave(null);
            }
        }
    }

    // 处理rtc回调动作
    private void handleRtcAction(long uid, int actionId, Runnable action) {
        handleRtcAction(uid, actionId, null, action);
    }

    // 处理rtc回调动作
    private void handleRtcAction(long uid, int actionId, Map<String, Object> params, Runnable action) {
        if (memberInfoMap.get(uid) == null) {
            appendRtcAction(uid, actionId, params);
            return;
        }
        action.run();
    }

    // 分派rtc 动作再次执行
    private void dispatchRtcAction(long uid, int action, Map<String, Object> params) {
        switch (action) {
            case UserRtcAction.JOIN: {
                handleUserJoinAction(uid);
                break;
            }
            case UserRtcAction.VIDEO_START: {
                handleUserVideoAction(uid, (int) params.get(RtcActionParamKeys.KEY_REASON), true);
                break;
            }
            case UserRtcAction.VIDEO_STOP: {
                handleUserVideoAction(uid, -1, false);
                break;
            }
            case UserRtcAction.AUDIO_START: {
                handleUserAudioAction(uid, true);
                break;
            }
            case UserRtcAction.AUDIO_STOP: {
                handleUserAudioAction(uid, false);
                break;
            }
            case UserRtcAction.FIRST_VIDEO_FRAME_DECODED: {
                handleUserFirstVideoFrameDecoded(uid, (int) params.get(RtcActionParamKeys.KEY_WIDTH),
                        (int) params.get(RtcActionParamKeys.KEY_HEIGHT));
                break;
            }
            case UserRtcAction.LEAVE: {
                handleUserLeave(uid, (int) params.get(RtcActionParamKeys.KEY_REASON));
                break;
            }
        }
    }

    // 添加rtc 动作
    private void appendRtcAction(long uid, int action, Map<String, Object> params) {
        List<Pair<Integer, Map<String, Object>>> actionList = rtcActionArray.get(uid);
        if (actionList == null) {
            actionList = new ArrayList<>();
            rtcActionArray.put(uid, actionList);
        }
        actionList.add(new Pair<>(action, params));
    }

    /**
     * 状态参数标记是否有效
     */
    private static class StateParam {
        /**
         * 参数有效
         */
        private static final int STATE_VALID = 1;
        /**
         * 参数无效
         */
        private static final int STATE_INVALID = -1;
        /**
         * 参数未初始化
         */
        private static final int STATE_INIT = 0;

        String param;
        int state;

        public StateParam() {
            this.param = null;
            this.state = STATE_INIT;
        }

        public void updateParam(String param) {
            this.param = param;
            this.state = STATE_VALID;
        }

        public void reset() {
            this.param = null;
            this.state = STATE_INIT;
        }

        public void error() {
            this.param = null;
            this.state = STATE_INVALID;
        }

        /**
         * 是否处于未初始化状态
         *
         * @return true 未初始化，false 已经完成初始化
         */
        public boolean isInit() {
            return this.state == STATE_INIT;
        }

        public boolean isValid() {
            return this.state == STATE_VALID;
        }

        public boolean isInvalid() {
            return this.state == STATE_INVALID;
        }
    }
}
