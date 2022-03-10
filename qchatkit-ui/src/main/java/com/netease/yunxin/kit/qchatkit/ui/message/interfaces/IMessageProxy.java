package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import android.content.Context;

public interface IMessageProxy {

    boolean sendTextMessage(String msg);

    boolean sendImage();

    boolean sendFile();

    boolean sendEmoji();

    boolean sendVoice();

    void onInputPanelExpand();

    void shouldCollapseInputPanel();

    String getAccount();

    Context getActivityContext();

}
