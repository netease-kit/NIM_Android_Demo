/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.ui.team.recyclerview.animation;

import android.animation.Animator;
import android.view.View;

public interface BaseAnimation {

    Animator[] getAnimators(View view);

}
