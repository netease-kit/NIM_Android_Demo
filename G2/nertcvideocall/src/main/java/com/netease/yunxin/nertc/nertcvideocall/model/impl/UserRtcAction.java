package com.netease.yunxin.nertc.nertcvideocall.model.impl;

import androidx.annotation.IntDef;

import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.AUDIO_START;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.AUDIO_STOP;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.FIRST_VIDEO_FRAME_DECODED;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.JOIN;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.LEAVE;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.VIDEO_START;
import static com.netease.yunxin.nertc.nertcvideocall.model.impl.UserRtcAction.VIDEO_STOP;

/**
 * rtc 动作定义
 */
@IntDef({JOIN, VIDEO_START, VIDEO_STOP, AUDIO_START, AUDIO_STOP, FIRST_VIDEO_FRAME_DECODED, LEAVE})
public @interface UserRtcAction {
    int JOIN = 1;

    int VIDEO_START = 2;

    int VIDEO_STOP = 3;

    int AUDIO_START = 4;

    int AUDIO_STOP = 5;

    int FIRST_VIDEO_FRAME_DECODED = 6;

    int LEAVE = 7;
}
