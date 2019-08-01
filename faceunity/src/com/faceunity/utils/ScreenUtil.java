package com.faceunity.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtil {
    private static float density;
    private static float scaleDensity;

    public static int dip2px(float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }

    public static int px2dip(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    public static int sp2px(float spValue) {
        return (int) (spValue * scaleDensity + 0.5f);
    }

    public static void init(Context context) {
        if (null == context) {
            return;
        }
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        density = dm.density;
        scaleDensity = dm.scaledDensity;
    }
}
