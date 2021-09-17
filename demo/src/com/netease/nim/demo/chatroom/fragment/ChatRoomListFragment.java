package com.netease.nim.demo.chatroom.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.demo.R;
import com.netease.nim.demo.chatroom.activity.ChatRoomActivity;
import com.netease.nim.demo.chatroom.activity.ChatRoomIndependentActivity;
import com.netease.nim.demo.chatroom.adapter.ChatRoomListAdapter;
import com.netease.nim.demo.chatroom.constants.EnterMode;
import com.netease.nim.demo.chatroom.constants.Extras;
import com.netease.nim.demo.chatroom.thridparty.ChatRoomHttpClient;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.ptr2.PullToRefreshLayout;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.decoration.SpacingDecoration;
import com.netease.nim.uikit.common.ui.recyclerview.listener.OnItemClickListener;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;

import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 直播间列表fragment
 * <p>
 * Created by huangjun on 2015/12/11.
 */
public class ChatRoomListFragment extends TFragment {

    private static final String TAG = ChatRoomListFragment.class.getSimpleName();

    private ChatRoomListAdapter adapter;

    private PullToRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;

    private int mode = EnterMode.NORMAL;

    private String appKey, account, pwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_rooms, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getArgumentsData();
        findViews();
        setViewsListener();
    }

    private void getArgumentsData() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mode = arguments.getInt(Extras.MODE);
        appKey = arguments.getString(Extras.APP_KEY);
        account = arguments.getString(Extras.ACCOUNT);
        pwd = arguments.getString(Extras.PWD);
    }

    private void setViewsListener() {
        View view = findView(R.id.independent);
        if (mode == EnterMode.NORMAL) {
            view.setVisibility(View.GONE);
            view.setOnClickListener(v -> ChatRoomIndependentActivity.start(getActivity()));
        } else {
            view.setVisibility(View.GONE);
            fetchData();
        }
    }

    public void onCurrent() {
        fetchData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void findViews() {
        // swipeRefreshLayout
        swipeRefreshLayout = findView(R.id.swipe_refresh);
        swipeRefreshLayout.setPullUpEnable(false);
        swipeRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {

            @Override
            public void onPullDownToRefresh() {
                fetchData();
            }

            @Override
            public void onPullUpToRefresh() {
            }
        });
        // recyclerView
        recyclerView = findView(R.id.recycler_view);
        adapter = new ChatRoomListAdapter(recyclerView);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        int spacing = ScreenUtil.dip2px(10);
        recyclerView.addItemDecoration(new SpacingDecoration(spacing, spacing, true));
        recyclerView.addOnItemTouchListener(new OnItemClickListener<ChatRoomListAdapter>() {

            @Override
            public void onItemClick(ChatRoomListAdapter adapter, View view, int position) {
                ChatRoomInfo room = adapter.getItem(position);
                ChatRoomActivity.start(getActivity(), room.getRoomId(), mode, appKey, account, pwd);
            }
        });
    }

    private void fetchData() {
        ChatRoomHttpClient client = ChatRoomHttpClient.getInstance();
        client.fetchChatRoomList(appKey,
                                 new ChatRoomHttpClient.ChatRoomHttpCallback<List<ChatRoomInfo>>() {

                                     @Override
                                     public void onSuccess(List<ChatRoomInfo> rooms) {
                                         onFetchDataDone(true, rooms);
                                     }

                                     @Override
                                     public void onFailed(int code, String errorMsg) {
                                         onFetchDataDone(false, null);
                                         if (getActivity() != null) {
                                             ToastHelper.showToast(getActivity(),
                                                                   "fetch chat room list failed, code=" +
                                                                   code);
                                         }
                                     }
                                 });
    }

    private void onFetchDataDone(final boolean success, final List<ChatRoomInfo> data) {
        Activity context = getActivity();
        if (context != null) {
            context.runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false); // 刷新结束
                if (success) {
                    adapter.setNewData(data); // 刷新数据源
                    postRunnable(() -> adapter.closeLoadAnimation());
                }
            });
        }
    }
}
