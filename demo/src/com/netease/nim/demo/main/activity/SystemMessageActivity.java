package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.protocol.IContactHttpCallback;
import com.netease.nim.demo.main.adapter.SystemMessageAdapter;
import com.netease.nim.demo.main.viewholder.SystemMessageViewHolder;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统消息中心界面
 * <p/>
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageActivity extends TActionBarActivity implements TAdapterDelegate,
        AutoRefreshListView.OnRefreshListener, SystemMessageViewHolder.SystemMessageListener {

    private static final int LOAD_MESSAGE_COUNT = 10;
    private static final int LOAD_SYSTEM_MESSAGE_COUNT = 10;

    // view
    private MessageListView listView;

    // adapter
    private SystemMessageAdapter adapter;
    private List<SystemMessage> items = new ArrayList<>();
    private Set<Long> itemIds = new HashSet<>();

    // db
    private boolean firstLoad = true;

    public static void start(Context context) {
        start(context, null, true);
    }

    public static void start(Context context, Intent extras, boolean clearTop) {
        Intent intent = new Intent();
        intent.setClass(context, SystemMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return SystemMessageViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }


    @Override
    public void onRefreshFromStart() {
    }

    @Override
    public void onRefreshFromEnd() {
        loadMessageFromDB(LOAD_MESSAGE_COUNT, true); // load old data
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.system_notification_message_activity);
        setTitle(R.string.verify_reminder);

        initAdapter();
        initListView();

        loadMessageFromDB(LOAD_MESSAGE_COUNT, true); // load old data
        registerSystemObserver(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onStop() {
        super.onStop();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerSystemObserver(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_menu_btn:
                deleteAllMessages();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        adapter = new SystemMessageAdapter(this, items, this, this);
    }

    private void initListView() {
        listView = (MessageListView) findViewById(R.id.messageListView);
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // adapter
        listView.setAdapter(adapter);

        // listener
        listView.setOnRefreshListener(this);
    }

    /**
     * 加载历史消息
     *
     * @param maxResultsCount 最多加载多少条
     * @param needOffset      是否ListView自动偏移
     */
    protected void loadMessageFromDB(final int maxResultsCount, final boolean needOffset) {
        NIMClient.getService(SystemMessageService.class).querySystemMessages(items.size(), maxResultsCount)
                .setCallback(new RequestCallback<List<SystemMessage>>() {
                    @Override
                    public void onSuccess(List<SystemMessage> msgList) {
                        boolean first = firstLoad;
                        firstLoad = false;
                        if (msgList != null) {
                            onMessageLoaded(msgList, first, LOAD_SYSTEM_MESSAGE_COUNT, needOffset);
                        }
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }

    private void onMessageLoaded(List<SystemMessage> loadedMsgList, boolean first, int requestCount,
                                 boolean needOffset) {
        int count = 0;
        if (loadedMsgList != null && loadedMsgList.size() > 0) {
            count = addOldMessages(loadedMsgList);
        }

        // 第一次加载后显示顶部
        if (first) {
            ListViewUtil.scrollToPostion(listView, 0, 0);
        }

        listView.onRefreshComplete(count, requestCount, needOffset);
    }

    // 去重
    private boolean duplicateFilter(final SystemMessage msg) {
        if (itemIds.contains(msg.getMessageId())) {
            return true;
        }

        return false;
    }

    private int addOldMessages(List<SystemMessage> messages) {
        List<String> unknowAccounts = new ArrayList<>();
        for (SystemMessage msg : messages) {
            if (duplicateFilter(msg)) {
                continue;
            }

            // 收到被删除好友的通知，不要提醒
            if (msg.getType() == SystemMessageType.DeleteFriend) {
                continue;
            }

            items.add(msg);
            itemIds.add(msg.getMessageId());

            if (!ContactDataCache.getInstance().hasUser(msg.getFromAccount())) {
                unknowAccounts.add(msg.getFromAccount());
            }
        }

        if (!unknowAccounts.isEmpty()) {
            requestUnknowUser(unknowAccounts);
        }

        refresh();
        return items.size();
    }

    @Override
    public void onAgree(SystemMessage message) {
        onSystemNotificationDeal(message, true);
    }

    @Override
    public void onReject(SystemMessage message) {
        onSystemNotificationDeal(message, false);
    }

    @Override
    public void onLongPressed(SystemMessage message) {
        showLongClickMenu(message);
    }

    private void onSystemNotificationDeal(final SystemMessage message, final boolean pass) {
        RequestCallback callback = new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onProcessSuccess(pass, message);
            }

            @Override
            public void onFailed(int code) {
                onProcessFailed(code, message);
            }

            @Override
            public void onException(Throwable exception) {

            }
        };
        if (message.getType() == SystemMessageType.TeamInvite) {
            if (pass) {
                NIMClient.getService(TeamService.class).acceptInvite(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).declineInvite(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }

        } else if (message.getType() == SystemMessageType.ApplyJoinTeam) {
            if (pass) {
                NIMClient.getService(TeamService.class).passApply(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).rejectApply(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }
        } else if (message.getType() == SystemMessageType.AddFriend) {
            NIMClient.getService(FriendService.class).ackAddFriendRequest(message.getFromAccount(), pass).setCallback(callback);
        }
    }

    private void onProcessSuccess(final boolean pass, SystemMessage message) {
        SystemMessageStatus status = pass ? SystemMessageStatus.passed : SystemMessageStatus.declined;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void onProcessFailed(final int code, SystemMessage message) {
        Toast.makeText(SystemMessageActivity.this, "failed, error code=" + code,
                Toast.LENGTH_LONG).show();

        SystemMessageStatus status = SystemMessageStatus.expired;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void refresh() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshViewHolder(final SystemMessage message) {
        final long messageId = message.getMessageId();

        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            SystemMessage item = items.get(i);
            if (messageId == item.getMessageId()) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        final int m = index;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m < 0) {
                    return;
                }

                Object tag = ListViewUtil.getViewHolderByIndex(listView, m);
                if (tag instanceof SystemMessageViewHolder) {
                    ((SystemMessageViewHolder) tag).refreshDirectly(message);
                }
            }
        });
    }

    private void registerSystemObserver(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(new Observer<SystemMessage>() {
            @Override
            public void onEvent(SystemMessage message) {
                // 收到被删除好友的通知，不要提醒
                if (message.getType() == SystemMessageType.DeleteFriend) {
                    return;
                }

                if (!items.contains(message)) {
                    items.add(0, message);
                }

                if (!ContactDataCache.getInstance().hasUser(message.getFromAccount())) {
                    List<String> accounts = new ArrayList<>();
                    accounts.add(message.getFromAccount());
                    requestUnknowUser(accounts);
                }

                refresh();
            }
        }, register);
    }

    private void requestUnknowUser(List<String> accounts) {
        ContactDataCache.getInstance().getUsersFromRemote(accounts, new IContactHttpCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    refresh();
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    private void deleteAllMessages() {
        NIMClient.getService(SystemMessageService.class).clearSystemMessages();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
        items.clear();
        refresh();
        Toast.makeText(SystemMessageActivity.this, R.string.clear_all_success, Toast.LENGTH_SHORT).show();
    }

    private void showLongClickMenu(final SystemMessage message) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.setTitle(R.string.delete_tip);
        String title = getString(R.string.delete_system_message);
        alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                deleteSystemMessage(message);
            }
        });
        alertDialog.show();
    }

    private void deleteSystemMessage(final SystemMessage message) {
        NIMClient.getService(SystemMessageService.class).deleteSystemMessage(message.getMessageId());
        items.remove(message);
        refresh();
        Toast.makeText(SystemMessageActivity.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
    }
}
