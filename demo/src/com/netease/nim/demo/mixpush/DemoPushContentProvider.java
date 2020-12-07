package com.netease.nim.demo.mixpush;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.json.JSONException;
import org.json.JSONObject;

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
        int sessionType = message.getSessionType().getValue();
        payload.put("sessionType", sessionType);
        String sessionId = "";
        if (message.getSessionType() == SessionTypeEnum.Team) {
            sessionId = message.getSessionId();
        } else if (message.getSessionType() == SessionTypeEnum.P2P) {
            sessionId = message.getFromAccount();
        }
        if (!TextUtils.isEmpty(sessionId)) {
            payload.put("sessionID", sessionId);
        }
        //华为推送
        setHwField(payload, sessionType, sessionId);

        return payload;
    }

    private void setHwField(Map<String, Object> pushPayload, int sessionType, String sessionId) {
        //hwField
        Intent hwIntent = new Intent(Intent.ACTION_VIEW);
        String intentStr = String.format(
                "pushscheme://com.huawei.codelabpush/deeplink?sessionID=%s&sessionType=%s",
                sessionId, sessionType
        );
        hwIntent.setData(Uri.parse(intentStr));
        hwIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String intentUri = hwIntent.toUri(Intent.URI_INTENT_SCHEME);
        //点击事件的内容
        JSONObject clickAction = new JSONObject();
        //通知的内容
        JSONObject notification = new JSONObject();

        try {
            clickAction.putOpt("type", 1)
                    .putOpt("intent", intentUri);
            notification.putOpt("click_action", clickAction);
            pushPayload.put("hwField", notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
