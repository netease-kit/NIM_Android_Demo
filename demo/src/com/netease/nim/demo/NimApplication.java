package com.netease.nim.demo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.multidex.MultiDex;

import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nim.demo.chatroom.ChatRoomSessionHelper;
import com.netease.nim.demo.common.util.crash.AppCrashHandler;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHelper;
import com.netease.nim.demo.event.DemoOnlineStateContentProvider;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.mixpush.DemoMixPushMessageHandler;
import com.netease.nim.demo.mixpush.DemoPushContentProvider;
import com.netease.nim.demo.redpacket.NIMRedPacketClient;
import com.netease.nim.demo.rts.RTSHelper;
import com.netease.nim.demo.session.NimDemoLocationProvider;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.ysf.imageloader.GlideImageLoader;
import com.netease.nim.demo.ysf.util.YsfHelper;
import com.netease.nim.rtskit.RTSKit;
import com.netease.nim.rtskit.api.config.RTSOptions;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.business.contact.core.query.PinYin;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.mixpush.NIMPushClient;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.qiyukf.unicorn.ysfkit.unicorn.api.OnBotEventListener;
import com.qiyukf.unicorn.ysfkit.unicorn.api.QuickEntry;
import com.qiyukf.unicorn.ysfkit.unicorn.api.QuickEntryListener;
import com.qiyukf.unicorn.ysfkit.unicorn.api.UICustomization;
import com.qiyukf.unicorn.ysfkit.unicorn.api.Unicorn;
import com.qiyukf.unicorn.ysfkit.unicorn.api.UnicornImageLoader;
import com.qiyukf.unicorn.ysfkit.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.ysfkit.unicorn.api.privatization.UnicornAddress;
import com.squareup.leakcanary.LeakCanary;

public class NimApplication extends Application {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    /**
     * 注意：每个进程都会创建自己的Application 然后调用onCreate() 方法，
     * 如果用户有自己的逻辑需要写在Application#onCreate()（还有Application的其他方法）中，一定要注意判断进程，不能把业务逻辑写在core进程，
     * 理论上，core进程的Application#onCreate()（还有Application的其他方法）只能做与im sdk 相关的工作
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 内存泄漏检测
        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
        }
        DemoCache.setContext(this);

        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        SDKOptions sdkOptions = NimSDKOptionConfig.getSDKOptions(this);
        NIMClient.init(this, getLoginInfo(), sdkOptions);

        // crash handler
        AppCrashHandler.getInstance(this);

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {

            ActivityMgr.INST.init(this);
            // 初始化OPPO PUSH服务，创建默认通道
            HeytapPushManager.init(this, true);

            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(new DemoMixPushMessageHandler());

            // 初始化红包模块，在初始化UIKit模块之前执行
            NIMRedPacketClient.init(this);
            // init pinyin
            PinYin.init(this);
            PinYin.validate();
            // 初始化UIKit模块
            initUIKit();
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
            //关闭撤回消息提醒
//            NIMClient.toggleRevokeMessageNotification(false);
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true);
            // 初始化rts模块
            initRTSKit();
        }
        //初始化融合 SDK 中的七鱼业务关业务
        initMixSdk();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(Process.myPid() + "");
        }
    }

    private void initMixSdk() {
        UnicornImageLoader imageLoader;
        imageLoader = new GlideImageLoader(this);
        //内部已经初始化了 Nim baseSDK
        Unicorn.init(this, YsfHelper.readAppKey(this), mixOptions(), imageLoader);
    }

    private YSFOptions mixOptions() {
        YSFOptions options = new YSFOptions();
        if (options.uiCustomization == null) {
            options.uiCustomization = new UICustomization();
        }
        options.onMessageItemClickListener = (context, url) -> ToastHelper.showToast(context, url);

        options.onBotEventListener = new OnBotEventListener() {
            @Override
            public boolean onUrlClick(Context context, String url) {
                ToastHelper.showToast(context, url);
                return true;
            }
        };
        options.quickEntryListener = new QuickEntryListener() {
            @Override
            public void onClick(Context context, String shopId, QuickEntry quickEntry) {
                ToastHelper.showToast(context, shopId);
                if (quickEntry.getId() == 0) {
                }
            }
        };
        options.isPullMessageFromServer = true;
        options.isMixSDK = true;
        if (!TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDaUrlLabel(this)) && !TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDefalutUrlLabel(this))) {
            UnicornAddress unicornAddress = new UnicornAddress();
            unicornAddress.defaultUrl = DemoPrivatizationConfig.getYsfDefalutUrlLabel(this);
            unicornAddress.daUrl = DemoPrivatizationConfig.getYsfDaUrlLabel(this);
            options.unicornAddress = unicornAddress;
        }
        return options;
    }

    public static LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void initUIKit() {
        // 初始化
        NimUIKit.init(this, buildUIKitOptions());

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // IM 会话窗口的定制初始化。
        SessionHelper.init();

        // 聊天室聊天窗口的定制初始化。
        ChatRoomSessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();

        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());

        NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }

    private UIKitOptions buildUIKitOptions() {
        UIKitOptions options = new UIKitOptions();
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app";
        return options;
    }

    private void initRTSKit() {
        RTSOptions rtsOptions = new RTSOptions() {
            @Override
            public void logout(Context context) {
                MainActivity.logout(context, true);
            }
        };
        RTSKit.init(rtsOptions);
        RTSHelper.init();
    }
}
