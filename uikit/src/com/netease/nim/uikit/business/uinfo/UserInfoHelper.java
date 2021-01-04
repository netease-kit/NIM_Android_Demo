package com.netease.nim.uikit.business.uinfo;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.SuperTeamHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import org.w3c.dom.Text;

public class UserInfoHelper {

    // 获取用户显示在标题栏和最近联系人中的名字
    public static String getUserTitleName(String id, SessionTypeEnum sessionType) {
        if (sessionType == SessionTypeEnum.P2P) {
            if (NimUIKit.getAccount().equals(id)) {
                return "我的电脑";
            } else {
                return getUserDisplayName(id);
            }
        } else if (sessionType == SessionTypeEnum.Team) {
            return TeamHelper.getTeamName(id);
        } else if (sessionType == SessionTypeEnum.Ysf) {
            return "我的客服";
        }
        return id;
    }

    /**
     * @param account 用户帐号
     * @return
     */
    public static String getUserDisplayName(String account) {
        String alias = NimUIKit.getContactProvider().getAlias(account);
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        } else {
            UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                return userInfo.getName();
            } else {
                return account;
            }
        }
    }

    /**
     * 获取消息发送者的展示名称，优先级序列：备注>群昵称>用户名>账号
     *
     * @param account 用户账号
     * @param sessionType 所处会话的类型
     * @param sessionId 所处会话的ID
     * @return 发送者的展示名称
     */
    public static String getUserDisplayNameInSession(String account, SessionTypeEnum sessionType, String sessionId) {
        if (TextUtils.isEmpty(account)) {
            return "";
        }

        // 备注
        String alias = NimUIKit.getContactProvider().getAlias(account);
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }

        // 群昵称
        String teamNick = null;
        if (sessionType == SessionTypeEnum.Team) {
            teamNick = TeamHelper.getTeamNick(sessionId, account);
        } else if (sessionType == SessionTypeEnum.SUPER_TEAM) {
            teamNick = SuperTeamHelper.getTeamNick(sessionId, account);
        }
        if (!TextUtils.isEmpty(teamNick)) {
            return teamNick;
        }

        // 用户名
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
            return userInfo.getName();
        }

        // 账号
        return account;

    }

    // 获取用户原本的昵称
    public static String getUserName(String account) {
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
            return userInfo.getName();
        } else {
            return account;
        }
    }

    /**
     * @param account         账号
     * @param selfNameDisplay 如果是自己，则显示内容
     * @return
     */
    public static String getUserDisplayNameEx(String account, String selfNameDisplay) {
        if (account.equals(NimUIKit.getAccount())) {
            return selfNameDisplay;
        }

        return getUserDisplayName(account);
    }
}
