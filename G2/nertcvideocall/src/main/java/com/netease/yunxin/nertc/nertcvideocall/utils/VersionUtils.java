/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.utils;


import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;

public final class VersionUtils {

    /**
     * 比较 version 大小 ，version 形如 xx.xx.xx
     * @param version1 待比较版本号
     * @param version2 待比较版本号
     * @return 0 版本号相同，1:version1 > version2 , -1:version1 < version2，如果版本号 为null 则为无限小
     */
    public static int compareVersion(String version1, String version2) {
        int comparisonResult = 0;
        if (version1 == null && version2 == null) {
            return 0;
        }

        if (version1 == null){
            return -1;
        }

        if (version2 == null){
            return 1;
        }

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++){
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }

        ALog.dApi("VersionUtils", new ParameterMap("compareVersion")
                .append("version1", version1)
                .append("version2", version2)
                .append("result", comparisonResult)
        );
        return comparisonResult;
    }
}
