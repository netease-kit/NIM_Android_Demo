package com.netease.nim.avchatkit.controll;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.R;
import com.netease.nim.avchatkit.common.log.LogUtil;
import com.netease.nim.avchatkit.common.widgets.MultiSelectDialog;
import com.netease.nim.avchatkit.config.AVChatConfigs;
import com.netease.nim.avchatkit.config.AVPrivatizationConfig;
import com.netease.nim.avchatkit.constant.AVChatExitCode;
import com.netease.nim.avchatkit.constant.CallStateEnum;
import com.netease.nim.avchatkit.module.AVChatControllerCallback;
import com.netease.nim.avchatkit.module.AVSwitchListener;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音视频控制器：用于实现音视频拨打接听，音视频切换的具体功能实现
 * Created by winnie on 2017/12/10.
 */

public class AVChatController {
    private static final String TAG = AVChatController.class.getSimpleName();

    protected Context context;
    private long timeBase = 0;
    protected AVChatData avChatData;
    private AVChatCameraCapturer mVideoCapturer;
    private AVChatConfigs avChatConfigs;


    public AtomicBoolean isCallEstablish = new AtomicBoolean(false);
    private boolean destroyRTC = false;
    private boolean isRecording = false;

    private boolean needRestoreLocalVideo = false;
    private boolean needRestoreLocalAudio = false;

    List<Pair<String, Boolean>> recordList = new LinkedList<Pair<String, Boolean>>();

    public interface RecordCallback {
        void onRecordUpdate(boolean isRecording);
    }


    /**
     * *************************** 初始化 ************************
     */

    public AVChatController(Context context, AVChatData avChatData) {
        this.context = context;
        this.avChatData = avChatData;
        this.avChatConfigs = new AVChatConfigs(context);
    }

    //恢复视频和语音发送
    public void resumeVideo() {
        if (needRestoreLocalVideo) {
            AVChatManager.getInstance().muteLocalVideo(false);
            needRestoreLocalVideo = false;
        }

        if (needRestoreLocalAudio) {
            AVChatManager.getInstance().muteLocalAudio(false);
            needRestoreLocalAudio = false;
        }

    }

    //关闭视频和语音发送.
    public void pauseVideo() {

        if (!AVChatManager.getInstance().isLocalVideoMuted()) {
            AVChatManager.getInstance().muteLocalVideo(true);
            needRestoreLocalVideo = true;
        }

        if (!AVChatManager.getInstance().isLocalAudioMuted()) {
            AVChatManager.getInstance().muteLocalAudio(true);
            needRestoreLocalAudio = true;
        }
    }

    /**
     * *************************** 拨打和接听 ****************************
     */

    public void doCalling(String account, final AVChatType avChatType, final AVChatControllerCallback<AVChatData> callback) {

        AVChatManager.getInstance().enableRtc(AVPrivatizationConfig.getServerAddresses(context));
        AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);

        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true);
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }

        if (avChatType == AVChatType.VIDEO) {
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().startVideoPreview();
        }

        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = "extra_data";
        // 默认forceKeepCalling为true，开发者如果不需要离线持续呼叫功能可以将forceKeepCalling设为false
        // notifyOption.forceKeepCalling = false;
        AVChatManager.getInstance().call2(account, avChatType, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;
                callback.onSuccess(data);
            }

            @Override
            public void onFailed(int code) {
                LogUtil.d(TAG, "avChat call failed code->" + code);

                if (code == ResponseCode.RES_FORBIDDEN) {
                    Toast.makeText(context, R.string.avchat_no_permission, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.avchat_call_failed, Toast.LENGTH_SHORT).show();
                }
                closeRtc(avChatType == AVChatType.VIDEO ? CallStateEnum.VIDEO : CallStateEnum.AUDIO);
                callback.onFailed(code, "");
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "avChat call onException->" + exception);
                closeRtc(avChatType == AVChatType.VIDEO ? CallStateEnum.VIDEO : CallStateEnum.AUDIO);
                callback.onFailed(-1, exception.toString());
            }
        });
    }

    public void receive(final AVChatType avChatType, final AVChatControllerCallback<Void> callback) {

        AVChatManager.getInstance().enableRtc(AVPrivatizationConfig.getServerAddresses(context));
        AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true);
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        if (avChatType == AVChatType.VIDEO) {
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().startVideoPreview();
        }

        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.i(TAG, "accept success");

                isCallEstablish.set(true);

                callback.onSuccess(aVoid);
            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(context, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                LogUtil.e(TAG, "accept onFailed->" + code);
                handleAcceptFailed(avChatType == AVChatType.VIDEO ?
                        CallStateEnum.VIDEO_CONNECTING : CallStateEnum.AUDIO);
                callback.onFailed(code, "");
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "accept exception->" + exception);
                handleAcceptFailed(avChatType == AVChatType.VIDEO ?
                        CallStateEnum.VIDEO_CONNECTING : CallStateEnum.AUDIO);
                callback.onFailed(-1, exception.toString());
            }
        });
        AVChatSoundPlayer.instance().stop();
    }

    public void toggleMute() {
        if (!AVChatManager.getInstance().isLocalAudioMuted()) { // isMute是否处于静音状态
            // 关闭音频
            AVChatManager.getInstance().muteLocalAudio(true);
        } else {
            // 打开音频
            AVChatManager.getInstance().muteLocalAudio(false);
        }
    }

    /**
     * ********************* 音视频切换 ***********************
     */

    // 发送视频切换为音频命令
    public void switchVideoToAudio(final AVSwitchListener avSwitchListener) {
        AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.SWITCH_VIDEO_TO_AUDIO, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "videoSwitchAudio onSuccess");
                //关闭视频
                AVChatManager.getInstance().stopVideoPreview();
                AVChatManager.getInstance().disableVideo();

                // 界面布局切换。
                avSwitchListener.onVideoToAudio();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.d(TAG, "videoSwitchAudio onFailed");
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "videoSwitchAudio onException");
            }
        });
    }

    // 发送音频切换为视频命令
    public void switchAudioToVideo(final AVSwitchListener avSwitchListener) {
        AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "requestSwitchToVideo onSuccess");
                avSwitchListener.onAudioToVideo();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.d(TAG, "requestSwitchToVideo onFailed" + code);
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "requestSwitchToVideo onException" + exception);
            }
        });
    }

    // 发送同意从音频切换为视频的命令
    public void receiveAudioToVideo(final AVSwitchListener avSwitchListener) {
        AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_AGREE, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "receiveAudioToVideo onSuccess");

                avSwitchListener.onReceiveAudioToVideoAgree();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.d(TAG, "receiveAudioToVideo onFailed");
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "receiveAudioToVideo onException");
            }
        });
    }


    /**
     * ********************* 其他设置 **************************
     */

    // 录制暂停和开始
    public void toggleRecord(int type, final String receiverId, final RecordCallback callback) {
        if (isRecording) {
            //停止录制
            isRecording = false;
            callback.onRecordUpdate(isRecording);
            if (recordList.size() == 3) {
                if (recordList.get(0).second) {
                    AVChatManager.getInstance().stopAudioRecording();
                }
                if (recordList.get(1).second) {
                    AVChatManager.getInstance().stopAVRecording(AVChatKit.getAccount());
                }
                if (recordList.get(2).second) {
                    AVChatManager.getInstance().stopAVRecording(receiverId);
                }
            }

        } else {
            //探测对话框
            final MultiSelectDialog selectDialog = new MultiSelectDialog(context);
            selectDialog.setTitle("选择录制内容");
            selectDialog.setMessage("录制的内容会被单独保存");
            selectDialog.setMessageTextColor(context.getResources().getColor(R.color.color_grey_999999));
            selectDialog.addItem("语音对话", false);
            if (type == CallStateEnum.AUDIO.getValue()) {
                selectDialog.addItem("我的音频", false);
            } else {
                selectDialog.addItem("我的音视频", false);
            }
            if (type == CallStateEnum.AUDIO.getValue()) {
                selectDialog.addItem("对方音频", false);
            } else {
                selectDialog.addItem("对方音视频", false);
            }
            selectDialog.addPositiveButton("开始录制", MultiSelectDialog.NO_TEXT_COLOR, MultiSelectDialog.NO_TEXT_SIZE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isRecording = true;
                            callback.onRecordUpdate(isRecording);
                            List<Pair<String, Boolean>> selectDialogList = selectDialog.getItemTextList();
                            if (selectDialogList.size() == 3) {
                                if (selectDialogList.get(0).second) {
                                    AVChatManager.getInstance().startAudioRecording();
                                }
                                if (selectDialogList.get(1).second) {
                                    AVChatManager.getInstance().startAVRecording(AVChatKit.getAccount());
                                }
                                if (selectDialogList.get(2).second) {
                                    AVChatManager.getInstance().startAVRecording(receiverId);
                                }
                            }
                            recordList.clear();
                            recordList.addAll(selectDialogList);
                            selectDialog.dismiss();
                        }
                    });
            selectDialog.addNegativeButton(context.getString(R.string.cancel), MultiSelectDialog.NO_TEXT_COLOR,
                    MultiSelectDialog.NO_TEXT_SIZE, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            selectDialog.dismiss();
                        }
                    });
            selectDialog.show();
        }
    }

    // 设置扬声器是否开启
    public void toggleSpeaker() {
        AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
    }

    // 切换摄像头（主要用于前置和后置摄像头切换）
    public void switchCamera() {
        mVideoCapturer.switchCamera();
    }

    /**
     * ********************** 挂断相关操作 **********************
     */

    public void hangUp(int type) {
        if (destroyRTC) {
            return;
        }
        if ((type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE
                || type == AVChatExitCode.CANCEL || type == AVChatExitCode.REJECT) && avChatData != null) {
            AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }

                @Override
                public void onFailed(int code) {
                    LogUtil.d(TAG, "hangup onFailed->" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    LogUtil.d(TAG, "hangup onException->" + exception);
                }
            });
        }
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        AVChatSoundPlayer.instance().stop();
        showQuitToast(type);
    }

    // 收到挂断通知，自己的处理
    public void onHangUp(int exitCode) {
        if (destroyRTC) {
            return;
        }
        AVChatSoundPlayer.instance().stop();
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        showQuitToast(exitCode);
        ((Activity) context).finish();
    }

    // 显示退出toast
    public void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(context, R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.REJECT:
                Toast.makeText(context, R.string.avchat_call_reject, Toast.LENGTH_SHORT).show();
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

    private void closeRtc(CallStateEnum callingState) {
        if (destroyRTC) {
            return;
        }
        if (callingState == CallStateEnum.OUTGOING_VIDEO_CALLING || callingState == CallStateEnum.VIDEO) {
            AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().disableVideo();
        }
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
        AVChatSoundPlayer.instance().stop();
    }

    private void handleAcceptFailed(CallStateEnum callingState) {
        if (callingState == CallStateEnum.VIDEO_CONNECTING) {
            AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().disableVideo();
        }
        hangUp(AVChatExitCode.CANCEL);
    }

    /**
     * ************************* 其他数据 ***********************
     */

    public long getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(long timeBase) {
        this.timeBase = timeBase;
    }

    public AVChatData getAvChatData() {
        return avChatData;
    }

    public void setAvChatData(AVChatData avChatData) {
        this.avChatData = avChatData;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

}
