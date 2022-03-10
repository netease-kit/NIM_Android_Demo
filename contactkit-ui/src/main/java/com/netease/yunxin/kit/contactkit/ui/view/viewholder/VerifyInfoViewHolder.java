/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;


import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.VerifyListViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.corekit.im.model.FriendNotify;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfo;


public class VerifyInfoViewHolder extends BaseContactViewHolder {

    private VerifyListener verifyListener;

    private VerifyListViewHolderBinding binding;

    public VerifyInfoViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
    }

    @Override
    public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
        binding = VerifyListViewHolderBinding.inflate(layoutInflater, container, true);
    }

    @Override
    public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
        SystemMessageInfo info = ((ContactVerifyInfoBean) bean).data;
        String name = info.getFromUserInfo() != null ? info.getFromUserInfo().getName():info.getFromAccount();
        String targetName = info.getTargetTeamInfo() != null ? info.getTargetTeamInfo().getName():info.getTargetId();
        String avatar  = info.getTargetTeamInfo() != null ? info.getFromUserInfo().getAvatar():null;
        binding.tvName.setText(name);
        binding.tvName.setTextColor(attrs.getNameTextColor());

        binding.avatarView.setData(avatar, name);

        switch (info.getInfoStatus()) {
            case Init:
                binding.llyVerifyResult.setVisibility(View.GONE);
                binding.llyVerify.setVisibility(View.VISIBLE);
                binding.tvAccept.setOnClickListener(v -> {
                    if (verifyListener != null) {
                        verifyListener.onAccept((ContactVerifyInfoBean) bean);
                    }
                });
                binding.tvReject.setOnClickListener(v -> {
                    if (verifyListener != null) {
                        verifyListener.onReject((ContactVerifyInfoBean) bean);
                    }
                });
                break;
            case Passed:
                showResult(context.getString(R.string.contact_verify_agreed),true);
                break;
            case Declined:
                showResult(context.getString(R.string.contact_verify_rejected),false);
                break;
            case Ignored:
            case Expired:
                showResult(context.getString(R.string.contact_verify_expired),false);
                break;
        }

        switch (info.getInfoType()) {
            case ApplyJoinTeam:
                binding.tvAction.setText(String.format(context.getString(R.string.apply_to_join_group), targetName));
                break;
            case RejectTeamApply:
                binding.tvAction.setText(String.format(context.getString(R.string.apply_to_join_group), targetName));
                showResult(context.getString(R.string.contact_verify_agreed),false);
                break;
            case TeamInvite:
                binding.tvAction.setText(String.format(context.getString(R.string.invited_to_join_group), targetName));
                break;
            case DeclineTeamInvite:
                binding.tvAction.setText(String.format(context.getString(R.string.invited_to_join_group), targetName));
                showResult(context.getString(R.string.contact_verify_agreed),false);
                break;
            case AddFriend:
                if (info.getAttachObject() != null && info.getAttachObject() instanceof FriendNotify){
                    FriendNotify notifyAttach = (FriendNotify)info.getAttachObject();
                    if (notifyAttach.getType() == FriendVerifyType.AgreeAdd){
                        binding.tvAction.setText(R.string.accept_your_friend_apply);
                        showResult(null,true);
                    }else if (notifyAttach.getType() == FriendVerifyType.DirectAdd){
                        binding.tvAction.setText(R.string.direct_add_your_friend_apply);
                        showResult(null,true);
                    }else if (notifyAttach.getType() == FriendVerifyType.RejectAdd){
                        binding.tvAction.setText(R.string.reject_your_friend_apply);
                        showResult(null,true);
                    }else if (notifyAttach.getType() == FriendVerifyType.VerifyRequest){
                        binding.tvAction.setText(R.string.friend_apply);
                    }
                }else {
                    binding.tvAction.setText(R.string.friend_apply);
                }
                break;
            case Undefined:
                break;
        }
    }

    private void showResult(String content,boolean agreeIcon){
        if (!TextUtils.isEmpty(content)) {
            binding.tvVerifyResult.setText(content);
            binding.ivVerifyResult.setImageResource(agreeIcon ? R.mipmap.ic_agree_status : R.mipmap.ic_reject_status);
            binding.llyVerifyResult.setVisibility(View.VISIBLE);
        }else {
            binding.llyVerifyResult.setVisibility(View.GONE);
        }
        binding.llyVerify.setVisibility(View.GONE);
    }

    public void setVerifyListener(VerifyListener verifyListener) {
        this.verifyListener = verifyListener;
    }

    public interface VerifyListener {
        void onAccept(ContactVerifyInfoBean bean);

        void onReject(ContactVerifyInfoBean bean);
    }
}
