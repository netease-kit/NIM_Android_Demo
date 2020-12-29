package com.netease.yunxin.nertc.nertcvideocall.model.impl;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.lava.nertc.sdk.NERtcCallback;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.lava.nertc.sdk.stats.NERtcAudioRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioSendStats;
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo;
import com.netease.lava.nertc.sdk.stats.NERtcStats;
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver;
import com.netease.lava.nertc.sdk.stats.NERtcVideoRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcVideoSendStats;
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
import com.netease.yunxin.nertc.nertcvideocall.bean.ControlInfo;
import com.netease.yunxin.nertc.nertcvideocall.bean.CustomInfo;
import com.netease.yunxin.nertc.nertcvideocall.utils.NrtcCallStatus;
import com.netease.yunxin.nertc.nertcvideocall.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class NERTCVideoCallImpl extends NERTCVideoCall {

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
    //呼叫的用户ID
    private String callUserId;
    //呼叫类型
    private int callType;

    //被邀请时候的信息，在用户正真加入rtc房间的时候回调
    private JoinChannelCallBack invitedChannelCallback;
    private ChannelFullInfo invitedChannelInfo;

    private TokenService tokenService;

    //****************数据存储于标记end*******************

    //************************呼叫超时start********************
    private static final int TIME_OUT_LIMITED = 2 * 60 * 1000;//呼叫超时限制

    private int timeOut = TIME_OUT_LIMITED;//呼叫超时，最长2分钟

    private CountDownTimer timer;//呼出倒计时
    //************************呼叫超时end********************

    private Map<Long, String> memberInfoMap;

    private static final String BUSY_LINE = "i_am_busy";

    NERtcEx neRtc;

    private String appKey;

    private Handler mHandler;

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
            if(event.getChannelBaseInfo().getChannelStatus() == ChannelStatus.NORMAL) {
                handleNIMEvent(event);
            }else {
                Log.d(LOG_TAG,"this event is INVALID and cancel eventType = 0 " + event.getEventType());
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
        switch (ackEvent.getEventType()) {
            case ACCEPT:
                currentState.release();
                delegateManager.onError(CallErrorCode.OTHER_CLIENT_ACCEPT, "other client have accept.", true);
                break;
            case REJECT:
                currentState.release();
                delegateManager.onError(CallErrorCode.OTHER_CLIENT_REJECT, "other client have reject.", true);
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
        Log.d(LOG_TAG, "handle IM Event type =  " + eventType + " channelId = " + event.getChannelBaseInfo().getChannelId());
        switch (eventType) {
            case CLOSE:
                //信令channel被关闭
                ChannelCloseEvent channelCloseEvent = (ChannelCloseEvent) event;
                //设置imChannelId 为null 不在调用iM close
                if(TextUtils.equals(channelCloseEvent.getChannelBaseInfo().getChannelId(),imChannelId)) {
                    imChannelId = null;
                }
                hangup(null);
                if (delegateManager != null) {
                    delegateManager.onCallEnd(channelCloseEvent.getFromAccountId());
                }
                break;
            case JOIN:
                UserJoinEvent userJoinEvent = (UserJoinEvent) event;
                updateMemberMap(userJoinEvent.getMemberInfo());
                break;
            case INVITE:
                InvitedEvent invitedEvent = (InvitedEvent) event;
                if (delegateManager != null) {
                    if (currentState.getStatus() != CallState.STATE_IDLE) { //占线，直接拒绝
                        Log.d(LOG_TAG, "user is busy status =  " + currentState.getStatus());
                        InviteParamBuilder paramBuilder = new InviteParamBuilder(invitedEvent.getChannelBaseInfo().getChannelId(),
                                invitedEvent.getFromAccountId(), invitedEvent.getRequestId());
                        paramBuilder.customInfo(BUSY_LINE);
                        reject(paramBuilder, false, null);
                        break;
                    } else {
                        startCount();
                        delegateManager.onInvited(invitedEvent);
                    }
                }
                setCallType(invitedEvent);
                currentState.onInvited();
                break;
            case CANCEL_INVITE:
                CanceledInviteEvent canceledInviteEvent = (CanceledInviteEvent) event;
                Log.d(LOG_TAG, "accept cancel signaling request Id = " + canceledInviteEvent.getRequestId());
                hangup(null);
                if (delegateManager != null) {
                    delegateManager.onCancelByUserId(canceledInviteEvent.getFromAccountId());
                }
                currentState.release();
                break;
            case REJECT:
            case ACCEPT:
                InviteAckEvent ackEvent = (InviteAckEvent) event;
                if(!TextUtils.equals(ackEvent.getChannelBaseInfo().getChannelId(),imChannelId)){
                    break;
                }
                if (ackEvent.getAckStatus() == InviteAckStatus.ACCEPT && callType == Utils.ONE_TO_ONE_CALL ) {
                    handleWhenUserAccept(ackEvent.getChannelBaseInfo().getChannelId());
                } else if (ackEvent.getAckStatus() == InviteAckStatus.REJECT) {
                    if(callType == Utils.ONE_TO_ONE_CALL) {
                        hangup(null);
                        currentState.release();
                    }
                    if (TextUtils.equals(ackEvent.getCustomInfo(), BUSY_LINE)) {
                        Log.d(LOG_TAG,"reject as busy from " + ackEvent.getFromAccountId());
                        if (callOrderListener != null && callType == Utils.ONE_TO_ONE_CALL) {
                            callOrderListener.onBusy(ackEvent.getChannelBaseInfo().getType(), ackEvent.getFromAccountId(), callType);
                        }
                        delegateManager.onUserBusy(ackEvent.getFromAccountId());
                    } else {
                        Log.d(LOG_TAG,"reject by user from " + ackEvent.getFromAccountId());
                        if (callOrderListener != null && callType == Utils.ONE_TO_ONE_CALL) {
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
                    if (controlInfo.cid == 1 && invitedChannelInfo != null && currentState.getStatus() == CallState.STATE_INVITED) {
                        loadToken(selfRtcUid, new RequestCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                if (invitedChannelInfo != null) {
                                    int rtcResult = joinChannel(s, invitedChannelInfo.getChannelId());
                                    if (rtcResult != 0) {
                                        ToastUtils.showShort("join Rtc failed code = " + rtcResult);
                                        delegateManager.onError(rtcResult, "join Rtc failed", true);
                                        hangup(null);
                                    }
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
    private NERtcStatsObserver statsObserver = new NERtcStatsObserver() {

        @Override
        public void onRtcStats(NERtcStats neRtcStats) {

        }

        @Override
        public void onLocalAudioStats(NERtcAudioSendStats neRtcAudioSendStats) {

        }

        @Override
        public void onRemoteAudioStats(NERtcAudioRecvStats[] neRtcAudioRecvStats) {

        }

        @Override
        public void onLocalVideoStats(NERtcVideoSendStats neRtcVideoSendStats) {

        }

        @Override
        public void onRemoteVideoStats(NERtcVideoRecvStats[] neRtcVideoRecvStats) {

        }

        @Override
        public void onNetworkQuality(NERtcNetworkQualityInfo[] neRtcNetworkQualityInfos) {
            delegateManager.onUserNetworkQuality(neRtcNetworkQualityInfos);
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
            callType = Utils.ONE_TO_ONE_CALL;
        }
    }

    /**
     * 操作用户加入房间的消息
     *
     * @param channelId
     */
    private void handleWhenUserAccept(String channelId) {
        Log.d(LOG_TAG, "handleWhenUserAccept handleUserAccept = " + handleUserAccept + " status = " + currentState.getStatus());
        if (!handleUserAccept && currentState.getStatus() == CallState.STATE_CALL_OUT) {
            loadToken(selfRtcUid, new RequestCallback<String>() {

                @Override
                public void onSuccess(String s) {
                    int rtcResult = joinChannel(s, channelId);
                    if (rtcResult != 0) {
                        delegateManager.onError(rtcResult, "join rtc channel failed", true);
                        currentState.release();
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
            if(timer != null){
                timer.cancel();
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
        NIMClient.getService(SignallingService.class).sendControl(channelId, accountId, GsonUtils.toJson(controlInfo)).setCallback(callback);
    }


    /**
     * Nertc的回调
     */
    private NERtcCallback rtcCallback = new NERtcCallback() {
        @Override
        public void onJoinChannel(int i, long l, long l1) {
            Log.d(LOG_TAG, "onJoinChannel i = " + i + " l = " + l + " l1 =" + l1);
            haveJoinNertcChannel = true;
            if (callType == Utils.ONE_TO_ONE_CALL && currentState.getStatus() == CallState.STATE_CALL_OUT &&
                    !TextUtils.isEmpty(callUserId) && !TextUtils.isEmpty(imChannelId)) {
                sendControlEvent(imChannelId, callUserId, new ControlInfo(1), null);
            }
            if (callType == Utils.ONE_TO_ONE_CALL && invitedChannelCallback != null && invitedChannelInfo != null) {
                invitedChannelCallback.onJoinChannel(invitedChannelInfo);
                invitedChannelCallback = null;
                invitedChannelInfo = null;
            }
        }

        @Override
        public void onLeaveChannel(int i) {
            haveJoinNertcChannel = false;
            Log.d(LOG_TAG, "onLeaveChannel set status idel when onleaveChannel");
            currentState.release();
        }

        @Override
        public void onUserJoined(long l) {
            if (!isCurrentUser(l)) {
                Log.d(LOG_TAG, "onUserJoined set status dialog");
                currentState.dialog();
                if(invitedParams != null){
                    invitedParams.clear();
                }
            }
            if (delegateManager != null) {
                delegateManager.onUserEnter(l,memberInfoMap.get(l));
            }
        }

        @Override
        public void onUserLeave(long uid, int reason) {
            Log.d(LOG_TAG, String.format("onUserLeave uid = %d,reason = %d",uid,reason));
            if (currentState.getStatus() == CallState.STATE_DIALOG) {
                if(reason == 0){//正常离开
                    delegateManager.onUserLeave(memberInfoMap.get(uid));
                } else {//非正常离开
                    delegateManager.onUserDisconnect(memberInfoMap.get(uid));
                    if(callType == Utils.ONE_TO_ONE_CALL){
                        currentState.release();
                        leave(null);
                    }
                }
            }
        }

        @Override
        public void onUserAudioStart(long l) {
            Log.d(LOG_TAG, "onUserAudioStart");
            if (!isCurrentUser(l)) {
                NERtcEx.getInstance().subscribeRemoteAudioStream(l, true);
            }
            if (delegateManager != null) {
                delegateManager.onAudioAvailable(l, true);
            }
        }

        @Override
        public void onUserAudioStop(long l) {
            Log.d(LOG_TAG, "onUserAudioStop");
            if (delegateManager != null) {
                delegateManager.onAudioAvailable(l, false);
            }
        }

        @Override
        public void onUserVideoStart(long l, int i) {
            Log.d(LOG_TAG, "onUserVideoStart");
            if (!isCurrentUser(l)) {
                NERtcEx.getInstance().subscribeRemoteVideoStream(l, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, true);
            }
            if (delegateManager != null) {
                delegateManager.onCameraAvailable(l, true);
            }
        }

        @Override
        public void onUserVideoStop(long l) {
            Log.d(LOG_TAG, "onUserVideoStop");
            if (delegateManager != null) {
                delegateManager.onCameraAvailable(l, false);
            }
        }

        @Override
        public void onDisconnect(int i) {
            Log.d(LOG_TAG, "onDisconnect");
            currentState.release();
            delegateManager.onError(CallErrorCode.NERTC_DISCONNECT, "disconnect error", true);
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

        //初始化之前 destroy
        if (neRtc != null) {
            destroy();
        }

        mHandler = new Handler(context.getMainLooper());

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
                    NIMClient.getService(AuthService.class).login(loginInfo).setCallback(callback);
                } else if (statusCode == StatusCode.LOGINED) {
                    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(this, false);
                    if (userInfoInitCallBack != null) {
                        userInfoInitCallBack.onUserLoginToIm(imAccount, imToken);
                    }
                }
            }
        }, true);

    }

    @Override
    public void logout() {
        NIMClient.getService(AuthService.class).logout();
    }

    @Override
    public void addDelegate(NERTCCallingDelegate delegate) {
        delegateManager.addDelegate(delegate);
    }

    public void addServiceDelegate(NERTCCallingDelegate delegate) {
        delegateManager.addDelegate(delegate);
        //处理保存的offline 消息
        if (offlineEvent.size() > 0) {
            Log.d(LOG_TAG, "offline event dispatch to service");
            handleOfflineEvents(offlineEvent);
            offlineEvent.clear();
        }
    }

    @Override
    public void removeDelegate(NERTCCallingDelegate delegate) {
        delegateManager.removeDelegate(delegate);
    }

    @Override
    public void setupRemoteView(NERtcVideoView videoRender, long uid) {
        if (neRtc == null) {
            return;
        }
        videoRender.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
        neRtc.setupRemoteVideoCanvas(videoRender, uid);
    }

    @Override
    public void setupLocalView(NERtcVideoView videoRender) {
        if (neRtc == null) {
            return;
        }
        neRtc.enableLocalAudio(true);
        neRtc.enableLocalVideo(true);
        videoRender.setZOrderMediaOverlay(true);
        videoRender.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
        neRtc.setupLocalVideoCanvas(videoRender);
    }

    @Override
    public void setAudioMute(boolean mute, long userId) {
        NERtcEx.getInstance().subscribeRemoteAudioStream(userId, !mute);
    }

    @Override
    public void switchCallType(ChannelType type, RequestCallback<Void> callback) {
        if (currentState.getStatus() != CallState.STATE_DIALOG) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "switchCallType status error, status = " + currentState.getStatus(), false);
        }
        if (type == ChannelType.AUDIO) {
            neRtc.enableLocalVideo(false);
            sendControlEvent(imChannelId, callUserId, new ControlInfo(2, type.getValue()), new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    NERtcEx.getInstance().enableLocalAudio(false);
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
    public void setTimeOut(int timeOut) {
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
            timer = null;
        }
        timer = new CountDownTimer(timeOut, 1000) {
            @Override
            public void onTick(long l) {
                if (currentState.getStatus() != CallState.STATE_CALL_OUT &&
                        currentState.getStatus() != CallState.STATE_INVITED) {
                    timer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (callOrderListener != null &&
                        callOutType != null && !TextUtils.isEmpty(callUserId)) {
                    callOrderListener.onTimeout(callOutType, callUserId, callType);
                }

                callOutType = null;
                callUserId = "";
                if (currentState.getStatus() == CallState.STATE_CALL_OUT) {
                    NERTCVideoCallImpl.this.cancel(null);
                } else if (currentState.getStatus() == CallState.STATE_INVITED) {
                    imChannelId = null;
                    hangup(null);
                }
                if (delegateManager != null) {
                    delegateManager.timeOut();
                }
            }
        };
        timer.start();
    }

    private String getCustomInfo(int callType, ArrayList<String> accounts, String groupId) {
        return GsonUtils.toJson(new CustomInfo(callType, accounts, groupId));
    }

    @Override
    public void call(final String userId, String selfUserId, ChannelType type, @NotNull JoinChannelCallBack joinChannelCallBack) {
        if (currentState.getStatus() != CallState.STATE_IDLE) {
            joinChannelCallBack.onJoinFail("status Error", -1);
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "call status error: status = " + currentState.getStatus(), false);
            return;
        }
        currentState.callOut();
        startCount();//启动倒计时
        callType = Utils.ONE_TO_ONE_CALL;
        //保存数据，用于生成话单
        callOutType = type;
        callUserId = userId;
        handleUserAccept = false;

        if (type == ChannelType.AUDIO) {
            neRtc.enableLocalVideo(false);
        }

        createIMChannelAndJoin(Utils.ONE_TO_ONE_CALL, null, type, selfUserId, null, userId, joinChannelCallBack);

    }

    @Override
    public void groupCall(ArrayList<String> userIds, String groupId, String selfUserId, ChannelType type, @NotNull JoinChannelCallBack joinChannelCallBack) {
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
        callType = Utils.GROUP_CALL;
        handleUserAccept = false;
        //1,创建channel
        createIMChannelAndJoin(Utils.GROUP_CALL, groupId, type, selfUserId, userIds, null, joinChannelCallBack);
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
                                        ArrayList<String> userIds, String callUserId, JoinChannelCallBack joinChannelCallBack) {
        NIMClient.getService(SignallingService.class).create(type, null, null).setCallback(new RequestCallback<ChannelBaseInfo>() {
            @Override
            public void onSuccess(ChannelBaseInfo param) {
                //2,join channel
                if (param != null) {
                    imChannelId = param.getChannelId();
                    joinIMChannel(callType, groupId, type, param, selfUserId, userIds, callUserId, joinChannelCallBack);
                }
            }

            @Override
            public void onFailed(int code) {
                Log.d(LOG_TAG, "create channel failed code = " + code);
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
                               String callUserId, JoinChannelCallBack joinChannelCallBack) {
        //2,join channel for IM
        NIMClient.getService(SignallingService.class).join(channelInfo.getChannelId(), 0, "", true).setCallback(new RequestCallback<ChannelFullInfo>() {
            @Override
            public void onSuccess(ChannelFullInfo param) {

                //保存Uid
                storeUid(param.getMembers(), selfUserId);

                if (callType == Utils.GROUP_CALL) {
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
                                        inviteOneUserWithIM(callType, type, userId, selfUserId, param, groupId, allUserIds);
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

                } else if (callType == Utils.ONE_TO_ONE_CALL) {
                    //一对一通话直接发起邀请，在对方接受邀请之后再加入channel
                    inviteOneUserWithIM(callType, type, callUserId, selfUserId, param, null, null);
                    joinChannelCallBack.onJoinChannel(param);
                }


            }

            @Override
            public void onFailed(int code) {
                Log.d(LOG_TAG, "join channel failed code = " + code);
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
        Log.d(LOG_TAG, "random int = " + randomInt);
        return System.currentTimeMillis() + randomInt + "_id";
    }

    /**
     * 邀请用户加入channel
     *
     * @param callType
     * @param userId
     * @param selfUid
     * @param channelInfo
     * @param callUsers
     */
    private void inviteOneUserWithIM(int callType, ChannelType channelType, String userId, String selfUid, ChannelFullInfo channelInfo, String groupId, ArrayList<String> callUsers) {
        String invitedRequestId = getRequestId();
        InviteParamBuilder inviteParam = new InviteParamBuilder(channelInfo.getChannelId(), userId, invitedRequestId);
        inviteParam.customInfo(getCustomInfo(callType, callUsers, groupId));
        inviteParam.pushConfig(getPushConfig(callType, channelType, invitedRequestId, selfUid, channelInfo.getChannelId(), callUsers));
        inviteParam.offlineEnabled(true);

        Log.d(LOG_TAG, "sendInvited channelName = " + channelInfo.getChannelId() + " userId = " + userId + " requestId = " + invitedRequestId);

        NIMClient.getService(SignallingService.class).invite(inviteParam).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                //保留邀请信息，取消用
                Log.d(LOG_TAG, "sendInvited success channelName = " + channelInfo.getChannelId() + " userId = " + userId + " requestId = " + invitedRequestId);
                saveInvitedInfo(inviteParam);
            }

            @Override
            public void onFailed(int code) {
                Log.d(LOG_TAG, "sendInvited failed channelName = " + code);
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
        currentState.release();
    }

    /**
     * 加入视频通话频道
     *
     * @param token       null时非安全模式
     * @param channelName
     * @return 0 方法调用成功，其他失败
     */
    private int joinChannel(String token, String channelName) {
        Log.d(LOG_TAG,"joinChannel token = " + token + " channelName = " + channelName);
        if (selfRtcUid != 0) {
            //加入rtc房间之前设置一个默认的videoConfig，清除上次通话的设置
            NERtcVideoConfig videoConfig = new NERtcVideoConfig();
            videoConfig.videoProfile = NERtcConstants.VideoProfile.HD720P;
            neRtc.setLocalVideoConfig(videoConfig);
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
        Log.d(LOG_TAG, "accept");
        if (timer != null) {
            timer.cancel();
        }
        if (currentState.getStatus() != CallState.STATE_INVITED) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "accept status error, status = " + currentState.getStatus(), false);
        }
        NIMClient.getService(SignallingService.class).acceptInviteAndJoin(inviteParam, 0).setCallback(
                new RequestCallbackWrapper<ChannelFullInfo>() {

                    @Override
                    public void onResult(int code, ChannelFullInfo channelFullInfo, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            Log.d(LOG_TAG, "accept success");
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

                            if (callType == Utils.GROUP_CALL) {
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
                            }

                        } else {
                            Log.d(LOG_TAG,"accept failed code = "+ code);
                            joinChannelCallBack.onJoinFail("accept channel failed", code);
                        }
                    }
                });
    }

    @Override
    public void reject(InviteParamBuilder inviteParam, RequestCallback<Void> callback) {
        if (currentState.getStatus() != CallState.STATE_INVITED) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "reject status error,status = " + currentState.getStatus(), false);
        }
        reject(inviteParam, true, callback);
    }

    /**
     * 拒绝
     *
     * @param inviteParam
     * @param byUser
     */
    private void reject(InviteParamBuilder inviteParam, boolean byUser, RequestCallback<Void> callback) {
        Log.d(LOG_TAG,"reject by user = " + byUser);
        NIMClient.getService(SignallingService.class).rejectInvite(inviteParam).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (byUser) {
                    currentState.release();
                }
                if (callback != null) {
                    callback.onSuccess(aVoid);
                }
            }

            @Override
            public void onFailed(int i) {
                Log.d(LOG_TAG,"reject failed code = "+i);
                if (byUser && i != ResponseCode.RES_INVITE_HAS_ACCEPT) {//已经接受
                    currentState.release();
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
                    currentState.release();
                }
            }
        });

    }

    @Override
    public void hangup(RequestCallback<Void> callback) {
        Log.d(LOG_TAG, "hangup");
        if (currentState.getStatus() != CallState.STATE_CALL_OUT && currentState.getStatus() != CallState.STATE_DIALOG) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "hangup status error,status = " + currentState.getStatus(), false);
        }
        //离开NERtc的channel
        int rtcResult = -1;
        if (neRtc != null) {
            rtcResult = neRtc.leaveChannel();
        }

        if (rtcResult != 0 && callback != null) {
            callback.onFailed(rtcResult);
        }
        //离开信令的channel
        if (!TextUtils.isEmpty(imChannelId)) {
            closeIMChannel(imChannelId, callback);
        }else if(callback != null){
            callback.onFailed(-1);
        }
        if(mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        currentState.release();
    }

    @Override
    public void leave(RequestCallback<Void> callback) {
        //群呼如果未接通走取消逻辑
        if (currentState.getStatus() != CallState.STATE_DIALOG) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "leave status error,status = " + currentState.getStatus(), false);
        }
        if (callType == Utils.GROUP_CALL && currentState.getStatus() == CallState.STATE_CALL_OUT) {
            cancel(new RequestCallback<Void>() {
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
     * @param callback
     */
    private void leaveAndClear(RequestCallback<Void> callback){
        singleLeave(callback);
        invitedParams.clear();
        invitedChannelCallback = null;
        invitedChannelInfo = null;
        mHandler.removeCallbacksAndMessages(null);
        currentState.release();
    }

    /**
     * 用户离开
     *
     * @param callback
     */
    private void singleLeave(RequestCallback<Void> callback) {
        //离开NERtc的channel
        int result = -1;
        if (neRtc != null) {
            result = neRtc.leaveChannel();
        }
        if (result != 0 && callback != null) {
            callback.onFailed(result);
        }
        //离开信令的channel
        if (!TextUtils.isEmpty(imChannelId)) {
            leaveIMChannel(imChannelId, callback);
        }else if(callback != null){
            callback.onFailed(-1);
        }
        currentState.release();
    }

    @Override
    public void cancel(RequestCallback<Void> callback) {
        Log.d(LOG_TAG, "cancel");
        if (handleUserAccept) {
            return;
        }
        if (currentState.getStatus() != CallState.STATE_CALL_OUT) {
            delegateManager.onError(CallErrorCode.STATUS_ERROR, "cancel status error,status = " + currentState.getStatus(), false);
        }
        final boolean[] needCallback = {callback != null};
//        final int statusOld = status;
//        status = STATE_CANCELED;
        if (invitedParams != null && invitedParams.size() > 0) {
            for (InviteParamBuilder inviteParam : invitedParams) {
                Log.d(LOG_TAG, "send cancel signaling");
                NIMClient.getService(SignallingService.class).cancelInvite(inviteParam).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG,"cancel success");
                        if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(callUserId)) {
                            callOrderListener.onCanceled(callOutType, callUserId, callType);
                        }

                        currentState.release();

                        invitedParams.clear();
                        callOutType = null;
                        callUserId = "";
                        if (needCallback[0] && callback != null) {
                            callback.onSuccess(aVoid);
                            needCallback[0] = false;
                        }
                        if (callType == Utils.ONE_TO_ONE_CALL) {
                            hangup(null);
                        }
                    }

                    @Override
                    public void onFailed(int i) {
                        if (i != ResponseCode.RES_INVITE_HAS_ACCEPT) {
                            if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(callUserId)) {
                                callOrderListener.onCanceled(callOutType, callUserId, callType);
                            }
                        }

                        Log.d(LOG_TAG, "send cancel signaling failed code = " + i);
                        if (needCallback[0] && callback != null) {
                            callback.onFailed(i);
                            needCallback[0] = false;
                        }

                        if (callType == Utils.ONE_TO_ONE_CALL) {
                            if(i == ResponseCode.RES_INVITE_HAS_ACCEPT){//用户已经接受
//                                status = statusOld;
                                handleWhenUserAccept(imChannelId);
                            }else {
                                currentState.release();
                                hangup(null);
                            }
                        }
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Log.d(LOG_TAG, "send cancel signaling exception", throwable);
                        if (callOrderListener != null && callOutType != null && !TextUtils.isEmpty(callUserId)) {
                            callOrderListener.onCanceled(callOutType, callUserId, callType);
                        }
                        if (needCallback[0] && callback != null) {
                            callback.onException(throwable);
                            needCallback[0] = false;
                        }

                        if (callType == Utils.ONE_TO_ONE_CALL) {
                            hangup(null);
                        }
                    }
                });
            }
        } else {
            if (needCallback[0]) {
                callback.onException(new Exception("invited params have clear"));
                needCallback[0] = false;
            }

            if (callType == Utils.ONE_TO_ONE_CALL) {
                hangup(null);
            }
        }
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
        Log.d(LOG_TAG, "closeIMChannel ");
        NIMClient.getService(SignallingService.class).close(channelId, false, null)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            Log.d(LOG_TAG, "closeIMChannel success channelId = " + channelId);
                            imChannelId = null;
                            if (callback != null) {
                                callback.onSuccess(result);
                            }
                        } else {
                            Log.d(LOG_TAG, "closeIMChannel failed code = " + code + "channelId" + channelId);
                            if (callback != null) {
                                callback.onFailed(code);
                            }
                        }
                        invitedParams.clear();
                        invitedChannelCallback = null;
                        invitedChannelInfo = null;
                        currentState.release();
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
        NERtcEx.getInstance().enableLocalAudio(!isMute);
    }


    private void destroy() {
        NIMClient.getService(SignallingServiceObserver.class).observeOnlineNotification(nimOnlineObserver, false);
        NIMClient.getService(SignallingServiceObserver.class).observeOfflineNotification(nimOfflineObserver, false);
        NIMClient.getService(SignallingServiceObserver.class).observeOtherClientInviteAckNotification(otherClientEvent, false);
        if (neRtc != null) {
            neRtc.setStatsObserver(null);
            neRtc.release();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    private void loadToken(final long uid, RequestCallback<String> callback) {
        if (TextUtils.isEmpty(appKey)) {
            callback.onFailed(-1);
            return;
        }

        if (tokenService == null) {
            callback.onFailed(-2);
            return;
        }

        tokenService.getToken(uid, callback);
    }

    private void loadTokenError(){
        Log.d(LOG_TAG,"request token failed ");
        if(callType == Utils.ONE_TO_ONE_CALL) {
            hangup(null);
            delegateManager.onError(CallErrorCode.LOAD_TOKEN_ERROR, "get token error", true);
        }else {
            leave(null);
        }
    }
}
