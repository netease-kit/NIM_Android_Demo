package com.netease.nim.demo.database.contact;

import android.database.Cursor;

import com.netease.nim.demo.database.base.DatabaseHelper;
import com.netease.nim.demo.contact.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * USER（第三方APP服务器用户资料数据）
 * Created by huangjun on 2015/3/12.
 */
public class ContactDatabaseHelper {

    private ContactDatabase database;

    public ContactDatabaseHelper(ContactDatabase openedDB) {
        database = openedDB;
    }

    private static final String TABLE_NAME = "buddylist";
    private static final String COLUMNS =
            "account," +
                    "name," +
                    "icon," +
                    "type";

    public void addUsers(List<User> users) {
        String sql = "insert or replace into " + TABLE_NAME + " (" + COLUMNS + ")";
        String _sql = "";

        for (User user : users) {
            if (_sql.length() == 0) {
                _sql += " select '";
            } else {
                _sql += " union select '";
            }

            _sql += (user.getAccount() + "','" + DatabaseHelper.escapeQuotes(user.getName()) + "','"
                    + user.getIcon() + "','" + 0 + "'");

            if (_sql.length() > 10000) { // sql语句长度限制

                database.exeSQL(sql + _sql);
                _sql = "";
            }
        }

        if (_sql.length() > 0) {
            database.exeSQL(sql + _sql);
        }
    }

    /**
     * 返回所有好友（包括群成员）
     */
    public List<User> getUsers() {
        String sql = "select " + COLUMNS + " from " + TABLE_NAME;
        Cursor cursor = database.rawQuery(sql);
        if (cursor == null) {
            return new ArrayList<>();
        }

        List<User> users = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            users.add(userFromCursor(cursor));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return users;
    }

    public User getUser(String account) {
        String sql = "select " + COLUMNS + " from " + TABLE_NAME + " where account='" + account + "'";
        Cursor cursor = database.rawQuery(sql);

        if (cursor != null) {
            User user = null;
            if (cursor.moveToNext()) {
                user = userFromCursor(cursor);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return user;
        }

        return null;
    }

    public void removeUser(String account) {
        String sql = "delete from " + TABLE_NAME + " where account='" + account + "'";
        database.exeSQL(sql);
    }

    private User userFromCursor(Cursor cursor) {
        User user = new User();

        int column = 0;
        user.setAccount(cursor.getString(column++));
        user.setName(cursor.getString(column++));
        user.setIcon(cursor.getInt(column++));

        return user;
    }
}
