package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

import com.netease.nim.demo.DemoPrivatizationConfig;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.http.NimHttpClient;
import com.netease.nim.uikit.common.ui.dialog.EasyProgressDialog;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.ServerAddresses;

import org.json.JSONException;
import org.json.JSONObject;


public class PrivatizationConfigActivity extends UI implements View.OnClickListener, SwitchButton.OnChangedListener {


    private EditText edtUrl;
    private SwitchButton enableButton;
    private EasyProgressDialog progressDialog;
    private EditText edtYsfDaUrl;
    private EditText edtYsfDefaultUrl;
    private EditText edt_appKey;

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
        edtYsfDaUrl = findView(R.id.edt_ysf_da_url);
        edtYsfDefaultUrl = findView(R.id.edt_ysf_default_url);
        enableButton = findView(R.id.privatization_enable_toggle);
        edt_appKey = findView(R.id.edt_appKey);
        findView(R.id.btn_read_config).setOnClickListener(this);
        enableButton.setOnChangedListener(this);

        progressDialog = new EasyProgressDialog(this, "正在读取配置...");
        enableButton.setCheck(!DemoPrivatizationConfig.isPrivateDisable(this));

        String url = DemoPrivatizationConfig.getConfigUrl(this);
        if (url != null) {
            edtUrl.setText(url);
        } else {
            edtUrl.setText("http://59.111.110.241:10081/lbs/demoConfig.jsp");
        }

        readLocalYsfUrlAndSetEditText();
    }

    private void readLocalYsfUrlAndSetEditText() {
        if (!TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDefalutUrlLabel(this))) {
            edtYsfDefaultUrl.setText(DemoPrivatizationConfig.getYsfDefalutUrlLabel(this));
        } else {
            edtYsfDefaultUrl.setText("http://qyqa.netease.com");
        }

        if (!TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDaUrlLabel(this))) {
            edtYsfDaUrl.setText(DemoPrivatizationConfig.getYsfDaUrlLabel(this));
        } else {
            edtYsfDaUrl.setText("http://qyqa.netease.com");
        }
    }

    private void fetchConfig() {
        String url = edtUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            ToastHelper.showToastLong(this, "请先填写配置文件URL");
            return;
        }

        saveNosUploadString(this, "netease_pomelo_nos_https_server", null);
        saveNosUploadString(this, "netease_pomelo_nos_server", null);
        saveNosUploadString(this, "netease_pomelo_nos_lbs", null);
        NimHttpClient.getInstance().init(this);
        NimHttpClient.getInstance().execute(url, null, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable e) {
                progressDialog.dismiss();
                if (code != ResponseCode.RES_SUCCESS) {
                    ToastHelper.showToastLong(PrivatizationConfigActivity.this, "读取失败 ， code = " + code);
                    return;
                }
                saveYsfPrivatizationUrl();
                String appkey = edt_appKey.getText() == null ? "" : edt_appKey.getText().toString();
                parseConfig(modifyResponseForAppkey(response, appkey));
            }
        });
    }

    private void saveYsfPrivatizationUrl() {
        if (TextUtils.isEmpty(edtYsfDaUrl.getText().toString())) {
            return;
        }
        DemoPrivatizationConfig.saveYsfDaUrl(this, edtYsfDaUrl.getText().toString());
        DemoPrivatizationConfig.saveYsfDefaultUrl(this, edtYsfDefaultUrl.getText().toString());
    }


    private void parseConfig(String response) {
        if (TextUtils.isEmpty(response)) {
            ToastHelper.showToastLong(PrivatizationConfigActivity.this, "配置失败，配置内容为空");
            return;
        }
        ServerAddresses serverAddresses = DemoPrivatizationConfig.checkConfigAndModifyConfig(response);
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

    private String modifyResponseForAppkey(String response, String appKey) {
        JSONObject jsonObject = parse(response);
        if (TextUtils.isEmpty(appKey) || jsonObject == null) {
            return response;
        }
        try {
            jsonObject.put("appkey", appKey);
        } catch (JSONException e) {

        }
        return jsonObject.toString();
    }

    private static JSONObject parse(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }
    }

    private static void saveNosUploadString(Context ctx, String key, String value) {
        try {
            SharedPreferences.Editor editor = getNosUploadSp(ctx).edit();
            editor.putString(key,
                    Base64.encodeToString(value != null ? value.getBytes() : "".getBytes(),
                            Base64.NO_WRAP));
            editor.apply();
        } catch (Exception e) {
        }
    }

    private static SharedPreferences getNosUploadSp(Context context) {
        return context.getSharedPreferences("xx_NOS_LBS", context.MODE_PRIVATE);
    }
}
