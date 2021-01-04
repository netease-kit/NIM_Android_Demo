package com.netease.nim.uikit.common;

import java.util.Collection;

public class CommonUtil {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
