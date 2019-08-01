package com.netease.nim.demo.team;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2015/9/25.
 */
public class TeamCreateHelper {
    private static final String TAG = TeamCreateHelper.class.getSimpleName();
    private static final int DEFAULT_TEAM_CAPACITY = 200;

    /**
     * 创建讨论组
     */
    public static void createNormalTeam(final Context context, List<String> memberAccounts, final boolean isNeedBack, final RequestCallback<CreateTeamResult> callback) {

        String teamName = "讨论组";

        DialogMaker.showProgressDialog(context, context.getString(com.netease.nim.uikit.R.string.empty), true);
        // 创建群
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
        fields.put(TeamFieldEnum.Name, teamName);
        NIMClient.getService(TeamService.class).createTeam(fields, TeamTypeEnum.Normal, "",
                memberAccounts).setCallback(
                new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult result) {
                        DialogMaker.dismissProgressDialog();

                        ArrayList<String> failedAccounts = result.getFailedInviteAccounts();
                        if (failedAccounts != null && !failedAccounts.isEmpty()) {
                            TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
                        } else {
                            ToastHelper.showToast(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_success);
                        }

                        if (isNeedBack) {
                            SessionHelper.startTeamSession(context, result.getTeam().getId(), MainActivity.class, null); // 进入创建的群
                        } else {
                            SessionHelper.startTeamSession(context, result.getTeam().getId());
                        }
                        if (callback != null) {
                            callback.onSuccess(result);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == ResponseCode.RES_TEAM_ECOUNT_LIMIT) {
                            String tip = context.getString(com.netease.nim.uikit.R.string.over_team_member_capacity, DEFAULT_TEAM_CAPACITY);
                            ToastHelper.showToast(DemoCache.getContext(), tip);
                        } else {
                            ToastHelper.showToast(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_failed);
                        }

                        Log.e(TAG, "create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                }
        );
    }

    /**
     * 创建高级群
     */
    public static void createAdvancedTeam(final Context context, List<String> memberAccounts) {

        String teamName = "高级群";

        DialogMaker.showProgressDialog(context, context.getString(com.netease.nim.uikit.R.string.empty), true);
        // 创建群
        TeamTypeEnum type = TeamTypeEnum.Advanced;
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<>();
        fields.put(TeamFieldEnum.Name, teamName);
        NIMClient.getService(TeamService.class).createTeam(fields, type, "",
                memberAccounts).setCallback(
                new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult result) {
                        Log.i(TAG, "create team success, team id =" + result.getTeam().getId() + ", now begin to update property...");
                        onCreateSuccess(context, result);
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        String tip;
                        if (code == ResponseCode.RES_TEAM_ECOUNT_LIMIT) {
                            tip = context.getString(com.netease.nim.uikit.R.string.over_team_member_capacity,
                                    DEFAULT_TEAM_CAPACITY);
                        } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                            tip = context.getString(com.netease.nim.uikit.R.string.over_team_capacity);
                        } else {
                            tip = context.getString(com.netease.nim.uikit.R.string.create_team_failed) + ", code=" + code;
                        }

                        ToastHelper.showToast(context, tip);

                        Log.e(TAG, "create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                }
        );
    }

    /**
     * 群创建成功回调
     */
    private static void onCreateSuccess(final Context context, CreateTeamResult result) {
        if (result == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }
        final Team team = result.getTeam();
        if (team == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }

        Log.i(TAG, "create and update team success");

        DialogMaker.dismissProgressDialog();
        // 检查有没有邀请失败的成员
        ArrayList<String> failedAccounts = result.getFailedInviteAccounts();
        if (failedAccounts != null && !failedAccounts.isEmpty()) {
            TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
        } else {
            ToastHelper.showToast(DemoCache.getContext(), com.netease.nim.uikit.R.string.create_team_success);
        }

        // 演示：向群里插入一条Tip消息，使得该群能立即出现在最近联系人列表（会话列表）中，满足部分开发者需求
        Map<String, Object> content = new HashMap<>(1);
        content.put("content", "成功创建高级群");
        IMMessage msg = MessageBuilder.createTipMessage(team.getId(), SessionTypeEnum.Team);
        msg.setRemoteExtension(content);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        msg.setConfig(config);
        msg.setStatus(MsgStatusEnum.success);
        NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);

        // 发送后，稍作延时后跳转
        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionHelper.startTeamSession(context, team.getId()); // 进入创建的群
            }
        }, 50);
    }
}
