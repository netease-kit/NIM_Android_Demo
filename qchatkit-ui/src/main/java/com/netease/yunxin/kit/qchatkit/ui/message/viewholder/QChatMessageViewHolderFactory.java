package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import static com.netease.yunxin.kit.qchatkit.ui.message.utils.QChatMessageConstant.MESSAGE_USER_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.qchatkit.ui.message.utils.QChatMessageConstant.MESSAGE_USER_VIEW_TYPE_TEXT;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class QChatMessageViewHolderFactory {
    public QChatBaseMessageViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {

        QChatBaseMessageViewHolder viewHolder = null;
        if (viewType == MESSAGE_USER_VIEW_TYPE_TEXT) {
            viewHolder = new QChatTextMessageViewHolder(parent);
        } else if (viewType == MESSAGE_USER_VIEW_TYPE_IMAGE) {
            viewHolder = new QChatImageMessageViewHolder(parent);
        }

        return viewHolder;
    }
}
