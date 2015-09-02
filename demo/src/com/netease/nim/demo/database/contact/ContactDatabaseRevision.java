package com.netease.nim.demo.database.contact;


import android.database.sqlite.SQLiteDatabase;

import com.netease.nim.demo.database.base.DatabaseRevision;

public class ContactDatabaseRevision extends DatabaseRevision {

    public static final String DB_NAME = "contact.db";

    public static final int VERSION = 1;

    public static final ContactDatabaseRevision INSTANCE = new ContactDatabaseRevision();

    public ContactDatabaseRevision() {
        super(tables());
    }

    private static Table[] tables() {
        return new Table[]{buddylist()};
    }

    private static Table buddylist() {
        return new Table("buddylist").appendVersion(new Version(1) {

            @Override
            protected String[] getUpgradeSQLs(SQLiteDatabase db, Version version) {
                return null;
            }

            @Override
            protected String[] getCreateSQLs(SQLiteDatabase db) {
                return new String[]{
                        "CREATE TABLE IF NOT EXISTS " +
                                "buddylist" +
                                "(" +
                                "account Varchar(32) NOT NULL PRIMARY KEY, " +
                                "name Varchar(32), " +
                                "icon Byte, " +
                                "type Byte" +
                                ")",

                        "CREATE INDEX IF NOT EXISTS " +
                                "buddylist_name " +
                                "on buddylist(name)",
                };
            }
        });
    }
}
