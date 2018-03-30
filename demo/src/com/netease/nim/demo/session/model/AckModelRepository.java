package com.netease.nim.demo.session.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo;

/**
 * Created by winnie on 2018/3/17.
 */

public class AckModelRepository {

    public LiveData<TeamMsgAckInfo> getMsgAckInfo(IMMessage message) {
        final MutableLiveData<TeamMsgAckInfo> teamMsgAckInfoLiveData = new MutableLiveData<TeamMsgAckInfo>();

        NIMSDK.getTeamService().fetchTeamMessageReceiptDetail(message).setCallback(new RequestCallback<TeamMsgAckInfo>() {
            @Override
            public void onSuccess(TeamMsgAckInfo param) {
                teamMsgAckInfoLiveData.setValue(param);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
        return teamMsgAckInfoLiveData;
    }

}
