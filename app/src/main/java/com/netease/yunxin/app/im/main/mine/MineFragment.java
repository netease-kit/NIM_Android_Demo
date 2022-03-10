/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.FragmentMineBinding;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.corekit.im.login.LoginService;
import com.netease.yunxin.kit.corekit.im.login.LoginUserInfo;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.qchatkit.ui.utils.ColorUtils;

public class MineFragment extends BaseFragment {
    private FragmentMineBinding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMineBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginUserInfo userInfo = LoginService.INSTANCE.userInfo();
        if (userInfo == null) {
            return;
        }
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK){
                refreshUserInfo(LoginService.INSTANCE.userInfo());
            }
        });

        refreshUserInfo(userInfo);
        binding.tvAccount.setText(getString(R.string.tab_mine_account, LoginService.INSTANCE.imAccId()));
        binding.tvLogout.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                AuthorManager.INSTANCE.logoutWitDialog(activity, new LoginCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void unused) {
                        LoginService.logoutIMWithQChat(new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                            @Override
                            public void onError(int errorCode, @NonNull String errorMsg) {
                                Toast.makeText(getContext(), "error code is " + errorCode + ", message is " + errorMsg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(@Nullable Void data) {
                                startActivity(new Intent(getContext(), WelcomeActivity.class));
                                activity.finish();
                            }
                        });
                    }

                    @Override
                    public void onError(int i, @NonNull String s) {
                    }
                });
            }
        });
    }

    private void refreshUserInfo(LoginUserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        binding.cavIcon.setData(userInfo.getAvatar(), userInfo.getNickname() == null ? "" : userInfo.getNickname(), ColorUtils.avatarColor(LoginService.INSTANCE.imAccId()));
        binding.tvName.setText(userInfo.getNickname());
    }
}
