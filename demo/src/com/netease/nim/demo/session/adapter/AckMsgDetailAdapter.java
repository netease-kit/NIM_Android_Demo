package com.netease.nim.demo.session.adapter;

import android.content.Context;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * Created by winnie on 2018/3/17.
 */

public class AckMsgDetailAdapter extends TAdapter {

    /**
     * GridView数据项
     */
    public static class AckMsgDetailItem {
        private String tid;
        private String account;

        public AckMsgDetailItem(String tid, String account) {
            this.tid = tid;
            this.account = account;
        }

        public String getTid() {
            return tid;
        }

        public String getAccount() {
            return account;
        }
    }

    public AckMsgDetailAdapter(Context context, List<?> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }
}
