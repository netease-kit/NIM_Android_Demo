package com.netease.yunxin.kit.qchatkit.ui.message.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMemberProfileLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.utils.ColorUtils;

import java.util.List;

/**
 * Dialog to show member profile in channel message
 */
public class MemberProfileDialog extends BaseBottomDialog {

    private QChatMemberProfileLayoutBinding viewBinding;
    private QChatServerMemberInfo memberInfo;
    private List<QChatServerRoleInfo> roleInfoList;

    @Nullable
    @Override
    protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        viewBinding = QChatMemberProfileLayoutBinding.inflate(inflater,container,false);
        return viewBinding.getRoot();
    }

    public void setData(QChatServerMemberInfo member, List<QChatServerRoleInfo> roleInfoList){
        memberInfo = member;
        this.roleInfoList = roleInfoList;
    }

    @Override
    protected void initData() {
        super.initData();
        loadData();
    }

    public void updateData(List<QChatServerRoleInfo> roleInfoList){
        this.roleInfoList = roleInfoList;
        loadData();
    }

    private void loadData(){
        if (!TextUtils.isEmpty(memberInfo.getNick())){
            viewBinding.qChatMemberProfileName.setText(memberInfo.getNick());
            viewBinding.qChatMemberProfileNick.setVisibility(View.VISIBLE);
            viewBinding.qChatMemberProfileNick.setText(memberInfo.getNicknameOfIM());
        }else {
            viewBinding.qChatMemberProfileName.setText(memberInfo.getNicknameOfIM());
            viewBinding.qChatMemberProfileNick.setVisibility(View.GONE);
        }

        viewBinding.qChatMemberProfileAvatar.setData(memberInfo.getAvatarUrl(), memberInfo.getNickName(), ColorUtils.avatarColor(memberInfo.getAccId()));
        if (roleInfoList == null || roleInfoList.size() < 1){
            viewBinding.qChatMemberProfileNull.setVisibility(View.VISIBLE);
        }else {
            viewBinding.qChatMemberProfileNull.setVisibility(View.GONE);
            viewBinding.qChatMemberProfileFlGroup.setData(roleInfoList);
        }
    }
}
