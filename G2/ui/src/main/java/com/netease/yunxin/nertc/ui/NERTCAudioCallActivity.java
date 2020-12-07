package com.netease.yunxin.nertc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.netease.lava.nertc.sdk.NERtc;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.stats.NERtcAudioRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioSendStats;
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo;
import com.netease.lava.nertc.sdk.stats.NERtcStats;
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver;
import com.netease.lava.nertc.sdk.stats.NERtcVideoRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcVideoSendStats;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.nimlib.sdk.avsignalling.model.MemberInfo;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.nertc.model.ProfileManager;
import com.netease.yunxin.nertc.model.UserModel;
import com.netease.yunxin.nertc.nertcvideocalldemo.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocalldemo.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocalldemo.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocalldemo.utils.CallParams;
import com.netease.yunxin.nertc.ui.team.AVChatSoundPlayer;

import java.util.ArrayList;
import java.util.List;

public class NERTCAudioCallActivity extends AppCompatActivity {

    private static final String LOG_TAG = NERTCAudioCallActivity.class.getSimpleName();

    public static final String INVENT_EVENT = "call_in_event";
    public static final String CALL_OUT_USER = "call_out_user";

    private NERTCVideoCall nertcVideoCall;

    private ImageView ivUserIcon;
    private TextView tvCallUser;
    private TextView tvCallComment;
    private ImageView ivMute;
    private ImageView ivHungUp;
    private LinearLayout llyCancel;
    private LinearLayout llyBingCall;
    private LinearLayout llyDialogOperation;
    private ImageView ivAccept;
    private ImageView ivReject;
    private RelativeLayout rlyTopUserInfo;

    private UserModel callOutUser;//呼出用户
    private boolean callReceived;

    private String inventRequestId;
    private String inventChannelId;
    private String inventFromAccountId;

    private boolean isMute;//是否静音
    private boolean isCamOff;//是否关闭摄像头

    private static final int DELAY_TIME = 0;//延时

    private String peerAccid;
    private long peerUid;

    private NERTCCallingDelegate nertcCallingDelegate = new NERTCCallingDelegate() {
        @Override
        public void onError(int errorCode, String errorMsg) {
            ToastUtils.showLong(errorMsg + " errorCode:" + errorCode);
            finish();
        }

        @Override
        public void onInvited(InvitedEvent invitedEvent) {

        }


        @Override
        public void onUserEnter(long uid, String accId) {
            llyCancel.setVisibility(View.GONE);
            llyDialogOperation.setVisibility(View.VISIBLE);
            llyBingCall.setVisibility(View.GONE);
            setupLocalAudio();
            AVChatSoundPlayer.instance().stop();

            peerAccid = accId;
            peerUid = uid;
            Log.i(LOG_TAG, String.format("onUserEnter uid:%d, accId:%s", uid, accId));
        }

        @Override
        public void onCallEnd(String userId) {
            onCallEnd(userId, "对方已经挂断");
        }

        @Override
        public void onUserLeave(String accountId) {
            Log.i(LOG_TAG, "onUserLeave:" + accountId);
            if (TextUtils.equals(accountId, peerAccid)) {
                onCallEnd(accountId, "对方已经离开");
            }
        }

        private void onCallEnd(String userId, String tip) {
            if (!isDestroyed() && !ProfileManager.getInstance().isCurrentUser(userId)) {
                ToastUtils.showLong(tip);
//                hungUpAndFinish();
                handler.postDelayed(() -> finish(), DELAY_TIME);
                AVChatSoundPlayer.instance().stop();
            }
        }

        @Override
        public void onRejectByUserId(String userId) {
            if (!isDestroyed() && !callReceived) {
                ToastUtils.showLong("对方已经拒绝");
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);
            }
        }


        @Override
        public void onUserBusy(String userId) {
            if (!isDestroyed() && !callReceived) {
                ToastUtils.showLong("对方占线");
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
            }
        }

        @Override
        public void onCancelByUserId(String uid) {
            if (!isDestroyed() && callReceived) {
                ToastUtils.showLong("对方取消");
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
                AVChatSoundPlayer.instance().stop();
            }
        }


        @Override
        public void onCameraAvailable(long userId, boolean isVideoAvailable) {

        }

        @Override
        public void onAudioAvailable(long userId, boolean isAudioAvailable) {
            if (!ProfileManager.getInstance().isCurrentUser(userId)) {
                if (isAudioAvailable) {
                    setUserInfoOnDialog();
                }
            }
        }

        @Override
        public void timeOut() {
            if(callReceived){
                ToastUtils.showLong("对方无响应");
            }else {
                ToastUtils.showLong("呼叫超时");
            }
            AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.NO_RESPONSE);
            handler.postDelayed(() -> {
                AVChatSoundPlayer.instance().stop();
                finish();
            }, DELAY_TIME);
        }
    };

    private Handler handler = new Handler();

    public static void startBeingCall(Context context, InvitedEvent invitedEvent) {
        Intent intent = new Intent(context, NERTCAudioCallActivity.class);
        intent.putExtra(INVENT_EVENT, invitedEvent);
        intent.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startCallOther(Context context, UserModel callOutUser) {
        Intent intent = new Intent(context, NERTCAudioCallActivity.class);
        intent.putExtra(CALL_OUT_USER, callOutUser);
        intent.putExtra(CallParams.INVENT_CALL_RECEIVED, false);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 应用运行时，保持不锁屏、全屏化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.audio_call_layout);
        initIntent();
        initView();
        initData();

        NERtcEx.getInstance().setStatsObserver(new NERtcStatsObserver() {
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

//            0	网络质量未知
//            1	网络质量极好
//            2	用户主观感觉和极好差不多，但码率可能略低于极好
//            3	能沟通但不顺畅
//            4	网络质量差
//            5	完全无法沟通
            @Override
            public void onNetworkQuality(NERtcNetworkQualityInfo[] neRtcNetworkQualityInfos) {
                if (neRtcNetworkQualityInfos == null && neRtcNetworkQualityInfos.length == 0) {
                    return;
                }

                for (NERtcNetworkQualityInfo networkQualityInfo : neRtcNetworkQualityInfos) {
                    if (networkQualityInfo.userId == peerUid) {
                        if (networkQualityInfo.upStatus >= 4 || networkQualityInfo.downStatus >= 4) {
                            Toast.makeText(NERTCAudioCallActivity.this,
                                    "对方网络质量差",
                                    Toast.LENGTH_SHORT).show();
                        } else if (networkQualityInfo.upStatus == 0 || networkQualityInfo.downStatus == 0) {
                            Toast.makeText(NERTCAudioCallActivity.this,
                                    "对方网络质量似乎较差",
                                    Toast.LENGTH_SHORT).show();
                        }

                        Log.i(LOG_TAG, String.format("NERtcNetworkQualityInfo: %d", networkQualityInfo.downStatus));
                    }
                }
            }
        });
    }


    private void initIntent() {
        inventRequestId = getIntent().getStringExtra(CallParams.INVENT_REQUEST_ID);
        inventChannelId = getIntent().getStringExtra(CallParams.INVENT_CHANNEL_ID);
        inventFromAccountId = getIntent().getStringExtra(CallParams.INVENT_FROM_ACCOUNT_ID);
        callOutUser = (UserModel) getIntent().getSerializableExtra(CALL_OUT_USER);
        callReceived = getIntent().getBooleanExtra(CallParams.INVENT_CALL_RECEIVED, false);
    }

    private void initData() {
        nertcVideoCall = NERTCVideoCall.sharedInstance();
        nertcVideoCall.setTimeOut(30 * 1000);
        nertcVideoCall.addDelegate(nertcCallingDelegate);

        ivMute.setOnClickListener(view -> {
            isMute = !isMute;
            nertcVideoCall.muteLocalAudio(isMute);
            if (isMute) {
                Glide.with(NERTCAudioCallActivity.this).load(R.drawable.voice_off).into(ivMute);
            } else {
                Glide.with(NERTCAudioCallActivity.this).load(R.drawable.voice_on).into(ivMute);
            }
        });

        ivHungUp.setOnClickListener(view -> {
            hungUpAndFinish();
        });

        if (!callReceived && callOutUser != null) {
            tvCallUser.setText("正在呼叫 " + callOutUser.nickname);
            llyCancel.setVisibility(View.VISIBLE);
            llyBingCall.setVisibility(View.GONE);
            llyCancel.setOnClickListener(v -> {
                nertcVideoCall.cancel(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        AVChatSoundPlayer.instance().stop();
                        finish();
                    }

                    @Override
                    public void onFailed(int i) {
                        // 10410 邀请已经接受了
                        if (i == 10410) {
                            return;
                        }

                        AVChatSoundPlayer.instance().stop();
                        finish();
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Log.e("NERTCVideoCallActivity", "cancel Failed", throwable);
                        AVChatSoundPlayer.instance().stop();
                        finish();
                    }
                });
            });
            Glide.with(this).load(callOutUser.avatar).apply(RequestOptions.bitmapTransform(new RoundedCorners(5))).into(ivUserIcon);
        } else if (!TextUtils.isEmpty(inventRequestId) && !TextUtils.isEmpty(inventFromAccountId) && !TextUtils.isEmpty(inventChannelId)) {
            llyCancel.setVisibility(View.GONE);
            llyBingCall.setVisibility(View.VISIBLE);
        }

        PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.CAMERA, PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> granted) {
                        if (callReceived) {
                            callIn();
                        } else {
                            callOUt();
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {

                    }
                }).request();
    }


    private void callOUt() {
        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
        String selfUserId = ProfileManager.getInstance().getUserModel().imAccid;
        nertcVideoCall.call(callOutUser.imAccid, selfUserId, ChannelType.AUDIO, new JoinChannelCallBack() {
            @Override
            public void onJoinChannel(ChannelFullInfo channelFullInfo) {
                resetUid(channelFullInfo, selfUserId);
            }

            @Override
            public void onJoinFail(String msg, int code) {
                if (code == 10201) {
                    return;
                }

                finishOnFailed();
            }
        });
    }

    private void resetUid(ChannelFullInfo channelFullInfo, String selfUserId) {
        if (channelFullInfo != null) {
            for (MemberInfo member : channelFullInfo.getMembers()) {
                if (TextUtils.equals(member.getAccountId(), selfUserId)) {
                    ProfileManager.getInstance().getUserModel().g2Uid = member.getUid();
                    break;
                }
            }
        }
    }

    private void callIn() {
        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
        InviteParamBuilder invitedParam = new InviteParamBuilder(inventChannelId, inventFromAccountId, inventRequestId);
        ivAccept.setOnClickListener(view -> {
            accept(invitedParam);
            AVChatSoundPlayer.instance().stop();
        });

        ivReject.setOnClickListener(view -> {
            nertcVideoCall.reject(invitedParam, new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    finish();
                }

                @Override
                public void onFailed(int i) {
                    Log.e(LOG_TAG, "reject failed error code = " + i);

                    if (i == 408) {
                        ToastUtils.showShort("Reject timeout");
                    } else {
                        Log.e(LOG_TAG, "Reject failed:" + i);
                        if (i == 10404 || i == 10408 || i == 10409 || i == 10201) {
                            finishOnFailed();
                        }
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.e(LOG_TAG, "reject failed onException", throwable);
                    finishOnFailed();
                }
            });
            AVChatSoundPlayer.instance().stop();

        });
        List<String> userAccount = new ArrayList<>();
        userAccount.add(inventFromAccountId);
        NIMClient.getService(UserService.class).fetchUserInfo(userAccount).setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {
                NimUserInfo userInfo = param.get(0);
                tvCallUser.setText(userInfo.getName());
                tvCallComment.setText("邀请您音频通话");

                NIMClient.getService(NosService.class).getOriginUrlFromShortUrl(userInfo.getAvatar()).setCallback(new RequestCallback<String>() {
                    @Override
                    public void onSuccess(String param) {
                        Glide.with(NERTCAudioCallActivity.this).load(param).apply(RequestOptions.bitmapTransform(new RoundedCorners(5))).into(ivUserIcon);
                    }

                    @Override
                    public void onFailed(int code) {
                        Glide.with(NERTCAudioCallActivity.this).load(userInfo.getAvatar()).apply(RequestOptions.bitmapTransform(new RoundedCorners(5))).into(ivUserIcon);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Glide.with(NERTCAudioCallActivity.this).load(userInfo.getAvatar()).apply(RequestOptions.bitmapTransform(new RoundedCorners(5))).into(ivUserIcon);
                    }
                });
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });

    }


    private void initView() {
        ivUserIcon = findViewById(R.id.iv_call_user);
        tvCallUser = findViewById(R.id.tv_call_user);
        tvCallComment = findViewById(R.id.tv_call_comment);
        ivMute = findViewById(R.id.iv_audio_control);
        ivHungUp = findViewById(R.id.iv_hangup);
        llyCancel = findViewById(R.id.lly_cancel);
        ivAccept = findViewById(R.id.iv_accept);
        ivReject = findViewById(R.id.iv_reject);
        llyBingCall = findViewById(R.id.lly_invited_operation);
        llyDialogOperation = findViewById(R.id.lly_dialog_operation);
        rlyTopUserInfo = findViewById(R.id.rly_top_user_info);
    }

    /**
     * 设置本地视频视图
     */
    private void setupLocalAudio() {
        NERtc.getInstance().enableLocalAudio(true);
    }

    private void accept(InviteParamBuilder invitedParam) {
        String selfUserId = String.valueOf(ProfileManager.getInstance().getUserModel().imAccid);
        nertcVideoCall.accept(invitedParam, selfUserId, new JoinChannelCallBack() {
            @Override
            public void onJoinChannel(ChannelFullInfo channelFullInfo) {
                resetUid(channelFullInfo, selfUserId);
            }

            @Override
            public void onJoinFail(String msg, int code) {
                if (code == 408) {
                    ToastUtils.showShort("Accept timeout");
                } else {
                    Log.e(LOG_TAG, "Accept failed:" + code);
                    if (code == 10404 || code == 10408 || code == 10409 || code == 10201) {
                        if(code == 10201){
                            ToastUtils.showShort("对方已经掉线");
                        }
                        handler.postDelayed(() -> {
                            finishOnFailed();
                        }, DELAY_TIME);
                    }
                }
            }
        });
    }

    private void setUserInfoOnDialog() {
        rlyTopUserInfo.setVisibility(View.VISIBLE);
        tvCallComment.setText("正在通话");
    }


    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        final AlertDialog.Builder confirmDialog =
                new AlertDialog.Builder(NERTCAudioCallActivity.this);
        confirmDialog.setTitle("结束通话");
        confirmDialog.setMessage("是否结束通话？");
        confirmDialog.setPositiveButton("是",
                (dialog, which) -> {
                    hungUpAndFinish();
                });
        confirmDialog.setNegativeButton("否",
                (dialog, which) -> {

                });
        confirmDialog.show();
    }

    private void hungUpAndFinish() {
        nertcVideoCall.hangup(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
            }

            @Override
            public void onFailed(int i) {
                Log.e(LOG_TAG, "error when hangup code = " + i);
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e(LOG_TAG, "onException when hangup", throwable);
                handler.postDelayed(() -> {
                    finish();
                }, DELAY_TIME);
            }
        });
    }

    private void finishOnFailed() {
        try {
            NERTCVideoCall.sharedInstance().leave(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(LOG_TAG, "finishOnFailed leave onSuccess");
                }

                @Override
                public void onFailed(int i) {
                    Log.e(LOG_TAG, "finishOnFailed leave onFailed code = " + i);
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.e(LOG_TAG, "finishOnFailed leave onException", throwable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        NERtcEx.getInstance().setStatsObserver(null);

        handler.removeCallbacksAndMessages(null);
        if (nertcCallingDelegate != null && nertcVideoCall != null) {
            nertcVideoCall.removeDelegate(nertcCallingDelegate);
        }
        super.onDestroy();
    }
}
