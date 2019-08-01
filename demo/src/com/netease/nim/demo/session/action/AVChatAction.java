package com.netease.nim.demo.session.action;


import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class AVChatAction extends BaseAction {
    private AVChatType avChatType;

    public AVChatAction(AVChatType avChatType) {
        super(avChatType == AVChatType.AUDIO ? R.drawable.message_plus_audio_chat_selector : R.drawable.message_plus_video_chat_selector,
                avChatType == AVChatType.AUDIO ? R.string.input_panel_audio_call : R.string.input_panel_video_call);
        this.avChatType = avChatType;
    }

    @Override
    public void onClick() {
        if (NetworkUtil.isNetAvailable(getActivity())) {
            startAudioVideoCall(avChatType);
        } else {
            ToastHelper.showToast(getActivity(), R.string.network_is_not_available);
        }
    }

    /************************ 音视频通话 ***********************/

    public void startAudioVideoCall(AVChatType avChatType) {
        AVChatKit.outgoingCall(getActivity(), getAccount(), UserInfoHelper.getUserDisplayName(getAccount()), avChatType.getValue(), AVChatActivity.FROM_INTERNAL);
    }
}
