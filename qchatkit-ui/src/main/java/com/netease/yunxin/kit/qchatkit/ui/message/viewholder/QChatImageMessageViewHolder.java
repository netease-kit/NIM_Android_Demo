package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.common.WatchImageActivity;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatImageMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.utils.ScreenUtil;

public class QChatImageMessageViewHolder extends QChatBaseMessageViewHolder {

    private QChatImageMessageViewHolderBinding imageBinding;

    public QChatImageMessageViewHolder(@NonNull ViewGroup parent) {
        super(parent);
    }

    @Override
    public void addContainer() {
        imageBinding = QChatImageMessageViewHolderBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(), true);
    }

    @Override
    public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
        super.bindData(data, lastMessage);
        String imageAttachStr = data.getAttachStr();
        ImageAttachment imageAttachment = new ImageAttachment(imageAttachStr);
        if (TextUtils.isEmpty(imageAttachment.getPath()) && TextUtils.isEmpty(imageAttachment.getUrl())){
            return;
        }
        data.setAttachment(imageAttachment);
        String path = imageAttachment.getPath();
        if (TextUtils.isEmpty(path)) {
            path = imageAttachment.getUrl();
        }
        Glide.with(itemView.getContext()).
                load(path).override(getImageMaxEdge(),getImageMaxEdge()).into(imageBinding.messageImage);
        itemView.setOnClickListener( view ->{
            WatchImageActivity.start(itemView.getContext(),data);
        });
    }

    private int getImageMaxEdge() {
        return (int) (165.0 / 320.0 * ScreenUtil.screenWidth);
    }

}
