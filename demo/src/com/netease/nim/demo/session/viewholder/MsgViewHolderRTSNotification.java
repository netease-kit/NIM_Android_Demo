package com.netease.nim.demo.session.viewholder;

import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;

import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderNotification;

public class MsgViewHolderRTSNotification extends MsgViewHolderNotification {

    @Override
    protected void bindContentView() {
        RTSAttachment attachment = (RTSAttachment) message.getAttachment();

        MoonUtil.identifyFaceExpressionAndATags(context, notificationTextView, attachment.getContent(), ImageSpan.ALIGN_BOTTOM);
        notificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

