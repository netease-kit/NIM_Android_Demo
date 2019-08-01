package com.netease.nim.avchatkit.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.netease.nim.avchatkit.R;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.model.AVChatImageFormat;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;

/**
 * Created by winnie on 2017/12/10.
 */

public class AVChatConfigs {

    private AVChatParameters avChatParameters;
    private Context context;

    //Config from Preference
    private int videoCropRatio;
    private boolean videoAutoRotate;
    private int videoQuality;
    private boolean serverRecordAudio;
    private boolean serverRecordVideo;
    private boolean autoCallProximity;
    private int videoHwEncoderMode;
    private int videoHwDecoderMode;
    private boolean videoFpsReported;
    private int audioEffectAecMode;
    private int audioEffectNsMode;
    private int videoMaxBitrate;
    private int deviceDefaultRotation;
    private int deviceRotationOffset;
    private boolean audioHighQuality;

    public AVChatConfigs(Context context) {
        this.context = context;
        this.avChatParameters = new AVChatParameters();
        configFromPreference(PreferenceManager.getDefaultSharedPreferences(context));
        updateAVChatOptionalConfig();
    }

    private void configFromPreference(SharedPreferences preferences) {
        videoCropRatio = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_crop_ratio_key), "0"));
        videoAutoRotate = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_rotation_key), true);
        videoQuality = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_quality_key), 0 + ""));
        serverRecordAudio = preferences.getBoolean(context.getString(R.string.nrtc_setting_other_server_record_audio_key), false);
        serverRecordVideo = preferences.getBoolean(context.getString(R.string.nrtc_setting_other_server_record_video_key), false);
        autoCallProximity = preferences.getBoolean(context.getString(R.string.nrtc_setting_voe_call_proximity_key), true);
        videoHwEncoderMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_hw_encoder_key), 0 + ""));
        videoHwDecoderMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_vie_hw_decoder_key), 0 + ""));
        videoFpsReported = preferences.getBoolean(context.getString(R.string.nrtc_setting_vie_fps_reported_key), true);
        audioEffectAecMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_voe_audio_aec_key), 2 + ""));
        audioEffectNsMode = Integer.parseInt(preferences.getString(context.getString(R.string.nrtc_setting_voe_audio_ns_key), 2 + ""));
        String value1 = preferences.getString(context.getString(R.string.nrtc_setting_vie_max_bitrate_key), 0 + "");
        videoMaxBitrate = Integer.parseInt(TextUtils.isDigitsOnly(value1) && !TextUtils.isEmpty(value1) ? value1 : 0 + "");
        String value2 = preferences.getString(context.getString(R.string.nrtc_setting_other_device_default_rotation_key), 0 + "");
        deviceDefaultRotation = Integer.parseInt(TextUtils.isDigitsOnly(value2) && !TextUtils.isEmpty(value2) ? value2 : 0 + "");
        String value3 = preferences.getString(context.getString(R.string.nrtc_setting_other_device_rotation_fixed_offset_key), 0 + "");
        deviceRotationOffset = Integer.parseInt(TextUtils.isDigitsOnly(value3) && !TextUtils.isEmpty(value3) ? value3 : 0 + "");
        audioHighQuality = preferences.getBoolean(context.getString(R.string.nrtc_setting_voe_high_quality_key), false);
    }

    /**
     * 1, autoCallProximity: 语音通话时使用, 距离感应自动黑屏
     * 2, videoCropRatio: 制定固定的画面裁剪比例，发送端有效
     * 3, videoAutoRotate: 结合自己设备角度和对方设备角度自动旋转画面
     * 4, serverRecordAudio: 需要服务器录制语音, 同时需要 APP KEY 下面开通了服务器录制功能
     * 5, serverRecordVideo: 需要服务器录制视频, 同时需要 APP KEY 下面开通了服务器录制功能
     * 7, videoQuality: 视频质量调整, 最高建议使用480P
     * 8, videoFpsReported: 是否开启视频绘制帧率汇报
     * 9, deviceDefaultRotation: 99.99%情况下你不需要设置这个参数, 当设备固定在水平方向时,并且设备不会移动, 这时是无法确定设备角度的,可以设置一个默认角度
     * 10, deviceRotationOffset: 99.99%情况下你不需要设置这个参数, 当你的设备传感器获取的角度永远偏移固定值时设置,用于修正旋转角度
     * 11, videoMaxBitrate: 视频最大码率设置, 100K ~ 5M. 如果没有特殊需求不要去设置,会影响SDK内部的调节机制
     * 12, audioEffectAecMode: 语音处理选择, 默认使用平台内置,当你发现平台内置不好用时可以设置到SDK内置
     * 13, audioEffectNsMode: 语音处理选择, 默认使用平台内置,当你发现平台内置不好用时可以设置到SDK内置
     * 14, videoHwEncoderMode: 视频编码类型, 默认情况下不用设置.
     * 15, videoHwDecoderMode: 视频解码类型, 默认情况下不用设置.
     * 16, audioHighQuality: 高清语音，采用更高的采样率来传输语音
     * 17, audioDtx: 非连续发送，当监测到人声非活跃状态时减少数据包的发送
     */
    private void updateAVChatOptionalConfig() {
        avChatParameters.setBoolean(AVChatParameters.KEY_AUDIO_CALL_PROXIMITY, autoCallProximity);
        avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FIXED_CROP_RATIO, videoCropRatio);
        avChatParameters.setBoolean(AVChatParameters.KEY_VIDEO_ROTATE_IN_RENDING, videoAutoRotate);
        avChatParameters.setBoolean(AVChatParameters.KEY_SERVER_AUDIO_RECORD, serverRecordAudio);
        avChatParameters.setBoolean(AVChatParameters.KEY_SERVER_VIDEO_RECORD, serverRecordVideo);
        avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, videoQuality);
        avChatParameters.setBoolean(AVChatParameters.KEY_VIDEO_FPS_REPORTED, videoFpsReported);
        avChatParameters.setInteger(AVChatParameters.KEY_DEVICE_DEFAULT_ROTATION, deviceDefaultRotation);
        avChatParameters.setInteger(AVChatParameters.KEY_DEVICE_ROTATION_FIXED_OFFSET, deviceRotationOffset);

        if (videoMaxBitrate > 0) {
            avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_MAX_BITRATE, videoMaxBitrate * 1024);
        }
        switch (audioEffectAecMode) {
            case 0:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER, AVChatAudioEffectMode.DISABLE);
                break;
            case 1:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER, AVChatAudioEffectMode.SDK_BUILTIN);
                break;
            case 2:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER, AVChatAudioEffectMode.PLATFORM_BUILTIN);
                break;
        }
        switch (audioEffectNsMode) {
            case 0:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR, AVChatAudioEffectMode.DISABLE);
                break;
            case 1:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR, AVChatAudioEffectMode.SDK_BUILTIN);
                break;
            case 2:
                avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR, AVChatAudioEffectMode.PLATFORM_BUILTIN);
                break;
        }
        switch (videoHwEncoderMode) {
            case 0:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_AUTO);
                break;
            case 1:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_HARDWARE);
                break;
        }
        switch (videoHwDecoderMode) {
            case 0:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_AUTO);
                break;
            case 1:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                avChatParameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_HARDWARE);
                break;
        }
        avChatParameters.setBoolean(AVChatParameters.KEY_AUDIO_HIGH_QUALITY, audioHighQuality);

        //观众角色,多人模式下使用. IM Demo没有多人通话, 全部设置为AVChatUserRole.NORMAL.
        avChatParameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);

        //采用I420图像格式
        avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_FILTER_FORMAT, AVChatImageFormat.I420);
    }

    public AVChatParameters getAvChatParameters() {
        return avChatParameters;
    }

    public boolean isServerRecordAudio() {
        return serverRecordAudio;
    }

    public boolean isServerRecordVideo() {
        return serverRecordVideo;
    }
}
