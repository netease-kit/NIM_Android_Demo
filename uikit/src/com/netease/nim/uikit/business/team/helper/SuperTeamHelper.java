package com.netease.nim.uikit.business.team.helper;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.superteam.SuperTeamMember;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;

/**
 * Created by hzsunyj on 2019-09-09.
 */
public class SuperTeamHelper {

    public static String getTeamName(String teamId) {
        SuperTeam team = NimUIKit.getSuperTeamProvider().getTeamById(teamId);
        return team == null ? teamId : TextUtils.isEmpty(team.getName()) ? team.getId() : team
                .getName();
    }

    /**
     * 获取显示名称。用户本人显示“你”
     *
     * @param tid
     * @param account
     * @return
     */
    public static String getTeamMemberDisplayNameYou(String tid, String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "你";
        }
        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人也显示昵称
     * 备注>群昵称>昵称
     */
    public static String getDisplayNameWithoutMe(String tid, String account) {
        String alias = NimUIKit.getContactProvider().getAlias(account);
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }
        String memberNick = getTeamNick(tid, account);
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }
        return UserInfoHelper.getUserName(account);
    }

    public static String getTeamNick(String tid, String account) {
        SuperTeam team = NimUIKit.getSuperTeamProvider().getTeamById(tid);
        if (team != null && team.getType() == TeamTypeEnum.Advanced) {
            SuperTeamMember member = NimUIKit.getSuperTeamProvider().getTeamMember(tid, account);
            if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
                return member.getTeamNick();
            }
        }
        return null;
    }

}
