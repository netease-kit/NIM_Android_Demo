package com.netease.nim.uikit.business.session.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nim.uikit.business.session.module.list.MsgAdapter;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MsgThreadOption;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话窗口消息列表项的ViewHolder基类，负责每个消息项的外层框架，包括头像，昵称，发送/接收进度条，重发按钮等。<br>
 * 具体的消息展示项可继承该基类，然后完成具体消息内容展示即可。
 */
public abstract class MsgViewHolderBase extends RecyclerViewHolder<BaseMultiItemFetchLoadAdapter, BaseViewHolder, IMMessage> {

    public MsgViewHolderBase(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
        this.adapter = adapter;
    }

    // basic
    protected View view;
    protected Context context;
    protected BaseMultiItemFetchLoadAdapter adapter;
    protected int layoutPosition;

    // data
    protected IMMessage message;

    // view
    protected View alertButton;
    protected TextView timeTextView;
    protected ProgressBar progressBar;
    protected TextView nameTextView;
    protected FrameLayout contentContainer;
    protected LinearLayout contentContainerWithReplyTip; //包含回复提示的内容部分
    protected TextView replyTipAboveMsg; //消息列表中，显示在消息体上方的回复提示
    protected LinearLayout nameContainer;
    protected TextView readReceiptTextView;
    protected TextView ackMsgTextView;
    protected TextView replyTipTextView; //回复消息时，显示在回复框上方的回复提示
    protected ImageView pinTipImg;

    private HeadImageView avatarLeft;
    private HeadImageView avatarRight;

    /** 合并转发用多选框 */
    private CheckBox multiCheckBox;

    public ImageView nameIconView;

    // contentContainerView的默认长按事件。如果子类需要不同的处理，可覆盖onItemLongClick方法
    // 但如果某些子控件会拦截触摸消息，导致contentContainer收不到长按事件，子控件也可在inflate时重新设置
    protected View.OnLongClickListener longClickListener;

    /// -- 以下接口可由子类覆盖或实现
    // 返回具体消息类型内容展示区域的layout res id
    abstract public int getContentResId();

    // 在该接口中根据layout对各控件成员变量赋值
    abstract public void inflateContentView();

    // 在该接口操作BaseViewHolder中的数据，进行事件绑定，可选
    protected void bindHolder(BaseViewHolder holder) {

    }

    // 将消息数据项与内容的view进行绑定
    abstract public void bindContentView();

    // 内容区域点击事件响应处理。
    public void onItemClick() {
    }

    // 内容区域长按事件响应处理。该接口的优先级比adapter中有长按事件的处理监听高，当该接口返回为true时，adapter的长按事件监听不会被调用到。
    protected boolean onItemLongClick() {
        return false;
    }

    // 当是接收到的消息时，内容区域背景的drawable id
    protected int leftBackground() {
        return NimUIKitImpl.getOptions().messageLeftBackground;
    }

    // 当是发送出去的消息时，内容区域背景的drawable id
    protected int rightBackground() {
        return NimUIKitImpl.getOptions().messageRightBackground;
    }

    // 返回该消息是不是居中显示
    protected boolean isMiddleItem() {
        return false;
    }

    //为Thread消息的设置回复提示语
    protected void setBeRepliedTip() {
        int count = 0;
        if (message.isThread()) {
            count = NIMClient.getService(MsgService.class).queryReplyCountInThreadTalkBlock(message);
        }
        if (count <= 0) {
            replyTipTextView.setVisibility(View.GONE);
            return;
        }
        replyTipTextView.setText(String.format(context.getResources().getString(R.string.reply_with_amount), String.valueOf(count)));
        replyTipTextView.setVisibility(View.VISIBLE);
    }

    protected void setReplyTip() {
        if (message.isThread()) {
            replyTipAboveMsg.setVisibility(View.GONE);
            return;
        }
        replyTipAboveMsg.setText(getReplyTip());
        replyTipAboveMsg.setVisibility(View.VISIBLE);
    }

    protected String getReplyTip() {
        //thread消息没有回复对象
        if (message.isThread()) {
           return "";
        }
        MsgThreadOption threadOption = message.getThreadOption();
        String replyFrom = threadOption.getReplyMsgFromAccount();
        if (TextUtils.isEmpty(replyFrom)) {
            NimLog.w("MsgViewHolderBase", "no reply message found, uuid=" + message.getUuid());
            return "";
        }
        String fromDisplayName = UserInfoHelper.getUserDisplayNameInSession(replyFrom, message.getSessionType(), message.getSessionId());

        String replyUuid = threadOption.getReplyMsgIdClient();
        String content = getMessageBrief(replyUuid, "...");

        return String.format(context.getString(R.string.reply_with_message), fromDisplayName, content);
    }

    protected String getMessageBrief(String uuid, String defaultValue) {
        if (TextUtils.isEmpty(uuid)) {
            return defaultValue;
        }
        List<String> uuidList = new ArrayList<>(1);
        uuidList.add(uuid);
        List<IMMessage> msgList = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuidList);
        if (msgList == null || msgList.isEmpty()) {
            return defaultValue;
        }
        IMMessage msg = msgList.get(0);
        SessionCustomization sessionCustomization = SessionTypeEnum.P2P == msg.getSessionType() ?
                NimUIKit.getCommonP2PSessionCustomization() : NimUIKit.getCommonTeamSessionCustomization();
        return sessionCustomization.getMessageDigest(msg);
    }

    // 是否显示头像，默认为显示
    protected boolean isShowHeadImage() {
        return true;
    }

    // 是否显示气泡背景，默认为显示
    protected boolean isShowBubble() {
        return true;
    }

    // 是否显示已读，默认为显示
    protected boolean shouldDisplayReceipt() {
        return true;
    }

    /// -- 以下接口可由子类调用
    protected final MsgAdapter getMsgAdapter() {
        return (MsgAdapter) adapter;
    }

    protected boolean shouldDisplayNick() {
        return message.getSessionType() == SessionTypeEnum.Team && isReceivedMessage() && !isMiddleItem();
    }


    /**
     * 下载附件/缩略图
     */
    protected void downloadAttachment(RequestCallback<Void> callback) {
        if (message.getAttachment() != null && message.getAttachment() instanceof FileAttachment)
            NIMClient.getService(MsgService.class).downloadAttachment(message, true).setCallback(callback);
    }

    // 设置FrameLayout子控件的gravity参数
    protected final void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
        view.setLayoutParams(params);
    }

    // 设置控件的长宽
    protected void setLayoutParams(int width, int height, View... views) {
        for (View view : views) {
            ViewGroup.LayoutParams maskParams = view.getLayoutParams();
            maskParams.width = width;
            maskParams.height = height;
            view.setLayoutParams(maskParams);
        }
    }

    // 根据layout id查找对应的控件
    protected <T extends View> T findViewById(int id) {
        return (T) view.findViewById(id);
    }

    // 判断消息方向，是否是接收到的消息
    protected boolean isReceivedMessage() {
        return message.getDirect() == MsgDirectionEnum.In;
    }

    /// -- 以下是基类实现代码
    @Override
    public void convert(BaseViewHolder holder, IMMessage data, int position, boolean isScrolling) {
        view = holder.getConvertView();
        context = holder.getContext();
        message = data;
        layoutPosition = holder.getLayoutPosition();

        inflate();
        refresh();
        bindHolder(holder);
    }

    public void initParameter(View itemView, Context context, IMMessage data, int position) {
        view = itemView;
        this.context = context;
        message = data;
        layoutPosition = position;


        timeTextView = new TextView(context);
        avatarLeft = new HeadImageView(context);
        avatarRight = new HeadImageView(context);
        multiCheckBox = new CheckBox(context);
        alertButton = new View(context);
        progressBar = new ProgressBar(context);
        nameTextView = new TextView(context);
        contentContainer = new FrameLayout(context);
        contentContainerWithReplyTip = new LinearLayout(context);
        replyTipAboveMsg = new TextView(context);
        nameIconView = new ImageView(context);
        nameContainer = new LinearLayout(context);
        readReceiptTextView = new TextView(context);
        ackMsgTextView = new TextView(context);
    }

    protected final void inflate() {
        timeTextView = findViewById(R.id.message_item_time);
        avatarLeft = findViewById(R.id.message_item_portrait_left);
        avatarRight = findViewById(R.id.message_item_portrait_right);
        multiCheckBox = findViewById(R.id.message_item_multi_check_box);
        alertButton = findViewById(R.id.message_item_alert);
        progressBar = findViewById(R.id.message_item_progress);
        nameTextView = findViewById(R.id.message_item_nickname);
        contentContainer = findViewById(R.id.message_item_content);
        contentContainerWithReplyTip = findViewById(R.id.message_item_container_with_reply_tip);
        replyTipAboveMsg = findViewById(R.id.tv_reply_tip_above_msg);
        nameIconView = findViewById(R.id.message_item_name_icon);
        nameContainer = findViewById(R.id.message_item_name_layout);
        readReceiptTextView = findViewById(R.id.textViewAlreadyRead);
        ackMsgTextView = findViewById(R.id.team_ack_msg);
        pinTipImg = findViewById(R.id.message_item_pin);
        replyTipTextView = findViewById(R.id.message_item_reply);

        // 这里只要inflate出来后加入一次即可
        if (contentContainer.getChildCount() == 0) {
            View.inflate(view.getContext(), getContentResId(), contentContainer);
        }
        inflateContentView();
    }

    protected final void refresh() {
        //如果是avchat类消息，先根据附件的from字段重置消息的方向和发送者ID
        MessageHelper.adjustAVChatMsgDirect(message);
        setHeadImageView();
        setNameTextView();
        setTimeTextView();
        setStatus();
        setOnClickListener();
        setLongClickListener();
        setContent();
//        setExtension();
        setReadReceipt();
        setAckMsg();
        setMultiCheckBox();
        bindContentView();
    }

    public void refreshCurrentItem() {
        if (message != null) {
            refresh();
        }
    }

    /**
     * 设置时间显示
     */
    private void setTimeTextView() {
        if (getMsgAdapter().needShowTime(message)) {
            timeTextView.setVisibility(View.VISIBLE);
        } else {
            timeTextView.setVisibility(View.GONE);
            return;
        }

        String text = TimeUtil.getTimeShowString(message.getTime(), false);
        timeTextView.setText(text);
    }

    /**
     * 设置消息发送状态
     */
    private void setStatus() {
        MsgStatusEnum status = message.getStatus();
        switch (status) {
            case fail:
                progressBar.setVisibility(View.GONE);
                alertButton.setVisibility(View.VISIBLE);
                break;
            case sending:
                progressBar.setVisibility(View.VISIBLE);
                alertButton.setVisibility(View.GONE);
                break;
            default:
                progressBar.setVisibility(View.GONE);
                alertButton.setVisibility(View.GONE);
                break;
        }
    }

    private void setHeadImageView() {
        HeadImageView show = isReceivedMessage() ? avatarLeft : avatarRight;
        HeadImageView hide = isReceivedMessage() ? avatarRight : avatarLeft;
        hide.setVisibility(View.GONE);
        if (!isShowHeadImage()) {
            show.setVisibility(View.GONE);
            return;
        }
        if (isMiddleItem()) {
            show.setVisibility(View.GONE);
        } else {
            show.setVisibility(View.VISIBLE);
            show.loadBuddyAvatar(message);
        }

    }

    private void setOnClickListener() {
        //消息是否处于可被选择状态，true: 点击只能改变被选择状态; false: 点击可执行消息的点击事件
        boolean inNormalMode = message.isChecked() == null;
        multiCheckBox.setOnClickListener((v) -> getMsgAdapter().getEventListener().onCheckStateChanged(layoutPosition, multiCheckBox.isChecked()));
        if (!inNormalMode) {
            alertButton.setClickable(false);
            contentContainer.setClickable(false);
            avatarLeft.setClickable(false);
            avatarRight.setClickable(false);
            ackMsgTextView.setClickable(false);
            return;
        }
        // 重发/重收按钮响应事件
        if (getMsgAdapter().getEventListener() != null) {
            alertButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getMsgAdapter().getEventListener().onFailedBtnClick(message);
                }
            });
        }

        // 内容区域点击事件响应， 相当于点击了整项
        contentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick();
            }
        });


        // 头像点击事件响应
        if (NimUIKitImpl.getSessionListener() != null) {
            View.OnClickListener portraitListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NimUIKitImpl.getSessionListener().onAvatarClicked(context, message);
                }
            };
            avatarLeft.setOnClickListener(portraitListener);
            avatarRight.setOnClickListener(portraitListener);
        }
        // 已读回执响应事件
        if (NimUIKitImpl.getSessionListener() != null) {
            ackMsgTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NimUIKitImpl.getSessionListener().onAckMsgClicked(context, message);
                }
            });
        }
    }

    /**
     * item长按事件监听
     */
    private void setLongClickListener() {
        longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 优先派发给自己处理，
                if (!onItemLongClick()) {
                    if (getMsgAdapter().getEventListener() != null) {
                        getMsgAdapter().getEventListener().onViewHolderLongClick(contentContainer, view, message);
                        return true;
                    }
                }
                return false;
            }
        };
        // 消息长按事件响应处理
        contentContainer.setOnLongClickListener(longClickListener);

        // 头像长按事件响应处理
        if (NimUIKitImpl.getSessionListener() != null) {
            View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    NimUIKitImpl.getSessionListener().onAvatarLongClicked(context, message);
                    return true;
                }
            };
            avatarLeft.setOnLongClickListener(longClickListener);
            avatarRight.setOnLongClickListener(longClickListener);
        }
    }

    private void setNameTextView() {
        if (!shouldDisplayNick()) {
            nameTextView.setVisibility(View.GONE);
            return;
        }
        nameTextView.setVisibility(View.VISIBLE);
        nameTextView.setText(getNameText());
    }


    protected String getNameText() {
        if (message.getSessionType() == SessionTypeEnum.Team) {
            return TeamHelper.getTeamMemberDisplayName(message.getSessionId(), message.getFromAccount());
        }
        return "";
    }

    private void setContent() {
        if (!isShowBubble() && !isMiddleItem()) {
            return;
        }

        LinearLayout bodyContainer = (LinearLayout) view.findViewById(R.id.message_item_body);

        // 调整container的位置
        int index = isReceivedMessage() ? 0 : 4;
        if (bodyContainer.getChildAt(index) != contentContainerWithReplyTip) {
            bodyContainer.removeView(contentContainerWithReplyTip);
            bodyContainer.addView(contentContainerWithReplyTip, index);
        }

        if (isMiddleItem()) {
            setGravity(bodyContainer, Gravity.CENTER);
        } else {
            if (isReceivedMessage()) {
                setGravity(bodyContainer, Gravity.LEFT);
                contentContainerWithReplyTip.setBackgroundResource(leftBackground());
                replyTipAboveMsg.setTextColor(Color.BLACK);
            } else {
                setGravity(bodyContainer, Gravity.RIGHT);
                contentContainerWithReplyTip.setBackgroundResource(rightBackground());
                replyTipAboveMsg.setTextColor(Color.WHITE);
            }
        }
    }

    private void setExtension() {
        if (!isShowBubble() && !isMiddleItem()) {
            return;
        }

        LinearLayout extensionContainer = view.findViewById(R.id.message_item_extension);

        // 调整扩展功能提示的位置
        int index = isReceivedMessage() ? 0 : 1;
        if (extensionContainer.getChildAt(index) != pinTipImg) {
            extensionContainer.removeView(pinTipImg);
            extensionContainer.addView(pinTipImg, index);
        }

        if (isMiddleItem()) {
            return;
        }
        setGravity(extensionContainer, isReceivedMessage() ? Gravity.LEFT : Gravity.RIGHT);
        setBeRepliedTip();
        setReplyTip();
    }

    private void setReadReceipt() {
        if (shouldDisplayReceipt() && !TextUtils.isEmpty(getMsgAdapter().getUuid()) && message.getUuid().equals(getMsgAdapter().getUuid())) {
            readReceiptTextView.setVisibility(View.VISIBLE);
        } else {
            readReceiptTextView.setVisibility(View.GONE);
        }
    }

    private void setAckMsg() {
        if (message.getSessionType() == SessionTypeEnum.Team && message.needMsgAck()) {
            if (isReceivedMessage()) {
                // 收到的需要已读回执的消息，需要给个反馈
                ackMsgTextView.setVisibility(View.GONE);
                NIMSDK.getTeamService().sendTeamMessageReceipt(message);
            } else {
                // 自己发的需要已读回执的消息，显示未读人数
                ackMsgTextView.setVisibility(View.VISIBLE);
                if (message.getTeamMsgAckCount() == 0 && message.getTeamMsgUnAckCount() == 0) {
                    ackMsgTextView.setText("还未查看");
                } else {
                    ackMsgTextView.setText(message.getTeamMsgUnAckCount() + "人未读");
                }
            }
        } else {
            ackMsgTextView.setVisibility(View.GONE);
        }
    }

    private void setMultiCheckBox() {
        Boolean selectState = message.isChecked();
        multiCheckBox.setVisibility(selectState == null ? View.GONE : View.VISIBLE);
        if (Boolean.TRUE.equals(selectState)) {
            multiCheckBox.setChecked(true);
        } else if (Boolean.FALSE.equals(selectState)) {
            multiCheckBox.setChecked(false);
        }
    }
}
