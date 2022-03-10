/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.channel;

import android.annotation.SuppressLint;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.observer.ObserverUnreadInfoResultHelper;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.NextInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatUnreadInfoItem;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.adapter.QChatFragmentChannelAdapter;
import com.netease.yunxin.kit.qchatkit.ui.common.LoadMoreRecyclerViewDecorator;
import com.netease.yunxin.kit.qchatkit.ui.common.NetworkUtils;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatFragmentChannelListBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.QChatChannelMessageActivity;
import com.netease.yunxin.kit.qchatkit.ui.server.QChatServerSettingActivity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Showing channel list by paging.
 */
public class QChatChannelListFragment extends BaseFragment {
    private static final int LOAD_MORE_LIMIT = 30;
    private QChatServerInfo serverInfo;

    private QChatFragmentChannelListBinding binding;
    private QChatFragmentChannelAdapter adapter;

    private final NetworkUtils.NetworkStateListener networkStateListener = new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
            if (binding == null){
                return;
            }
            binding.networkTip.getRoot().setVisibility(View.GONE);
        }

        @Override
        public void onLost(NetworkInfo network) {
            if (binding == null){
                return;
            }
            binding.networkTip.getRoot().setVisibility(View.VISIBLE);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = QChatFragmentChannelListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new QChatFragmentChannelAdapter(getContext());
        adapter.setItemClickListener((data, holder) -> QChatChannelMessageActivity.launch(this.getActivity(), data.getServerId(), data.getChannelId(), data.getName(), data.getTopic()));
        binding.ryChannelList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.ryChannelList.setLayoutManager(layoutManager);
        binding.ivMore.setOnClickListener(v -> QChatServerSettingActivity.launch(this.getActivity(), serverInfo));

        binding.ivAddChannel.setOnClickListener(v -> QChatChannelCreateActivity.launch(this.getActivity(), serverInfo.getServerId()));

        LoadMoreRecyclerViewDecorator<QChatChannelInfo> loadMoreRecyclerViewDecorator = new LoadMoreRecyclerViewDecorator<>(binding.ryChannelList, layoutManager, adapter);
        loadMoreRecyclerViewDecorator.setLoadMoreListener(data -> {
            if (serverInfo == null) {
                return;
            }
            NextInfo info;
            if (data == null || data.getNextInfo() == null) {
                info = null;
            } else {
                info = data.getNextInfo();
            }
            if (info != null && !info.getHasMore()) {
                return;
            }
            long timeTag = info != null ? info.getNextTimeTag() : 0;
            if (timeTag == 0) {
                return;
            }
            loadMore(serverInfo.getServerId(), timeTag, false);
        });

        NetworkUtils.registerStateListener(networkStateListener);
    }

    public void updateData(QChatServerInfo serverInfo, Map<Long, QChatUnreadInfoItem> unReadInfo) {
        adapter.updateUnreadCount(unReadInfo);
        binding.groupChannelInfo.setVisibility(View.VISIBLE);
        this.serverInfo = serverInfo;
        binding.tvTitle.setText(serverInfo.getName());
        loadMore(serverInfo.getServerId(), 0, true);
    }

    private void loadMore(long serverId, long timeTag, boolean clear) {
        QChatChannelRepo.fetchChannelsByServerId(serverId, timeTag, LOAD_MORE_LIMIT, new FetchCallback<List<QChatChannelInfo>>() {
            @Override
            public void onSuccess(@Nullable List<QChatChannelInfo> param) {
                if (param != null && !param.isEmpty()) {
                    binding.ryChannelList.setVisibility(View.VISIBLE);
                    binding.groupNoChannelTip.setVisibility(View.GONE);
                    adapter.addDataList(param, clear);
                } else if (adapter.getItemCount() == 0 || clear) {
                    binding.groupNoChannelTip.setVisibility(View.VISIBLE);
                    adapter.addDataList(Collections.emptyList(), true);
                }
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(getContext(), getString(R.string.qchat_server_request_fail) + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                Toast.makeText(getContext(), getString(R.string.qchat_server_request_fail) + exception, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void emptyServerInfo() {
        binding.groupChannelInfo.setVisibility(View.GONE);
        binding.groupNoChannelTip.setVisibility(View.GONE);
        binding.ryChannelList.setVisibility(View.GONE);
    }

    /**
     * refresh channel list.
     */
    public void refreshData(long serverId, Map<Long, QChatUnreadInfoItem> unReadInfo) {
        adapter.updateUnreadCount(unReadInfo);
        if (serverInfo != null && serverInfo.getServerId() == serverId) {
            loadMore(serverInfo.getServerId(), 0, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.unregisterStateListener(networkStateListener);
    }

    /**
     * update unread info
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshUnreadData() {
        if (serverInfo != null) {
            adapter.updateUnreadCount(ObserverUnreadInfoResultHelper.getUnreadInfoMap(serverInfo.getServerId()));
            adapter.notifyDataSetChanged();
        }
    }
}
