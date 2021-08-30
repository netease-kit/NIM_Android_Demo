package com.netease.nim.demo.session.action;


import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class AVChatAction extends BaseAction {
    private ChannelType avChatType;

    public AVChatAction(ChannelType avChatType) {
        super(avChatType == ChannelType.AUDIO ? R.drawable.message_plus_audio_chat_selector : R.drawable.message_plus_video_chat_selector,
                avChatType == ChannelType.AUDIO ? R.string.input_panel_audio_call : R.string.input_panel_video_call);
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

    public void startAudioVideoCall(ChannelType avChatType) {
        startAudioVideoCall(avChatType,getAccount());
    }

    public void startAudioVideoCall(ChannelType avChatType, String imAccId) {

        CallParam param = CallParam
                .createSingleCallParam(avChatType.getValue(),
                        DemoCache.getAccount(),
                        imAccId);

        CallKitUI.startSingleCall(getActivity(),param);
    }
}
