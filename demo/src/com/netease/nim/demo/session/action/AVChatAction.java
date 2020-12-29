package com.netease.nim.demo.session.action;


import com.netease.nim.demo.R;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.nertc.model.UserModel;
import com.netease.yunxin.nertc.ui.NERTCVideoCallActivity;

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
//        AVChatKit.outgoingCall(getActivity(), getAccount(), UserInfoHelper.getUserDisplayName(getAccount()), avChatType.getValue(), AVChatActivity.FROM_INTERNAL);

        UserModel userModel = new UserModel();
        userModel.imAccid = getAccount();
        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(getAccount());
        userModel.nickname = userInfo.getName();

        NIMClient.getService(NosService.class).getOriginUrlFromShortUrl(userInfo.getAvatar()).setCallback(new RequestCallback<String>() {
            @Override
            public void onSuccess(String param) {
                userModel.avatar = param;
                startAudioVideoCall(avChatType, userModel);
            }

            @Override
            public void onFailed(int code) {
                userModel.avatar = userInfo.getAvatar();
                startAudioVideoCall(avChatType, userModel);
            }

            @Override
            public void onException(Throwable exception) {
                userModel.avatar = userInfo.getAvatar();
                startAudioVideoCall(avChatType, userModel);
            }
        });
    }

    public void startAudioVideoCall(ChannelType avChatType, UserModel userModel) {
        if (avChatType == ChannelType.AUDIO) {
            NERTCVideoCallActivity.startAudioCallOther(getActivity(), userModel);
        } else {
            NERTCVideoCallActivity.startCallOther(getActivity(), userModel);
        }
    }
}
