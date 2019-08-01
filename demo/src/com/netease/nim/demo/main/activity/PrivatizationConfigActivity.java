package com.netease.nim.demo.main.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.netease.nim.demo.DemoPrivatizationConfig;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.http.NimHttpClient;
import com.netease.nim.uikit.common.ui.dialog.EasyProgressDialog;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.ServerAddresses;

import org.json.JSONObject;


public class PrivatizationConfigActivity extends UI implements View.OnClickListener, SwitchButton.OnChangedListener {


    private EditText edtUrl;
    private SwitchButton enableButton;
    private EasyProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privatization_config);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        setupView();
    }


    private void setupView() {
        edtUrl = findView(R.id.edt_config_url);
        enableButton = findView(R.id.privatization_enable_toggle);
        findView(R.id.btn_read_config).setOnClickListener(this);
        enableButton.setOnChangedListener(this);

        progressDialog = new EasyProgressDialog(this, "正在读取配置...");
        enableButton.setCheck(!DemoPrivatizationConfig.isPrivateDisable(this));

        String url = DemoPrivatizationConfig.getConfigUrl(this);
        if (url != null) {
            edtUrl.setText(url);
        } else {
            edtUrl.setText("http://59.111.110.17:8281/lbs/demoConfig.jsp");
        }
    }

    private void fetchConfig() {
        String url = edtUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            ToastHelper.showToastLong(this, "请先填写配置文件URL");
            return;
        }
        NimHttpClient.getInstance().init(this);
        NimHttpClient.getInstance().execute(url, null, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable e) {
                progressDialog.dismiss();
                if (code != ResponseCode.RES_SUCCESS) {
                    ToastHelper.showToastLong(PrivatizationConfigActivity.this, "读取失败 ， code = " + code);
                    return;
                }
                parseConfig(response);
            }
        });

    }

    private void parseConfig(String response) {
        if (TextUtils.isEmpty(response)) {
            ToastHelper.showToastLong(PrivatizationConfigActivity.this, "配置失败，配置内容为空");
            return;
        }
        ServerAddresses serverAddresses = DemoPrivatizationConfig.checkConfig(response);
        if (serverAddresses != null) {
            DemoPrivatizationConfig.updateConfig(response, this);
            enableButton.setCheck(true);
            DemoPrivatizationConfig.enablePrivateConfig(true, this);
            ToastHelper.showToastLong(PrivatizationConfigActivity.this, "配置成功");
            String url = edtUrl.getText().toString().trim();
            DemoPrivatizationConfig.saveConfigUrl(this, url);
        } else {
            ToastHelper.showToastLong(PrivatizationConfigActivity.this, "配置失败，Json解析错误");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_read_config) {
            fetchConfig();
        }
    }

    @Override
    public void OnChanged(View v, boolean checkState) {
        JSONObject jsonObject = DemoPrivatizationConfig.getConfig(this);
        if (jsonObject == null && checkState) {
            ToastHelper.showToastLong(this, "请先填写URL并读取配置");
            DemoPrivatizationConfig.enablePrivateConfig(false, this);
            enableButton.setCheck(false);
            return;
        }
        DemoPrivatizationConfig.enablePrivateConfig(checkState, this);
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
