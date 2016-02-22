package com.netease.nim.demo.chatroom.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.chatroom.activity.ChatRoomActivity;
import com.netease.nim.demo.chatroom.adapter.ChatRoomAdapter;
import com.netease.nim.demo.chatroom.thridparty.ChatRoomHttpClient;
import com.netease.nim.demo.chatroom.viewholder.ChatRoomViewHolder;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshGridView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播间列表fragment
 * Created by huangjun on 2015/12/11.
 */
public class ChatRoomsFragment extends TFragment implements TAdapterDelegate, ChatRoomAdapter.ViewHolderEventListener {

    private static final String TAG = ChatRoomsFragment.class.getSimpleName();
    private View loadingFrame;
    private PullToRefreshGridView gridView;
    private List<ChatRoomInfo> items = new ArrayList<>();
    private ChatRoomAdapter adapter;

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return ChatRoomViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_rooms, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initAdapter();
        findViews();
    }

    public void onCurrent() {
        fetchData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void findViews() {
        // loading
        loadingFrame = findView(com.netease.nim.uikit.R.id.contact_loading_frame);
        loadingFrame.setVisibility(View.VISIBLE);

        gridView = findView(R.id.chat_room_grid_view);
        gridView.setAdapter(adapter);
        gridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                onFetchDataDone(false);
            }
        });
    }

    @Override
    public void onItemClick(String roomId) {
        ChatRoomActivity.start(getActivity(), roomId);
    }

    private void initAdapter() {
        adapter = new ChatRoomAdapter(getActivity(), items, this, this);
    }

    private void fetchData() {
        ChatRoomHttpClient.getInstance().fetchChatRoomList(new ChatRoomHttpClient.ChatRoomHttpCallback<List<ChatRoomInfo>>() {
            @Override
            public void onSuccess(List<ChatRoomInfo> rooms) {
                if (items.isEmpty()) {
                    items.addAll(rooms);
                }

                onFetchDataDone(true);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                onFetchDataDone(false);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "fetch chat room list failed, code=" + code, Toast.LENGTH_SHORT);
                }

                LogUtil.d(TAG, "fetch chat room list failed, code:" + code
                        + " errorMsg:" + errorMsg);
            }
        });
    }

    private void onFetchDataDone(boolean success) {
        loadingFrame.setVisibility(View.GONE);
        gridView.onRefreshComplete();
        if (success) {
            refresh();
        }
    }

    private void refresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

}
