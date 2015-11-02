package com.netease.nim.demo.session.viewholder;

import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.demo.session.extension.CustomNotificationAttachment;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;

/**
 * Created by huangjun on 2015/10/27.
 * 自定义消息（通知示例）对应的ViewHolder
 */
public class MsgViewHolderCustomNotification extends MsgViewHolderBase {

    protected TextView notificationTextView;

    @Override
    protected int getContentResId() {
        return com.netease.nim.uikit.R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = (TextView) view.findViewById(com.netease.nim.uikit.R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        CustomNotificationAttachment attachment = (CustomNotificationAttachment) message.getAttachment();
        handleTextNotification(attachment.getNotificationText());
        timeTextView.setVisibility(View.GONE);
    }

    private void handleTextNotification(String text) {
        MoonUtil.identifyFaceExpressionAndATags(context, notificationTextView, text, ImageSpan.ALIGN_BOTTOM);
        notificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}
