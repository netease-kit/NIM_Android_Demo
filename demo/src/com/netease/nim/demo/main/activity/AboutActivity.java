package com.netease.nim.demo.main.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.netease.nim.demo.BuildConfig;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;

public class AboutActivity extends UI {

    private TextView version;
    private TextView versionGit;
    private TextView versionDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        version = findViewById(R.id.version_detail);
        versionGit = findViewById(R.id.version_detail_git);
        versionDate = findViewById(R.id.version_detail_date);
    }

    private void initViewData() {
        version.setText("Version: " + BuildConfig.VERSION_NAME);
        versionGit.setText("Git Version: " + BuildConfig.GIT_REVISION);
        versionDate.setText("Build Date:" + BuildConfig.BUILD_DATE);
    }
}
