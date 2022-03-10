/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityUserInfoBinding;
import com.netease.yunxin.kit.corekit.im.login.LoginService;
import com.netease.yunxin.kit.corekit.im.login.LoginUserInfo;
import com.netease.yunxin.kit.corekit.im.model.UserField;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoProvider;
import com.netease.yunxin.kit.qchatkit.ui.common.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.qchatkit.ui.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {
    private ActivityUserInfoBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private LoginUserInfo userInfo;
    private int resultCode = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_e9eff5));

        userInfo = LoginService.INSTANCE.userInfo();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                refreshUserInfo(LoginService.INSTANCE.userInfo());
            }
            if (resultCode == RESULT_OK) {
                return;
            }
            resultCode = result.getResultCode();
        });

        initView();
    }

    private void initView() {
        refreshUserInfo(LoginService.INSTANCE.userInfo());
        binding.cavAvatar.setOnClickListener(v -> new PhotoChoiceDialog(UserInfoActivity.this).show(new FetchCallback<String>() {
            @Override
            public void onSuccess(@Nullable String urlParam) {
                Map<UserField, Object> map = new HashMap<>(1);
                map.put(UserField.Avatar, urlParam);
                UserInfoProvider.updateUserInfo(map, new FetchCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                        resultCode = RESULT_OK;
                        userInfo.setAvatar(urlParam);
                        binding.cavAvatar.setData(urlParam, userInfo.getNickname() == null ? "" : userInfo.getNickname(), 0);
                    }

                    @Override
                    public void onFailed(int code) {
                        Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail) + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail), Toast.LENGTH_SHORT).show();
            }
        }));
        binding.tvName.setOnClickListener(v -> EditNicknameActivity.launch(getApplicationContext(), launcher));
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void refreshUserInfo(LoginUserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        binding.cavAvatar.setData(userInfo.getAvatar(), userInfo.getNickname() == null ? "" : userInfo.getNickname(), ColorUtils.avatarColor(LoginService.INSTANCE.imAccId()));
        binding.tvName.setText(userInfo.getNickname());
    }

    @Override
    public void finish() {
        setResult(resultCode);
        super.finish();
    }

    public static void launch(Context context, @NonNull ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        launcher.launch(intent);
    }
}