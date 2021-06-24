/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 收拢说有的Delegate
 */
public class NERTCInternalDelegateManager implements NERTCCallingDelegate {

    private CopyOnWriteArrayList<WeakReference<NERTCCallingDelegate>> mWeakReferenceList;

    public NERTCInternalDelegateManager() {
        mWeakReferenceList = new CopyOnWriteArrayList<>();
    }

    public void addDelegate(NERTCCallingDelegate listener) {
        WeakReference<NERTCCallingDelegate> listenerWeakReference = new WeakReference<>(listener);
        mWeakReferenceList.add(listenerWeakReference);
    }

    public boolean isEmpty() {
        return mWeakReferenceList == null || mWeakReferenceList.isEmpty();
    }

    public void removeDelegate(NERTCCallingDelegate listener) {
        Iterator iterator = mWeakReferenceList.iterator();
        while (iterator.hasNext()) {
            WeakReference<NERTCCallingDelegate> reference = (WeakReference<NERTCCallingDelegate>) iterator.next();
            if (reference != null && reference.get() == listener) {
                mWeakReferenceList.remove(reference);
            }
        }
    }

    @Override
    public void onError(int errorCode, String errorMsg, boolean needFinish) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onError(errorCode, errorMsg, needFinish);
            }
        }
    }

    @Override
    public void onInvited(InvitedInfo invitedInfo) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onInvited(invitedInfo);
            }
        }
    }


    @Override
    public void onUserEnter(String accId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onUserEnter(accId);
            }
        }
    }


    @Override
    public void onCallEnd(String userId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onCallEnd(userId);
            }
        }
    }

    @Override
    public void onUserLeave(String accountID) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onUserLeave(accountID);
            }
        }
    }

    @Override
    public void onUserDisconnect(String userId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onUserDisconnect(userId);
            }
        }
    }

    @Override
    public void onRejectByUserId(String userId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onRejectByUserId(userId);
            }
        }
    }

    @Override
    public void onUserBusy(String userId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onUserBusy(userId);
            }
        }
    }

    @Override
    public void onCancelByUserId(String userId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onCancelByUserId(userId);
            }
        }
    }

    @Override
    public void onCameraAvailable(String userId, boolean isVideoAvailable) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onCameraAvailable(userId, isVideoAvailable);
            }
        }
    }

    @Override
    public void onVideoMuted(String userId, boolean isMuted) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onVideoMuted(userId, isMuted);
            }
        }
    }

    @Override
    public void onAudioMuted(String userId, boolean isMuted) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onAudioMuted(userId, isMuted);
            }
        }
    }

    @Override
    public void onJoinChannel(String accId, long uid, String channelName, long rtcChannelId) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onJoinChannel(accId, uid, channelName, rtcChannelId);
            }
        }
    }

    @Override
    public void onAudioAvailable(String userId, boolean isVideoAvailable) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onAudioAvailable(userId, isVideoAvailable);
            }
        }
    }

    @Override
    public void onDisconnect(int res) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onDisconnect(res);
            }
        }
    }

    @Override
    public void onUserNetworkQuality(Entry<String, Integer>[] stats) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onUserNetworkQuality(stats);
            }
        }
    }

    @Override
    public void onCallTypeChange(ChannelType type) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onCallTypeChange(type);
            }
        }
    }

    @Override
    public void timeOut() {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.timeOut();
            }
        }
    }

    @Override
    public void onFirstVideoFrameDecoded(String userId, int width, int height) {
        for (WeakReference<NERTCCallingDelegate> reference : mWeakReferenceList) {
            NERTCCallingDelegate listener = reference.get();
            if (listener != null) {
                listener.onFirstVideoFrameDecoded(userId, width, height);
            }
        }
    }
}
