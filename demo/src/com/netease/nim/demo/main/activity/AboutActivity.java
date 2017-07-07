package com.netease.nim.demo.main.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.netease.nim.demo.BuildConfig;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.robot.parser.elements.group.TemplateRoot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutActivity extends UI {

    private TextView versionGit;
    private TextView versionDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        ToolBarOptions options = new ToolBarOptions();
        setToolBar(R.id.toolbar, options);

        findViews();
        initViewData();

        test();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        versionGit = (TextView) findViewById(R.id.version_detail_git);
        versionDate = (TextView) findViewById(R.id.version_detail_date);

//        CustomActions.customButton((Button) findViewById(R.id.about_custom_button_1));
    }

    private void initViewData() {
        // 如果使用的IDE是Eclipse， 将该函数体注释掉。这里使用了Android Studio编译期添加BuildConfig字段的特性
        versionGit.setText("Git Version: " + BuildConfig.GIT_REVISION);
        versionDate.setText("Build Date:" + BuildConfig.BUILD_DATE);
    }

    private void test() {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("xmldata")));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String xml = sb.toString();
        Log.i("huangjun", "xml --------------------------");
        Log.i("huangjun", xml);
        Log.i("huangjun", "json --------------------------");
        TemplateRoot templateRoot = new TemplateRoot(xml);
        Log.i("huangjun", templateRoot.toString());
    }


}
