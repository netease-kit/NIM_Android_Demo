package com.netease.nim.demo.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.NimApplication;
import com.netease.nim.demo.R;
import com.netease.nim.demo.common.ui.viewpager.FadeInOutPageTransformer;
import com.netease.nim.demo.common.ui.viewpager.PagerSlidingTabStrip;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.contact.activity.AddFriendActivity;
import com.netease.nim.demo.login.LoginActivity;
import com.netease.nim.demo.login.LogoutHelper;
import com.netease.nim.demo.main.adapter.MainTabPagerAdapter;
import com.netease.nim.demo.main.helper.CustomNotificationCache;
import com.netease.nim.demo.main.helper.SystemMessageUnreadManager;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderItem;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.team.TeamCreateHelper;
import com.netease.nim.demo.team.activity.AdvancedTeamSearchActivity;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.nertc.model.ProfileManager;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.TokenService;
import com.netease.yunxin.nertc.nertcvideocall.model.UIService;
import com.netease.yunxin.nertc.nertcvideocall.model.VideoCallOptions;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.UIServiceManager;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.ui.NERTCVideoCallActivity;
import com.netease.yunxin.nertc.ui.team.TeamG2Activity;
import com.qiyukf.unicorn.ysfkit.unicorn.api.Unicorn;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends UI implements ViewPager.OnPageChangeListener,
        ReminderManager.UnreadNumChangedCallback {

    private static final String TAG = "MainActivity";

    private static final String EXTRA_APP_QUIT = "APP_QUIT";

    private static final int REQUEST_CODE_NORMAL = 1;

    private static final int REQUEST_CODE_ADVANCED = 2;

    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;

    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private PagerSlidingTabStrip tabs;

    private ViewPager pager;

    private int scrollState;

    private MainTabPagerAdapter adapter;


    private boolean isFirstIn;

    private Observer<Integer> sysMsgUnreadCountChangedObserver = (Observer<Integer>) unreadCount -> {
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unreadCount);
        ReminderManager.getInstance().updateContactUnreadNum(unreadCount);
    };


    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
        setTitle(R.string.app_name);
        isFirstIn = true;
        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return;
        }
        init();

        // 初始化G2组件
        initG2();
    }

    private void initG2() {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                if (statusCode == StatusCode.LOGINED) {
                    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(this, false);

                    // TODO G2 用户根据实际配置方式获取
                    LoginInfo loginInfo = NimApplication.getLoginInfo();
                    if (loginInfo == null) {
                        return;
                    }

                    String imAccount = loginInfo.getAccount();
                    String imToken = loginInfo.getToken();

                    ApplicationInfo appInfo = null;
                    try {
                        // TODO G2 用户根据实际配置方式获取
                        appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                        String appKey = appInfo.metaData.getString("com.netease.nim.appKey");

                        NERTCVideoCall.sharedInstance().setupAppKey(getApplicationContext(), appKey, new VideoCallOptions(null, new UIService() {
                            @Override
                            public Class getOneToOneAudioChat() {
                                return NERTCVideoCallActivity.class;
                            }

                            @Override
                            public Class getOneToOneVideoChat() {
                                return NERTCVideoCallActivity.class;
                            }

                            @Override
                            public Class getGroupVideoChat() {
                                return TeamG2Activity.class;
                            }

                            @Override
                            public int getNotificationIcon() {
                                return R.drawable.ic_logo;
                            }

                            @Override
                            public int getNotificationSmallIcon() {
                                return R.drawable.ic_logo;
                            }

                            @Override
                            public void startContactSelector(Context context, String teamId, List<String> excludeUserList, int requestCode) {

                            }
                        }, ProfileManager.getInstance()));

                        NERTCVideoCall.sharedInstance().login(imAccount, imToken, new RequestCallback<LoginInfo>() {
                            @Override
                            public void onSuccess(LoginInfo param) {

                            }

                            @Override
                            public void onFailed(int code) {

                            }

                            @Override
                            public void onException(Throwable exception) {

                            }
                        });

                        //注册获取token的服务
                        //在线上环境中，token的获取需要放到您的应用服务端完成，然后由服务器通过安全通道把token传递给客户端
                        //Demo中使用的URL仅仅是demoserver，不要在您的应用中使用
                        //详细请参考: http://dev.netease.im/docs?doc=server
                        NERTCVideoCall.sharedInstance().setTokenService((uid, callback) -> {
                            String demoServer = "https://nrtc.netease.im/demo/getChecksum.action";
                            new Thread(() -> {
                                try {
                                    String queryString = demoServer + "?uid=" +
                                            uid + "&appkey=" + appKey;
                                    URL requestedUrl = new URL(queryString);
                                    HttpURLConnection connection = (HttpURLConnection) requestedUrl.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setConnectTimeout(6000);
                                    connection.setReadTimeout(6000);
                                    if (connection.getResponseCode() == 200) {
                                        String result = readFully(connection.getInputStream());
                                        Log.d("Demo", result);
                                        if (!TextUtils.isEmpty(result)) {
                                            org.json.JSONObject object = new org.json.JSONObject(result);
                                            int code = object.getInt("code");
                                            if (code == 200) {
                                                String token = object.getString("checksum");
                                                if (!TextUtils.isEmpty(token)) {
                                                    new Handler(getMainLooper()).post(() -> {
                                                        callback.onSuccess(token);
                                                    });
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                new Handler(getMainLooper()).post(() -> {
                                    //fixme 此处因为demo可以走非安全模式所以返回null，线上环境请在此处走 onFailed 逻辑
                                    callback.onSuccess(null);
//                                    callback.onFailed(-1);
                                });
                            }).start();
                        });

                        Intent intent = getIntent();
                        NimLog.d(TAG, String.format("onNotificationClicked INVENT_NOTIFICATION_FLAG:%s", intent.hasExtra(CallParams.INVENT_NOTIFICATION_FLAG)));
                        if (intent.hasExtra(CallParams.INVENT_NOTIFICATION_FLAG) && intent.getBooleanExtra(CallParams.INVENT_NOTIFICATION_FLAG, false)) {
                            Bundle extraIntent = intent.getBundleExtra(CallParams.INVENT_NOTIFICATION_EXTRA);
                            intent.removeExtra(CallParams.INVENT_NOTIFICATION_FLAG);
                            intent.removeExtra(CallParams.INVENT_NOTIFICATION_EXTRA);

                            Intent avChatIntent = new Intent();
                            for (String key : CallParams.CallParamKeys) {
                                avChatIntent.putExtra(key, extraIntent.getString(key));
                            }

                            String callType = extraIntent.getString(CallParams.INVENT_CALL_TYPE);
                            String channelType = extraIntent.getString(CallParams.INVENT_CHANNEL_TYPE);
                            NimLog.d(TAG, String.format("onNotificationClicked callType:%s channelType:%s", callType, channelType));

                            if (TextUtils.equals(String.valueOf(CallParams.CallType.TEAM), callType)) {
                                avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getGroupVideoChat());

                                try {
                                    String userIdsBase64 = extraIntent.getString(CallParams.INVENT_USER_IDS);
                                    String userIdsJson = new String(Base64.decode(userIdsBase64, Base64.DEFAULT));
                                    JSONArray jsonArray = new JSONArray(userIdsJson);

                                    ArrayList<String> userIds = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        String userId = jsonArray.getString(i);
                                        userIds.add(userId);
                                    }

                                    String fromAccountId = extraIntent.getString(CallParams.INVENT_FROM_ACCOUNT_ID);
                                    userIds.add(fromAccountId);

                                    avChatIntent.putExtra(CallParams.INVENT_USER_IDS, userIds);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    NimLog.e(TAG, "onNotificationClicked Exception:" + e);
                                }
                            } else {
                                if (TextUtils.equals(String.valueOf(ChannelType.AUDIO.getValue()), channelType)) {
                                    avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getOneToOneAudioChat());
                                } else {
                                    avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getOneToOneVideoChat());
                                }
                            }

                            avChatIntent.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                            startActivity(avChatIntent);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, true);
    }

    private String readFully(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return "";
        }

        ByteArrayOutputStream byteArrayOutputStream;

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int available;

            while ((available = bufferedInputStream.read(buffer)) >= 0) {
                byteArrayOutputStream.write(buffer, 0, available);
            }

            return byteArrayOutputStream.toString();

        } finally {
            bufferedInputStream.close();
        }
    }

    private void init() {
        observerSyncDataComplete();
        findViews();
        setupPager();
        setupTabs();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        registerCustomMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
        requestBasicPermission();
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        NimLog.d("G2", String.format("parseIntent INVENT_NOTIFICATION_FLAG:%s", intent.hasExtra(CallParams.INVENT_NOTIFICATION_FLAG)));

        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT);
            onLogout();
            return true;
        }
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) intent.getSerializableExtra(
                    NimIntent.EXTRA_NOTIFY_CONTENT);
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
            }
            return true;
        }
        return false;
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance()
                                                           .observeSyncDataCompletedEvent(
                                                                   (Observer<Void>) v -> DialogMaker
                                                                           .dismissProgressDialog());
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data))
                       .setCanceledOnTouchOutside(false);
        }
    }

    private void findViews() {
        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
    }

    private void setupPager() {
        adapter = new MainTabPagerAdapter(getSupportFragmentManager(), this, pager);
        pager.setOffscreenPageLimit(adapter.getCacheCount());
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
    }

    private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {

            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.tab_layout_main;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnTabClickListener(adapter);
        tabs.setOnTabDoubleTapListener(adapter);
    }


    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(
                sysMsgUnreadCountChangedObserver, register);
    }

    // sample
    Observer<CustomNotification> customNotificationObserver = (Observer<CustomNotification>) notification -> {
        // 处理自定义通知消息
        LogUtil.i("demo", "receive custom notification: " + notification.getContent() + " from :" +
                          notification.getSessionId() + "/" + notification.getSessionType() +
                          "unread=" + (notification.getConfig() == null ? "" : notification.getConfig().enableUnreadCount +  " " + "push=" +
                          notification.getConfig().enablePush + " nick=" +
                          notification.getConfig().enablePushNick));
        try {
            JSONObject obj = JSONObject.parseObject(notification.getContent());
            if (obj != null && obj.getIntValue("id") == 2) {
                // 加入缓存中
                CustomNotificationCache.getInstance().addCustomNotification(notification);
                // Toast
                String content = obj.getString("content");
                String tip = String.format("自定义消息[%s]：%s", notification.getFromAccount(), content);
                ToastHelper.showToast(MainActivity.this, tip);
            }
        } catch (JSONException e) {
            LogUtil.e("demo", e.getMessage());
        }
    };

    private void registerCustomMessageObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(
                customNotificationObserver, register);
    }

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class)
                              .querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }

    //初始化未读红点动画
    private void initUnreadCover() {
        DropManager.getInstance().init(this, findView(R.id.unread_cover), (id, explosive) -> {
            if (id == null || !explosive) {
                return;
            }
            if (id instanceof RecentContact) {
                RecentContact r = (RecentContact) id;
                NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(),
                                                                        r.getSessionType());
                return;
            }
            if (id instanceof String) {
                if (((String) id).contentEquals("0")) {
                    NIMClient.getService(MsgService.class).clearAllUnreadCount();
                } else if (((String) id).contentEquals("1")) {
                    NIMClient.getService(SystemMessageService.class)
                             .resetSystemMessageUnreadCount();
                }
            }
        });
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this).setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                   .permissions(BASIC_PERMISSIONS).request();
    }

    private void onLogout() {
        Preferences.saveUserToken("");
        // 清理缓存&注销监听
        LogoutHelper.logout();
        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    private void selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
        }
    }

    /**
     * 设置最近联系人的消息为已读
     * <p>
     * account, 聊天对象帐号，或者以下两个值：
     * {@link MsgService#MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * {@link MsgService#MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     */
    private void enableMsgNotification(boolean enable) {
        boolean msg = (pager.getCurrentItem() != MainTab.RECENT_CONTACTS.tabIndex);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.view_cloud_session:
                RecentSessionActivity.start(this);
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null,
                                                                                              50);
                NimUIKit.startContactSelector(MainActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper
                        .getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(MainActivity.this, advancedOption,
                                              REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            case R.id.enter_ysf:
                Unicorn.openServiceActivity(this, "七鱼测试", null);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        boolean temp = isFirstIn;
        isFirstIn = false;
        if (pager == null && temp) {
            return;
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init();
        }
        enableMsgNotification(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pager == null) {
            return;
        }
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        registerCustomMessageObservers(false);
        DropManager.getInstance().destroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_NORMAL) {
            final ArrayList<String> selected = data.getStringArrayListExtra(
                    ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
            } else {
                ToastHelper.showToast(MainActivity.this, "请选择至少一个联系人！");
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(
                    ContactSelectActivity.RESULT_DATA);
            TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
        tabs.onPageSelected(position);
        selectPage();
        enableMsgNotification(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        tabs.onPageScrollStateChanged(state);
        scrollState = state;
        selectPage();
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        MainTab tab = MainTab.fromReminderId(item.getId());
        if (tab != null) {
            tabs.updateTab(tab.tabIndex, item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            ToastHelper.showToast(this, "授权成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

}
