package com.netease.nim.demo;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.contact.FriendDataCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户资料(包含非好友）数据缓存
 * 缓存NimUserInfo，适用于用户体系使用网易云信用户资料托管
 * <p/>
 * Created by huangjun on 2015/8/20.
 */
public class NimUserInfoCache {

    private static final String TAG = NimUserInfoCache.class.getSimpleName();

    /**
     * 构造
     */

    public static NimUserInfoCache getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 属性
     */

    private Handler uiHandler;

    private Map<String, NimUserInfo> account2UserMap = new ConcurrentHashMap<>();

    private List<UserDataChangedObserver> userObservers = new ArrayList<>();

    private Map<String, List<RequestCallback<NimUserInfo>>> requestUserInfoMap = new HashMap<>();

    /**
     * 构建缓存
     */
    public void buildCache() {
        List<NimUserInfo> users = NIMClient.getService(UserService.class).getAllUserInfo();
        if (users != null && !users.isEmpty()) {
            for (NimUserInfo u : users) {
                account2UserMap.put(u.getAccount(), u);
            }
        }

        Log.i(TAG, "build nim user info data cache completed, users count = " + account2UserMap.size());
    }

    /**
     * 清理
     */
    public void clear() {
        clearUserCache();
    }

    /**
     * 异步从应用服务器获取用户信息
     *
     * @param account  用户帐号
     * @param callback 回调函数
     */
    public void getUserInfoFromRemote(final String account, final RequestCallback<NimUserInfo> callback) {
        if (requestUserInfoMap.containsKey(account)) {
            if (callback != null) {
                requestUserInfoMap.get(account).add(callback);
            }
            // 已经在请求中，不要重复请求
            return;
        } else {
            List<RequestCallback<NimUserInfo>> cbs = new ArrayList<>();
            if (callback != null) {
                cbs.add(callback);
            }
            requestUserInfoMap.put(account, cbs);
        }

        List<String> accounts = new ArrayList<>(1);
        accounts.add(account);

        NIMClient.getService(UserService.class).fetchUserInfo(accounts).setCallback(new RequestCallbackWrapper<List<NimUserInfo>>() {

            @Override
            public void onResult(int code, List<NimUserInfo> users, Throwable exception) {
                NimUserInfo user = null;
                if (code == ResponseCode.RES_SUCCESS && users != null && !users.isEmpty()) {
                    user = users.get(0);
                    addOrUpdateUser(user);
                }

                List<RequestCallback<NimUserInfo>> cbs = requestUserInfoMap.get(account);
                for (RequestCallback<NimUserInfo> cb : cbs) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        cb.onSuccess(user);
                    } else {
                        cb.onFailed(code);
                    }
                }

                requestUserInfoMap.remove(account);
            }
        });
    }

    /**
     * 异步从应用服务器获取批量用户信息
     *
     * @param accounts 用户帐号集合
     * @param callback 回调函数
     */
    public void getUserInfoFromRemote(List<String> accounts, final RequestCallback<List<NimUserInfo>> callback) {
        NIMClient.getService(UserService.class).fetchUserInfo(accounts).setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> users) {
                addOrUpdateUsers(users);

                if (callback != null) {
                    callback.onSuccess(users);
                }
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
            }

            @Override
            public void onException(Throwable exception) {
                callback.onException(exception);
            }
        });
    }

    /**
     * ******************************* 获取好友用户信息 *********************************
     */

    /**
     * 获取我所有好友的用户资料（异步方法）
     */
    public void getUsersOfMyFriendFromRemote(final RequestCallback<List<NimUserInfo>> callback) {
        // 获取我所有好友的帐号
        List<String> accounts = FriendDataCache.getInstance().getMyFriendAccounts();
        if (accounts == null || accounts.isEmpty()) {
            if (callback != null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(new ArrayList<NimUserInfo>());
                    }
                });
            }

            return;
        }

        // 获取所有好友的用户资料
        if (!accounts.isEmpty()) {
            getUserInfoFromRemote(accounts, new RequestCallbackWrapper<List<NimUserInfo>>() {
                @Override
                public void onResult(int code, List<NimUserInfo> users, Throwable exception) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        if (users != null && !users.isEmpty()) {
                            addOrUpdateUsers(users);
                            Log.i(TAG, "request users data completed, data size =" + users.size());
                        }

                        if (callback != null) {
                            callback.onSuccess(users);
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailed(code);
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(new ArrayList<NimUserInfo>());
                    }
                });
            }
        }
    }

    private Handler getHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(NimUIKit.getContext().getMainLooper());
        }

        return uiHandler;
    }

    /**
     * 从缓存中获取我所有好友的用户资料(Provider异步加载需要同步)
     */
    public synchronized List<NimUserInfo> getUsersOfMyFriend() {
        List<String> accounts = FriendDataCache.getInstance().getMyFriendAccounts();
        List<NimUserInfo> users = new ArrayList<>();
        for (String account : accounts) {
            if (NimUserInfoCache.getInstance().hasUser(account)) {
                users.add(NimUserInfoCache.getInstance().getUserInfo(account));
            }
        }

        return users;
    }


    /**
     * ******************************* 业务接口（获取缓存的用户信息） *********************************
     */

    public NimUserInfo getUserInfo(String account) {
        return account2UserMap.get(account);
    }

    public boolean hasUser(String account) {
        return account2UserMap.containsKey(account);
    }

    public String getUserDisplayName(String account) {
        NimUserInfo u = getUserInfo(account);
        if (u != null) {
            return !TextUtils.isEmpty(u.getName()) ? u.getName() : u.getAccount();
        }

        return account;
    }

    public String getUserDisplayNameEx(String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "我";
        }

        return getUserDisplayName(account);
    }

    public String getUserDisplayNameYou(String account) {
        // 若为用户自己，显示“你”
        if (account.equals(NimUIKit.getAccount())) {
            return "你";
        }

        return getUserDisplayName(account);
    }

    private void clearUserCache() {
        account2UserMap.clear();
    }

    /**
     * ****************************** 缓存用户信息变更监听&通知 ******************************
     */

    public interface UserDataChangedObserver {
        void onUpdateUsers(List<NimUserInfo> users);
    }

    public void registerUserDataChangedObserver(UserDataChangedObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!userObservers.contains(o)) {
                userObservers.add(o);
            }
        } else {
            userObservers.remove(o);
        }
    }

    private void notifyUserUpdate(List<NimUserInfo> users) {
        // 通知观察者
        for (UserDataChangedObserver o : userObservers) {
            o.onUpdateUsers(users);
        }

        // 通知到UI组件
        List<String> accounts = new ArrayList<>(users.size());
        for (NimUserInfo user : users) {
            accounts.add(user.getAccount());
        }
        NimUIKit.notifyUserInfoChanged(accounts);
    }

    /**
     * *************************************** 用户资料变更监听 ********************************************
     */

    public void registerObservers(boolean register) {
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, register);
    }

    private Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> users) {
            if (users == null || users.isEmpty()) {
                return;
            }

            Log.i(TAG, "receive userInfo changed notify, size=" + users.size());
            addOrUpdateUsers(users);
        }
    };

    /**
     * *************************************** User缓存管理 ********************************************
     */

    private void addOrUpdateUsers(final List<NimUserInfo> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        for (NimUserInfo u : users) {
            Log.i(TAG, "add user:" + u.getAccount());
            account2UserMap.put(u.getAccount(), u);
        }

        notifyUserUpdate(users); // 通知变更
    }

    private void addOrUpdateUser(final NimUserInfo user) {
        if (user == null) {
            return;
        }

        List<NimUserInfo> users = new ArrayList<>(1);
        users.add(user);
        addOrUpdateUsers(users);
    }

    /**
     * ************************************ 单例 **********************************************
     */

    static class InstanceHolder {
        final static NimUserInfoCache instance = new NimUserInfoCache();
    }
}
