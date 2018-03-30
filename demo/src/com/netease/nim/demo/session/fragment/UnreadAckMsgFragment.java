package com.netease.nim.demo.session.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.netease.nim.avchatkit.common.TFragment;
import com.netease.nim.demo.R;
import com.netease.nim.demo.session.activity.AckMsgInfoActivity;
import com.netease.nim.demo.session.adapter.AckMsgDetailAdapter;
import com.netease.nim.demo.session.model.AckMsgViewModel;
import com.netease.nim.demo.session.viewholder.AckMsgDetailHolder;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 群未读人员界面
 * Created by winnie on 2018/3/19.
 */

public class UnreadAckMsgFragment extends TFragment implements TAdapterDelegate {
    private AckMsgViewModel viewModel;
    private AckMsgDetailAdapter adapter;
    private List<AckMsgDetailAdapter.AckMsgDetailItem> dataSource;
    private View rootView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        findViews();
        IMMessage message = (IMMessage) getActivity().getIntent().getSerializableExtra(AckMsgInfoActivity.EXTRA_MESSAGE);
        viewModel = ViewModelProviders.of(getActivity()).get(AckMsgViewModel.class);
        viewModel.init(message);
        viewModel.getTeamMsgAckInfo().observe(getActivity(), new Observer<TeamMsgAckInfo>() {
            @Override
            public void onChanged(@Nullable TeamMsgAckInfo teamMsgAckInfo) {
                for (String account : teamMsgAckInfo.getUnAckAccountList()) {
                    dataSource.add(new AckMsgDetailAdapter.AckMsgDetailItem(teamMsgAckInfo.getTeamId(), account));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.unread_ack_msg_fragment, container, false);
        return rootView;
    }

    private void initAdapter() {
        dataSource = new ArrayList<>();
        adapter = new AckMsgDetailAdapter(getActivity(), dataSource, this);
    }

    private void findViews() {
        TeamInfoGridView teamInfoGridView = rootView.findViewById(R.id.team_member_grid);
        teamInfoGridView.setSelector(R.color.transparent);
        teamInfoGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        teamInfoGridView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        teamInfoGridView.setAdapter(adapter);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return AckMsgDetailHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }
}
