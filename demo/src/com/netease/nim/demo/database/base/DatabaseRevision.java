package com.netease.nim.demo.database.base;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseRevision {
    private static final String TAG = "db";

    public static abstract class Version {
        final int versionNumber;

        public Version(int versionNumber) {
            this.versionNumber = versionNumber;
        }

        protected abstract String[] getCreateSQLs(SQLiteDatabase db);

        protected abstract String[] getUpgradeSQLs(SQLiteDatabase db, Version version);

        @Override
        public String toString() {
            return Integer.toString(versionNumber);
        }
    }

    public static final class Table {
        private final String tableName;

        private final boolean stepMode;

        private final List<Version> versions = new ArrayList<Version>();

        public Table(String tableName) {
            this(tableName, true);
        }

        public Table(String tableName, boolean stepMode) {
            this.tableName = tableName;
            this.stepMode = stepMode;
        }

        public final Table appendVersion(Version vertion) {
            versions.add(vertion);

            return this;
        }

        void onCreate(SQLiteDatabase db, int ver) {
            int at = at(ver);

            // not yet
            if (at < 0) {
                return;
            }

            create(db, at);
        }

        void onCreate(SQLiteDatabase db) {
            int at = at();

            // ?
            if (at < 0) {
                return;
            }

            create(db, at);
        }

        void onUpgrade(SQLiteDatabase db, int verInitial, int verTarget) {
            int atTarget = at(verTarget);
            int atInitial = at(verInitial);

            // same
            if (atTarget == atInitial) {
                return;
            }

            // not yet
            if (atInitial < 0) {
                create(db, atTarget);
            } else if (atInitial < atTarget) {
                if (stepMode) {
                    while (atInitial < atTarget) {
                        upgrade(db, atInitial, atInitial + 1);

                        atInitial++;
                    }
                } else {
                    upgrade(db, atInitial, atTarget);
                }
            }
        }

        private int at(int dbVer) {
            int at = -1;
            for (int i = 0; i < versions.size(); i++) {
                if (dbVer >= versions.get(i).versionNumber) {
                    at = i;
                }
            }

            return at;
        }

        private int at() {
            return versions.size() - 1;
        }

        private void create(SQLiteDatabase db, int atTarget) {
            Version target = versions.get(atTarget);

            Log.i(TAG, "create:" + " table " + this + " target " + target);

            execSQLs(db, target.getCreateSQLs(db));
        }

        private void upgrade(SQLiteDatabase db, int atInitial, int atTarget) {
            Version initial = versions.get(atInitial);
            Version target = versions.get(atTarget);

            Log.i(TAG, "upgrade:" + " table " + this + " initial " + initial + " target " + target);

            execSQLs(db, target.getUpgradeSQLs(db, initial));
        }

        @Override
        public String toString() {
            return tableName;
        }

        private static void execSQLs(SQLiteDatabase db, String[] sqls) {
            if (sqls != null) {
                for (String sql : sqls) {
                    db.execSQL(sql);
                }
            }
        }
    }

    private final Table[] tables;

    public DatabaseRevision(Table[] tables) {
        this.tables = tables;
    }

    public void onCreate(SQLiteDatabase db, int ver) {
        for (Table table : tables) {
            table.onCreate(db, ver);
        }
    }

    public void onCreate(SQLiteDatabase db, String tableName) {
        Table table = table(tableName);

        if (table != null) {
            table.onCreate(db);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Table table : tables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }

    private Table table(String tableName) {
        for (Table table : tables) {
            if (table.tableName.equals(tableName)) {
                return table;
            }
        }

        return null;
    }
}
