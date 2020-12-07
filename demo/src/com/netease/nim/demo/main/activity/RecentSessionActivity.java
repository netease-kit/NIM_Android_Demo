package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.recent.adapter.RecentContactAdapter;
import com.netease.nim.uikit.business.recent.adapter.RecentSessionAdapter;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.recyclerview.listener.SimpleClickListener;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.RecentSession;
import com.netease.nimlib.sdk.msg.model.RecentSessionList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecentSessionActivity extends UI {

    private static final String TAG = "RecentSessionActivity";
    /** 每次拉取的数量 */
    private static final int COUNT_PER_PAGE = 20;

    private boolean hasMore = true;

    public static void start(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, RecentSessionActivity.class);
        activity.startActivity(intent);
    }

    private RecyclerView recyclerView;
    private RecentContactAdapter adapter;
    private List<RecentContact> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_session_activity);
        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.cloud_session_list;
        setToolBar(R.id.toolbar, options);

        initViews();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_session_list);
        items = new ArrayList<>();
        adapter = new RecentSessionAdapter(recyclerView, items);
//         recyclerView
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(new SimpleClickListener<RecentSessionAdapter>() {
            @Override
            public void onItemClick(RecentSessionAdapter adapter, View view, int position) {
                RecentContact recent = adapter.getItem(position);
                switch (recent.getSessionType()) {
                    case SUPER_TEAM:
                        ToastHelper.showToast(RecentSessionActivity.this, getString(R.string.super_team_impl_by_self));
                        break;
                    case Team:
                        NimUIKit.startTeamSession(RecentSessionActivity.this, recent.getContactId());
                        finish();
                        break;
                    case P2P:
                        NimUIKit.startP2PSession(RecentSessionActivity.this, recent.getContactId());
                        finish();
                        break;
                    case None:
                        long maxTimestamp = items.isEmpty() ? 0 : items.get(items.size() - 2).getTime();
                        loadMore(items, maxTimestamp);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onItemLongClick(RecentSessionAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildClick(RecentSessionAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildLongClick(RecentSessionAdapter adapter, View view, int position) {

            }
        });
        initSessionList();
    }

    private void initSessionList() {
        items.clear();
        loadMore(items, 0);
    }

    private void loadMore(List<RecentContact> data, long maxTimestamp) {
        if (!hasMore) {
            return;
        }
        int aimSize = items.isEmpty() ? COUNT_PER_PAGE : items.size() - 1 + COUNT_PER_PAGE;
        final RequestCallback<RecentSessionList> callback = new RequestSessionListCallback(data, COUNT_PER_PAGE, aimSize);
        NIMClient.getService(MsgService.class).queryMySessionList(0, maxTimestamp, 1, COUNT_PER_PAGE << 1, 1).setCallback(callback);
    }

    private class RequestSessionListCallback implements RequestCallback<RecentSessionList> {
        /** 每页的会话数量 */
        final int countPerPage;
        /** 总会话列表的数据源，可能拉取多次，每次拉到的新会话都添加到此列表中 */
        final List<RecentContact> sessionList;

        /** 此次拉取操作后sessionList中会话的长度，不包括加载更多项 */
        final int aimSize;

        final int startPosition;

        RequestSessionListCallback(List<RecentContact> data, int countPerPage, int aimSize) {
            this.sessionList = data == null ? new ArrayList<>() : data;
            this.countPerPage = countPerPage;
            this.aimSize = aimSize;
            this.startPosition = sessionList.isEmpty() ? 0 : sessionList.size() - 1;
        }

        @Override
        public void onSuccess(RecentSessionList param) {
            if (param == null) {
                return;
            }
            removeLoadMoreItem(sessionList);
            List<RecentSession> newList = param.getSessionList();
            RecentSessionActivity.this.hasMore = param.hasMore() || sessionList.size() + newList.size() > aimSize;
            //添加前countPerPage个到
            addSessionListToContactList(newList, sessionList, aimSize);

            //按照设计，addSessionListToContactList已经做了限制，不会大于aimSize
            if (sessionList.size() >= aimSize || !hasMore) {
                addOrUpdateLoadMoreItem(sessionList, hasMore);
                //什么都没有拉取到，则不用刷列表
                if (sessionList.size() <= startPosition) {
                    return;
                }
                adapter.notifyItemChanged(startPosition);
                adapter.notifyItemRangeInserted(startPosition + 1, sessionList.size() - startPosition);
                return;
            }
            //如果拉取的量不够一页，但是还有会话没有被拉取到，则再拉取
            long maxTimestamp = sessionList.isEmpty() ? 0 : sessionList.get(sessionList.size() - 1).getTime();
            NIMClient.getService(MsgService.class).queryMySessionList(0, maxTimestamp, 1, countPerPage, 1).setCallback(this);
        }

        @Override
        public void onFailed(int code) {
            NimLog.d(TAG, "failed to query more session list, code=" + code);
        }

        @Override
        public void onException(Throwable exception) {
            NimLog.d(TAG, "query more session list with exception, msg=" + exception.getMessage());

        }


        /**
         * 移除加载更多的项
         *
         * @param data 数据源
         */
        private void removeLoadMoreItem(List<RecentContact> data) {
            //空数据没有项
            if (data == null || data.isEmpty()) {
                return;
            }
            //加载更多的项存在的话，在最后一个
            int lastIndex = data.size() - 1;
            //加载更多的项的会话类型为None
            if (data.get(lastIndex) instanceof LoadMoreItemData) {
                data.remove(lastIndex);
            }
        }


        /**
         * 添加或更新加载更多的项
         *
         * @param data    数据源
         * @param hasMore 是否还有更多
         */
        private void addOrUpdateLoadMoreItem(List<RecentContact> data, boolean hasMore) {
            if (data == null) {
                data = new ArrayList<>();
            }
            //如果还存在老的加载更多，则删除
            removeLoadMoreItem(data);
            data.add(new LoadMoreItemData(hasMore));
        }

        /**
         * 批量添加 {@link RecentSession} 到 {@link RecentContact}列表
         *
         * @param sessionList 被添加对象
         * @param contactList 容器
         * @param aimSize     contactList的最大长度，如果sessionList的过长，则不再添加后面的元素
         */
        private void addSessionListToContactList(List<RecentSession> sessionList, List<RecentContact> contactList, int aimSize) {
            if (sessionList == null || contactList == null || sessionList.isEmpty()) {
                return;
            }
            int startIndex = 0;
            //先去掉重复部分
            if (!contactList.isEmpty()) {
                String lastContactId = contactList.get(contactList.size() - 1).getContactId();
                for (int i = 0; i < sessionList.size(); ++i) {
                    String contactId = sessionList.get(i).parseSessionId().second;
                    if (!TextUtils.isEmpty(contactId) && contactId.equals(lastContactId)) {
                        startIndex = i + 1;
                        break;
                    }
                }
            }

            for (int i = startIndex; i < sessionList.size(); i++) {
                RecentSession session = sessionList.get(i);
                if (session == null) {
                    continue;
                }
                if (contactList.size() >= aimSize) {
                    return;
                }
                final RecentContact contact = session.toRecentContact();
                if (!TextUtils.isEmpty(contact.getContactId()) && contact.getSessionType() != null) {
                    contactList.add(contact);
                }
            }
        }
    }

    /**
     * 只有getTag方法和getSessionType方法有效
     */
    private class LoadMoreItemData implements RecentContact {
        /** 0: 没有更多了; 1: 还有更多 */
        private final long tag;

        LoadMoreItemData(boolean hasMore) {
            this.tag = hasMore ? RecentSessionAdapter.TAG_HAS_MORE : RecentSessionAdapter.TAG_NO_MORE;
        }

        @Override
        public String getContactId() {
            return null;
        }

        @Override
        public String getFromAccount() {
            return null;
        }

        @Override
        public String getFromNick() {
            return null;
        }

        @Override
        public SessionTypeEnum getSessionType() {
            return SessionTypeEnum.None;
        }

        @Override
        public String getRecentMessageId() {
            return null;
        }

        @Override
        public MsgTypeEnum getMsgType() {
            return null;
        }

        @Override
        public MsgStatusEnum getMsgStatus() {
            return null;
        }

        @Override
        public void setMsgStatus(MsgStatusEnum msgStatus) {

        }

        @Override
        public int getUnreadCount() {
            return 0;
        }

        @Override
        public String getContent() {
            return null;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public MsgAttachment getAttachment() {
            return null;
        }

        @Override
        public void setTag(long tag) {

        }

        @Override
        public long getTag() {
            return this.tag;
        }

        @Override
        public Map<String, Object> getExtension() {
            return null;
        }

        @Override
        public void setExtension(Map<String, Object> extension) {

        }
    }
}