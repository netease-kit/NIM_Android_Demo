/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.selector;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactViewModel;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactSelectorActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;

import java.util.ArrayList;

public class ContactSelectorActivity extends BaseActivity {

    public static final int MAX_SELECT_COUNT = 10;

    protected ContactSelectorActivityLayoutBinding binding;

    private ContactViewModel viewModel;

    private SelectedListAdapter selectedListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContactSelectorActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        initView();
        initData();
    }

    protected void initView() {
        binding.title.setOnBackIconClickListener(v -> onBackPressed())
                .setTitle(R.string.select)
                .setActionText(String.format(getString(R.string.selector_sure), 0))
                .setActionTextColor(getResources().getColor(R.color.color_337eff))
                .setActionListener(v -> {
                    Intent result = new Intent();
                    if (!selectedListAdapter.getSelectedFriends().isEmpty()) {
                        result.putExtra(REQUEST_CONTACT_SELECTOR_KEY, getSelectedAccount());
                    }
                    setResult(RESULT_OK, result);
                    finish();
                });
        ContactActions actions = new ContactActions();
        actions.addSelectorListener(IViewTypeConstant.CONTACT_FRIEND, (selector, data) -> {
            if (selector) {
                if (selectedListAdapter.getItemCount() >= MAX_SELECT_COUNT) {
                    Toast.makeText(this, R.string.contact_selector_max_count, Toast.LENGTH_LONG).show();
                    ((ContactFriendBean) data).setSelected(false);
                    binding.contactListView.updateContactData((data));
                } else {
                    selectedListAdapter.addFriend((ContactFriendBean) data);
                }
            } else {
                selectedListAdapter.removeFriend((ContactFriendBean) data);
            }
            binding.title.setActionText(String.format(getString(R.string.selector_sure),
                    selectedListAdapter.getItemCount()));
        });
        binding.contactListView.setContactAction(actions);
        //top selected list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        binding.rvSelected.setLayoutManager(layoutManager);
        selectedListAdapter = new SelectedListAdapter();
        selectedListAdapter.setItemClickListener(item -> {
            item.setSelected(false);
            binding.contactListView.updateContactData(item);
            binding.title.setActionText(String.format(getString(R.string.selector_sure),
                    selectedListAdapter.getItemCount()));
        });
        binding.rvSelected.setAdapter(selectedListAdapter);
    }

    protected void initData() {
        viewModel.getContactLiveData().observe(this,
                contactBeansResult -> {
                    if (contactBeansResult.getLoadStatus() == LoadStatus.Success) {
                        binding.contactListView.onFriendDataSourceChanged(contactBeansResult.getData());
                    }
                });
        viewModel.fetchContactList();
    }

    private ArrayList<String> getSelectedAccount() {
        ArrayList<String> result = new ArrayList<>();
        for (ContactFriendBean bean : selectedListAdapter.getSelectedFriends()) {
            result.add(bean.data.getAccount());
        }
        return result;
    }
}
