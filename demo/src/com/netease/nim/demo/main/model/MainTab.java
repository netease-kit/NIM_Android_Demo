package com.netease.nim.demo.main.model;

import com.netease.nim.demo.main.fragment.ContactsFragment;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.netease.nim.demo.main.fragment.SessionListFragment;
import com.netease.nim.demo.main.reminder.ReminderId;
import com.netease.nim.uikit.recent.RecentContactsFragment;
import com.netease.nim.demo.R;

public enum MainTab {
	RECENT_CONTACTS(0, ReminderId.SESSION, SessionListFragment.class, R.string.main_tab_session, R.layout.messages),
	CONTACT(1, ReminderId.CONTACT, ContactsFragment.class, R.string.main_tab_contact, R.layout.contacts),
	;

	public final int tabIndex;

	public final int reminderId;

	public final Class<? extends MainTabFragment> clazz;

	public final int resId;

	public final int fragmentId;

    public final int layoutId;

	private MainTab(int index, int reminderId, Class<? extends MainTabFragment> clazz, int resId, int layoutId) {
		this.tabIndex = index;
		this.reminderId = reminderId;
		this.clazz = clazz;
		this.resId = resId;
		this.fragmentId = index;
        this.layoutId = layoutId;
	}

	public static final MainTab fromReminderId(int reminderId) {
		for (MainTab value : MainTab.values()) {
			if (value.reminderId == reminderId) {
				return value;
			}
		}

		return null;
	}

	public static final MainTab fromTabIndex(int tabIndex) {
		for (MainTab value : MainTab.values()) {
			if (value.tabIndex == tabIndex) {
				return value;
			}
		}

		return null;
	}
}
