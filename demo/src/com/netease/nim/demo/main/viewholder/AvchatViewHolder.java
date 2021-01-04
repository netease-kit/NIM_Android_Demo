package com.netease.nim.demo.main.viewholder;

import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.R;
import com.netease.nim.demo.main.helper.MessageHelper;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

public class AvchatViewHolder extends TViewHolder {

    private HeadImageView imgHead;

    private TextView lblNickname;

    private TextView lblMessage;

    private TextView lblDatetime;

    private IMMessage message;

    private View bottomLine;

    private View topLine;

    public void refresh(Object item) {
        message = (IMMessage) item;
        updateBackground();
        loadPortrait();
        updateNickLabel(MessageHelper.getName(message.getFromAccount(), message.getSessionType()));
        updateMsgLabel();
    }

    private void updateBackground() {
        topLine.setVisibility(isFirstItem() ? View.GONE : View.VISIBLE);
        bottomLine.setVisibility(isLastItem() ? View.VISIBLE : View.GONE);
        view.setBackgroundResource(R.drawable.nim_list_item_bg_selecter);
    }

    protected void loadPortrait() {
        // 设置头像
        if (message.getSessionType() == SessionTypeEnum.P2P) {
            imgHead.loadBuddyAvatar(message.getFromAccount());
        } else if (message.getSessionType() == SessionTypeEnum.Team) {
            imgHead.setImageResource(R.drawable.nim_avatar_group);
        } else if (message.getSessionType() == SessionTypeEnum.Team) {
            imgHead.setImageResource(R.drawable.nim_avatar_group);
        }
    }

    private void updateMsgLabel() {
        lblMessage.setText(getContent());
        String timeString = TimeUtil.getTimeShowString(message.getTime(), true);
        lblDatetime.setText(timeString);
    }


    private String getContent() {
        AVChatAttachment avChatAttachment = (AVChatAttachment) message.getAttachment();
        JSONObject node = new JSONObject();
        node.put("聊天对象ID", message.getSessionId());
        node.put("Nick", message.getFromNick());
        node.put("Time", message.getTime());
        node.put("SessionType", message.getSessionType());
        node.put("MsgType", message.getMsgType());
        node.put("state:", avChatAttachment.getState());
        node.put("duration:", avChatAttachment.getDuration());
        node.put("type:", avChatAttachment.getType());
        return node.toJSONString();
    }

    protected void updateNickLabel(String nick) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 70); // 减去固定的头像和时间宽度
        if (labelWidth > 0) {
            lblNickname.setMaxWidth(labelWidth);
        }
        lblNickname.setText(nick);
    }

    protected int getResId() {
        return R.layout.item_avchat_view_holder;
    }

    public void inflate() {
        this.imgHead = view.findViewById(R.id.img_head);
        this.lblNickname = view.findViewById(R.id.tv_nick_name);
        this.lblMessage = view.findViewById(R.id.tv_message);
        this.lblDatetime = view.findViewById(R.id.tv_date_time);
        this.topLine = view.findViewById(R.id.top_line);
        this.bottomLine = view.findViewById(R.id.bottom_line);
    }
}
