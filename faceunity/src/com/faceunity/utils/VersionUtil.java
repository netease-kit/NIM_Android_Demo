package com.faceunity.utils;

import android.os.Build;

/**
 * Created by huangjun on 2017/7/13.
 */
public class VersionUtil {

    public final static boolean isGEJellyBean() {
        return isCompatible(Build.VERSION_CODES.JELLY_BEAN);
    }

    public final static boolean isKITKAT() {
        return isCompatible(Build.VERSION_CODES.KITKAT);
    }

    public static boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isGreaterVersion(int apiLevel) {
        return Build.VERSION.SDK_INT > apiLevel;
    }
}
