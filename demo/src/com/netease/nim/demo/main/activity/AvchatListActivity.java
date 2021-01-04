package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.netease.nim.demo.R;
import com.netease.nim.demo.main.viewholder.AvchatViewHolder;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义通知
 * <p/>
 * Created by huangjun on 2015/5/28
 */
public class AvchatListActivity extends UI implements TAdapterDelegate {

    // view
    private MessageListView listView;

    // adapter
    private TAdapter adapter;

    private List<IMMessage> items = new ArrayList<>();

    public static void start(Context context) {
        start(context, null, true);
    }

    public static void start(Context context, Intent extras, boolean clearTop) {
        Intent intent = new Intent();
        intent.setClass(context, AvchatListActivity.class);
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
        return AvchatViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avchat_list_activity);
        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.avchat_list;
        setToolBar(R.id.toolbar, options);
        initAdapter();
        initListView();
        loadData(); // load old data
    }

    private void initAdapter() {
        adapter = new TAdapter(this, items, this);
    }

    private void initListView() {
        listView = findViewById(R.id.messageListView);
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        // adapter
        listView.setAdapter(adapter);
    }


    private void loadData() {
        NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.avchat,
                                                                      (Long) null, 10).setCallback(
                new RequestCallbackWrapper<List<IMMessage>>() {

                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        if (code == 200 && result != null && !result.isEmpty()) {
                            items.addAll(result);
                            refresh();
                        }
                    }
                });

    }

    private void refresh() {
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

}
