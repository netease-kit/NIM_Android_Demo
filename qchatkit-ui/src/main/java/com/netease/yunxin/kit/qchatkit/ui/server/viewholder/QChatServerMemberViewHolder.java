package com.netease.yunxin.kit.qchatkit.ui.server.viewholder;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.utils.ColorUtils;

public class QChatServerMemberViewHolder extends BaseMoreViewHolder<QChatServerRoleMemberInfo, QChatRoleMemberViewHolderBinding> {

    private DeleteListener deleteListener;

    private SelectListener selectListener;

    private boolean isDelete;

    private boolean isSelect;

    public QChatServerMemberViewHolder(@NonNull QChatRoleMemberViewHolderBinding binding) {
        super(binding);
    }

    @Override
    public void bind(QChatServerRoleMemberInfo item) {
        String nick = TextUtils.isEmpty(item.getNick()) ? item.getImNickname() : item.getNick();
        getBinding().avatar.setData(item.getAvatarUrl(), nick, ColorUtils.avatarColor(item.getAccId()));
        getBinding().tvName.setText(nick);
        if (isDelete) {
            getBinding().ivDelete.setVisibility(View.VISIBLE);
            getBinding().ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.deleteClick(item);
                }
            });
        } else {
            getBinding().ivDelete.setVisibility(View.GONE);
        }
        if (isSelect) {
            getBinding().rbCheck.setVisibility(View.VISIBLE);
            getBinding().getRoot().setOnClickListener(v -> {
                getBinding().rbCheck.setChecked(!getBinding().rbCheck.isChecked());
            });

            getBinding().rbCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (selectListener != null) {
                    selectListener.onSelected(item, isChecked);
                }
            });
        } else {
            getBinding().rbCheck.setVisibility(View.GONE);
        }
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setSelectListener(SelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public interface DeleteListener {
        void deleteClick(QChatServerRoleMemberInfo item);
    }

    public interface SelectListener {
        void onSelected(QChatServerRoleMemberInfo item, boolean selected);
    }
}
