package com.netease.nim.demo.session.model;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.fragment.tab.AckMsgTabFragment;
import com.netease.nim.demo.session.fragment.tab.ReadAckMsgTabFragment;
import com.netease.nim.demo.session.fragment.tab.UnreadAckMsgTabFragment;

/**
 * Created by winnie on 2018/3/14.
 */

public enum  AckMsgTab {
    UNREAD(0, AckMsgReminderId.UNREAD, UnreadAckMsgTabFragment.class, R.string.unread, R.layout.ack_msg_unread_layout),
    READ(1, AckMsgReminderId.READ, ReadAckMsgTabFragment.class, R.string.readed, R.layout.ack_msg_readed_layout);

    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends AckMsgTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    AckMsgTab(int index, int reminderId, Class<? extends AckMsgTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final AckMsgTab fromReminderId(int reminderId) {
        for (AckMsgTab value : AckMsgTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final AckMsgTab fromTabIndex(int tabIndex) {
        for (AckMsgTab value : AckMsgTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }
}
