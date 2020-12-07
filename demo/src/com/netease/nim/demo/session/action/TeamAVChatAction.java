package com.netease.nim.demo.session.action;

import android.text.TextUtils;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItemFilter;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.model.TeamRequestCode;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.nertc.ui.team.TeamG2Activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzchenkang on 2017/5/3.
 */

public class TeamAVChatAction extends AVChatAction {

    private static final int MAX_INVITE_NUM = 8;

    // private String teamID;

    private LaunchTransaction transaction;

    public TeamAVChatAction(ChannelType avChatType) {
        super(avChatType);
    }

    @Override
    public void startAudioVideoCall(ChannelType avChatType) {
        if (transaction != null) {
            return;
        }

        final String tid = getAccount();
        if (TextUtils.isEmpty(tid)) {
            return;
        }
        transaction = new LaunchTransaction();
        transaction.setTeamID(tid);

        // load 一把群成员
        NimUIKit.getTeamProvider().fetchTeamMemberList(tid, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result, int code) {
                // 检查下 tid 是否相等
                if (!checkTransactionValid()) {
                    return;
                }
                if (success && result != null) {
                    if (result.size() < 2) {
                        transaction = null;
                        ToastHelper.showToast(getActivity(), getActivity().getString(R.string.t_avchat_not_start_with_less_member));
                    } else {
                        NimUIKit.startContactSelector(getActivity(), getContactSelectOption(tid), TeamRequestCode.REQUEST_TEAM_VIDEO);
                    }
                }
            }
        });
    }

    public void onSelectedAccountFail() {
        transaction = null;
    }

    public void onSelectedAccountsResult(final ArrayList<String> accounts) {
        LogUtil.ui("start teamVideo " + getAccount() + " accounts = " + accounts);

        if (!checkTransactionValid()) {
            return;
        }

        TeamG2Activity.startActivity(getActivity(), false, transaction.getTeamID(), accounts, "");
        transaction = null;
    }

    private boolean checkTransactionValid() {
        if (transaction == null) {
            return false;
        }
        if (transaction.getTeamID() == null || !transaction.getTeamID().equals(getAccount())) {
            transaction = null;
            return false;
        }
        return true;
    }

    //
    private ContactSelectActivity.Option getContactSelectOption(String teamId) {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.teamId = teamId;
        option.maxSelectNum = MAX_INVITE_NUM;
        option.maxSelectNumVisible = true;
        option.title = NimUIKit.getContext().getString(com.netease.nim.uikit.R.string.invite_member);
        option.maxSelectedTip = NimUIKit.getContext().getString(com.netease.nim.uikit.R.string.reach_capacity);
        option.itemFilter = new ContactItemFilter() {
            @Override
            public boolean filter(AbsContactItem item) {
                IContact contact = ((ContactItem) item).getContact();
                // 过滤掉自己
                return contact.getContactId().equals(DemoCache.getAccount());
            }
        };
        return option;
    }

    private class LaunchTransaction implements Serializable {
        private String teamID;
        private String roomName;

        public String getRoomName() {
            return roomName;
        }

        public String getTeamID() {
            return teamID;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public void setTeamID(String teamID) {
            this.teamID = teamID;
        }
    }
}
