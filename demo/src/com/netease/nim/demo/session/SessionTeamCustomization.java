package com.netease.nim.demo.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.team.model.TeamRequestCode;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 抽象出来的，群组更多定制化选项，普通群和高级群同样功能的抽象
 * Created by winnie on 2018/3/19.
 */

public class SessionTeamCustomization extends SessionCustomization{

    public interface SessionTeamCustomListener extends Serializable {
        void initPopupWindow(Context context,View view, String sessionId, SessionTypeEnum sessionTypeEnum);

        void onSelectedAccountsResult(ArrayList<String> selectedAccounts);

        void onSelectedAccountFail();
    }

    private SessionTeamCustomListener sessionTeamCustomListener;

    public SessionTeamCustomization(SessionTeamCustomListener listener) {
        this.sessionTeamCustomListener = listener;
        // 定制ActionBar右边的按钮，可以加多个
        ArrayList<SessionCustomization.OptionsButton> optionsButtons = new ArrayList<>();
        SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
            @Override
            public void onClick(Context context, View view, String sessionId) {
                sessionTeamCustomListener.initPopupWindow(context, view, sessionId, SessionTypeEnum.Team);
            }
        };
        cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

        SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
            @Override
            public void onClick(Context context, View view, String sessionId) {
                Team team = NimUIKit.getTeamProvider().getTeamById(sessionId);
                if (team != null && team.isMyTeam()) {
                    NimUIKit.startTeamInfo(context, sessionId);
                } else {
                    ToastHelper.showToast(context, R.string.team_invalid_tip);
                }
            }
        };
        infoButton.iconId = R.drawable.nim_ic_message_actionbar_team;
        optionsButtons.add(cloudMsgButton);
        optionsButtons.add(infoButton);

        buttons = optionsButtons;
        withSticker = true;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == TeamRequestCode.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String reason = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                boolean finish = reason != null && (reason.equals(TeamExtras
                        .RESULT_EXTRA_REASON_DISMISS) || reason.equals(TeamExtras.RESULT_EXTRA_REASON_QUIT));
                if (finish) {
                    activity.finish(); // 退出or解散群直接退出多人会话
                }
            }
        } else if (requestCode == TeamRequestCode.REQUEST_TEAM_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> selectedAccounts = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                sessionTeamCustomListener.onSelectedAccountsResult(selectedAccounts);
            } else {
                sessionTeamCustomListener.onSelectedAccountFail();
            }
        }
    }

    @Override
    public MsgAttachment createStickerAttachment(String category, String item) {
        return new StickerAttachment(category, item);
    }
}
