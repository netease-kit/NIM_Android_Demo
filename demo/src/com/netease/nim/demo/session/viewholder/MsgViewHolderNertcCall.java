package com.netease.nim.demo.session.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.yunxin.nertc.nertcvideocall.utils.NrtcCallStatus;

/**
 * Created by zhoujianghua on 2015/8/6.
 */
public class MsgViewHolderNertcCall extends MsgViewHolderBase {

    private ImageView typeImage;
    private TextView statusLabel;

    public MsgViewHolderNertcCall(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    public int getContentResId() {
        return R.layout.nim_message_item_avchat;
    }

    @Override
    public void inflateContentView() {
        typeImage = findViewById(R.id.message_item_avchat_type_img);
        statusLabel = findViewById(R.id.message_item_avchat_state);
    }

    @Override
    public void bindContentView() {
        if (message.getAttachment() == null) {
            return;
        }

        layoutByDirection();

        refreshContent();
    }

    private void layoutByDirection() {
        NetCallAttachment attachment = (NetCallAttachment) message.getAttachment();

        if (isReceivedMessage()) {
            if (attachment.getType() == ChannelType.AUDIO.getValue()) {
                typeImage.setImageResource(R.drawable.avchat_left_type_audio);
            } else {
                typeImage.setImageResource(R.drawable.avchat_left_type_video);
            }
            statusLabel.setTextColor(context.getResources().getColor(R.color.color_grey_999999));
        } else {
            if (attachment.getType() == ChannelType.AUDIO.getValue()) {
                typeImage.setImageResource(R.drawable.avchat_right_type_audio);
            } else {
                typeImage.setImageResource(R.drawable.avchat_right_type_video);
            }
            statusLabel.setTextColor(Color.WHITE);
        }
    }

    private void refreshContent() {
        NetCallAttachment attachment = (NetCallAttachment) message.getAttachment();

        String textString = "";
        switch (attachment.getStatus()) {
            case NrtcCallStatus.NrtcCallStatusComplete: //成功接听
                if (attachment.getDurations() == null) {
                    break;
                }

                for (NetCallAttachment.Duration duration : attachment.getDurations()) {
                    if (TextUtils.equals(duration.getAccid(), DemoCache.getAccount())) {
                        textString = TimeUtil.secToTime(duration.getDuration());
                    }
                }

                break;
            case NrtcCallStatus.NrtcCallStatusCanceled:
                textString = context.getString(R.string.avchat_cancel);
                break;

            case NrtcCallStatus.NrtcCallStatusRejected: { // "被拨打方" 拒绝接听电话或者正忙
                int strID = message.getDirect() == MsgDirectionEnum.In ? R.string.avchat_has_reject : R.string.avchat_be_rejected;
                textString = context.getString(strID);
                break;
            }
            case NrtcCallStatus.NrtcCallStatusTimeout: //未接听
                textString = context.getString(R.string.avchat_no_pick_up);
                break;
            case NrtcCallStatus.NrtcCallStatusBusy: { //对方正忙
                int strID = message.getDirect() == MsgDirectionEnum.In ? R.string.avchat_is_busy_self : R.string.avchat_is_busy_opposite;
                textString = context.getString(strID);
                break;
            }

            default:
                break;
        }

        statusLabel.setText(textString);
    }
}
