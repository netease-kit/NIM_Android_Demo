package com.netease.nim.demo.mixpush;

import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 示例：
 * 1.自定义的推送文案
 * 2.自定义推送 payload 实现特定的点击通知栏跳转行为{@link DemoMixPushMessageHandler}
 * <p>
 * 如果自定义文案和payload，请开发者在各端发送消息时保持一致。
 */

public class DemoPushContentProvider implements CustomPushContentProvider {

    @Override
    public String getPushContent(IMMessage message) {
        return null;
    }

    @Override
    public Map<String, Object> getPushPayload(IMMessage message) {
        return getPayload(message);
    }

    private Map<String, Object> getPayload(IMMessage message) {
        if (message == null) {
            return null;
        }
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("sessionType", message.getSessionType().getValue());
        if (message.getSessionType() == SessionTypeEnum.Team) {
            payload.put("sessionID", message.getSessionId());
        } else if (message.getSessionType() == SessionTypeEnum.P2P) {
            payload.put("sessionID", message.getFromAccount());
        }

        return payload;
    }
}
