package com.netease.nim.demo.login;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;

import com.netease.nim.demo.DemoPrivatizationConfig;
import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
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
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;

    private TextView rightTopBtn;  // ActionBar完成按钮
    private TextView switchModeBtn;  // 注册/登录切换按钮

    private ClearableEditTextWithIcon loginAccountEdit;
    private ClearableEditTextWithIcon loginPasswordEdit;

    private ClearableEditTextWithIcon registerAccountEdit;
    private ClearableEditTextWithIcon registerNickNameEdit;
    private ClearableEditTextWithIcon registerPasswordEdit;

    private View loginLayout;
    private View registerLayout;

    private AbortableFuture<LoginInfo> loginRequest;
    private boolean registerMode = false; // 注册模式
    private boolean registerPanelInited = false; // 注册面板是否初始化

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
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
        setupLoginPanel();
        setupRegisterPanel();
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private void requestBasicPermission() {
        MPermission.with(LoginActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        int type = NIMClient.getService(AuthService.class).getKickedClientType();
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
                String.format(getString(R.string.kickout_content), client),
                getString(R.string.ok),
                true,
                null);

    }

    /**
     * ActionBar 右上角按钮
     */
    private void initRightTopBtn() {
        rightTopBtn = addRegisterRightTopBtn(this, R.string.login);
        rightTopBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (registerMode) {
                    register();
                } else {
                    //fakeLoginTest(); // 假登录代码示例
                    login();
                }
            }
        });
    }

    /**
     * 登录面板
     */
    private void setupLoginPanel() {
        loginAccountEdit = findView(R.id.edit_login_account);
        loginPasswordEdit = findView(R.id.edit_login_password);

        loginAccountEdit.setIconResource(R.drawable.user_account_icon);
        loginPasswordEdit.setIconResource(R.drawable.user_pwd_lock_icon);

        loginAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        loginPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        loginAccountEdit.addTextChangedListener(textWatcher);
        loginPasswordEdit.addTextChangedListener(textWatcher);
        loginPasswordEdit.setOnKeyListener(this);

        String account = Preferences.getUserAccount();
        loginAccountEdit.setText(account);
    }

    /**
     * 注册面板
     */
    private void setupRegisterPanel() {
        loginLayout = findView(R.id.login_layout);
        registerLayout = findView(R.id.register_layout);
        switchModeBtn = findView(R.id.register_login_tip);

        switchModeBtn.setVisibility(DemoPrivatizationConfig.isPrivateDisable(this) ? View.VISIBLE : View.GONE);

        switchModeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode();
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (registerMode) {
                return;
            }
            // 登录模式  ，更新右上角按钮状态
            boolean isEnable = loginAccountEdit.getText().length() > 0 &&
                    loginPasswordEdit.getText().length() > 0;
            updateRightTopBtn(rightTopBtn, isEnable);
        }
    };

    private void updateRightTopBtn(TextView rightTopBtn, boolean isEnable) {
        rightTopBtn.setText(R.string.done);
        rightTopBtn.setBackgroundResource(R.drawable.g_white_btn_selector);
        rightTopBtn.setEnabled(isEnable);
        rightTopBtn.setTextColor(getResources().getColor(R.color.color_blue_0888ff));
        rightTopBtn.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
    }

    /**
     * ***************************************** 登录 **************************************
     */
    private void login() {
        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (loginRequest != null) {
                    loginRequest.abort();
                    onLoginDone();
                }
            }
        }).setCanceledOnTouchOutside(false);

        // 云信只提供消息通道，并不包含用户资料逻辑。开发者需要在管理后台或通过服务器接口将用户帐号和token同步到云信服务器。
        // 在这里直接使用同步到云信服务器的帐号和token登录。
        // 这里为了简便起见，demo就直接使用了密码的md5作为token。
        // 如果开发者直接使用这个demo，只更改appkey，然后就登入自己的账户体系的话，需要传入同步到云信服务器的token，而不是用户密码。
        final String account = loginAccountEdit.getEditableText().toString().toLowerCase();
        final String token = tokenFromPassword(loginPasswordEdit.getEditableText().toString());
        // 登录
        loginRequest = NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
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
                    ToastHelper.showToast(LoginActivity.this, R.string.login_failed);
                } else {
                    ToastHelper.showToast(LoginActivity.this, "登录失败: " + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast(LoginActivity.this, R.string.login_exception);
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
                "fe416640c8e8a72734219e1847ad2547".equals(appKey);

        return isDemo ? MD5.getStringMD5(password) : password;
    }

    private static String readAppKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ***************************************** 注册 **************************************
     */
    private void register() {
        if (!registerMode || !registerPanelInited) {
            return;
        }
        if (!checkRegisterContentValid()) {
            return;
        }

        if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
            ToastHelper.showToast(LoginActivity.this, R.string.network_is_not_available);
            return;
        }

        DialogMaker.showProgressDialog(this, getString(R.string.registering), false);

        // 注册流程
        final String account = registerAccountEdit.getText().toString();
        final String nickName = registerNickNameEdit.getText().toString();
        final String password = registerPasswordEdit.getText().toString();

        ContactHttpClient.getInstance().register(account, nickName, password, new ContactHttpClient.ContactHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast(LoginActivity.this, R.string.register_success);
                switchMode();  // 切换回登录
                loginAccountEdit.setText(account);
                loginPasswordEdit.setText(password);

                registerAccountEdit.setText("");
                registerNickNameEdit.setText("");
                registerPasswordEdit.setText("");

                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                ToastHelper.showToast(LoginActivity.this, getString(R.string.register_failed, String.valueOf(code), errorMsg));
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    private boolean checkRegisterContentValid() {
        if (!registerMode || !registerPanelInited) {
            return false;
        }

        // 帐号检查
        String account = registerAccountEdit.getText().toString().trim();
        if (account.length() <= 0 || account.length() > 20) {
            ToastHelper.showToast(this, R.string.register_account_tip);
            return false;
        }

        // 昵称检查
        String nick = registerNickNameEdit.getText().toString().trim();
        if (nick.length() <= 0 || nick.length() > 10) {
            ToastHelper.showToast(this, R.string.register_nick_name_tip);
            return false;
        }

        // 密码检查
        String password = registerPasswordEdit.getText().toString().trim();
        if (password.length() < 6 || password.length() > 20) {
            ToastHelper.showToast(this, R.string.register_password_tip);
            return false;
        }

        return true;
    }

    /**
     * ***************************************** 注册/登录切换 **************************************
     */
    private void switchMode() {
        registerMode = !registerMode;

        if (registerMode && !registerPanelInited) {
            registerAccountEdit = findView(R.id.edit_register_account);
            registerNickNameEdit = findView(R.id.edit_register_nickname);
            registerPasswordEdit = findView(R.id.edit_register_password);

            registerAccountEdit.setIconResource(R.drawable.user_account_icon);
            registerNickNameEdit.setIconResource(R.drawable.nick_name_icon);
            registerPasswordEdit.setIconResource(R.drawable.user_pwd_lock_icon);

            registerAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            registerNickNameEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            registerPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

            registerAccountEdit.addTextChangedListener(textWatcher);
            registerNickNameEdit.addTextChangedListener(textWatcher);
            registerPasswordEdit.addTextChangedListener(textWatcher);

            registerPanelInited = true;
        }

        setTitle(registerMode ? R.string.register : R.string.login);
        loginLayout.setVisibility(registerMode ? View.GONE : View.VISIBLE);
        registerLayout.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        switchModeBtn.setText(registerMode ? R.string.login_has_account : R.string.register);
        if (registerMode) {
            rightTopBtn.setEnabled(true);
        } else {
            boolean isEnable = loginAccountEdit.getText().length() > 0
                    && loginPasswordEdit.getText().length() > 0;
            rightTopBtn.setEnabled(isEnable);
        }
    }

    public TextView addRegisterRightTopBtn(UI activity, int strResId) {
        String text = activity.getResources().getString(strResId);
        TextView textView = findView(R.id.action_bar_right_clickable_textview);
        textView.setText(text);
        textView.setBackgroundResource(R.drawable.register_right_top_btn_selector);
        textView.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
        return textView;
    }

    /**
     * *********** 假登录示例：假登录后，可以查看该用户数据，但向云信发送数据会失败；随后手动登录后可以发数据 **************
     */
    private void fakeLoginTest() {
        // 获取账号、密码；账号用于假登录，密码在手动登录时需要
        final String account = loginAccountEdit.getEditableText().toString().toLowerCase();
        final String token = tokenFromPassword(loginPasswordEdit.getEditableText().toString());

        // 执行假登录
        boolean res = NIMClient.getService(AuthService.class).openLocalCache(account); // SDK会将DB打开，支持查询。
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
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(account, token));
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
            }
        }, 15 * 1000);
    }
}
