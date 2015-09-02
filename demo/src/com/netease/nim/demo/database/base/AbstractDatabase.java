package com.netease.nim.demo.database.base;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import java.io.File;

public abstract class AbstractDatabase {
    private static final String TAG = "db";

    private boolean upgrade;
    private Context context;
    private String dbName;
    private String password;
    protected final int version;

    protected SQLiteDatabase database;

    public AbstractDatabase(Context context, String dbName, String password, int dbVersion) {
        this(context, dbName, password, dbVersion, true);
    }

    public AbstractDatabase(Context context, String dbName, String password, int dbVersion, boolean upgrade) {
        this.context = context;
        this.dbName = dbName;
        this.password = password;
        this.version = dbVersion;
        this.upgrade = upgrade;

        open();
    }

    protected abstract DatabaseRevision getDatabaseRevision();

    public boolean open() {
        if (upgrade) {
            openOrUpdagrade(dbName, version);
        } else {
            openOnly(dbName, version);
        }
        return database != null;
    }

    public boolean opened() {
        return database != null;
    }

    private String getAbsDBPath(String dbName) {
        String path = context.getApplicationInfo().dataDir + "/" + dbName;
        ;

        File dbPathFile = new File(path);
        if (!dbPathFile.exists()) {
            dbPathFile.getParentFile().mkdirs();
        }

        Log.i(TAG, "ready to open db, path=" + path);
        return path;
    }

    private void openOnly(String dbName, int dbVersion) {
        try {
            this.database = SQLiteDatabase.openDatabase(getAbsDBPath(dbName), null,
                    SQLiteDatabase.OPEN_READWRITE);
            if (database.getVersion() < dbVersion) {
                // need upgrade
                database.close();
                database = null;
            }
        } catch (SQLiteException e) {
            Log.i(TAG, "open database " + dbName + " only failed: " + e);
        }
    }

    private void openOrUpdagrade(String dbName, int dbVersion) {
        try {
            this.database = SQLiteDatabase.openOrCreateDatabase(getAbsDBPath(dbName), null);
        } catch (SQLiteException e) {
            Log.i(TAG, "error=" + e.getLocalizedMessage());
        }

        int old = database.getVersion();
        if (old != dbVersion) {
            database.beginTransaction();
            try {
                if (old == 0) {
                    Log.i(TAG, "create database " + dbName);

                    onCreate();
                } else {
                    if (old < dbVersion) {
                        Log.i(TAG, "upgrade database " + dbName + " from " + old + " to " + dbVersion);

                        onUpgrade(old, dbVersion);
                    }
                }
                database.setVersion(dbVersion);
                database.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "create or upgrade database " + dbName + " error: " + e);
            } finally {
                database.endTransaction();
            }
        }

    }

    public void close() {
        if (database != null) {
            database.close();
            database = null;
        }
    }

    public void exeSQL(String sql) {
        if (database != null) {
            DatabaseHelper.exeSQL(database, sql);
        }
    }

    public Cursor rawQuery(String sql) {
        if (database != null) {
            return DatabaseHelper.rawQuery(database, sql);
        }
        return null;
    }

    private void onCreate() {
        getDatabaseRevision().onCreate(database, version);
    }

    private void onUpgrade(int oldVersion, int newVersion) {
        getDatabaseRevision().onUpgrade(database, oldVersion, newVersion);
    }
}
