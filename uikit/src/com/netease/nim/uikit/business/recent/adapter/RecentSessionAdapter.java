package com.netease.nim.uikit.business.recent.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.List;

public class RecentSessionAdapter extends RecentContactAdapter {

    private static final int VIEW_TYPE_LOAD_MORE = 4;

    public static final int TAG_NO_MORE = 0;
    public static final int TAG_HAS_MORE = 1;

    public RecentSessionAdapter(RecyclerView recyclerView, List<RecentContact> data) {
        super(recyclerView, data);
        addItemType(VIEW_TYPE_LOAD_MORE, R.layout.nim_recent_contact_list_item_load_more, LoadMoreViewHolder.class);
    }

    @Override
    protected int getViewType(RecentContact item) {
        switch (item.getSessionType()) {
            case Team:
                return ViewType.VIEW_TYPE_TEAM;
            case None:
                return VIEW_TYPE_LOAD_MORE;
            default:
                return ViewType.VIEW_TYPE_COMMON;
        }
    }

    private static class LoadMoreViewHolder extends RecyclerViewHolder<RecentSessionAdapter, BaseViewHolder, RecentContact> {

        TextView loadMoreTV;

        public LoadMoreViewHolder(RecentSessionAdapter adapter) {
            super(adapter);
        }

        @Override
        public void convert(BaseViewHolder holder, RecentContact data, int position, boolean isScrolling) {
            loadMoreTV = holder.getView(R.id.tv_load_more);
            Context context = loadMoreTV.getContext();
            Resources resources = context.getResources();
            if (data.getTag() == TAG_HAS_MORE) {
                loadMoreTV.setText(context.getString(R.string.load_more));
                loadMoreTV.setTextColor(resources.getColor(R.color.color_blue_0888ff));
            } else {
                loadMoreTV.setText(context.getString(R.string.no_more_session));
                loadMoreTV.setTextColor(resources.getColor(R.color.color_gray_bfc2c5));
            }
        }
    }
}
