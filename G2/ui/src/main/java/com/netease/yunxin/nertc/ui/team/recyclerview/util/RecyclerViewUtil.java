/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.ui.team.recyclerview.util;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * Created by hzxuwen on 2017/1/13.
 */

public class RecyclerViewUtil {

    public static void changeItemAnimation(RecyclerView recyclerView, boolean isOpen) {
        // 关闭viewholder动画效果。解决viewholder闪烁问题
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(isOpen);
        }
    }
}
