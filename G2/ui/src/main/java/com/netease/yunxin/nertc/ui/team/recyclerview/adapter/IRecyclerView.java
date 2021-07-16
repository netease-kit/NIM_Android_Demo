/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.ui.team.recyclerview.adapter;

/**
 * Created by huangjun on 2016/12/8.
 */

public interface IRecyclerView {
    /**
     * special view type
     */
    int FETCHING_VIEW = 0x00001000;
    int HEADER_VIEW = 0x00001001;
    int LOADING_VIEW = 0x00001002;
    int FOOTER_VIEW = 0x00001003;
    int EMPTY_VIEW = 0x00001004;

    /**
     * 获取Header item的数量（包含FetchItem）
     */
    int getHeaderLayoutCount();

    /**
     * 获取Item视图类型
     *
     * @param position Item位置
     * @return
     */
    int getItemViewType(int position);
}
