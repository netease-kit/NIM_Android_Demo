package com.netease.nim.demo.config.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.DemoCache;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class UserPreferences {

    private final static String KEY_DOWNTIME_TOGGLE = "down_time_toggle";

    private final static String KEY_SB_NOTIFY_TOGGLE = "sb_notify_toggle";

    private final static String KEY_TEAM_ANNOUNCE_CLOSED = "team_announce_closed";

    private final static String KEY_STATUS_BAR_NOTIFICATION_CONFIG = "KEY_STATUS_BAR_NOTIFICATION_CONFIG";

    // 测试过滤通知
    private final static String KEY_MSG_IGNORE = "KEY_MSG_IGNORE";

    // 响铃配置
    private final static String KEY_RING_TOGGLE = "KEY_RING_TOGGLE";

    // 震动配置
    private final static String KEY_VIBRATE_TOGGLE = "KEY_VIBRATE_TOGGLE";

    // 呼吸灯配置
    private final static String KEY_LED_TOGGLE = "KEY_LED_TOGGLE";

    // 通知栏标题配置
    private final static String KEY_NOTICE_CONTENT_TOGGLE = "KEY_NOTICE_CONTENT_TOGGLE";

    // 删除好友同时删除备注
    private final static String KEY_DELETE_FRIEND_AND_DELETE_ALIAS = "KEY_DELETE_FRIEND_AND_DELETE_ALIAS";

    // 通知栏样式（展开、折叠）配置
    private final static String KEY_NOTIFICATION_FOLDED_TOGGLE = "KEY_NOTIFICATION_FOLDED";

    // 保存在线状态订阅时间
    private final static String KEY_SUBSCRIBE_TIME = "KEY_SUBSCRIBE_TIME";

    /*************************no disturb begin***************************************/
    public static final String DOWN_TIME_BEGIN = "downTimeBegin";

    public static final String DOWN_TIME_END = "downTimeEnd";

    public static final String DOWN_TIME_TOGGLE = "downTimeToggle";

    public static final String DOWN_TIME_ENABLE_NOTIFICATION = "downTimeEnableNotification";

    public static final String RING = "ring";

    public static final String VIBRATE = "vibrate";

    public static final String NOTIFICATION_SMALL_ICON_ID = "notificationSmallIconId";

    public static final String NOTIFICATION_SOUND = "notificationSound";

    public static final String HIDE_CONTENT = "hideContent";

    public static final String LEDARGB = "ledargb";

    public static final String LEDONMS = "ledonms";

    public static final String LEDOFFMS = "ledoffms";

    public static final String TITLE_ONLY_SHOW_APP_NAME = "titleOnlyShowAppName";

    public static final String NOTIFICATION_FOLDED = "notificationFolded";

    public static final String NOTIFICATION_ENTRANCE = "notificationEntrance";

    public static final String NOTIFICATION_COLOR = "notificationColor";
    /**************************no disturb end************************************/

    public static void setMsgIgnore(boolean enable) {
        saveBoolean(KEY_MSG_IGNORE, enable);
    }

    public static boolean getMsgIgnore() {
        return getBoolean(KEY_MSG_IGNORE, false);
    }

    public static void setNotificationToggle(boolean on) {
        saveBoolean(KEY_SB_NOTIFY_TOGGLE, on);
    }

    public static boolean getNotificationToggle() {
        return getBoolean(KEY_SB_NOTIFY_TOGGLE, true);
    }

    public static void setRingToggle(boolean on) {
        saveBoolean(KEY_RING_TOGGLE, on);
    }

    public static boolean getRingToggle() {
        return getBoolean(KEY_RING_TOGGLE, true);
    }

    public static void setVibrateToggle(boolean on) {
        saveBoolean(KEY_VIBRATE_TOGGLE, on);
    }

    public static boolean getVibrateToggle() {
        return getBoolean(KEY_VIBRATE_TOGGLE, true);
    }

    public static void setLedToggle(boolean on) {
        saveBoolean(KEY_LED_TOGGLE, on);
    }

    public static boolean getLedToggle() {
        return getBoolean(KEY_LED_TOGGLE, true);
    }

    public static boolean getNoticeContentToggle() {
        return getBoolean(KEY_NOTICE_CONTENT_TOGGLE, false);
    }


    public static void setNoticeContentToggle(boolean on) {
        saveBoolean(KEY_NOTICE_CONTENT_TOGGLE, on);
    }

    public static boolean isDeleteFriendAndDeleteAlias() {
        return getBoolean(KEY_DELETE_FRIEND_AND_DELETE_ALIAS, false);
    }

    public static void setDeleteFriendAndDeleteAlias(boolean on) {
        saveBoolean(KEY_DELETE_FRIEND_AND_DELETE_ALIAS, on);
    }

    public static void setDownTimeToggle(boolean on) {
        saveBoolean(KEY_DOWNTIME_TOGGLE, on);
    }

    public static boolean getDownTimeToggle() {
        return getBoolean(KEY_DOWNTIME_TOGGLE, false);
    }

    public static void setNotificationFoldedToggle(boolean folded) {
        saveBoolean(KEY_NOTIFICATION_FOLDED_TOGGLE, folded);
    }

    public static boolean getNotificationFoldedToggle() {
        return getBoolean(KEY_NOTIFICATION_FOLDED_TOGGLE, true);
    }

    public static void setStatusConfig(StatusBarNotificationConfig config) {
        saveStatusBarNotificationConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG, config);
    }

    public static StatusBarNotificationConfig getStatusConfig() {
        return getConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG);
    }

    public static void setTeamAnnounceClosed(String teamId, boolean closed) {
        saveBoolean(KEY_TEAM_ANNOUNCE_CLOSED + teamId, closed);
    }

    public static boolean getTeamAnnounceClosed(String teamId) {
        return getBoolean(KEY_TEAM_ANNOUNCE_CLOSED + teamId, false);
    }

    public static void setOnlineStateSubsTime(long time) {
        saveLong(KEY_SUBSCRIBE_TIME, time);
    }

    public static long getOnlineStateSubsTime() {
        return getLong(KEY_SUBSCRIBE_TIME, 0);
    }

    private static StatusBarNotificationConfig getConfig(String key) {
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        String jsonString = getSharedPreferences().getString(key, "");
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            if (jsonObject == null) {
                return null;
            }
            config.downTimeBegin = jsonObject.getString(DOWN_TIME_BEGIN);
            config.downTimeEnd = jsonObject.getString(DOWN_TIME_END);
            config.downTimeToggle = jsonObject.getBoolean(DOWN_TIME_TOGGLE);

            Boolean downTimeEnableNotification = jsonObject.getBoolean(DOWN_TIME_ENABLE_NOTIFICATION);
            config.downTimeEnableNotification = downTimeEnableNotification == null ? true : downTimeEnableNotification;
            Boolean ring = jsonObject.getBoolean(RING);
            config.ring = ring == null ? true : ring;
            Boolean vibrate = jsonObject.getBoolean(VIBRATE);
            config.vibrate = vibrate == null ? true : vibrate;

            config.notificationSmallIconId = jsonObject.getIntValue(NOTIFICATION_SMALL_ICON_ID);
            config.notificationSound = jsonObject.getString(NOTIFICATION_SOUND);
            config.hideContent = jsonObject.getBooleanValue(HIDE_CONTENT);
            config.ledARGB = jsonObject.getIntValue(LEDARGB);
            config.ledOnMs = jsonObject.getIntValue(LEDONMS);
            config.ledOffMs = jsonObject.getIntValue(LEDOFFMS);
            config.titleOnlyShowAppName = jsonObject.getBooleanValue(TITLE_ONLY_SHOW_APP_NAME);

            Boolean notificationFolded = jsonObject.getBoolean(NOTIFICATION_FOLDED);
            config.notificationFolded = notificationFolded == null ? true : notificationFolded;

            config.notificationEntrance = (Class<? extends Activity>) Class.forName(
                    jsonObject.getString(NOTIFICATION_ENTRANCE));
            config.notificationColor = jsonObject.getInteger(NOTIFICATION_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    private static void saveStatusBarNotificationConfig(String key, StatusBarNotificationConfig config) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DOWN_TIME_BEGIN, config.downTimeBegin);
            jsonObject.put(DOWN_TIME_END, config.downTimeEnd);
            jsonObject.put(DOWN_TIME_TOGGLE, config.downTimeToggle);
            jsonObject.put(DOWN_TIME_ENABLE_NOTIFICATION, config.downTimeEnableNotification);
            jsonObject.put(RING, config.ring);
            jsonObject.put(VIBRATE, config.vibrate);
            jsonObject.put(NOTIFICATION_SMALL_ICON_ID, config.notificationSmallIconId);
            jsonObject.put(NOTIFICATION_SOUND, config.notificationSound);
            jsonObject.put(HIDE_CONTENT, config.hideContent);
            jsonObject.put(LEDARGB, config.ledARGB);
            jsonObject.put(LEDONMS, config.ledOnMs);
            jsonObject.put(LEDOFFMS, config.ledOffMs);
            jsonObject.put(TITLE_ONLY_SHOW_APP_NAME, config.titleOnlyShowAppName);
            jsonObject.put(NOTIFICATION_FOLDED, config.notificationFolded);
            jsonObject.put(NOTIFICATION_ENTRANCE, config.notificationEntrance.getName());
            jsonObject.put(NOTIFICATION_COLOR, config.notificationColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(key, jsonObject.toString());
        editor.commit();
    }

    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void saveLong(String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static long getLong(String key, long value) {
        return getSharedPreferences().getLong(key, value);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo." + DemoCache.getAccount(), Context.MODE_PRIVATE);
    }
}
