package com.netease.nim.demo.database.base;

import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

/**
 * a cursor wrapper to eat all DatabaseLockedException
 */
public class LockSafeCursor extends CursorWrapper {

	private Cursor cursor;

	private LockSafeCursor(Cursor cursor) {
		super(cursor);
		this.cursor = cursor;
	}

	@SuppressWarnings("resource")
	public static LockSafeCursor wrap(Cursor cursor) {
		return cursor == null ? null : new LockSafeCursor(cursor);
	}

	@Override
	public int getCount() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getCount();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public int getPosition() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getPosition();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public boolean move(int offset) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.move(offset);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}
		return false;
	}

	@Override
	public boolean moveToPosition(int position) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.moveToPosition(position);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}
		return false;
	}

	@Override
	public boolean moveToFirst() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.moveToFirst();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return false;
	}

	@Override
	public boolean moveToLast() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.moveToLast();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return false;
	}

	@Override
	public boolean moveToNext() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.moveToNext();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}
		return false;
	}

	@Override
	public boolean moveToPrevious() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.moveToPrevious();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return false;
	}

	@Override
	public int getColumnIndex(String columnName) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getColumnIndex(columnName);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}
		return -1;
	}

	@Override
	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getColumnIndexOrThrow(columnName);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return -1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getColumnName(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return null;
	}

	@Override
	public String[] getColumnNames() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getColumnNames();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return null;
	}

	@Override
	public int getColumnCount() {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getColumnCount();
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getBlob(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return null;
	}

	@Override
	public String getString(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getString(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return null;
	}

	@Override
	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				cursor.copyStringToBuffer(columnIndex, buffer);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}
	}

	@Override
	public short getShort(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getShort(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public int getInt(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getInt(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public long getLong(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getLong(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	@Override
	public float getFloat(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getFloat(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0f;
	}

	@Override
	public double getDouble(int columnIndex) {
		for (int chance = 0; chance < LOCK_RETRY_CHANCES; chance++) {
			try {
				return cursor.getDouble(columnIndex);
			} catch (RuntimeException e) {
				if (!isSQLiteDatabaseLockedException(e)) {
					throw e;
				}
			}
		}

		return 0;
	}

	private static final int LOCK_RETRY_CHANCES = 3;

	private static final boolean isSQLiteDatabaseLockedException(Exception e) {
		e.printStackTrace();
		if (e instanceof SQLiteException) {
			String message = e.getMessage();
			boolean locked = (!TextUtils.isEmpty(message) && message.contains("lock"));
			if (locked) {
				Log.w("db", "query locked!");
			}
			return locked;
		} else {
			return false;
		}
	}
}
