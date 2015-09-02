package com.netease.nim.demo.avchat;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.avchat.activity.AVChatExitCode;
import com.netease.nim.demo.avchat.constant.CallStateEnum;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音视频管理器, 音视频相关功能管理
 * Created by hzxuwen on 2015/4/23.
 */
public class AVChatUIManager implements AVChatUIListener {
    // constant
    private static final String TAG = "AVChatUIManager";

    // data
    private Context context;
    private AVChatData avChatData;
    private final AVChatListener aVChatListener;
    private String receiverId;
    private AVChatAudioManager aVChatAudioManager;
    private AVChatVideoManager aVChatVideoManager;
    private AVChatSurfaceManager aVChatSurfaceManager;
    private VideoChatParam videoParam; // 视频采集参数
    private String videoAccount; // 发送视频请求，onUserJoin回调的user account

    private int mPendingExitCode = -1; // 挂起的closesession操作
    private CallStateEnum callingState = CallStateEnum.INVALID;

    private long timeBase = 0;

    // view
    private View root;

    // state
    public boolean canSwitchCamera = false;
    private boolean isClosedCamera = false;
    public AtomicBoolean isCallEstablish = new AtomicBoolean(false);

    public interface AVChatListener {
        void uiExit();
    }

    public AVChatUIManager(Context context, View root, AVChatListener listener) {
        this.context = context;
        this.root = root;
        this.aVChatListener = listener;
    }

    /**
     * ******************************初始化******************************
     */

    /**
     * 初始化
     *
     * @return boolean
     */
    public boolean initiation() {
        AVChatProfile.getInstance().setAVChatting(true);
        aVChatAudioManager = new AVChatAudioManager(root.findViewById(R.id.avchat_audio_layout), this, this);
        aVChatVideoManager = new AVChatVideoManager(context, root.findViewById(R.id.avchat_video_layout), this, this);
        aVChatSurfaceManager = new AVChatSurfaceManager(context, this, root.findViewById(R.id.avchat_surface_layout));

        return true;
    }

    /**
     * ******************************拨打和接听***************************
     */

    /**
     * 来电
     *
     */
    public void inComingCalling(AVChatData avChatData) {
        this.avChatData = avChatData;
        receiverId = avChatData.getAccount();
        if (avChatData.getChatType() == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.INCOMING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.INCOMING_VIDEO_CALLING);
        }
    }

    /**
     * 拨打音视频
     *
     */
    public void outGoingCalling(String account, AVChatType callTypeEnum) {
        DialogMaker.showProgressDialog(context, null);
        this.receiverId = account;
        VideoChatParam videoParam = null;
        if (callTypeEnum == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
            if (videoParam == null) {
                videoParam = new VideoChatParam(aVChatSurfaceManager.mCapturePreview,
                        ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
            }
        }
        /**
         * 发起通话
         * account 对方账号
         * callTypeEnum 通话类型：语音、视频
         * videoParam 发起视频通话时传入，发起音频通话传null
         * AVChatCallback 回调函数，返回AVChatInfo
         */
        AVChatManager.getInstance().call(account, callTypeEnum, videoParam, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                Log.d(TAG, "success");
                avChatData = data;
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "failed code->" + code);
                DialogMaker.dismissProgressDialog();
                Toast.makeText(context, R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                closeSessions(-1);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "start call onException->" + exception);
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 状态改变
     *
     * @param stateEnum
     */
    public void onCallStateChange(CallStateEnum stateEnum) {
        callingState = stateEnum;
        aVChatSurfaceManager.onCallStateChange(stateEnum);
        aVChatAudioManager.onCallStateChange(stateEnum);
        aVChatVideoManager.onCallStateChange(stateEnum);
    }

    /**
     * 挂断
     *
     * @param type 音视频类型
     */
    private void hangUp(final int type) {
        if (type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE || type == AVChatExitCode.CANCEL) {
            AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "hangup onSuccess");
                }

                @Override
                public void onFailed(int code) {
                    Log.d(TAG, "hangup onFailed->" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    Log.d(TAG, "hangup onException->" + exception);
                }
            });
        }

        closeSessions(type);
    }

    /**
     * 关闭本地音视频各项功能
     *
     * @param exitCode 音视频类型
     */
    public void closeSessions(int exitCode) {
        //not  user  hang up active  and warning tone is playing,so wait its end
        exitCode = mPendingExitCode == -1 ? exitCode : mPendingExitCode;
        mPendingExitCode = -1;
        Log.i(TAG, "close session -> " + AVChatExitCode.getExitString(exitCode));
        if (aVChatAudioManager != null)
            aVChatAudioManager.closeSession(exitCode);
        if(aVChatVideoManager != null)
            aVChatVideoManager.closeSession(exitCode);
        showQuitToast(exitCode);
        isCallEstablish.set(false);
        canSwitchCamera = false;
        isClosedCamera = false;
        aVChatListener.uiExit();
    }

    /**
     * 给出结束的提醒
     *
     * @param code
     */
    public void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(context, R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_HANGUP:
            case AVChatExitCode.HANGUP:
                if (isCallEstablish.get()) {
                    Toast.makeText(context, R.string.avchat_call_finish, Toast.LENGTH_SHORT).show();
                }
                break;
            case AVChatExitCode.PEER_BUSY:
                Toast.makeText(context, R.string.avchat_peer_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_REJECT:
//                if (callConfig.getChatType() == NetCallTypeEnum.VIDEO)
//                    Toast.makeText(context, R.string.avchat_peer_reject, Toast.LENGTH_SHORT).show();
//                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_PEER_LOWER:
                Toast.makeText(context, R.string.avchat_peer_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_SELF_LOWER:
                Toast.makeText(context, R.string.avchat_local_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.INVALIDE_CHANNELID:
                Toast.makeText(context, R.string.avchat_invalid_channel_id, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.LOCAL_CALL_BUSY:
                Toast.makeText(context, R.string.avchat_local_call_busy, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * ******************************* 接听与拒绝操作 *********************************
     */

    /**
     * 拒绝来电
     */
    private void rejectInComingCall() {
        /**
         * 接收方拒绝通话
         * AVChatCallback 回调函数
         */
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "reject sucess");
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "reject sucess->" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "reject sucess");
            }
        });
        closeSessions(AVChatExitCode.REJECT);
    }

    /**
     * 拒绝音视频切换
     */
    private void rejectAudioToVideo() {
        onCallStateChange(CallStateEnum.AUDIO);
        AVChatManager.getInstance().ackSwitchToVideo(false, videoParam, null); // 音频切换到视频请求的回应. true为同意，false为拒绝
    }

    /**
     * 接听来电
     */
    private void receiveInComingCall() {
        //接听，告知服务器，以便通知其他端
        VideoChatParam videoParam = null;

        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {
            onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
        } else {
            onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
            videoParam = new VideoChatParam(aVChatSurfaceManager.mCapturePreview,
                    ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        }

        /**
         * 接收方接听电话
         * videoParam 接听视频通话时传入，接听音频通话传null
         * AVChatCallback 回调函数。成功则连接建立，不成功则关闭activity。
         */
        AVChatManager.getInstance().accept(videoParam, new AVChatCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                Log.d(TAG, "accept sucess");

                if (success) {
                    isCallEstablish.set(true);
                    canSwitchCamera = true;
                }
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "accept onFailed->" + code);
                closeSessions(AVChatExitCode.CANCEL);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "accept exception->" + exception);
            }
        });
    }

    /*************************** AVChatUIListener ******************************/

    /**
     * 点击挂断或取消
     */
    @Override
    public void onHangUp() {
        if (isCallEstablish.get()) {
            hangUp(AVChatExitCode.HANGUP);
        } else {
            hangUp(AVChatExitCode.CANCEL);
        }
    }

    /**
     * 拒绝操作，根据当前状态来选择合适的操作
     */
    @Override
    public void onRefuse() {
        switch (callingState) {
            case INCOMING_AUDIO_CALLING:
            case AUDIO_CONNECTING:
                rejectInComingCall();
                break;
            case INCOMING_AUDIO_TO_VIDEO:
                rejectAudioToVideo();
                break;
            case INCOMING_VIDEO_CALLING:
            case VIDEO_CONNECTING: // 连接中点击拒绝
                rejectInComingCall();
                break;
            default:
                break;
        }
    }

    /**
     * 开启操作，根据当前状态来选择合适的操作
     */
    @Override
    public void onReceive() {
        switch (callingState) {
            case INCOMING_AUDIO_CALLING:
                receiveInComingCall();
                onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
                break;
            case AUDIO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_VIDEO_CALLING:
                receiveInComingCall();
                onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
                break;
            case VIDEO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_AUDIO_TO_VIDEO:
                receiveAudioToVideo();
            default:
                break;
        }
    }

    @Override
    public void toggleMute() {
        if (!isCallEstablish.get()) { // 连接未建立，在这里记录静音状态
            return;
        } else { // 连接已经建立
            if(!AVChatManager.getInstance().isMute()) { // isMute是否处于静音状态
                // 关闭音频
                AVChatManager.getInstance().setMute(true);
            } else {
                // 打开音频
                AVChatManager.getInstance().setMute(false);
            }
        }
    }

    @Override
    public void toggleSpeaker() {
        AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled()); // 设置扬声器是否开启
    }

    @Override
    public void videoSwitchAudio() {
        /**
         * 请求视频切换到音频
         */
        AVChatManager.getInstance().requestSwitchToAudio(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 界面布局切换。
                onCallStateChange(CallStateEnum.AUDIO);
                // 判断是否静音，扬声器是否开启，对界面相应控件进行显隐处理。
                aVChatAudioManager.onVideoToAudio(AVChatManager.getInstance().isMute(), AVChatManager.getInstance().speakerEnabled());
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    @Override
    public void audioSwitchVideo() {
        onCallStateChange(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO);
        videoParam = new VideoChatParam(aVChatSurfaceManager.mCapturePreview,
                ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        /**
         * 请求音频切换到视频
         */
        AVChatManager.getInstance().requestSwitchToVideo(videoParam, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "requestSwitchToVideo onSuccess");
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "requestSwitchToVideo onFailed" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "requestSwitchToVideo onException" + exception);
            }
        });
    }

    @Override
    public void switchCamera() {
        AVChatManager.getInstance().toggleCamera(); // 切换摄像头（主要用于前置和后置摄像头切换）
    }

    @Override
    public void closeCamera() {
        if (!isClosedCamera) {
            // 关闭摄像头
            AVChatManager.getInstance().toggleLocalVideo(false, null);
            isClosedCamera = true;
            aVChatSurfaceManager.localVideoOff();
        } else {
            // 打开摄像头
            AVChatManager.getInstance().toggleLocalVideo(true, null);
            isClosedCamera = false;
            aVChatSurfaceManager.localVideoOn();
        }

    }

    @Override
    public boolean inAudioMode() {
        return CallStateEnum.isAudioMode(callingState);
    }

    @Override
    public boolean inVideoMode() {
        return CallStateEnum.isVideoMode(callingState);
    }

    /**
     * 音频切换为视频的请求
     */
    public void incomingAudioToVideo() {
        onCallStateChange(CallStateEnum.INCOMING_AUDIO_TO_VIDEO);
        if (videoParam == null) {
            videoParam = new VideoChatParam(aVChatSurfaceManager.mCapturePreview,
                    ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        }
    }

    /**
     * 同意音频切换为视频
     */
    private void receiveAudioToVideo() {
        AVChatManager.getInstance().ackSwitchToVideo(true, videoParam, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onAudioToVideo();
                initSurfaceView(videoAccount);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        }); // 音频切换到视频请求的回应. true为同意，false为拒绝

    }

    /**
     * 初始化大小图像
     * @param largeAccount 对方的账号
     */
    public void initSurfaceView(String largeAccount) {
        aVChatSurfaceManager.initLargeSurfaceView(largeAccount);
        aVChatSurfaceManager.initSmallSurfaceView(DemoCache.getAccount());
    }

    /**
     * 音频切换为视频
     */
    private void onAudioToVideo() {
        onCallStateChange(CallStateEnum.VIDEO);
        aVChatVideoManager.onAudioToVideo(AVChatManager.getInstance().isMute()); // isMute是否处于静音状态
        if(!AVChatManager.getInstance().isVideoSend()) { // 是否在发送视频 即摄像头是否开启
            AVChatManager.getInstance().toggleLocalVideo(true, null);
            aVChatSurfaceManager.localVideoOn();
        }
    }

    public boolean canSwitchCamera() {
        return canSwitchCamera;
    }

    public AVChatSurfaceManager getAVChatSurfaceManager() {
        return aVChatSurfaceManager;
    }

    public AVChatVideoManager getAVChatVideoManager() {
        return aVChatVideoManager;
    }

    public AVChatAudioManager getAVChatAudioManager() {
        return aVChatAudioManager;
    }

    public CallStateEnum getCallingState() {
        return callingState;
    }

    public String getVideoAccount() {
        return videoAccount;
    }

    public void setVideoAccount(String videoAccount) {
        this.videoAccount = videoAccount;
    }

    public String getUid() {
        if (receiverId != null)
            return receiverId;
        return null;
    }

    public long getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(long timeBase) {
        this.timeBase = timeBase;
    }
}
