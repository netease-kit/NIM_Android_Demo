package com.netease.nim.demo.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.core.item.ContactIdFilter;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.protocol.ContactHttpClient;
import com.netease.nim.demo.contact.protocol.IContactHttpCallback;
import com.netease.nim.demo.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单
 * Created by huangjun on 2015/8/12.
 */
public class BlackListActivity extends TActionBarActivity {
    private static final String TAG = "BlackListActivity";
    private static final int REQUEST_CODE_BLACK = 1;

    private RecyclerView recyclerView;
    private List<User> data = new ArrayList<>();
    private BlackListAdapter adapter;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, BlackListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.black_list);
        setContentView(R.layout.black_list_activity);

        initRecyclerView();
        initData();
        initActionbar();
    }

    private void initData() {
        final List<String> accounts = NIMClient.getService(FriendService.class).getBlackList();
        ContactDataCache.getInstance().getUsersFromRemote(accounts, new IContactHttpCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                data.addAll(users);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    private void initActionbar() {
        TextView textView = ActionBarUtil.addRightClickableBlueTextViewOnActionBar(this, R.string.add);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactSelectActivity.Option option = new ContactSelectActivity.Option();
                option.title = "选择黑名单";
                ArrayList<String> excludeUids = new ArrayList<>();
                for (User user : data) {
                    excludeUids.add(user.getAccount());
                }
                option.itemFilter = new ContactIdFilter(excludeUids, true);
                ContactSelectActivity.startActivityForResult(BlackListActivity.this, option, REQUEST_CODE_BLACK);
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = findView(R.id.black_list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlackListAdapter(data, viewListener);
        recyclerView.setAdapter(adapter);
    }

    private BlackListAdapter.ViewListener viewListener = new BlackListAdapter.ViewListener() {
        @Override
        public void onRemove(final User user) {
            NIMClient.getService(FriendService.class).removeFromBlackList(user.getAccount()).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    Toast.makeText(BlackListActivity.this, "移出黑名单成功", Toast.LENGTH_SHORT).show();
                    data.remove(user);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailed(int code) {
                    Toast.makeText(BlackListActivity.this, "移出黑名单失败，错误码：" + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }

        @Override
        public void onItemClick(int position) {
            Log.i(TAG, "onItemClick " + position);
        }
    };

    private void addBuddysToBlackList(ArrayList<String> selected) {
        for (String account : selected) {
            NIMClient.getService(FriendService.class).addToBlackList(account);
            data.add(ContactDataCache.getInstance().getUser(account));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_BLACK:
                    final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                    if (selected != null && !selected.isEmpty()) {
                        addBuddysToBlackList(selected);
                    }
                    break;
            }
        }
    }
}
