package com.netease.nim.demo.database.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseHelper {
	private static final String TAG = "db";

	private static final int LOCK_RETRY_CHANCES = 3;

	public static final Cursor rawQuery(SQLiteDatabase db, String sql) {
		Cursor cursor = null;

		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			boolean locked = false;

			try {
				cursor = db.rawQuery(sql, null);
			} catch (SQLiteException e) {
				// trace
				e.printStackTrace();
				Log.e(TAG, "exec sql exception: " + e);

				// database locked
				locked = isSQLiteDatabaseLockedException(e);
			} catch (Exception e) {
				// trace
				e.printStackTrace();
			}

			if (locked) {
				Log.w(TAG, "locked");
			}

			if (cursor != null || !locked) {
				break;
			}
		}

		return LockSafeCursor.wrap(cursor);
	}

	public static final void exeSQL(SQLiteDatabase db, String sql) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			boolean ok = false;
			boolean locked = false;

			try {
				db.execSQL(sql);

				// well
				ok = true;
			} catch (SQLiteException e) {
				// trace
				e.printStackTrace();
				Log.e(TAG, "exec sql exception: " + e);

				// database locked
				locked = isSQLiteDatabaseLockedException(e);
			} catch (Exception e) {
				// trace
				e.printStackTrace();
			}

			if (locked) {
				Log.w(TAG, "locked");
			}

			if (ok || !locked) {
				break;
			}
		}
	}

	public static String escapeQuotes(String field) {
		if (field == null || field.length() == 0)
			return "";
		field = field.replace("'", "''");
		return field;
	}

	public static String nullStringToEmpty(String str) {
		return str == null ? "" : str;
	}

	public static boolean isTableExists(SQLiteDatabase db, String tableName) {
		if (tableName == null || db == null || !db.isOpen()) {
			return false;
		}

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type=? AND name=?", new String[] {
				"table", tableName });
		if (cursor == null || !cursor.moveToFirst()) {
			return false;
		}

		int count = cursor.getInt(0);
		cursor.close();
		return count > 0;
	}

	public static boolean checkIntegrity(SQLiteDatabase db) {
		if (db == null || !db.isOpen()) {
			return false;
		}

		String sql = "PRAGMA quick_check";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null || !cursor.moveToFirst()) {
			return false;
		}

		boolean ok = false;
		if (cursor.getCount() == 1) {
			String result = cursor.getString(0);
			ok = result.equalsIgnoreCase("ok");
		}
		cursor.close();

		return ok;

	}

	public static final boolean isSQLiteDatabaseLockedException(SQLiteException e) {
		String message = e.getMessage();

		return !TextUtils.isEmpty(message) && message.contains("lock");
	}
}
