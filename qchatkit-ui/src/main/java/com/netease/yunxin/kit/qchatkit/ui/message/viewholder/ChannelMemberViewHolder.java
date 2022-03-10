package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberStatusViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.model.ChannelMemberStatusBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.utils.ColorUtils;

public class ChannelMemberViewHolder extends CommonViewHolder<QChatBaseBean> {

    private QChatChannelMemberStatusViewHolderBinding binding;

    public ChannelMemberViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public ChannelMemberViewHolder(QChatChannelMemberStatusViewHolderBinding viewBinding) {
        this(viewBinding.getRoot());
        binding = viewBinding;
        binding.getRoot().setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onClick(this.data, this.position);
            }
        });
    }

    @Override
    protected void onBindData(QChatBaseBean data, int position) {
        this.data = data;
        this.position = position;
        if (data instanceof ChannelMemberStatusBean) {
            ChannelMemberStatusBean bean = (ChannelMemberStatusBean) data;
            binding.qChatMemberNameTv.setText(bean.channelMember.getNickName());
            binding.qChatMemberAvatarIv.setData(bean.channelMember.getAvatarUrl(), bean.channelMember.getNickName(), ColorUtils.avatarColor(bean.channelMember.getAccId()));
            binding.qChatMemberStatusIv.setVisibility(View.GONE);
            binding.qChatMemberCoverView.setVisibility(View.GONE);
        }
    }
}
