package com.netease.nim.demo.session.viewholder;

import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;

/**
 * Created by zhoujianghua on 2015/8/6.
 */
public class MsgViewHolderAVChat extends MsgViewHolderBase {

    private ImageView typeImage;
    private TextView statusLabel;

    public MsgViewHolderAVChat(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_avchat;
    }

    @Override
    protected void inflateContentView() {
        typeImage = findViewById(R.id.message_item_avchat_type_img);
        statusLabel = findViewById(R.id.message_item_avchat_state);
    }

    @Override
    protected void bindContentView() {
        if (message.getAttachment() == null) {
            return;
        }

        layoutByDirection();

        refreshContent();
    }

    private void layoutByDirection() {
        AVChatAttachment attachment = (AVChatAttachment) message.getAttachment();

        if (isReceivedMessage()) {
            if (attachment.getType() == AVChatType.AUDIO) {
                typeImage.setImageResource(R.drawable.avchat_left_type_audio);
            } else {
                typeImage.setImageResource(R.drawable.avchat_left_type_video);
            }
            statusLabel.setTextColor(context.getResources().getColor(R.color.color_grey_999999));
        } else {
            if (attachment.getType() == AVChatType.AUDIO) {
                typeImage.setImageResource(R.drawable.avchat_right_type_audio);
            } else {
                typeImage.setImageResource(R.drawable.avchat_right_type_video);
            }
            statusLabel.setTextColor(Color.WHITE);
        }
    }

    private void refreshContent() {
        AVChatAttachment attachment = (AVChatAttachment) message.getAttachment();

        String textString = "";
        switch (attachment.getState()) {
            case Success: //成功接听
                textString = TimeUtil.secToTime(attachment.getDuration());
                break;
            case Missed: //未接听
                textString = context.getString(R.string.avchat_no_pick_up);
                break;
            case Rejected: // "被拨打方" 拒绝接听电话或者正忙
                int strID = message.getDirect() == MsgDirectionEnum.In ? R.string.avchat_has_reject : R.string.avchat_be_rejected;
                textString = context.getString(strID);
                break;
            case Canceled:
                textString = context.getString(R.string.avchat_cancel);
            default:
                break;
        }

        statusLabel.setText(textString);
    }
}
