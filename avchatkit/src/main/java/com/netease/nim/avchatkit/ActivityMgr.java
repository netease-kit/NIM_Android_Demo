package com.netease.nim.avchatkit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 *
 */
public final class ActivityMgr implements Application.ActivityLifecycleCallbacks {

    public static final ActivityMgr INST = new ActivityMgr();

    private WeakReference<Activity> top;

    private ActivityMgr() {
    }

    public void init(Application application) {
        if (application == null) {
            // do nothing
        } else {
            application.unregisterActivityLifecycleCallbacks(INST);
            application.registerActivityLifecycleCallbacks(INST);
        }
    }

    public Activity getTopActivity() {
        return top != null ? top.get() : null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        this.top = new WeakReference(activity);
    }
    @Override
    public void onActivityStarted(Activity activity) {
        this.top = new WeakReference(activity);
    }
    @Override
    public void onActivityResumed(Activity activity) {
        this.top = new WeakReference(activity);
    }
    @Override
    public void onActivityPaused(Activity activity) {
    }
    @Override
    public void onActivityStopped(Activity activity) {
    }
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
