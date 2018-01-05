package com.netease.nim.demo.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.avchatkit.TeamAVChatProfile;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.activity.RobotProfileActivity;
import com.netease.nim.demo.contact.activity.UserProfileActivity;
import com.netease.nim.demo.redpacket.NIMRedPacketClient;
import com.netease.nim.demo.session.action.AVChatAction;
import com.netease.nim.demo.session.action.FileAction;
import com.netease.nim.demo.session.action.GuessAction;
import com.netease.nim.demo.session.action.RTSAction;
import com.netease.nim.demo.session.action.RedPacketAction;
import com.netease.nim.demo.session.action.SnapChatAction;
import com.netease.nim.demo.session.action.TeamAVChatAction;
import com.netease.nim.demo.session.action.TipAction;
import com.netease.nim.demo.session.activity.MessageHistoryActivity;
import com.netease.nim.demo.session.activity.MessageInfoActivity;
import com.netease.nim.demo.session.extension.CustomAttachParser;
import com.netease.nim.demo.session.extension.CustomAttachment;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.demo.session.extension.RedPacketAttachment;
import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.demo.session.search.SearchMessageActivity;
import com.netease.nim.demo.session.viewholder.MsgViewHolderAVChat;
import com.netease.nim.demo.session.viewholder.MsgViewHolderDefCustom;
import com.netease.nim.demo.session.viewholder.MsgViewHolderFile;
import com.netease.nim.demo.session.viewholder.MsgViewHolderGuess;
import com.netease.nim.demo.session.viewholder.MsgViewHolderOpenRedPacket;
import com.netease.nim.demo.session.viewholder.MsgViewHolderRTS;
import com.netease.nim.demo.session.viewholder.MsgViewHolderRedPacket;
import com.netease.nim.demo.session.viewholder.MsgViewHolderSnapChat;
import com.netease.nim.demo.session.viewholder.MsgViewHolderSticker;
import com.netease.nim.demo.session.viewholder.MsgViewHolderTip;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.recent.RecentCustomization;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.api.model.session.SessionEventListener;
import com.netease.nim.uikit.api.wrapper.NimMessageRevokeObserver;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.business.session.module.MsgForwardFilter;
import com.netease.nim.uikit.business.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderUnknown;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.team.model.TeamRequestCode;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.popupmenu.NIMPopupMenu;
import com.netease.nim.uikit.common.ui.popupmenu.PopupMenuItem;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.impl.customization.DefaultRecentCustomization;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static final int ACTION_HISTORY_QUERY = 0;
    private static final int ACTION_SEARCH_MESSAGE = 1;
    private static final int ACTION_CLEAR_MESSAGE = 2;

    private static SessionCustomization p2pCustomization;
    private static SessionCustomization teamCustomization;
    private static SessionCustomization myP2pCustomization;
    private static SessionCustomization robotCustomization;
    private static RecentCustomization recentCustomization;

    private static NIMPopupMenu popupMenu;
    private static List<PopupMenuItem> menuItemList;

    public static void init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders();

        // 设置会话中点击事件响应处理
        setSessionListener();

        // 注册消息转发过滤器
        registerMsgForwardFilter();

        // 注册消息撤回过滤器
        registerMsgRevokeFilter();

        // 注册消息撤回监听器
        registerMsgRevokeObserver();

        NimUIKit.setCommonP2PSessionCustomization(getP2pCustomization());

        NimUIKit.setCommonTeamSessionCustomization(getTeamCustomization());

        NimUIKit.setRecentCustomization(getRecentCustomization());
    }

    public static void startP2PSession(Context context, String account) {
        startP2PSession(context, account, null);
    }

    public static void startP2PSession(Context context, String account, IMMessage anchor) {
        if (!DemoCache.getAccount().equals(account)) {
            if (NimUIKit.getRobotInfoProvider().getRobotByAccount(account) != null) {
                NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getRobotCustomization(), anchor);
            } else {
                NimUIKit.startP2PSession(context, account, anchor);
            }
        } else {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getMyP2pCustomization(), anchor);
        }
    }

    public static void startTeamSession(Context context, String tid) {
        startTeamSession(context, tid, null);
    }

    public static void startTeamSession(Context context, String tid, IMMessage anchor) {
        NimUIKit.startTeamSession(context, tid, anchor);
    }

    // 打开群聊界面(用于 UIKIT 中部分界面跳转回到指定的页面)
    public static void startTeamSession(Context context, String tid, Class<? extends Activity> backToClass, IMMessage anchor) {
        NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, getTeamCustomization(), backToClass, anchor);
    }

    // 定制化单聊界面。如果使用默认界面，返回null即可
    private static SessionCustomization getP2pCustomization() {
        if (p2pCustomization == null) {
            p2pCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(activity, requestCode, resultCode, data);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                actions.add(new AVChatAction(AVChatType.AUDIO));
                actions.add(new AVChatAction(AVChatType.VIDEO));
            }
            actions.add(new RTSAction());
            actions.add(new SnapChatAction());
            actions.add(new GuessAction());
            actions.add(new FileAction());
            actions.add(new TipAction());
            if (NIMRedPacketClient.isEnable()) {
                actions.add(new RedPacketAction());
            }
            p2pCustomization.actions = actions;
            p2pCustomization.withSticker = true;

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {

                    MessageInfoActivity.startActivity(context, sessionId); //打开聊天信息
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
                    if (requestCode == TeamRequestCode.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        String result = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                        if (result == null) {
                            return;
                        }
                        if (result.equals(TeamExtras.RESULT_EXTRA_REASON_CREATE)) {
                            String tid = data.getStringExtra(TeamExtras.RESULT_EXTRA_DATA);
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
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };

            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            buttons.add(cloudMsgButton);
            myP2pCustomization.buttons = buttons;
        }
        return myP2pCustomization;
    }

    private static SessionCustomization getRobotCustomization() {
        if (robotCustomization == null) {
            robotCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(activity, requestCode, resultCode, data);

                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return null;
                }
            };

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {

                    RobotProfileActivity.start(context, sessionId); //打开聊天信息
                }
            };


            infoButton.iconId = R.drawable.nim_ic_actionbar_robot_info;

            buttons.add(cloudMsgButton);
            buttons.add(infoButton);
            robotCustomization.buttons = buttons;
        }

        return robotCustomization;
    }

    private static RecentCustomization getRecentCustomization() {
        if (recentCustomization == null) {
            recentCustomization = new DefaultRecentCustomization() {
                @Override
                public String getDefaultDigest(RecentContact recent) {
                    switch (recent.getMsgType()) {
                        case avchat:
                            MsgAttachment attachment = recent.getAttachment();
                            AVChatAttachment avchat = (AVChatAttachment) attachment;
                            if (avchat.getState() == AVChatRecordState.Missed && !recent.getFromAccount().equals(NimUIKit.getAccount())) {
                                // 未接通话请求
                                StringBuilder sb = new StringBuilder("[未接");
                                if (avchat.getType() == AVChatType.VIDEO) {
                                    sb.append("视频电话]");
                                } else {
                                    sb.append("音频电话]");
                                }
                                return sb.toString();
                            } else if (avchat.getState() == AVChatRecordState.Success) {
                                StringBuilder sb = new StringBuilder();
                                if (avchat.getType() == AVChatType.VIDEO) {
                                    sb.append("[视频电话]: ");
                                } else {
                                    sb.append("[音频电话]: ");
                                }
                                sb.append(TimeUtil.secToTime(avchat.getDuration()));
                                return sb.toString();
                            } else {
                                if (avchat.getType() == AVChatType.VIDEO) {
                                    return ("[视频电话]");
                                } else {
                                    return ("[音频电话]");
                                }
                            }
                    }
                    return super.getDefaultDigest(recent);
                }
            };
        }

        return recentCustomization;
    }

    private static SessionCustomization getTeamCustomization() {
        if (teamCustomization == null) {

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            final TeamAVChatAction avChatAction = new TeamAVChatAction(AVChatType.VIDEO);
            TeamAVChatProfile.sharedInstance().registerObserver(true);

            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(avChatAction);
            actions.add(new GuessAction());
            actions.add(new FileAction());
            actions.add(new TipAction());
            if (NIMRedPacketClient.isEnable()) {
                actions.add(new RedPacketAction());
            }

            teamCustomization = new SessionCustomization() {
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
                            avChatAction.onSelectedAccountsResult(selectedAccounts);
                        } else {
                            avChatAction.onSelectedAccountFail();
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            teamCustomization.actions = actions;

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.Team);
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
                        Toast.makeText(context, R.string.team_invalid_tip, Toast.LENGTH_SHORT).show();
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
        NimUIKit.registerMsgItemViewHolder(RTSAttachment.class, MsgViewHolderRTS.class);
        NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip.class);
        registerRedPacketViewHolder();
    }

    private static void registerRedPacketViewHolder() {
        if (NIMRedPacketClient.isEnable()) {
            NimUIKit.registerMsgItemViewHolder(RedPacketAttachment.class, MsgViewHolderRedPacket.class);
            NimUIKit.registerMsgItemViewHolder(RedPacketOpenedAttachment.class, MsgViewHolderOpenRedPacket.class);
        } else {
            NimUIKit.registerMsgItemViewHolder(RedPacketAttachment.class, MsgViewHolderUnknown.class);
            NimUIKit.registerMsgItemViewHolder(RedPacketOpenedAttachment.class, MsgViewHolderUnknown.class);
        }
    }

    private static void setSessionListener() {
        SessionEventListener listener = new SessionEventListener() {
            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 一般用于打开用户资料页面
                if (message.getMsgType() == MsgTypeEnum.robot && message.getDirect() == MsgDirectionEnum.In) {
                    RobotAttachment attachment = (RobotAttachment) message.getAttachment();
                    if (attachment.isRobotSend()) {
                        RobotProfileActivity.start(context, attachment.getFromRobotAccount());
                        return;
                    }
                }
                UserProfileActivity.start(context, message.getFromAccount());
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }
        };

        NimUIKit.setSessionListener(listener);
    }


    /**
     * 消息转发过滤器
     */
    private static void registerMsgForwardFilter() {
        NimUIKit.setMsgForwardFilter(new MsgForwardFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getDirect() == MsgDirectionEnum.In
                        && (message.getAttachStatus() == AttachStatusEnum.transferring
                        || message.getAttachStatus() == AttachStatusEnum.fail)) {
                    // 接收到的消息，附件没有下载成功，不允许转发
                    return true;
                } else if (message.getMsgType() == MsgTypeEnum.custom && message.getAttachment() != null
                        && (message.getAttachment() instanceof SnapChatAttachment
                        || message.getAttachment() instanceof RTSAttachment
                        || message.getAttachment() instanceof RedPacketAttachment)) {
                    // 白板消息和阅后即焚消息，红包消息 不允许转发
                    return true;
                } else if (message.getMsgType() == MsgTypeEnum.robot && message.getAttachment() != null && ((RobotAttachment) message.getAttachment()).isRobotSend()) {
                    return true; // 如果是机器人发送的消息 不支持转发
                }
                return false;
            }
        });
    }

    /**
     * 消息撤回过滤器
     */
    private static void registerMsgRevokeFilter() {
        NimUIKit.setMsgRevokeFilter(new MsgRevokeFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getAttachment() != null
                        && (message.getAttachment() instanceof AVChatAttachment
                        || message.getAttachment() instanceof RTSAttachment
                        || message.getAttachment() instanceof RedPacketAttachment)) {
                    // 视频通话消息和白板消息，红包消息 不允许撤回
                    return true;
                } else if (DemoCache.getAccount().equals(message.getSessionId())) {
                    // 发给我的电脑 不允许撤回
                    return true;
                }
                return false;
            }
        });
    }

    private static void registerMsgRevokeObserver() {
        NIMClient.getService(MsgServiceObserve.class).observeRevokeMessage(new NimMessageRevokeObserver(), true);
    }


    private static void initPopuptWindow(Context context, View view, String sessionId, SessionTypeEnum sessionTypeEnum) {
        if (popupMenu == null) {
            menuItemList = new ArrayList<>();
            popupMenu = new NIMPopupMenu(context, menuItemList, listener);
        }
        menuItemList.clear();
        menuItemList.addAll(getMoreMenuItems(context, sessionId, sessionTypeEnum));
        popupMenu.notifyData();
        popupMenu.show(view);
    }

    private static NIMPopupMenu.MenuItemClickListener listener = new NIMPopupMenu.MenuItemClickListener() {
        @Override
        public void onItemClick(final PopupMenuItem item) {
            switch (item.getTag()) {
                case ACTION_HISTORY_QUERY:
                    MessageHistoryActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum()); // 漫游消息查询
                    break;
                case ACTION_SEARCH_MESSAGE:
                    SearchMessageActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum());
                    break;
                case ACTION_CLEAR_MESSAGE:
                    EasyAlertDialogHelper.createOkCancelDiolag(item.getContext(), null, "确定要清空吗？", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            NIMClient.getService(MsgService.class).clearChattingHistory(item.getSessionId(), item.getSessionTypeEnum());
                            MessageListPanelHelper.getInstance().notifyClearMessages(item.getSessionId());
                        }
                    }).show();
                    break;
            }
        }
    };

    private static List<PopupMenuItem> getMoreMenuItems(Context context, String sessionId, SessionTypeEnum sessionTypeEnum) {
        List<PopupMenuItem> moreMenuItems = new ArrayList<PopupMenuItem>();
        moreMenuItems.add(new PopupMenuItem(context, ACTION_HISTORY_QUERY, sessionId,
                sessionTypeEnum, DemoCache.getContext().getString(R.string.message_history_query)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_SEARCH_MESSAGE, sessionId,
                sessionTypeEnum, DemoCache.getContext().getString(R.string.message_search_title)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_CLEAR_MESSAGE, sessionId,
                sessionTypeEnum, DemoCache.getContext().getString(R.string.message_clear)));
        return moreMenuItems;
    }
}
