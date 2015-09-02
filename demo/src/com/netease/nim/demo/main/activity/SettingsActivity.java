package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.main.adapter.SettingsAdapter;
import com.netease.nim.demo.main.setting.SettingTemplate;
import com.netease.nim.demo.main.setting.SettingType;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.msg.MsgService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/6/26.
 */
public class SettingsActivity extends TActionBarActivity implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_HEAD = 1;
    private static final int TAG_NOTICE= 2;
    private static final int TAG_NO_DISTURBE = 3;
    private static final int TAG_CLEAR = 4;
    private static final int TAG_CUSTOM_NOTIFY = 5;
    private static final int TAG_ABOUT = 6;
    ListView listView;
    SettingsAdapter adapter;
    private List<SettingTemplate> items = new ArrayList<SettingTemplate>();
    private String noDisturbTime;
    private SettingTemplate disturbItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setTitle(R.string.settings);
        initData();
        initUI();
    }

    private void initData() {
        if(UserPreferences.getStatusConfig() == null || !UserPreferences.getStatusConfig().downTimeToggle) {
            noDisturbTime = getString(R.string.setting_close);
        } else {
            noDisturbTime = String.format("%s到%s", UserPreferences.getStatusConfig().downTimeBegin,
                    UserPreferences.getStatusConfig().downTimeEnd);
        }
    }

    private void initUI() {
        initItems();
        listView = (ListView) findViewById(R.id.settings_listview);
        View footer = LayoutInflater.from(this).inflate(R.layout.settings_logout_footer, null);
        listView.addFooterView(footer);

        adapter = new SettingsAdapter(this, this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingTemplate item = items.get(position);
                onListItemClick(item);
            }
        });
        View logoutBtn = footer.findViewById(R.id.settings_button_logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void initItems() {
        items.clear();

        items.add(new SettingTemplate(TAG_HEAD, SettingType.TYPE_HEAD));
        items.add(new SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_TOGGLE, UserPreferences.getNotificationToggle()));
        items.add(SettingTemplate.makeSeperator());
        disturbItem = new SettingTemplate(TAG_NO_DISTURBE, getString(R.string.no_disturb), noDisturbTime);
        items.add(disturbItem);
        items.add(SettingTemplate.makeSeperator());
        items.add(new SettingTemplate(TAG_CLEAR, getString(R.string.about_clear_msg_history)));
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_CUSTOM_NOTIFY, getString(R.string.custom_notification)));
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_ABOUT, getString(R.string.setting_about)));
    }

    private void onListItemClick(SettingTemplate item) {
        if(item == null) return;

        switch (item.getId()) {
            case TAG_HEAD:
//                EditNicknameActivity.startActivityForResult(this, EditNicknameActivity.RES_CODE); 本期不做
                break;
            case TAG_NOTICE:
                break;
            case TAG_NO_DISTURBE:
                startNoDisturb();
                break;
            case TAG_CUSTOM_NOTIFY:
                CustomNotificationActivity.start(SettingsActivity.this);
                break;
            case TAG_ABOUT:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
            case TAG_CLEAR:
                NIMClient.getService(MsgService.class).clearMsgDatabase(true);
                Toast.makeText(SettingsActivity.this, R.string.clear_msg_history_success, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 注销
     */
    private void logout() {
        removeLoginState();
        MainActivity.logout(SettingsActivity.this, false);

        finish();
        NIMClient.getService(AuthService.class).logout();
    }

    /**
     * 清除登陆状态
     */
    private void removeLoginState() {
        Preferences.saveUserToken("");
    }

    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NOTICE:
                try {
                    setNotificationToggle(checkState);
                    NIMClient.toggleNotification(checkState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void setNotificationToggle(boolean on) {
        UserPreferences.setNotificationToggle(on);
    }

    private void startNoDisturb() {
        NoDisturbActivity.startActivityForResult(this, UserPreferences.getStatusConfig(), noDisturbTime, NoDisturbActivity.NO_DISTURB_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case NoDisturbActivity.NO_DISTURB_REQ:
                    setNoDisturbTime(data);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置免打扰时间
     * @param data
     */
    private void setNoDisturbTime(Intent data) {
        boolean isChecked = data.getBooleanExtra(NoDisturbActivity.EXTRA_ISCHECKED, false);
        noDisturbTime = getString(R.string.setting_close);
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        if(isChecked) {
            config.downTimeBegin = data.getStringExtra(NoDisturbActivity.EXTRA_START_TIME);
            config.downTimeEnd = data.getStringExtra(NoDisturbActivity.EXTRA_END_TIME);
            noDisturbTime = String.format("%s到%s", config.downTimeBegin, config.downTimeEnd);
        } else {
            config.downTimeBegin = null;
            config.downTimeEnd = null;
        }
        disturbItem.setDetail(noDisturbTime);
        adapter.notifyDataSetChanged();
        UserPreferences.setDownTimeToggle(isChecked);
        config.downTimeToggle = isChecked;
        UserPreferences.setStatusConfig(config);
        NIMClient.updateStatusBarNotificationConfig(config);
    }
}
