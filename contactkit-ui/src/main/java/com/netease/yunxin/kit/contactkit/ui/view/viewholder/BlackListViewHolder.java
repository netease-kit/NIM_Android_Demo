/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.contactkit.ui.databinding.BlackListViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactBlackListBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

public class BlackListViewHolder extends BaseContactViewHolder {

    private RelieveListener relieveListener;

    private BlackListViewHolderBinding binding;


    public BlackListViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
    }

    @Override
    public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
        binding = BlackListViewHolderBinding.inflate(layoutInflater, container, true);
    }

    @Override
    public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
        UserInfo friendData = ((ContactBlackListBean) bean).data;
        binding.tvName.setText(friendData.getName());
        binding.tvName.setTextColor(attrs.getNameTextColor());

        binding.avatarView.setData(friendData.getAvatar(),friendData.getName(), ColorUtils.avatarColor(friendData.getAccount()));

        binding.tvRelieve.setOnClickListener(v -> {
            if (relieveListener != null) {
                relieveListener.onUserRelieve((ContactBlackListBean) bean);
            }
        });
    }

    public void setRelieveListener(RelieveListener relieveListener) {
        this.relieveListener = relieveListener;
    }

    public interface RelieveListener {
        void onUserRelieve(ContactBlackListBean data);
    }
}
