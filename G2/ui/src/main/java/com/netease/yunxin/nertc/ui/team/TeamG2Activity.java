package com.netease.yunxin.nertc.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.nimlib.sdk.avsignalling.model.MemberInfo;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.nertc.model.ProfileManager;
import com.netease.yunxin.nertc.nertcvideocall.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.UIServiceManager;
import com.netease.yunxin.nertc.nertcvideocall.utils.ALog;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.ui.R;
import com.netease.yunxin.nertc.ui.team.model.TeamG2Adapter;
import com.netease.yunxin.nertc.ui.team.model.TeamG2Item;
import com.netease.yunxin.nertc.ui.team.recyclerview.decoration.SpacingDecoration;
import com.netease.yunxin.nertc.ui.team.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.netease.yunxin.nertc.ui.team.model.TeamG2Item.TYPE.TYPE_DATA;


/**
 * 一、新版item顺序:邀请者放第一个，如果自己是邀请者，列表展示邀请顺序；否则按进房间顺序从左到右、从上到下排列
 *
 */
public class TeamG2Activity extends UI {
    // CONST
    private static final String TAG = "TeamAVChat";
    private static final String KEY_TEAM_ID = "teamid";
    private static final String KEY_ACCOUNTS = CallParams.INVENT_USER_IDS;
    private static final String KEY_TNAME = "teamName";
    private static final int AUTO_REJECT_CALL_TIMEOUT = 45 * 1000;
    private static final int CHECK_RECEIVED_CALL_TIMEOUT = 45 * 1000;
    private static final int MAX_SUPPORT_ROOM_USERS_COUNT = 9;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 0x100;

    // DATA
    private String teamId;
    private String chatId;
    private ArrayList<String> accounts;
    private boolean receivedCall;
    private boolean destroyRTC;
    private String teamName;
    private String groupId;//群Id

    // CONTEXT
    private Handler mainHandler;

    // LAYOUT
    private View callLayout;
    private View surfaceLayout;

    // VIEW
    private RecyclerView recyclerView;
    private TeamG2Adapter adapter;
    private List<TeamG2Item> data;
    private View voiceMuteButton;

    // TIMER
    private Timer timer;
    private int seconds;
    private TextView timerText;
    private Runnable autoRejectTask;

    // CONTROL STATE
    boolean videoMute = false;
    boolean microphoneMute = false;
    boolean speakerMode = true;

    // AVCAHT OBSERVER
    NERTCCallingDelegate nertcCallingDelegate;

    //invited
    private String invitedChannelId;
    private String invitedRequestId;
    private String invitedAccid;

    private final static int CONTACT_SELECTOR_REQUEST_CODE =1000;

    public static void startActivity(Context context, boolean receivedCall, String teamId, ArrayList<String> accounts, String teamName) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClass(context, TeamG2Activity.class);
        intent.putExtra(CallParams.INVENT_CALL_RECEIVED, receivedCall);
        intent.putExtra(KEY_TEAM_ID, teamId);
        intent.putExtra(KEY_ACCOUNTS, accounts);
        intent.putExtra(KEY_TNAME, teamName);
        intent.putExtra(CallParams.TEAM_CHAT_GROUP_ID, teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log( "TeamAVChatActivity onCreate, savedInstanceState=" + savedInstanceState);
        dismissKeyguard();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.team_avchat_activity);
        onInit();
        onIntent();
        findLayouts();
        showViews();
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> granted) {
                        if (granted!=null&&!granted.isEmpty()){
                            for (String s : granted) {
                                 log("onGranted:"+s);
                            }
                        }
                       startRtc();
                    }

                    @Override
                    public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if (denied!=null&&!denied.isEmpty()){
                            for (String s : denied) {
                                log("onDenied:"+s);
                            }
                        }
                        startRtc();
                        ToastUtils.showShort("您拒绝了相关权限，可能无法正常使用，请前往设置页打开相机和录音权限！");
                    }
                }).request();

        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 禁止自动锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log( "TeamAVChatActivity onDestroy");

        if (timer != null) {
            timer.cancel();
        }

        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        hangup(); // 页面销毁的时候要保证离开房间，rtc释放。
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, false);
        if(nertcCallingDelegate != null) {
            NERTCVideoCall.sharedInstance().removeDelegate(nertcCallingDelegate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        log( "TeamAVChatActivity onSaveInstanceState");
    }

    /**
     * ************************************ 初始化 ***************************************
     */

    // 设置窗口flag，亮屏并且解锁/覆盖在锁屏界面上
    private void dismissKeyguard() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    private void onInit() {
        mainHandler = new Handler(this.getMainLooper());
    }

    private void onIntent() {
        Intent intent = getIntent();
        receivedCall = intent.getBooleanExtra(CallParams.INVENT_CALL_RECEIVED, false);
        teamId = intent.getStringExtra(KEY_TEAM_ID);
        accounts = (ArrayList<String>) intent.getSerializableExtra(KEY_ACCOUNTS);
        accounts = accounts == null ? new ArrayList<String>(0) : accounts;
        teamName = intent.getStringExtra(KEY_TNAME);
        invitedChannelId = intent.getStringExtra(CallParams.INVENT_CHANNEL_ID);
        invitedRequestId = intent.getStringExtra(CallParams.INVENT_REQUEST_ID);
        invitedAccid = intent.getStringExtra(CallParams.INVENT_FROM_ACCOUNT_ID);
        groupId = intent.getStringExtra(CallParams.TEAM_CHAT_GROUP_ID);
        log( "onIntent teamId=" + teamId
                + ", receivedCall=" + receivedCall
                + ", accounts=" + accounts.size()
                + ", teamName = " + teamName
                +",invitedChannelId="+invitedChannelId
                +",invitedRequestId="+invitedRequestId
                +",invitedAccid="+invitedAccid
                +",groupId="+groupId
        );
    }

    private void findLayouts() {
        callLayout = findView(R.id.team_avchat_call_layout);
        surfaceLayout = findView(R.id.team_avchat_surface_layout);
        voiceMuteButton = findView(R.id.avchat_shield_user);

    }

    /**
     * ************************************ 主流程 ***************************************
     */

    private void showViews() {
        if (receivedCall) {
            showReceivedCallLayout();
        } else {
            showSurfaceLayout();
        }
    }

    /*
     * 接听界面
     */
    private void showReceivedCallLayout() {
        callLayout.setVisibility(View.VISIBLE);
        // 提示
        TextView textView = (TextView) callLayout.findViewById(R.id.received_call_tip);

        final String tipText = TextUtils.isEmpty(teamName) ? "你有一条视频通话" : teamName + "的视频通话";
        textView.setText(tipText);

        // 播放铃声
        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);

        //邀请参数
        final InviteParamBuilder inviteParam = new InviteParamBuilder(invitedChannelId,
                invitedAccid, invitedRequestId);

        // 拒绝
        callLayout.findViewById(R.id.refuse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NERTCVideoCall.sharedInstance().reject(inviteParam, new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cancelAutoRejectTask();
                        finish();
                    }

                    @Override
                    public void onFailed(int i) {
                        ToastUtils.showShort("reject failed errorCode = " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        ToastUtils.showShort("reject failed ");
                        Log.e("TeamG2Activity", "reject onException", throwable);
                    }
                });
                AVChatSoundPlayer.instance().stop();

            }
        });

        // 接听
        callLayout.findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NERTCVideoCall.sharedInstance().accept(inviteParam, ProfileManager.getInstance().getUserModel().imAccid, new JoinChannelCallBack() {
                    @Override
                    public void onJoinChannel(ChannelFullInfo channelFullInfo) {
                        if (channelFullInfo != null && !TextUtils.isEmpty(channelFullInfo.getChannelId())) {
                            for (MemberInfo member : channelFullInfo.getMembers()) {
                                if (TextUtils.equals(member.getAccountId(), ProfileManager.getInstance().getUserModel().imAccid)) {
                                    ProfileManager.getInstance().getUserModel().g2Uid = member.getUid();
                                    break;
                                }
                            }

                            log( "join room success, chatId=" + chatId);
                            chatId = channelFullInfo.getChannelId();
                            onJoinRoomSuccess();
                        } else {
                            int code = -1;
                            onJoinRoomFailed(ResponseCode.RES_ENONEXIST, null);
                            log("join room failed, code=" + code);
                        }
                    }

                    @Override
                    public void onJoinFail(String msg, int code) {
                        onJoinRoomFailed(ResponseCode.RES_ENONEXIST, null);
                        log("join room failed, code=" + code + ", msg=" + msg);
                    }
                });
                AVChatSoundPlayer.instance().stop();
                cancelAutoRejectTask();
                callLayout.setVisibility(View.GONE);
                showSurfaceLayout();
            }
        });

        startAutoRejectTask();
    }

    /*
     * 通话界面
     */
    private void showSurfaceLayout() {
        // 列表
        surfaceLayout.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) surfaceLayout.findViewById(R.id.recycler_view);
        initRecyclerView();

        // 通话计时
        timerText = (TextView) surfaceLayout.findViewById(R.id.timer_text);

        // 控制按钮
        ViewGroup settingLayout = (ViewGroup) surfaceLayout.findViewById(R.id.avchat_setting_layout);
        for (int i = 0; i < settingLayout.getChildCount(); i++) {
            View v = settingLayout.getChildAt(i);
            if (v instanceof RelativeLayout) {
                ViewGroup vp = (ViewGroup) v;
                if (vp.getChildCount() == 1) {
                    vp.getChildAt(0).setOnClickListener(settingBtnClickListener);
                }
            }
        }
    }

    /**
     * ************************************ 音视频事件 ***************************************
     */

    private void startRtc() {
        // rtc init
        log("start rtc done");

        // state observer
        if (nertcCallingDelegate != null) {
            NERTCVideoCall.sharedInstance().removeDelegate(nertcCallingDelegate);
        }
        nertcCallingDelegate = new NERTCCallingDelegate() {
            @Override
            public void onError(int errorCode, String errorMsg, boolean needFinish) {
                log("startRtc onError->"+errorCode+",errorMsg:"+errorMsg+",needFinish:"+needFinish);
                if (needFinish) {
                    ToastUtils.showLong(errorMsg + " errorCode:" + errorCode);
                    AVChatSoundPlayer.instance().stop();
                    finish();
                }
            }

            @Override
            public void onInvited(InvitedEvent invitedEvent) {

            }

            @Override
            public void onUserEnter(String accId) {
                onAVChatUserJoined(accId);
            }

            @Override
            public void onCallEnd(String userId) {
                hangup();
            }

            @Override
            public void onUserLeave(String accountId) {
                onAVChatUserLeave(accountId);
            }

            @Override
            public void onUserDisconnect(String userId) {
                onAVChatUserLeave(userId);
            }

            @Override
            public void onRejectByUserId(String userId) {
                onSignalingUserReject(userId);
            }

            @Override
            public void onUserBusy(String userId) {
                onSignalingUserReject(userId);
            }

            @Override
            public void onCancelByUserId(String userId) {
                if (!isDestroyed() && receivedCall) {
                    ToastUtils.showLong("对方取消");
                    hangup();
                    AVChatSoundPlayer.instance().stop();
                    finish();

                }
            }

            @Override
            public void onCameraAvailable(String userId, boolean isVideoAvailable) {

            }

            @Override
            public void onAudioAvailable(String userId, boolean isAudioAvailable) {

            }

            @Override
            public void onDisconnect(int reason) {
                ToastUtils.showLong("onDisconnect reason:" + reason);
                AVChatSoundPlayer.instance().stop();
                finish();
            }

            @Override
            public void onUserNetworkQuality(Entry<String, Integer>[] stats) {

            }

            @Override
            public void onCallTypeChange(ChannelType type) {

            }

            @Override
            public void timeOut() {

            }
        };
        NERTCVideoCall.sharedInstance().addDelegate(nertcCallingDelegate);
        log("observe rtc state done" );

        if (!receivedCall) {
            // join
            NERTCVideoCall.sharedInstance().groupCall(accounts, groupId, ProfileManager.getInstance().getUserModel().imAccid, ChannelType.VIDEO, new JoinChannelCallBack() {
                @Override
                public void onJoinChannel(ChannelFullInfo channelFullInfo) {
                    if (channelFullInfo != null && !TextUtils.isEmpty(channelFullInfo.getChannelId())) {
                        for (MemberInfo member : channelFullInfo.getMembers()) {
                            if (TextUtils.equals(member.getAccountId(), ProfileManager.getInstance().getUserModel().imAccid)) {
                                ProfileManager.getInstance().getUserModel().g2Uid = member.getUid();
                                break;
                            }
                        }

                        log("join room success, chatId=" + chatId );
                        chatId = channelFullInfo.getChannelId();
                        onJoinRoomSuccess();
                    } else {
                        int code = -1;
                        onJoinRoomFailed(code, null);
                        log("join room failed, code=" + code );
                    }
                }

                @Override
                public void onJoinFail(String msg, int code) {
                    onJoinRoomFailed(code, null);
                    log("join room failed, code=" + code + ", msg=" + msg );
                }
            });
        }
        log("start join room");
    }

    private void onJoinRoomSuccess() {
        startTimer();
        startLocalPreview();
        startTimerForCheckReceivedCall();
        log("team onJoinRoomSuccess...");
    }

    private void onJoinRoomFailed(int code, Throwable e) {
        if (code == ResponseCode.RES_ENONEXIST) {
            showToast(getString(R.string.t_avchat_join_fail_not_exist));
        } else {
            showToast("join room failed, code=" + code + ", e=" + (e == null ? "" : e.getMessage()));
        }
        hangup();
        finish();
    }

    public void onAVChatUserJoined(String accId) {
        int index = getItemIndex(accId);
        if (index==-1){
            //新加进来的人状态由占位改成视频流
            for (int i = 0; i < data.size(); i++) {
                TeamG2Item item = data.get(i);
                if (item!=null&&item.account==null){
                    index=i;
                    item.type=TYPE_DATA;
                    item.teamId=teamId;
                    item.account=accId;
                    item.state=TeamG2Item.STATE.STATE_PLAYING;
                    item.videoLive=true;
                    break;
                }
            }
        }
        if (index >= 0) {
            TeamG2Item item = data.get(index);
            item.state = TeamG2Item.STATE.STATE_PLAYING;
            item.videoLive = true;
            adapter.notifyDataItemChanged(index);
        }
        updateAudioMuteButtonState();

        log("on user joined, account=" + accId);
    }

    public void onAVChatUserLeave(String account) {
        int index = getItemIndex(account);
        if (index >= 0) {
            TeamG2Item item = data.get(index);
            item.state = TeamG2Item.STATE.STATE_HANGUP;
            item.volume = 0;
            adapter.notifyItemChanged(index);
        }
        updateAudioMuteButtonState();

        log("on user leave, account=" + account);
    }

    public void onSignalingUserReject(String accountId){
        int index = getItemIndex(accountId);
        if (index >= 0) {
            TeamG2Item item = data.get(index);
            item.state = TeamG2Item.STATE.STATE_REJECTED;
            item.volume = 0;
            adapter.notifyItemChanged(index);
        }
        updateAudioMuteButtonState();

        log("on user reject, account=" + accountId);
    }

    private void startLocalPreview() {
        if (data.size() > 1 && data.get(0).account.equals(ProfileManager.getInstance().getUserModel().imAccid)) {
            data.get(0).state = TeamG2Item.STATE.STATE_PLAYING;
            data.get(0).videoLive = true;
            adapter.notifyItemChanged(0);
        }
    }

    /**
     * ************************************ 音视频状态 ***************************************
     */

    private void updateSelfItemVideoState(boolean live) {
        int index = getItemIndex(ProfileManager.getInstance().getUserModel().imAccid);
        if (index >= 0) {
            TeamG2Item item = data.get(index);
            item.videoLive = live;
            adapter.notifyItemChanged(index);
        }
    }

    private void hangup() {
        if (destroyRTC) {
            return;
        }

        try {
            NERTCVideoCall.sharedInstance().leave(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailed(int i) {
                    Log.e(TAG, "leave failed code = " + i);
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.e(TAG, "leave failed onException", throwable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        destroyRTC = true;
        log("destroy rtc & leave room");
    }

    /**
     * ************************************ 定时任务 ***************************************
     */

    private void startTimer() {
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
        timerText.setText("00:00");
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            seconds++;
            int m = seconds / 60;
            int s = seconds % 60;
            final String time = String.format(Locale.CHINA, "%02d:%02d", m, s);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timerText.setText(time);
                }
            });
        }
    };

    private void startTimerForCheckReceivedCall() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //与IOS保持一致，去掉单人的超时逻辑
//                int index = 0;
//                for (TeamG2Item item : data) {
//                    if (item.type == TYPE_DATA && item.state == TeamG2Item.STATE.STATE_WAITING) {
//                        item.state = TeamG2Item.STATE.STATE_END;
//                        adapter.notifyItemChanged(index,item);
//                    }
//                    index++;
//                }
                checkAllHangUp();
            }
        }, CHECK_RECEIVED_CALL_TIMEOUT);
    }

    private void startAutoRejectTask() {
        if (autoRejectTask == null) {
            autoRejectTask = new Runnable() {
                @Override
                public void run() {
                    AVChatSoundPlayer.instance().stop();
                    finish();
                }
            };
        }

        mainHandler.postDelayed(autoRejectTask, AUTO_REJECT_CALL_TIMEOUT);
    }

    private void cancelAutoRejectTask() {
        if (autoRejectTask != null) {
            mainHandler.removeCallbacks(autoRejectTask);
        }
    }

    /*
     * 除了所有人都没接通，其他情况不做自动挂断
     */
    private void checkAllHangUp() {
        for (TeamG2Item item : data) {
            if (item.account != null &&
                    !TextUtils.equals(item.account, ProfileManager.getInstance().getUserModel().imAccid)&&
                    item.state != TeamG2Item.STATE.STATE_END) {
                return;
            }
        }
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                log("所有人都没接通,自动挂断");
                hangup();
                finish();
            }
        }, 200);
    }

    /**
     * ************************************ 点击事件 ***************************************
     */

    private View.OnClickListener settingBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            log(v+"");
            if (i == R.id.avchat_switch_camera) {// 切换前后摄像头
                NERTCVideoCall.sharedInstance().switchCamera();
            } else if (i == R.id.avchat_enable_video) {// 视频
                videoMute = !videoMute;
                NERTCVideoCall.sharedInstance().enableLocalVideo(!videoMute);
                v.setBackgroundResource(videoMute ? R.drawable.t_avchat_camera_mute_selector : R.drawable.t_avchat_camera_selector);
                updateSelfItemVideoState(!videoMute);
            } else if (i == R.id.avchat_enable_audio) {// 麦克风开关
                NERTCVideoCall.sharedInstance().muteLocalAudio(microphoneMute = !microphoneMute);
                v.setBackgroundResource(microphoneMute ? R.drawable.t_avchat_microphone_mute_selector : R.drawable.t_avchat_microphone_selector);
            } else if (i == R.id.avchat_volume) {// 听筒扬声器切换
                showIncompleteFeatureToast("听筒扬声器切换");
            } else if (i == R.id.avchat_shield_user) {// 屏蔽用户音频
                disableUserAudio();
            } else if (i == R.id.hangup) {// 挂断
                hangup();
                finish();

            }
        }
    };

    private void updateAudioMuteButtonState() {
        boolean enable = false;
        for (TeamG2Item item : data) {
            if (item.state == TeamG2Item.STATE.STATE_PLAYING &&
                    !TextUtils.equals(ProfileManager.getInstance().getUserModel().imAccid, item.account)) {
                enable = true;
                break;
            }
        }
        voiceMuteButton.setEnabled(enable);
        voiceMuteButton.invalidate();
    }

    private void disableUserAudio() {
        // TODO G2
        showIncompleteFeatureToast("屏蔽用户音频");
//        List<Pair<String, Boolean>> voiceMutes = new ArrayList<>();
//        for (TeamG2Item item : data) {
//            if (item.state == TeamG2Item.STATE.STATE_PLAYING &&
//                    !SDKCache.getAccount().equals(item.account)) {
//                voiceMutes.add(new Pair<>(item.account, AVChatManager.getInstance().isRemoteAudioMuted(item.account)));
//            }
//        }
//        TeamAVChatVoiceMuteDialog dialog = new TeamAVChatVoiceMuteDialog(this, teamId, voiceMutes);
//        dialog.setTeamVoiceMuteListener(new TeamAVChatVoiceMuteDialog.TeamVoiceMuteListener() {
//            @Override
//            public void onVoiceMuteChange(List<Pair<String, Boolean>> voiceMuteAccounts) {
//                if (voiceMuteAccounts != null) {
//                    for (Pair<String, Boolean> voiceMuteAccount : voiceMuteAccounts) {
//                        AVChatManager.getInstance().muteRemoteAudio(voiceMuteAccount.first, voiceMuteAccount.second);
//                    }
//                }
//            }
//        });
//        dialog.show();
    }

    private void showIncompleteFeatureToast(String feature) {
        Toast.makeText(TeamG2Activity.this, String.format("该功能暂未实现：%s", feature), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // 屏蔽BACK
    }

    @Override
    public void finish() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        } else {
            super.finish();
        }
    }

    /**
     * ************************************ 数据源 ***************************************
     */

    private void initRecyclerView() {
        // 确认数据源,发起邀请者放首位
        data = new ArrayList<>(accounts.size() + 1);
        //房间发起者在第一位
        if (receivedCall){
            TeamG2Item invitedAccItem=null;
            for (String account : accounts) {
                 if (!TextUtils.isEmpty(invitedAccid)&&invitedAccid.equals(account)){
                     invitedAccItem=new TeamG2Item(TYPE_DATA, teamId, account);
                 }else {
                     TeamG2Item item = new TeamG2Item(TYPE_DATA, teamId, account);
                     item.isSelf=TextUtils.equals(account, ProfileManager.getInstance().getUserModel().imAccid);
                     if (item.isSelf){
                         item.state = TeamG2Item.STATE.STATE_PLAYING;
                         item.videoLive=true;
                     }
                     data.add(item);
                 }
            }
            if (invitedAccItem!=null){
                data.add(0,invitedAccItem);
            }
        }else {
            // 自己是邀请者
            for (String account : accounts) {
                if (TextUtils.equals(account, ProfileManager.getInstance().getUserModel().imAccid)) {
                    continue;
                }

                data.add(new TeamG2Item(TYPE_DATA, teamId, account));
            }
            TeamG2Item selfItem = new TeamG2Item(TYPE_DATA, teamId, ProfileManager.getInstance().getUserModel().imAccid);
            selfItem.state = TeamG2Item.STATE.STATE_PLAYING; // 自己直接采集摄像头画面
            selfItem.isSelf = true;
            data.add(0, selfItem);
        }



        // 补充占位符
        int holderLength = MAX_SUPPORT_ROOM_USERS_COUNT - data.size();
        for (int i = 0; i < holderLength; i++) {
            data.add(new TeamG2Item(teamId));
        }

        // RecyclerView
        adapter = new TeamG2Adapter(recyclerView, data);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemMuteChangeListener((uid, isMute) -> {
            NERTCVideoCall.sharedInstance().setAudioMute(isMute, uid);
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new SpacingDecoration(ScreenUtil.dip2px(1), ScreenUtil.dip2px(1), true));
    }

    private int getItemIndex(final String account) {
        int index = 0;
        boolean find = false;
        for (TeamG2Item i : data) {
            if (TextUtils.equals(i.account,account)) {
                find = true;
                break;
            }
            index++;
        }

        return find ? index : -1;
    }

    /**
     * ************************************ 权限检查 ***************************************
     */
    // TODO G2

    /**
     * ************************************ helper ***************************************
     */

    private void showToast(String content) {
        log(content);
        Toast.makeText(TeamG2Activity.this, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 在线状态观察者
     */
    private Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            log(String.valueOf(code.getValue()));
            if (code.wontAutoLogin()) {
                log("code.wontAutoLogin()");
                AVChatSoundPlayer.instance().stop();
                hangup();
                finish();
            }
        }
    };



    private void log(String msg){
        if (TextUtils.isEmpty(msg)){
            return;
        }
        ALog.i(TAG, msg);
    }

}
