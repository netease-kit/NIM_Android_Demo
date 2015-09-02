package com.netease.nim.demo.database.contact;

import android.content.Context;

import com.netease.nim.demo.database.base.AbstractDatabase;
import com.netease.nim.demo.database.base.DatabaseRevision;

public class ContactDatabase extends AbstractDatabase {

	public ContactDatabase(Context context, String uid, String password) {
		super(context, buildDBName(uid), password, ContactDatabaseRevision.VERSION);
	}

	public ContactDatabase(Context context, String uid, String password, boolean upgrade) {
		super(context, buildDBName(uid), password, ContactDatabaseRevision.VERSION, upgrade);
	}

	private static String buildDBName(String uid) {
		return String.format("%s/%s", uid, ContactDatabaseRevision.DB_NAME);
	}

	@Override
	protected DatabaseRevision getDatabaseRevision() {
		return ContactDatabaseRevision.INSTANCE;
	}
}
