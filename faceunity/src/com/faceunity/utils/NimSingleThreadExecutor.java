package com.faceunity.utils;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by huangjun on 2015/3/12.
 */
public class NimSingleThreadExecutor {

    private static NimSingleThreadExecutor instance;

    private Handler uiHandler;
    private Executor executor;

    private NimSingleThreadExecutor(Context context) {
        uiHandler = new Handler(context.getApplicationContext().getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }

    public synchronized static NimSingleThreadExecutor getInstance(Context context) {
        if (instance == null) {
            instance = new NimSingleThreadExecutor(context);
        }

        return instance;
    }

    public <T> void execute(NimTask<T> task) {
        if (executor != null) {
            executor.execute(new NimRunnable<>(task));
        }
    }

    public void execute(Runnable runnable) {
        if (executor != null) {
            executor.execute(runnable);
        }
    }

    /**
     * ****************** model *************************
     */

    public interface NimTask<T> {
        T runInBackground();

        void onCompleted(T result);
    }

    private class NimRunnable<T> implements Runnable {

        public NimRunnable(NimTask<T> task) {
            this.task = task;
        }

        private NimTask<T> task;

        @Override
        public void run() {
            final T res = task.runInBackground();
            if (uiHandler != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        task.onCompleted(res);
                    }
                });
            }
        }
    }
}
