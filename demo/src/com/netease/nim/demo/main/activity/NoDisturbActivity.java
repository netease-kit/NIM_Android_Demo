package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.main.adapter.SettingsAdapter;
import com.netease.nim.demo.main.model.SettingTemplate;
import com.netease.nim.demo.main.model.SettingType;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/7/3.
 */
public class NoDisturbActivity extends TActionBarActivity implements SettingsAdapter.SwitchChangeListener, View.OnClickListener{
    public static final int NO_DISTURB_REQ = 0x01;
    private static final int TAG_NO_DISTURB = 1;
    private static final String EXTRA_TIME = "EXTRA_TIME";
    public static final String EXTRA_START_TIME = "EXTRA_START_TIME";
    public static final String EXTRA_END_TIME = "EXTRA_END_TIME";
    public static final String EXTRA_CONFIG = "EXTRA_CONFIG";
    public static final String EXTRA_ISCHECKED = "EXTRA_ISCHECKED";

    private ListView noDisturbList;
    private List<SettingTemplate> items = new ArrayList<>();
    private SettingsAdapter adapter;

    private String startTime;
    private String endTime;
    private boolean ischecked = false;

    private View timeLayout;
    private TextView startText;
    private TextView endText;
    private RelativeLayout startLayout;
    private RelativeLayout endLayout;

    public static void startActivityForResult(Activity activity, StatusBarNotificationConfig config, String time, int reqcode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, time);
        intent.putExtra(EXTRA_CONFIG, config);
        intent.setClass(activity, NoDisturbActivity.class);
        activity.startActivityForResult(intent, reqcode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_disturb_activity);
        setTitle(R.string.no_disturb);
        parseIntentData();
        findViews();
    }

    private void findViews() {
        initItems();
        noDisturbList = (ListView) findViewById(R.id.no_disturb_list);
        initFooter();

        adapter = new SettingsAdapter(this, this, items);
        noDisturbList.setAdapter(adapter);
        startLayout.setOnClickListener(this);
        endLayout.setOnClickListener(this);
    }

    private void initFooter() {
        View footer = LayoutInflater.from(this).inflate(R.layout.no_disturb_footer, null);
        timeLayout = footer.findViewById(R.id.time_layout);
        startLayout = (RelativeLayout) footer.findViewById(R.id.start_time_layout);
        endLayout = (RelativeLayout) footer.findViewById(R.id.end_time_layout);
        startText = (TextView) footer.findViewById(R.id.start_time_value);
        endText = (TextView) footer.findViewById(R.id.end_time_value);
        noDisturbList.addFooterView(footer);
        if(ischecked) {
            showTimeSetting();
        } else {
            closeTimeSetting();
        }
    }

    private void initItems() {
        items.clear();
        items.add(new SettingTemplate(TAG_NO_DISTURB, getString(R.string.no_disturb), SettingType.TYPE_TOGGLE, UserPreferences.getDownTimeToggle()));
        items.add(SettingTemplate.addLine());
    }

    private void parseIntentData() {
        StatusBarNotificationConfig config = (StatusBarNotificationConfig) getIntent().getSerializableExtra(EXTRA_CONFIG);
        if (config != null) {
            ischecked = config.downTimeToggle;
        }

        if(ischecked) {
            String time = getIntent().getStringExtra(EXTRA_TIME);
            startTime = time.substring(0, 5);
            endTime = time.substring(6, 11);
        }

    }

    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NO_DISTURB:
                try {
                    ischecked = checkState;
                    UserPreferences.setDownTimeToggle(checkState);
                    StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                    if (config == null) {
                        config = new StatusBarNotificationConfig();
                    }
                    config.downTimeToggle = checkState;
                    UserPreferences.setStatusConfig(config);
                    NIMClient.updateStatusBarNotificationConfig(config);
                    if(checkState) {
                        showTimeSetting();
                    } else {
                        closeTimeSetting();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void showTimeSetting() {
        timeLayout.setVisibility(View.VISIBLE);
        if(startTime == null || endTime == null) {
            startTime = getString(R.string.time_from_default);
            endTime = getString(R.string.time_to_default);
        }
        startText.setText(startTime);
        endText.setText(endTime);
    }

    private void closeTimeSetting() {
        timeLayout.setVisibility(View.GONE);
    }

    private void openFromTimePicker(final boolean isStartTime, String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3, 5));
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(isStartTime) {
                    startTime = String.format("%02d:%02d", hourOfDay, minute);
                    startText.setText(startTime);
                } else {
                    endTime = String.format("%02d:%02d", hourOfDay, minute);
                    endText.setText(endTime);
                }

            }
        }, hour, minute, true);
        timePicker.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_time_layout:
                openFromTimePicker(true, startText.getText().toString());
                break;
            case R.id.end_time_layout:
                openFromTimePicker(false, endText.getText().toString());
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if(ischecked) {
            intent.putExtra(EXTRA_START_TIME, startText.getText().toString());
            intent.putExtra(EXTRA_END_TIME, endText.getText().toString());
        }
        intent.putExtra(EXTRA_ISCHECKED, ischecked);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
