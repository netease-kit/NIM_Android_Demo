package com.netease.yunxin.nertc.ui.team.model;

import com.netease.yunxin.nertc.ui.team.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.yunxin.nertc.ui.team.recyclerview.holder.BaseViewHolder;
import com.netease.yunxin.nertc.ui.team.recyclerview.holder.RecyclerViewHolder;

/**
 * Created by huangjun on 2017/5/9.
 */

abstract class TeamAVChatItemViewHolderBase<T> extends RecyclerViewHolder<BaseMultiItemFetchLoadAdapter, BaseViewHolder, T> {

    TeamAVChatItemViewHolderBase(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(final BaseViewHolder holder, T data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data);
    }

    protected abstract void inflate(final BaseViewHolder holder);

    protected abstract void refresh(final T data);
}
