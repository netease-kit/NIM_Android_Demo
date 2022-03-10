package com.netease.yunxin.app.im.utils;

import com.netease.yunxin.kit.corekit.im.login.LoginUserInfo;

public class ConvertUtils {

    public static LoginUserInfo convertTo(com.netease.yunxin.kit.login.model.UserInfo user) {
        return new LoginUserInfo(user.getUser(), user.getAccessToken(),
                user.getImAccid(), user.getImToken(), user.getAvatar(), user.getNickname());
    }
}
