/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.ui.team.recyclerview.loadmore;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.netease.yunxin.nertc.ui.team.recyclerview.holder.BaseViewHolder;

public abstract class LoadMoreView {

    public static final int STATUS_DEFAULT = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_END = 4;

    private int mLoadMoreStatus = STATUS_DEFAULT;
    private boolean mLoadMoreEndGone = false;

    public void setLoadMoreStatus(int loadMoreStatus) {
        this.mLoadMoreStatus = loadMoreStatus;
    }

    public int getLoadMoreStatus() {
        return mLoadMoreStatus;
    }

    public void convert(BaseViewHolder holder) {
        switch (mLoadMoreStatus) {
            case STATUS_LOADING:
                visibleLoading(holder, true);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_FAIL:
                visibleLoading(holder, false);
                visibleLoadFail(holder, true);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_END:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, true);
                break;
        }
    }

    private void visibleLoading(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadingViewId(), visible);
    }

    private void visibleLoadFail(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadFailViewId(), visible);
    }

    private void visibleLoadEnd(BaseViewHolder holder, boolean visible) {
        final int loadEndViewId = getLoadEndViewId();
        if (loadEndViewId != 0) {
            holder.setVisible(loadEndViewId, visible);
        }
    }

    public final void setLoadMoreEndGone(boolean loadMoreEndGone) {
        this.mLoadMoreEndGone = loadMoreEndGone;
    }

    public final boolean isLoadEndMoreGone() {
        if (getLoadEndViewId() == 0) {
            return true;
        }
        return mLoadMoreEndGone;
    }

    /**
     * No more data is hidden
     *
     * @return true for no more data hidden load more
     * @deprecated
     */
    @Deprecated
    public boolean isLoadEndGone() {
        return mLoadMoreEndGone;
    }

    /**
     * load more layout
     *
     * @return
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * loading view
     *
     * @return
     */
    protected abstract
    @IdRes
    int getLoadingViewId();

    /**
     * load fail view
     *
     * @return
     */
    protected abstract
    @IdRes
    int getLoadFailViewId();

    /**
     * load end view, you can return 0
     *
     * @return
     */
    protected abstract
    @IdRes
    int getLoadEndViewId();
}
