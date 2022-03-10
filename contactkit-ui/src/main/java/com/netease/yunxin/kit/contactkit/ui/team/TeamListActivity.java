/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.team;


import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.TeamListViewHolder;

public class TeamListActivity extends BaseListActivity {

    private TeamListViewModel viewModel;

    @Override
    protected void initView() {
        binding.title.setTitle(R.string.my_team);
        binding.contactListView.setViewHolderFactory(new ContactViewHolderFactory() {
            @Override
            protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
                if (viewType == IViewTypeConstant.CONTACT_TEAM_LIST) {
                    TeamListViewHolder viewHolder = new TeamListViewHolder(view);
                    viewHolder.setItemClickListener(data -> {
                        //todo 跳转到群聊
                    });
                    return viewHolder;
                }
                return null;
            }
        });
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(TeamListViewModel.class);
        viewModel.getFetchResult().observe(this, result -> {
            if(result.getLoadStatus() == LoadStatus.Success){
                binding.contactListView.addContactData(result.getData());
            }else if (result.getLoadStatus() == LoadStatus.Finish){
                if (result.getType() == FetchResult.FetchType.Add){
                    binding.contactListView.addContactData(result.getData());
                }else if (result.getType() == FetchResult.FetchType.Remove){
                    binding.contactListView.addContactData(result.getData());
                }
            }
        });
        viewModel.fetchTeamList();
    }
}
