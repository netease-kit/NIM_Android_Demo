package com.netease.nim.rtskit;

import android.content.Context;

import com.netease.nim.rtskit.activity.RTSActivity;
import com.netease.nim.rtskit.api.IUserInfoProvider;
import com.netease.nim.rtskit.api.config.RTSOptions;
import com.netease.nim.rtskit.api.listener.RTSEventListener;
import com.netease.nim.rtskit.common.log.ILogUtil;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSData;

/**
 * 云信音白板组件定制化入口
 * Created by winnie on 2018/3/21.
 */

public class RTSKit {
    private static final String TAG = RTSKit.class.getSimpleName();

    private static Context context;

    private static String account;

    private static RTSOptions rtsOptions;

    private static IUserInfoProvider userInfoProvider;

    private static ILogUtil iLogUtil;

    private static RTSEventListener rtsEventListener;

    public static void init(RTSOptions rtsOptions) {
        RTSKit.rtsOptions = rtsOptions;

        // 注册白板会话
        registerRTSIncomingObserver(true);
    }

    public static void setContext(Context context) {
        RTSKit.context = context;
    }

    public static Context getContext() {
        return context;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        RTSKit.account = account;
    }

    /**
     * 获取 rts 初始化设置
     * @return RTSOptions
     */
    public static RTSOptions getRtsOptions() {
        return rtsOptions;
    }

    /**
     * 设置用户相关资料提供者
     * @param userInfoProvider 用户相关资料提供者
     */
    public static void setUserInfoProvider(IUserInfoProvider userInfoProvider) {
        RTSKit.userInfoProvider = userInfoProvider;
    }

    /**
     * 获取用户相关资料提供者
     * @return IUserInfoProvider
     */
    public static IUserInfoProvider getUserInfoProvider() {
        return userInfoProvider;
    }

    /**
     * 获取日志系统接口
     * @return ILogUtil
     */
    public static ILogUtil getiLogUtil() {
        return iLogUtil;
    }

    /**
     * 设置日志系统接口
     * @param iLogUtil 日志系统接口
     */
    public static void setiLogUtil(ILogUtil iLogUtil) {
        RTSKit.iLogUtil = iLogUtil;
    }

    /**
     * 设置 rts 事件监听器
     * @param rtsEventListener
     */
    public static void setRTSEventListener(RTSEventListener rtsEventListener) {
        RTSKit.rtsEventListener = rtsEventListener;
    }

    /**
     * 获取 rts 事件监听器
     * @return RTSEventListener
     */
    public static RTSEventListener getRTSEventListener() {
        return rtsEventListener;
    }

    /**
     * 发起 rts 呼叫
     * @param context 上下文
     * @param account 被叫方电话
     */
    public static void startRTSSession(Context context, String account) {
        RTSActivity.startSession(context, account, RTSActivity.FROM_INTERNAL);
    }

    /**
     * 注册白板来电观察者
     *
     * @param register
     */
    private static void registerRTSIncomingObserver(boolean register) {
        RTSManager.getInstance().observeIncomingSession(new Observer<RTSData>() {
            @Override
            public void onEvent(RTSData rtsData) {
                RTSActivity.incomingSession(RTSKit.getContext(), rtsData, RTSActivity.FROM_BROADCAST_RECEIVER);
            }
        }, register);
    }
}
