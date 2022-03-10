/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityEditNicknameBinding;
import com.netease.yunxin.kit.corekit.im.login.LoginService;
import com.netease.yunxin.kit.corekit.im.login.LoginUserInfo;
import com.netease.yunxin.kit.corekit.im.model.UserField;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoProvider;

import java.util.HashMap;
import java.util.Map;

public class EditNicknameActivity extends AppCompatActivity {
    private ActivityEditNicknameBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNicknameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_e9eff5));

        LoginUserInfo userInfo = LoginService.INSTANCE.userInfo();
        if (userInfo == null) {
            finish();
            return;
        }
        String remoteNickname = userInfo.getNickname();

        binding.ivBack.setOnClickListener(v -> finish());
        binding.tvDone.setOnClickListener(v -> {
            Map<UserField, Object> map = new HashMap<>(1);
            String nickname = binding.etNickname.getText().toString();
            map.put(UserField.Name, nickname);
            UserInfoProvider.updateUserInfo(map, new FetchCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void param) {
                    userInfo.setNickname(nickname);
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailed(int code) {
                    Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail) + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                    Toast.makeText(getApplicationContext(), getString(R.string.qchat_server_request_fail), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.etNickname.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        binding.etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                    binding.ivClear.setVisibility(View.GONE);
                    binding.tvDone.setEnabled(false);
                    binding.tvDone.setAlpha(0.5f);
                } else {
                    binding.ivClear.setVisibility(View.VISIBLE);
                    binding.tvDone.setEnabled(true);
                    binding.tvDone.setAlpha(1f);
                }
            }
        });
        binding.etNickname.setText(remoteNickname);
        binding.etNickname.requestFocus();

        binding.ivClear.setOnClickListener(v -> binding.etNickname.setText(null));
    }

    public static void launch(Context context, @NonNull ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, EditNicknameActivity.class);
        launcher.launch(intent);
    }
}
