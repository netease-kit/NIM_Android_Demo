package com.netease.nim.demo.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.DemoPrivatizationConfig;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.main.activity.PrivatizationConfigActivity;
import com.netease.nim.demo.network.DemoAuthService;
import com.netease.nim.demo.network.NetResponse;
import com.netease.nim.demo.network.UserData;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;

/**
 * 登录/注册界面
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class LoginActivity extends UI implements OnKeyListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String KICK_OUT = "KICK_OUT";
    private static final String KICK_OUT_DESC = "KICK_OUT_DESC";
    /**
     * 验证码登录
     */
    private static final int TYPE_LOGIN_SMS_CODE = 1;
    /**
     * 账号密码登录
     */
    private static final int TYPE_LOGIN_ACCOUNT_ID = 2;
    /**
     * 注册新账号
     */
    private static final int TYPE_REGISTER = 3;

    private final int BASIC_PERMISSION_REQUEST_CODE = 110;

    private TextView rightTopBtn;  // ActionBar完成按钮

    private TextView loginTypeSwitchBtn; // 登录方式验证码/账号密码登录切换按钮

    private ClearableEditTextWithIcon loginPhoneEdit;
    private ClearableEditTextWithIcon loginSmsCodeEdit;
    private TextView loginSmsCodeTv;

    private ClearableEditTextWithIcon loginAccountEdit;
    private ClearableEditTextWithIcon loginPasswordEdit;

    private ClearableEditTextWithIcon registerPhoneEdit;
    private ClearableEditTextWithIcon registerNickNameEdit;
    private ClearableEditTextWithIcon registerSmsCodeEdit;
    private TextView registerSmsCodeTv;

    private View loginSmsCodeLayout;
    private View loginAccountLayout;
    private View registerLayout;
    private View registerBtn;

    private AbortableFuture<LoginInfo> loginRequest;

    private int currentType = TYPE_LOGIN_SMS_CODE;
    private final LoginCountDownTimer timer = new LoginCountDownTimer(60000, 1000,
            new LoginCountDownTimer.TickListener() {
                @Override
                public void onTick(long millisUntilFinished) {
                    registerSmsCodeTv.setEnabled(false);
                    loginSmsCodeTv.setEnabled(false);
                    String tip = getString(R.string.refetch_sms_code_later, String.valueOf(millisUntilFinished / 1000));
                    loginSmsCodeTv.setText(tip);
                    registerSmsCodeTv.setText(tip);
                }
            }, new Runnable() {
        @Override
        public void run() {
            registerSmsCodeTv.setText(R.string.fetch_sms_code);
            registerSmsCodeTv.setEnabled(true);
            loginSmsCodeTv.setText(R.string.fetch_sms_code);
            loginSmsCodeTv.setEnabled(true);
        }
    });

    public static void start(Context context) {
        start(context, false, "");
    }

    public static void start(Context context, boolean kickOut, String kickOutDesc) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        intent.putExtra(KICK_OUT_DESC, kickOutDesc);
        context.startActivity(intent);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ToolBarOptions options = new NimToolBarOptions();
        options.isNeedNavigate = false;
        options.logoId = R.drawable.actionbar_white_logo_space;
        setToolBar(R.id.toolbar, options);
        requestBasicPermission();
        onParseIntent();
        initRightTopBtn();
        initLeftTopBtn();
        setupLoginPanel();
        setupRegisterPanel();
        switchMode(TYPE_LOGIN_SMS_CODE);
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private void requestBasicPermission() {
        MPermission.with(LoginActivity.this).setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS).request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        ToastHelper.showToast(this, "授权成功");
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        ToastHelper.showToast(this, "授权失败");
    }

    private void onParseIntent() {
        if (!getIntent().getBooleanExtra(KICK_OUT, false)) {
            return;
        }

        String desc = getIntent().getStringExtra(KICK_OUT_DESC);
        if (!TextUtils.isEmpty(desc)) {
            EasyAlertDialogHelper.showOneButtonDiolag(LoginActivity.this,
                    getString(R.string.kickout_notify), desc, getString(R.string.ok),
                    true, null);
            return;
        }

        int type = NIMClient.getService(AuthService.class).getKickedClientType();
        int customType = NIMClient.getService(AuthService.class).getKickedCustomClientType();
        String client;
        switch (type) {
            case ClientType.Web:
                client = "网页端";
                break;
            case ClientType.Windows:
            case ClientType.MAC:
                client = "电脑端";
                break;
            case ClientType.REST:
                client = "服务端";
                break;
            default:
                client = "移动端";
                break;
        }
        EasyAlertDialogHelper.showOneButtonDiolag(LoginActivity.this,
                getString(R.string.kickout_notify),
                String.format(getString(R.string.kickout_content),
                        client + customType), getString(R.string.ok),
                true, null);
    }

    /**
     * ActionBar 右上角按钮
     */
    private void initLeftTopBtn() {
        TextView leftTopBtn = addRegisterLeftTopBtn(this, R.string.login_privatization_config_str);
        leftTopBtn.setOnClickListener(v -> startActivity(new Intent(this, PrivatizationConfigActivity.class)));
    }

    /**
     * ActionBar 右上角按钮
     */
    private void initRightTopBtn() {
        rightTopBtn = addRegisterRightTopBtn(this, R.string.done);
        rightTopBtn.setOnClickListener(v -> {
            if (currentType == TYPE_LOGIN_ACCOUNT_ID) {
                String account = getText(loginAccountEdit);
                String password = getText(loginPasswordEdit);
                doIMLogin(account, tokenFromPassword(password));
                return;
            }
            String phone = "";
            String smsCode = "";

            if (currentType == TYPE_REGISTER) {
                phone = getText(registerPhoneEdit);
                smsCode = getText(registerSmsCodeEdit);
            } else if (currentType == TYPE_LOGIN_SMS_CODE) {
                phone = getText(loginPhoneEdit);
                smsCode = getText(loginSmsCodeEdit);
            }
            if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(smsCode)) {
                ToastHelper.showToast(LoginActivity.this, "请填入正确信息");
                return;
            }
            if (phone.length() != 11) {
                ToastHelper.showToast(LoginActivity.this, "手机号格式不正确");
                return;
            }

            if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
                ToastHelper.showToast(LoginActivity.this, R.string.network_is_not_available);
                return;
            }

            if (currentType == TYPE_REGISTER) {
                String nickName = getText(registerNickNameEdit);
                if (TextUtils.isEmpty(nickName)) {
                    ToastHelper.showToast(LoginActivity.this, "请填入用户昵称");
                    return;
                }
                DemoAuthService.registerBySmsCode(phone, smsCode, nickName, this::handleUserResponse);
            } else if (currentType == TYPE_LOGIN_SMS_CODE) {
                DemoAuthService.loginBySmsCode(phone, smsCode, this::handleUserResponse);
            }
        });
    }

    private void handleUserResponse(NetResponse<UserData> response) {
        UserData data = response.getData();
        if (response.isSuccessful() && data != null) {
            doIMLogin(data.getAccId(), data.getToken());
        } else {
            ToastHelper.showToast(LoginActivity.this, response.getMsg());
        }
    }

    /**
     * 登录面板
     */
    private void setupLoginPanel() {
        loginAccountLayout = findView(R.id.login_account_layout);
        loginAccountEdit = findView(R.id.edit_login_account);
        loginPasswordEdit = findView(R.id.edit_login_password);
        loginAccountEdit.setIconResource(R.drawable.user_account_icon);
        loginPasswordEdit.setIconResource(R.drawable.user_pwd_lock_icon);
        loginAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        loginPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        loginAccountEdit.addTextChangedListener(textWatcher);
        loginPasswordEdit.addTextChangedListener(textWatcher);
        loginPasswordEdit.setOnKeyListener(this);

        loginSmsCodeLayout = findView(R.id.login_sms_code_layout);
        loginPhoneEdit = findView(R.id.edit_login_phone);
        loginSmsCodeEdit = findView(R.id.edit_login_sms_code);
        loginSmsCodeTv = findView(R.id.fetch_login_sms_code);
        loginPhoneEdit.setIconResource(R.drawable.user_phone_icon);
        loginSmsCodeEdit.setIconResource(R.drawable.user_pwd_lock_icon);
        loginPhoneEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        loginSmsCodeEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        loginPhoneEdit.addTextChangedListener(textWatcher);
        loginSmsCodeEdit.addTextChangedListener(textWatcher);
        loginSmsCodeEdit.setOnKeyListener(this);
        loginSmsCodeTv.setOnClickListener(v -> doFetchSmsCode(loginPhoneEdit, v));

        loginTypeSwitchBtn = findView(R.id.login_type_switch);
        loginTypeSwitchBtn.setOnClickListener(v -> {
            if (currentType == TYPE_LOGIN_SMS_CODE) {
                switchMode(TYPE_LOGIN_ACCOUNT_ID);
            } else {
                switchMode(TYPE_LOGIN_SMS_CODE);
            }
        });
    }

    /**
     * 注册面板
     */
    private void setupRegisterPanel() {
        registerLayout = findView(R.id.register_layout);

        registerPhoneEdit = findView(R.id.edit_register_phone);
        registerNickNameEdit = findView(R.id.edit_register_nickname);
        registerSmsCodeEdit = findView(R.id.edit_register_sms_code);
        registerSmsCodeTv = findView(R.id.fetch_register_sms_code);

        registerPhoneEdit.setIconResource(R.drawable.user_phone_icon);
        registerNickNameEdit.setIconResource(R.drawable.nick_name_icon);
        registerSmsCodeEdit.setIconResource(R.drawable.user_pwd_lock_icon);

        registerPhoneEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        registerNickNameEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        registerSmsCodeEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        registerPhoneEdit.addTextChangedListener(textWatcher);
        registerNickNameEdit.addTextChangedListener(textWatcher);
        registerSmsCodeEdit.addTextChangedListener(textWatcher);

        registerSmsCodeTv.setOnClickListener(v -> doFetchSmsCode(registerPhoneEdit, v));

        // 注册按钮
        registerBtn = findView(R.id.register_login_tip);
        registerBtn.setVisibility(
                DemoPrivatizationConfig.isPrivateDisable(this) ? View.VISIBLE : View.GONE);
        registerBtn.setOnClickListener(v -> switchMode(TYPE_REGISTER));
    }

    private final TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isEnable = false;

            if (currentType == TYPE_REGISTER) {
                isEnable = isNotEmptyText(registerPhoneEdit)
                        && isNotEmptyText(registerSmsCodeEdit);
            } else if (currentType == TYPE_LOGIN_SMS_CODE) {
                isEnable = isNotEmptyText(loginSmsCodeEdit)
                        && isNotEmptyText(loginPhoneEdit);
            } else if (currentType == TYPE_LOGIN_ACCOUNT_ID) {
                isEnable = isNotEmptyText(loginAccountEdit)
                        && isNotEmptyText(loginPasswordEdit);
            }

            enableView(rightTopBtn, isEnable);
        }
    };

    /**
     * ***************************************** 登录 **************************************
     */
    private void doIMLogin(String account, String token) {
        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), true, dialog -> {
            if (loginRequest != null) {
                loginRequest.abort();
                onLoginDone();
            }
        }).setCanceledOnTouchOutside(false);

        // 登录
        loginRequest = NimUIKit.login(new LoginInfo(account, token),
                new RequestCallback<LoginInfo>() {

                    @Override
                    public void onSuccess(LoginInfo param) {
                        LogUtil.i(TAG, "login success");
                        onLoginDone();
                        DemoCache.setAccount(account);
                        saveLoginInfo(account, token);
                        // 初始化消息提醒配置
                        initNotificationConfig();
                        // 进入主界面
                        MainActivity.start(LoginActivity.this, null);
                        finish();
                    }

                    @Override
                    public void onFailed(int code) {
                        onLoginDone();
                        if (code == 302 || code == 404) {
                            ToastHelper.showToast(LoginActivity.this,
                                    R.string.login_failed);
                        } else {
                            ToastHelper.showToast(LoginActivity.this,
                                    "登录失败: " + code);
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        ToastHelper.showToast(LoginActivity.this,
                                R.string.login_exception);
                        onLoginDone();
                    }
                });
    }

    private void initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
        // 加载状态栏配置
        StatusBarNotificationConfig statusBarNotificationConfig = UserPreferences.getStatusConfig();
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig();
            UserPreferences.setStatusConfig(statusBarNotificationConfig);
        }
        // 更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig);
    }

    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void saveLoginInfo(final String account, final String token) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
    }

    //DEMO中使用 username 作为 NIM 的account ，md5(password) 作为 token
    //开发者需要根据自己的实际情况配置自身用户系统和 NIM 用户系统的关系
    private String tokenFromPassword(String password) {
        String appKey = readAppKey(this);
        boolean isDemo = "45c6af3c98409b18a84451215d0bdd6e".equals(appKey) ||
                         "fe416640c8e8a72734219e1847ad2547".equals(appKey) ||
                         "a24e6c8a956a128bd50bdffe69b405ff".equals(appKey);
        return isDemo ? MD5.getStringMD5(password) : password;
    }

    private static String readAppKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ***************************************** 注册/登录切换 **************************************
     */
    private void switchMode(int type) {
        if (currentType == type) {
            return;
        }
        currentType = type;

        if (type == TYPE_LOGIN_SMS_CODE) {
            loginAccountLayout.setVisibility(View.GONE);
            loginSmsCodeLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
            loginTypeSwitchBtn.setText(R.string.login_by_accid);
            copyFromEditText(loginPhoneEdit, registerPhoneEdit);
            copyFromEditText(loginSmsCodeEdit, registerSmsCodeEdit);

            setTitle(R.string.login);
        } else if (type == TYPE_LOGIN_ACCOUNT_ID) {
            loginAccountLayout.setVisibility(View.VISIBLE);
            loginSmsCodeLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
            loginTypeSwitchBtn.setText(R.string.login_by_sms_code);
            boolean isEnable = isNotEmptyText(loginAccountEdit) &&
                    isNotEmptyText(loginPasswordEdit);
            rightTopBtn.setEnabled(isEnable);

            setTitle(R.string.login);
        } else if (type == TYPE_REGISTER) {
            loginAccountLayout.setVisibility(View.GONE);
            loginSmsCodeLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
            registerBtn.setVisibility(View.GONE);
            loginTypeSwitchBtn.setText(R.string.login_by_sms_code);
            copyFromEditText(registerPhoneEdit, loginPhoneEdit);
            copyFromEditText(registerSmsCodeEdit, loginSmsCodeEdit);

            rightTopBtn.setEnabled(true);
            setTitle(R.string.register);
        }
    }

    public TextView addRegisterRightTopBtn(UI activity, int strResId) {
        String text = activity.getResources().getString(strResId);
        TextView textView = findView(R.id.action_bar_right_clickable_textview);
        textView.setText(text);
        textView.setBackgroundResource(R.drawable.g_white_btn_selector);
        textView.setTextColor(getResources().getColor(R.color.color_blue_0888ff));
        textView.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
        return textView;
    }

    private void enableView(View view, boolean isEnable) {
        view.setEnabled(isEnable);
    }

    public TextView addRegisterLeftTopBtn(UI activity, int strResId) {
        String text = activity.getResources().getString(strResId);
        TextView textView = findView(R.id.action_bar_left_clickable_textview);
        textView.setText(text);
        textView.setBackgroundResource(R.drawable.register_right_top_btn_selector);
        textView.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
        return textView;
    }

    private void doFetchSmsCode(EditText editText, View clickView) {
        String phone = getText(editText);
        if (TextUtils.isEmpty(phone) || phone.length() != 11) {
            ToastHelper.showToast(LoginActivity.this, "手机号格式不正确");
            return;
        }
        if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
            ToastHelper.showToast(LoginActivity.this, R.string.network_is_not_available);
            return;
        }
        clickView.setEnabled(false);
        timer.start();
        DemoAuthService.fetchSmsCode(phone, response -> {
            String result;
            if (response.isSuccessful()) {
                result = "成功";
            } else {
                result = "失败：" + response.getMsg();
                timer.stop();
            }
            ToastHelper.showToast(LoginActivity.this, "发送验证码" + result);
        });
    }

    private void copyFromEditText(EditText destEdit, EditText sourceEdit) {
        String phone = getText(sourceEdit);
        if (!TextUtils.isEmpty(phone)) {
            destEdit.setText(phone);
        }
    }

    private boolean isNotEmptyText(EditText editText) {
        return !TextUtils.isEmpty(getText(editText));
    }

    private String getText(EditText editText) {
        if (editText == null) {
            return "";
        }
        Editable editable = editText.getText();
        if (editable == null) {
            return "";
        }
        return editable.toString().trim();
    }

    /**
     * *********** 假登录示例：假登录后，可以查看该用户数据，但向云信发送数据会失败；随后手动登录后可以发数据 **************
     */
    private void fakeLoginTest() {
        // 获取账号、密码；账号用于假登录，密码在手动登录时需要
        final String account = loginAccountEdit.getEditableText().toString().toLowerCase();
        final String token = tokenFromPassword(loginPasswordEdit.getEditableText().toString());
        // 执行假登录
        boolean res = NIMClient.getService(AuthService.class).openLocalCache(
                account); // SDK会将DB打开，支持查询。
        Log.i("test", "fake login " + (res ? "success" : "failed"));
        if (!res) {
            return;
        }
        // Demo缓存当前假登录的账号
        DemoCache.setAccount(account);
        // 初始化消息提醒配置
        initNotificationConfig();
        // 设置uikit
        NimUIKit.loginSuccess(account);
        // 进入主界面，此时可以查询数据（最近联系人列表、本地消息历史、群资料等都可以查询，但当云信服务器发起请求会返回408超时）
        MainActivity.start(LoginActivity.this, null);
        // 演示15s后手动登录，登录成功后，可以正常收发数据
        getHandler().postDelayed(() -> {
            loginRequest = NIMClient.getService(AuthService.class).login(
                    new LoginInfo(account, token));
            loginRequest.setCallback(new RequestCallbackWrapper() {

                @Override
                public void onResult(int code, Object result, Throwable exception) {
                    Log.i("test", "real login, code=" + code);
                    if (code == ResponseCode.RES_SUCCESS) {
                        saveLoginInfo(account, token);
                        finish();
                    }
                }
            });
        }, 15 * 1000);
    }
}
