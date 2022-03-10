/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMainBinding;
import com.netease.yunxin.app.im.main.mine.MineFragment;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;
import com.netease.yunxin.kit.qchatkit.ui.common.NetworkUtils;
import com.netease.yunxin.kit.qchatkit.ui.server.QChatServerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * IM  Main Page
 * include four tab , message/contact/live/profile
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private static final int START_INDEX = 0;
    private View mCurrentTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        initView();
        // init network state listen
        NetworkUtils.init(getApplicationContext());
    }

    private void initView(){
        List<Fragment> fragments = new ArrayList<>();
        QChatServerFragment qChatServerFragment = new QChatServerFragment();
        fragments.add(qChatServerFragment);
        ContactFragment.Builder builder = new ContactFragment.Builder();
        builder.setTitle(getResources().getString(R.string.tab_contact_tab_text));
        ContactFragment fragment = builder.build();
        fragments.add(fragment);
        fragments.add(new MineFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(fragments);
        activityMainBinding.viewPager.setUserInputEnabled(false);
        activityMainBinding.viewPager.setAdapter(fragmentAdapter);
        activityMainBinding.viewPager.setCurrentItem(START_INDEX, false);
        activityMainBinding.viewPager.setOffscreenPageLimit(3);
        mCurrentTab = activityMainBinding.qchatBtnGroup;
        changeStatusBarColor(R.color.color_e9eff5);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void tabClick(View view) {

        if (mCurrentTab != null && mCurrentTab == view) {
            return;
        }
        resetTabStyle();
        mCurrentTab = view;
        if (mCurrentTab == activityMainBinding.contactBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(1, false);
            activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked), null, null);
            changeStatusBarColor(R.color.color_white);
        }else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(2, false);
            activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_mine_tab_checked), null, null);
            changeStatusBarColor(R.color.color_white);
        } else if (mCurrentTab == activityMainBinding.qchatBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(0, false);
            activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_qchat_checked), null, null);
            changeStatusBarColor(R.color.color_e9eff5);
        }
    }

    private void changeStatusBarColor(@ColorRes int colorResId){
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, colorResId));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetTabStyle(){
        activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.mipmap.ic_contact_tab_unchecked),null,null);

        activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.mipmap.ic_mine_tab_unchecked),null,null);

        activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_qchat_unchecked),null,null);

    }

}