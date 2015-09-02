package com.netease.nim.demo.database;

import android.content.Context;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.database.contact.ContactDatabase;
import com.netease.nim.demo.database.contact.ContactDatabaseHelper;

public class DatabaseManager {

	private ContactDatabase contactDb;
	private ContactDatabaseHelper contactDbHelper;

	public static final String DEFAULT_PASSWORD = "NETEASE";

	private static DatabaseManager instance = new DatabaseManager();

	public static DatabaseManager getInstance() {
		return instance;
	}

	public synchronized boolean open(Context context) {
		return open(context, true);
	}

	public synchronized boolean open(Context context, boolean upgrade) {
		if (contactDb == null || !contactDb.opened()) {
            contactDb = new ContactDatabase(context, DemoCache.getAccount(), DEFAULT_PASSWORD, upgrade);
		}

		if (opened()) {
            contactDbHelper = new ContactDatabaseHelper(contactDb);
		}

		return opened();
	}

	public boolean opened() {
		return contactDb != null && contactDb.opened();
	}

	public void close() {
		if (contactDb != null) {
            contactDb.close();
            contactDb = null;
		}
	}

	public ContactDatabaseHelper getContactDbHelper() {
		return contactDbHelper;
	}
}
