package com.netease.nim.demo.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.activity.UserProfileActivity;
import com.netease.nim.demo.session.action.AVChatAction;
import com.netease.nim.demo.session.action.FileAction;
import com.netease.nim.demo.session.action.GuessAction;
import com.netease.nim.demo.session.action.SnapChatAction;
import com.netease.nim.demo.session.activity.MessageHistoryActivity;
import com.netease.nim.demo.session.extension.CustomAttachParser;
import com.netease.nim.demo.session.extension.CustomAttachment;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.demo.session.viewholder.MsgViewHolderAVChat;
import com.netease.nim.demo.session.viewholder.MsgViewHolderDefCustom;
import com.netease.nim.demo.session.viewholder.MsgViewHolderFile;
import com.netease.nim.demo.session.viewholder.MsgViewHolderGuess;
import com.netease.nim.demo.session.viewholder.MsgViewHolderRTSNotification;
import com.netease.nim.demo.session.viewholder.MsgViewHolderSnapChat;
import com.netease.nim.demo.session.viewholder.MsgViewHolderSticker;
import com.netease.nim.demo.team.activity.AdvancedTeamInfoActivity;
import com.netease.nim.demo.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.SessionEventListener;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.team.TeamDataCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static SessionCustomization p2pCustomization;
    private static SessionCustomization teamCustomization;
    private static SessionCustomization myP2pCustomization;

    public static void init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders();

        // 设置会话中点击事件响应处理
        setSessionListener();
    }

    public static void startP2PSession(Context context, String account) {
        if (!DemoCache.getAccount().equals(account)) {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getP2pCustomization());
        } else {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getMyP2pCustomization());
        }
    }

    public static void startTeamSession(Context context, String tid) {
        NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, getTeamCustomization());
    }

    // 定制化单聊界面。如果使用默认界面，返回null即可
    private static SessionCustomization getP2pCustomization() {
        if (p2pCustomization == null) {
             p2pCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == NormalTeamInfoActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        String result = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_REASON);
                        if (result == null) {
                            return;
                        }
                        if (result.equals(NormalTeamInfoActivity.RESULT_EXTRA_REASON_CREATE)) {
                            String tid = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_DATA);
                            if (TextUtils.isEmpty(tid)) {
                                return;
                            }

                            startTeamSession(activity, tid);
                            activity.finish();
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 背景
//            p2pCustomization.backgroundColor = Color.BLUE;
//            p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
//            p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
//            p2pCustomization.backgroundUri = "android.resource://com.netease.nim.demo/drawable/bk"

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new AVChatAction(AVChatType.AUDIO));
            actions.add(new AVChatAction(AVChatType.VIDEO));
            actions.add(new SnapChatAction());
            actions.add(new GuessAction());
            actions.add(new FileAction());
            p2pCustomization.actions = actions;
            p2pCustomization.withSticker = true;

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    MessageHistoryActivity.start(context, sessionId, SessionTypeEnum.P2P); // 漫游消息查询
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    NormalTeamInfoActivity.startForCreateNormalTeam(context, sessionId); // 创建群
                }
            };

            infoButton.iconId = R.drawable.nim_ic_message_actionbar_p2p_add;

            buttons.add(cloudMsgButton);
            buttons.add(infoButton);
            p2pCustomization.buttons = buttons;
        }

        return p2pCustomization;
    }

    private static SessionCustomization getMyP2pCustomization() {
        if (myP2pCustomization == null) {
            myP2pCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == NormalTeamInfoActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        String result = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_REASON);
                        if (result == null) {
                            return;
                        }
                        if (result.equals(NormalTeamInfoActivity.RESULT_EXTRA_REASON_CREATE)) {
                            String tid = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_DATA);
                            if (TextUtils.isEmpty(tid)) {
                                return;
                            }

                            startTeamSession(activity, tid);
                            activity.finish();
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 背景
//            p2pCustomization.backgroundColor = Color.BLUE;
//            p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
//            p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
//            p2pCustomization.backgroundUri = "android.resource://com.netease.nim.demo/drawable/bk"

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new SnapChatAction());
            actions.add(new GuessAction());
            actions.add(new FileAction());
            myP2pCustomization.actions = actions;
            myP2pCustomization.withSticker = true;
            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    MessageHistoryActivity.start(context, sessionId, SessionTypeEnum.P2P); // 漫游消息查询
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            buttons.add(cloudMsgButton);
            myP2pCustomization.buttons = buttons;
        }
        return myP2pCustomization;
    }

    private static SessionCustomization getTeamCustomization() {
        if (teamCustomization == null) {
            teamCustomization = new SessionCustomization() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == NormalTeamInfoActivity.REQUEST_CODE) {
                        if (resultCode == Activity.RESULT_OK) {
                            String reason = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_REASON);
                            boolean finish = reason != null && (reason.equals(NormalTeamInfoActivity
                                    .RESULT_EXTRA_REASON_DISMISS) || reason.equals(NormalTeamInfoActivity.RESULT_EXTRA_REASON_QUIT));
                            if (finish) {
                                activity.finish(); // 退出or解散群直接退出多人会话
                            }
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new GuessAction());
            actions.add(new FileAction());
            teamCustomization.actions = actions;

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    MessageHistoryActivity.start(context, sessionId, SessionTypeEnum.Team); // 群漫游消息查询; // 漫游消息查询
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    Team team = TeamDataCache.getInstance().getTeamById(sessionId);
                    if (team == null) {
                        return;
                    }

                    if (team.getType() == TeamTypeEnum.Advanced) {
                        AdvancedTeamInfoActivity.start(context, sessionId); // 启动固定群组资料页
                    } else {
                        NormalTeamInfoActivity.startForTeamInfo(context, sessionId); // 启动普通群组资料页
                    }
                }
            };
            infoButton.iconId = R.drawable.nim_ic_message_actionbar_team;
            buttons.add(cloudMsgButton);
            buttons.add(infoButton);
            teamCustomization.buttons = buttons;

            teamCustomization.withSticker = true;
        }

        return teamCustomization;
    }

    private static void registerViewHolders() {
        NimUIKit.registerMsgItemViewHolder(FileAttachment.class, MsgViewHolderFile.class);
        NimUIKit.registerMsgItemViewHolder(AVChatAttachment.class, MsgViewHolderAVChat.class);
        NimUIKit.registerMsgItemViewHolder(GuessAttachment.class, MsgViewHolderGuess.class);
        NimUIKit.registerMsgItemViewHolder(CustomAttachment.class, MsgViewHolderDefCustom.class);
        NimUIKit.registerMsgItemViewHolder(StickerAttachment.class, MsgViewHolderSticker.class);
        NimUIKit.registerMsgItemViewHolder(SnapChatAttachment.class, MsgViewHolderSnapChat.class);
        NimUIKit.registerMsgItemViewHolder(RTSAttachment.class, MsgViewHolderRTSNotification.class);
    }

    private static void setSessionListener() {
        SessionEventListener listener = new SessionEventListener() {
            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 一般用于打开用户资料页面
                UserProfileActivity.start(context, message.getFromAccount());
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }
        };

        NimUIKit.setSessionListener(listener);
    }
}
