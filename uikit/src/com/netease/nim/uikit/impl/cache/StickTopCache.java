package com.netease.nim.uikit.impl.cache;

import android.text.TextUtils;

import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StickTopCache {
    private static final Set<String> stickTopSet = new HashSet<>();

    static {
        List<StickTopSessionInfo> infoList = NIMClient.getService(MsgService.class).queryStickTopSessionBlock();
        recordStickTop(infoList, true);
    }

    public synchronized static void recordStickTop(StickTopSessionInfo info, boolean stickTop) {
        if (info == null) {
            return;
        }
        recordStickTop(info.getSessionId(), info.getSessionType(), stickTop);
    }

    public synchronized static void recordStickTop(List<StickTopSessionInfo> infoList, boolean stickTop) {
        if (CommonUtil.isEmpty(infoList)) {
            return;
        }
        for (StickTopSessionInfo info: infoList) {
            if (info == null) {
                continue;
            }
            recordStickTop(info.getSessionId(), info.getSessionType(), stickTop);
        }
    }

    public synchronized static void recordStickTop(String sessionId, SessionTypeEnum sessionType, boolean stickTop) {
        String sessionKey = MessageHelper.combineSessionKey(sessionType, sessionId);

        if (TextUtils.isEmpty(sessionKey)) {
            return;
        }
        if (stickTop) {
            stickTopSet.add(sessionKey);
        } else {
            stickTopSet.remove(sessionKey);
        }
    }

    public static boolean isStickTop(RecentContact recent) {
        String sessionId;
        SessionTypeEnum sessionType;
        if (recent == null || TextUtils.isEmpty(sessionId = recent.getContactId()) || (sessionType = recent.getSessionType()) == null) {
            return false;
        }
        return isStickTop(sessionId, sessionType);
    }

    public static boolean isStickTop(String sessionId, SessionTypeEnum sessionType) {
        // 合并成会话Key
        String sessionKey = MessageHelper.combineSessionKey(sessionType, sessionId);
        // 参数不合法，返回false
        if (TextUtils.isEmpty(sessionKey)) {
            return false;
        }

        return stickTopSet.contains(sessionKey);
    }
}
