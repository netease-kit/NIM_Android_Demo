/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.ui.team;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.netease.yunxin.kit.alog.ALog;

import androidx.fragment.app.Fragment;

public abstract class TFragment extends Fragment {
    private static final String TAG = "TFragment";

    private static final Handler handler = new Handler();

    private int containerId;

    private boolean destroyed;

    protected final boolean isDestroyed() {
        return destroyed;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ALog.i(TAG, "fragment: " + getClass().getSimpleName() + " onActivityCreated()");

        destroyed = false;
    }

    public void onDestroy() {
        super.onDestroy();

        ALog.i(TAG, "fragment: " + getClass().getSimpleName() + " onDestroy()");

        destroyed = true;
    }

    protected final Handler getHandler() {
        return handler;
    }

    protected final void postRunnable(final Runnable runnable) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        });
    }

    protected final void postDelayed(final Runnable runnable, long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        }, delay);
    }

    protected void showKeyboard(boolean isShow) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        if (isShow) {
            if (activity.getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                imm.showSoftInput(activity.getCurrentFocus(), 0);
            }
        } else {
            if (activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    }

    protected void hideKeyboard(View view) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.hideSoftInputFromWindow(
                view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected <T extends View> T findView(int resId) {
        return (T) (getView().findViewById(resId));
    }

    protected void setToolBar(int toolbarId, int titleId, int logoId) {
        if (getActivity() != null && getActivity() instanceof UI) {
            ((UI) getActivity()).setToolBar(toolbarId, titleId, logoId);
        }
    }

    protected void setTitle(int titleId) {
        if (getActivity() != null && getActivity() instanceof UI) {
            getActivity().setTitle(titleId);
        }
    }
}
