/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.contact;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FriendChangeType;
import com.netease.yunxin.kit.corekit.im.provider.FriendObserver;
import com.netease.yunxin.kit.corekit.im.provider.LoginSyncObserver;
import com.netease.yunxin.kit.corekit.im.provider.SyncStatus;
import com.netease.yunxin.kit.corekit.im.provider.SystemUnreadCountObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * contact view model
 */
public class ContactViewModel extends BaseViewModel {

    private final MutableLiveData<FetchResult<List<ContactFriendBean>>> contactLiveData = new MutableLiveData<>();
    private final FetchResult<List<ContactFriendBean>> fetchResult = new FetchResult<>(LoadStatus.Finish);
    private final List<ContactFriendBean> contactFriendBeanList = new ArrayList<>();
    private final MutableLiveData<ContactEntranceBean> contactEntranceLiveData = new MutableLiveData<>();
    private ContactEntranceBean verifyBean;
    private int unreadCount = 0;

    private final ContactRepo contactRepo = new ContactRepo();

    public ContactViewModel() {
        registerObserver();
    }

    public LiveData<FetchResult<List<ContactFriendBean>>> getContactLiveData() {
        return contactLiveData;
    }

    public LiveData<ContactEntranceBean> getContactEntranceLiveData() {
        return contactEntranceLiveData;
    }

    public void fetchContactList() {
        contactRepo.fetchContactList(new FetchCallback<List<FriendInfo>>() {
            @Override
            public void onSuccess(@Nullable List<FriendInfo> param) {
                contactFriendBeanList.clear();
                if (param != null) {
                    for (FriendInfo info : param) {
                        ContactFriendBean bean = new ContactFriendBean(info);
                        bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
                        contactFriendBeanList.add(bean);
                    }
                }
                fetchResult.setStatus(LoadStatus.Success);
                fetchResult.setData(contactFriendBeanList);
                contactLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code, "");
                contactLiveData.postValue(fetchResult);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setError(-1, "");
                contactLiveData.postValue(fetchResult);
            }
        });

        contactRepo.fetchSystemMessageUnreadCount(new FetchCallback<Integer>() {
            @Override
            public void onSuccess(@Nullable Integer count) {
                updateVerifyNumber(count);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }


    private void registerObserver() {
        contactRepo.registerFriendObserver(friendObserver);
        contactRepo.registerSystemUnreadCountObserver(unreadCountObserver);
        contactRepo.registerLoginSyncObserver(loginSyncObserver);
    }

    private final FriendObserver friendObserver = (friendChangeType, accountList) -> {
        if (friendChangeType == FriendChangeType.Delete
                || friendChangeType == FriendChangeType.AddBlack) {
            removeFriend(accountList);
            return;
        }
        if (friendChangeType == FriendChangeType.Update) {
            updateFriend(accountList);
            return;
        }
        // get new friend account, add or remove
        List<String> addFriendList = new ArrayList<>();
        if (friendChangeType == FriendChangeType.RemoveBlack) {
            for (String account : accountList) {
                if (contactRepo.isFriend(account)) {
                    addFriendList.add(account);
                }
            }
        }
        if (friendChangeType == FriendChangeType.Add){
            addFriendList.addAll(accountList);
        }
        // fetch friend info and update to view
        addFriend(addFriendList, FetchResult.FetchType.Add);

    };

    private final SystemUnreadCountObserver unreadCountObserver = (count) -> {
        updateVerifyNumber(count);
    };

    private void updateVerifyNumber(int count){
        if (verifyBean != null) {
            if (count != unreadCount) {
                verifyBean.number = count;
                unreadCount = count;
                contactEntranceLiveData.postValue(verifyBean);
            }
        } else {
            unreadCount = count;
        }
    }

    private final LoginSyncObserver loginSyncObserver = (syncStatus) -> {
        if (syncStatus == SyncStatus.Complete){
            fetchContactList();
        }else if (syncStatus == SyncStatus.BeginSync){
            fetchResult.setStatus(LoadStatus.Loading);
            fetchResult.setData(null);
            contactLiveData.postValue(fetchResult);
        }
    };

    private void removeFriend(List<String> accountList) {
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        List<ContactFriendBean> removeData = new ArrayList<>();
        for (String account : accountList) {
            for (ContactFriendBean bean : contactFriendBeanList) {
                if (TextUtils.equals(account, bean.data.getAccount())) {
                    contactFriendBeanList.remove(bean);
                    removeData.add(bean);
                    break;
                }
            }
        }
        if (!removeData.isEmpty()) {
            fetchResult.setFetchType(FetchResult.FetchType.Remove);
            fetchResult.setData(removeData);
            contactLiveData.postValue(fetchResult);
        }
    }

    private void addFriend(List<String> accountList, FetchResult.FetchType type) {
        if (!accountList.isEmpty()) {
            contactRepo.fetchUserInfo(accountList, new FetchCallback<List<UserInfo>>() {
                @Override
                public void onSuccess(@Nullable List<UserInfo> param) {
                    List<ContactFriendBean> addList = new ArrayList<>();
                    for (int index = 0; param != null && index < param.size(); index++) {
                        FriendInfo friendInfo = contactRepo.getFriendInfo(param.get(index).getAccount());
                        if (friendInfo != null) {
                            friendInfo.setUserInfo(param.get(index));
                            ContactFriendBean bean = new ContactFriendBean(friendInfo);
                            bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
                            contactFriendBeanList.add(bean);
                            addList.add(bean);
                        }
                    }
                    fetchResult.setFetchType(type);
                    fetchResult.setData(addList);
                    contactLiveData.postValue(fetchResult);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(@Nullable Throwable exception) {

                }
            });
        }
    }

    private void updateFriend(List<String> accountList) {
        List<ContactFriendBean> updateBean = new ArrayList<>();
        for (String account : accountList) {
            FriendInfo friendInfo = contactRepo.getFriendInfo(account);
            if (friendInfo == null) {
                continue;
            }
            for (ContactFriendBean bean : contactFriendBeanList) {
                if (TextUtils.equals(friendInfo.getAccount(), bean.data.getAccount())) {
                    bean.data = friendInfo;
                    updateBean.add(bean);
                    break;
                }

            }
        }
        if (updateBean.size() > 0) {
            fetchResult.setFetchType(FetchResult.FetchType.Update);
            fetchResult.setData(updateBean);
            contactLiveData.postValue(fetchResult);
        }

    }

    public List<ContactEntranceBean> getContactEntranceList(Context context) {
        List<ContactEntranceBean> contactDataList = new ArrayList<>();
        //verify message
        verifyBean = new ContactEntranceBean(R.mipmap.ic_contact_verfiy_msg, context.getString(R.string.contact_list_verify_msg));
        verifyBean.number = unreadCount;
        verifyBean.router = ContactEntranceBean.EntranceRouter.VERIFY_LIST;
        //black list
        ContactEntranceBean blackBean = new ContactEntranceBean(R.mipmap.ic_contact_black_list, context.getString(R.string.contact_list_black_list));
        blackBean.router = ContactEntranceBean.EntranceRouter.BLACK_LIST;
        //my group
        ContactEntranceBean groupBean = new ContactEntranceBean(R.mipmap.ic_contact_my_group, context.getString(R.string.contact_list_my_group));
        groupBean.router = ContactEntranceBean.EntranceRouter.TEAM_LIST;

        contactDataList.add(verifyBean);
        contactDataList.add(blackBean);
        contactDataList.add(groupBean);
        return contactDataList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contactRepo.unregisterFriendObserver(friendObserver);
        contactRepo.unregisterSystemUnreadCountObserver(unreadCountObserver);
    }
}
