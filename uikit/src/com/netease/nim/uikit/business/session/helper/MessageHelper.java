package com.netease.nim.uikit.business.session.helper;

import android.text.TextUtils;
import android.util.Pair;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.wrapper.MessageRevokeTip;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hzxuwen on 2016/8/19.
 */
public class MessageHelper {
    private static final String TAG = "MessageHelper";

    public static MessageHelper getInstance() {
        return InstanceHolder.instance;
    }

    static class InstanceHolder {
        final static MessageHelper instance = new MessageHelper();
    }

    // 消息撤回
    public void onRevokeMessage(IMMessage item, String revokeAccount) {
        onRevokeMessage(item, revokeAccount, null, null);
    }

    public void onRevokeMessage(IMMessage item, String revokeAccount, String attach, String callbackExt) {
        if (item == null) {
            return;
        }
        IMMessage message = MessageBuilder.createTipMessage(item.getSessionId(), item.getSessionType());
        message.setContent(MessageRevokeTip.getRevokeTipContent(item, revokeAccount));
        message.setStatus(MsgStatusEnum.success);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        message.setConfig(config);
        if (!TextUtils.isEmpty(attach) || !TextUtils.isEmpty(callbackExt)) {
            Map<String, Object> localExt = new HashMap<>();
            if (!TextUtils.isEmpty(attach)) {
                localExt.put("attach", attach);
            }
            if (!TextUtils.isEmpty(callbackExt)) {
                localExt.put("callbackExt", callbackExt);
            }
            message.setLocalExtension(localExt);
        }
        NIMClient.getService(MsgService.class).saveMessageToLocalEx(message, true, item.getTime());
    }

    /**
     * 从 mItems 按顺序取出被勾选的消息
     *
     * @return 被勾选的消息
     */
    public LinkedList<IMMessage> getCheckedItems(List<IMMessage> items) {
        LinkedList<IMMessage> checkedList = new LinkedList<>();
        for (IMMessage msg : items) {
            if (msg.isChecked()) {
                checkedList.add(msg);
            }
        }
        return checkedList;
    }

    /**
     * 通过id和type，从本地存储中查询对应的群名或用户名
     *
     * @param id          群或用户的id
     * @param sessionType 会话类型
     * @return id对应的昵称
     */
    public String getStoredNameFromSessionId(final String id, final SessionTypeEnum sessionType) {
        switch (sessionType) {
            case P2P:
                //读取对方用户名称
                NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(id);
                if (userInfo == null) {
                    return null;
                }
                return userInfo.getName();
            case Team:
                //获取群信息
                Team team = NimUIKit.getTeamProvider().getTeamById(id);
                if (team == null) {
                    return null;
                }
                return team.getName();
            case SUPER_TEAM:
                //获取群信息
                SuperTeam superTeam = NimUIKit.getSuperTeamProvider().getTeamById(id);
                if (superTeam == null) {
                    return null;
                }
                return superTeam.getName();
            default:
                return null;
        }
    }



    /**
     * 判断消息是否被加入合并转发
     *
     * @param message 待测消息
     * @return true: 可以; false: 不能
     */
    public boolean isAvailableInMultiRetweet(IMMessage message) {
        if (message == null) {
            return false;
        }
        MsgTypeEnum msgType = message.getMsgType();
        //过滤掉不能单条转发的消息、null、未知类型消息、音视频通话、通知消息和提醒类消息
        return !NimUIKitImpl.getMsgForwardFilter().shouldIgnore(message) && msgType != null && !MsgTypeEnum.undef.equals(msgType) && !MsgTypeEnum.avchat.equals(msgType) && !MsgTypeEnum.notification.equals(msgType) && !MsgTypeEnum.tip.equals(msgType);
    }


    /**
     * 根据ids字段设置P2P AVChat消息的发送方向和发送者
     *
     * @param message 点对点AVChat消息
     */
    public static void adjustAVChatMsgDirect(IMMessage message) {
        if (message == null || message.getMsgType() != MsgTypeEnum.avchat || message.getAttachment() == null) {
            return;
        }
        String attachmentStr = message.getAttachment().toJson(false);
        try {
            JSONObject attachmentJson = new JSONObject(attachmentStr);
            JSONObject dataJson = attachmentJson.getJSONObject("data");
            String fromAccount = dataJson.optString("from");
            if (TextUtils.isEmpty(fromAccount)) {
                JSONArray arr = dataJson.optJSONArray("ids");
                fromAccount = (String) arr.get(0);
            }
            message.setDirect(fromAccount.equals(NimUIKit.getAccount()) ? MsgDirectionEnum.Out : MsgDirectionEnum.In);
            message.setFromAccount(fromAccount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getJsonStringFromMap(final Map<String, Object> map) {
        String result = null;
        if (map != null && !map.isEmpty()) {
            try {
                JSONObject json = new JSONObject(map);
                result = json.toString();
            } catch (Exception e) {
                LogUtil.e(TAG, "getJsonStringFromMap exception =" + e.getMessage());
            }
        }

        return result;
    }

    public static Map<String, Object> getMapFromJsonString(final String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) {
            return null;
        }

        try {
            org.json.JSONObject json = new org.json.JSONObject(jsonStr);
            return recursiveParseJsonObject(json);
        } catch (org.json.JSONException e) {
            LogUtil.e(TAG, "getMapFromJsonString exception =" + e.getMessage());
        }

        return null;
    }

    private static Map<String, Object> recursiveParseJsonObject(org.json.JSONObject json) throws org.json.JSONException {
        if (json == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>(json.length());
        String key;
        Object value;
        Iterator<String> i = json.keys();
        while (i.hasNext()) {
            key = i.next();
            value = json.get(key);
            if (value instanceof org.json.JSONArray) {
                map.put(key, recursiveParseJsonArray((org.json.JSONArray) value));
            } else if (value instanceof org.json.JSONObject) {
                map.put(key, recursiveParseJsonObject((org.json.JSONObject) value));
            } else {
                map.put(key, value);
            }
        }

        return map;
    }

    private static List recursiveParseJsonArray(org.json.JSONArray array) throws org.json.JSONException {
        if (array == null) {
            return null;
        }

        List list = new ArrayList(array.length());
        Object value;
        for (int m = 0; m < array.length(); m++) {
            value = array.get(m);
            if (value instanceof org.json.JSONArray) {
                list.add(recursiveParseJsonArray((org.json.JSONArray) value));
            } else if (value instanceof org.json.JSONObject) {
                list.add(recursiveParseJsonObject((org.json.JSONObject) value));
            } else {
                list.add(value);
            }
        }

        return list;
    }

    public static Set<String> getUuidSet(Collection<IMMessage> msgList) {
        if (CommonUtil.isEmpty(msgList)) {
            return new HashSet<>(0);
        }
        HashSet<String> set = new HashSet<>(msgList.size() << 1);
        for (IMMessage msg : msgList) {
            set.add(msg.getUuid());
        }
        return set;
    }

    /**
     * 将会话类型和会话ID组成的字符串拆解为Pair
     * 如果会话类型部分解析不出来，返回null；如果会话ID解析不出来，返回""
     *
     * @param idStr 分为p2p/team/superTeam，格式分别是：p2p|accid、team|tid、super_team|tid
     * @return 拆解结果，first: 会话类型; second: 会话ID
     */
    public static Pair<SessionTypeEnum, String> parseSessionKey(String idStr) {
        if (TextUtils.isEmpty(idStr)) {
            return new Pair<>(null, "");
        }
        String[] sessionIdArr = idStr.split("\\|");
        if (sessionIdArr.length < 2){
            //不能为None否则可能成为加载更多项，而且因为不在最后而不稳定
            return new Pair<>(null, "");
        }
        SessionTypeEnum type = null;
        switch (sessionIdArr[0]) {
            case "team":
                type = SessionTypeEnum.Team;
                break;
            case "super_team":
                type = SessionTypeEnum.SUPER_TEAM;
                break;
            case "p2p":
                type = SessionTypeEnum.P2P;
                break;
            default:
                break;
        }
        return new Pair<>(type, sessionIdArr[1]);
    }

    /**
     * 将会话类型和会话ID组合成具有唯一性的会话ID字符串
     *
     * @param sessionType 会话类型，可以填P2P/Team/SUPER_TEAM，否则返回""
     * @param sessionId 会话ID，如果为空，返回""
     * @return 拆解结果，first: 会话类型; second: 会话ID
     */
    public static String combineSessionKey(SessionTypeEnum sessionType, String sessionId) {
        if (sessionType == null || TextUtils.isEmpty(sessionId)) {
            return "";
        }
        String typeStr;
        switch (sessionType) {
            case P2P:
                typeStr = "p2p";
                break;
            case Team:
                typeStr = "team";
                break;
            case SUPER_TEAM:
                typeStr = "super_team";
                break;
            default:
                return "";
        }

        return typeStr + "|" + sessionId;
    }
}
