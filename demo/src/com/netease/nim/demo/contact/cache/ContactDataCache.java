package com.netease.nim.demo.contact.cache;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.protocol.ContactHttpClient;
import com.netease.nim.demo.contact.protocol.IContactHttpCallback;
import com.netease.nim.demo.database.DatabaseManager;
import com.netease.nim.demo.database.contact.ContactDatabaseHelper;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.framework.NimDBExecutor;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 用户(User)及好友(Friend)数据缓存
 * 注意：获取通讯录列表即是根据Friend列表账号，去取对应的User
 * <p/>
 * Created by huangjun on 2015/8/20.
 */
public class ContactDataCache {

    private static final String TAG = ContactDataCache.class.getSimpleName();

    private static ContactDataCache instance = new ContactDataCache();

    private ContactDatabaseHelper database;

    private ContactDataCache() {
        initDatabase();
    }

    private boolean initDatabase() {
        database = DatabaseManager.getInstance().getContactDbHelper();
        return database != null;
    }

    public static ContactDataCache getInstance() {
        return instance;
    }

    /**
     * 好友数据
     */
    private Set<String> friendSet = new HashSet<>();

    private List<FriendDataChangedObserver> friendObservers = new ArrayList<>();

    public interface FriendDataChangedObserver {
        void onAddFriend(String account);

        void onDeleteFriend(String account);

        void onUpdateFriend(String account);

        void onAddUserToBlackList(String account);

        void onRemoveUserFromBlackList(String account);
    }

    /**
     * 用户资料数据
     */
    private Map<String, User> account2UserMap = new HashMap<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock(false);

    private List<UserDataChangedObserver> userObservers = new ArrayList<>();

    public interface UserDataChangedObserver {

        void onUpdateUsers(List<User> users);
    }

    public interface UserCacheBuiltCallback {

        void onBuddyCacheBuilt();
    }

    /**
     * 初始化&监听
     */
    public void init() {
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, true);
        NIMClient.getService(FriendServiceObserve.class).observeBlackListChangedNotify(blackListChangedNotifyObserver, true);
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

    public void registerFriendDataChangedObserver(FriendDataChangedObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!friendObservers.contains(o)) {
                friendObservers.add(o);
            }
        } else {
            friendObservers.remove(o);
        }
    }

    /***
     * ********************************* 好友管理 **************************************
     */

    /**
     * 监听好友关系变化
     */
    private Observer<FriendChangedNotify> friendChangedNotifyObserver = new Observer<FriendChangedNotify>() {
        @Override
        public void onEvent(FriendChangedNotify friendChangedNotify) {
            final String account = friendChangedNotify.getAccount();
            if (friendChangedNotify.getChangeType() == FriendChangedNotify.ChangeType.ADD) {
                // 新增好友
                if (NIMClient.getService(FriendService.class).isInBlackList(account)) {
                    // 如果在黑名单中，那么不加到好友列表中
                    return;
                }

                friendSet.add(account);
                if (hasUser(account)) {
                    // 通知观察者
                    for (FriendDataChangedObserver o : friendObservers) {
                        o.onAddFriend(account);
                    }
                } else {
                    ContactHttpClient.getInstance().getUserInfo(account, new IContactHttpCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            Log.i(TAG, "add friend " + account);

                            addOrUpdateUser(user);

                            // 通知观察者
                            for (FriendDataChangedObserver o : friendObservers) {
                                o.onAddFriend(account);
                            }
                        }

                        @Override
                        public void onFailed(int code, String errorMsg) {

                        }
                    });
                }
            } else if (friendChangedNotify.getChangeType() == FriendChangedNotify.ChangeType.DELETE) {
                // 删除好友
                friendSet.remove(account);

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onDeleteFriend(account);
                }
            }
        }
    };

    /**
     * 监听黑名单变化
     */
    private Observer<BlackListChangedNotify> blackListChangedNotifyObserver = new Observer<BlackListChangedNotify>() {
        @Override
        public void onEvent(BlackListChangedNotify blackListChangedNotify) {
            String account = blackListChangedNotify.getAccount();
            if (blackListChangedNotify.getChangeType() == BlackListChangedNotify.ChangeType.ADD) {
                // 拉黑，即从好友名单中移除
                friendSet.remove(account);

                // 拉黑，要从最近联系人列表中删除该好友
                NIMClient.getService(MsgService.class).deleteRecentContact2(account, SessionTypeEnum.P2P);

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onAddUserToBlackList(account);
                }
            } else if (blackListChangedNotify.getChangeType() == BlackListChangedNotify.ChangeType.REMOVE) {
                // 移出黑名单，判断是否假如好友名单
                if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                    friendSet.add(account);
                }

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onRemoveUserFromBlackList(account);
                }
            }
        }
    };

    /**
     * 获取我所有好友的用户资料（异步方法）
     */
    public void getUsersOfMyFriend(final IContactHttpCallback<List<User>> callback) {
        // 获取我所有好友的账号
        List<Friend> friends = NIMClient.getService(FriendService.class).getFriends();
        if (friends == null) {
            if (callback != null) {
                callback.onSuccess(new ArrayList<User>());
            }

            return;
        }

        List<String> accounts = new ArrayList<>(friends.size());
        for (Friend f : friends) {
            if (f != null) {
                accounts.add(f.getAccount());
                friendSet.add(f.getAccount());
            }
        }

        // 排除黑名单
        List<String> blacks = NIMClient.getService(FriendService.class).getBlackList();
        for (String b : blacks) {
            if (friendSet.contains(b)) {
                accounts.remove(b);
                friendSet.remove(b);
            }
        }

        // 排除掉自己
        if (friendSet.size() > 0) {
            friendSet.remove(DemoCache.getAccount());
        }

        if (accounts.size() > 0) {
            accounts.remove(DemoCache.getAccount());
        }

        Log.i(TAG, "get friends count = " + friendSet.size());

        // 获取所有好友的用户资料
        if (!accounts.isEmpty()) {
            ContactHttpClient.getInstance().getUserInfo(accounts, new IContactHttpCallback<List<User>>() {
                @Override
                public void onSuccess(List<User> users) {
                    if (users != null && !users.isEmpty()) {
                        ContactDataCache.getInstance().addOrUpdateUsers(users);
                        Log.i(TAG, "request users data completed, data size =" + users.size());
                    }

                    if (callback != null) {
                        callback.onSuccess(users);
                    }
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                }
            });
        } else {
            if (callback != null) {
                new Handler(DemoCache.getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(new ArrayList<User>());
                    }
                });
            }
        }

        // 取自己的用户资料
        ContactHttpClient.getInstance().getUserInfo(DemoCache.getAccount(), new IContactHttpCallback<User>() {
            @Override
            public void onSuccess(User user) {
                ContactDataCache.getInstance().addOrUpdateUser(user);
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    /**
     * 从缓存中获取我所有好友的用户资料(Provider异步加载需要同步)
     */
    public synchronized List<User> getUsersOfMyFriendFromCache() {
        List<User> users = new ArrayList<>();
        lock.readLock().lock();
        {
            for (String account : friendSet) {
                if (account2UserMap.containsKey(account)) {
                    users.add(account2UserMap.get(account));
                }
            }
        }
        lock.readLock().unlock();

        return users;
    }

    public int getMyFriendCounts() {
        int count = 0;
        for (String account : friendSet) {
            if (account2UserMap.containsKey(account)) {
                count++;
            }
        }
        return count;
    }

    public void clearFriendCache() {
        friendSet.clear();
    }

    /**
     * ********************************* User管理（包含非好友）/DB存储 **************************************
     */

    public boolean isUserCacheDataEmpty() {
        return account2UserMap.isEmpty();
    }

    /**
     * 从DB中读取(异步方法，防止登录时调用卡顿)
     */
    public void initUserCache(final UserCacheBuiltCallback cb) {
        if (!initDatabase()) {
            return;
        }

        NimDBExecutor.getInstance().execute(new NimDBExecutor.NimDBTask<List<User>>() {
            @Override
            public List<User> runInBackground() {
                return database.getUsers();
            }

            @Override
            public void onCompleted(List<User> users) {
                lock.writeLock().lock();
                {
                    account2UserMap.clear();
                    for (User b : users) {
                        account2UserMap.put(b.getAccount(), b);
                    }
                }
                lock.writeLock().unlock();

                if (cb != null) {
                    cb.onBuddyCacheBuilt();
                }
            }
        });
    }

    public void getUserFromRemote(String account, final IContactHttpCallback<User> callback) {
        ContactHttpClient.getInstance().getUserInfo(account, new IContactHttpCallback<User>() {
            @Override
            public void onSuccess(User user) {
                addOrUpdateUser(user);
                if (callback != null) {
                    callback.onSuccess(user);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                if (callback != null) {
                    callback.onFailed(code, errorMsg);
                }
            }
        });
    }

    public void getUsersFromRemote(List<String> accounts, final IContactHttpCallback<List<User>> callback) {
        ContactHttpClient.getInstance().getUserInfo(accounts, new IContactHttpCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                addOrUpdateUsers(users);
                if (callback != null) {
                    callback.onSuccess(users);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                if (callback != null) {
                    callback.onFailed(code, errorMsg);
                }
            }
        });
    }

    public User getUser(String account) {
        return account2UserMap.get(account);
    }

    public String getUserDisplayName(String account) {
        User u = getUser(account);
        if (u != null) {
            return !TextUtils.isEmpty(u.getName()) ? u.getName() : u.getAccount();
        }

        return account;
    }

    public String getUserDisplayNameEx(String account) {
        if (account.equals(DemoCache.getAccount())) {
            return "我";
        }

        return getUserDisplayName(account);
    }

    public String getUserDisplayNameYou(String account) {
        // 若为用户自己，显示“你”
        if (account.equals(DemoCache.getAccount())) {
            return "你";
        }

        return getUserDisplayName(account);
    }

    public boolean addOrUpdateUsers(final List<User> users) {
        if (users == null || users.isEmpty()) {
            return false;
        }

        boolean res = false;
        List<User> needUpdateUsers = new ArrayList<>();

        lock.writeLock().lock();
        {
            for (User u : users) {
                if (!account2UserMap.containsKey(u.getAccount())) {
                    account2UserMap.put(u.getAccount(), u);
                    needUpdateUsers.add(u);
                } else {
                    if (!account2UserMap.get(u.getAccount()).equals(u)) {
                        account2UserMap.put(u.getAccount(), u);
                        needUpdateUsers.add(u);
                    }
                }
            }

            if (needUpdateUsers.size() > 0) {
                NimDBExecutor.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        database.addUsers(users);
                    }
                });
                res = true;
            }
        }
        lock.writeLock().unlock();

        notifyUserUpdate(needUpdateUsers);

        return res;
    }

    public void addOrUpdateUser(final User user) {
        if (user == null) {
            return;
        }

        List<User> users = new ArrayList<>(1);
        users.add(user);
        addOrUpdateUsers(users);
    }

    public boolean hasUser(String account) {
        return account2UserMap.containsKey(account);
    }

    public void clearUserCache() {
        lock.writeLock().lock();
        {
            account2UserMap.clear();
        }
        lock.writeLock().unlock();
    }

    private void notifyUserUpdate(List<User> users) {
        // 通知观察者
        for (UserDataChangedObserver o : userObservers) {
            o.onUpdateUsers(users);
        }

        // 通知到UI组件
        List<String> accounts = new ArrayList<>(users.size());
        for (User user : users) {
            accounts.add(user.getAccount());
        }
        NimUIKit.notifyUserInfoChanged(accounts);
    }
}
