package com.netease.yunxin.nertc.nertcvideocall.model;

import android.app.Activity;
import android.content.Context;

import java.util.List;

public interface UIService {
    Class<? extends Activity> getOneToOneAudioChat();

    Class<? extends Activity> getOneToOneVideoChat();

    Class<? extends Activity> getGroupVideoChat();

    int getNotificationIcon();

    int getNotificationSmallIcon();

    void startContactSelector(Context context, String teamId, List<String> excludeUserList, int requestCode);

}
