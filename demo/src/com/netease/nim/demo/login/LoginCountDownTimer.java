/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.nim.demo.login;

import android.os.CountDownTimer;

class LoginCountDownTimer {
    private CountDownTimer timer;
    private final long totalMillis;
    private final long intervalMillis;
    private final TickListener tickListener;
    private final Runnable onFinishAction;


    public LoginCountDownTimer(long millisInFuture, long countDownInterval,
                               TickListener tickListener, Runnable onFinishAction) {
        this.totalMillis = millisInFuture;
        this.intervalMillis = countDownInterval;
        this.tickListener = tickListener;
        this.onFinishAction = onFinishAction;
    }

    boolean start() {
        if (isTicking()) {
            return false;
        }

        timer = new CountDownTimer(totalMillis, intervalMillis) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (tickListener != null) {
                    tickListener.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                timer = null;
                if (onFinishAction != null) {
                    onFinishAction.run();
                }
            }
        };
        timer.start();
        return true;
    }

    boolean stop() {
        if (isTicking()) {
            timer.cancel();
            timer = null;
            if (onFinishAction != null) {
                onFinishAction.run();
            }
            return true;
        }
        return false;
    }

    boolean isTicking() {
        return timer != null;
    }

    interface TickListener {
        void onTick(long millisUntilFinished);
    }
}
