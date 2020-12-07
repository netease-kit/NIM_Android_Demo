package com.netease.nim.demo.ysf.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.netease.nim.demo.DemoPrivatizationConfig;

/**
 * Created by zhanyage on 2020/3/18
 * Describe: Ysf 帮助类
 */
public class YsfHelper {

    /**
     * 读取 Appkey ,先从私有化配置中读取，然后再从 AndroidManifest 中读取
     * @param context context
     * @return
     */
    public static String readAppKey(Context context) {
        try {
            String appKey = "";
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                if (TextUtils.isEmpty(readPrivatizationAppkey(context))) {
                    appKey = appInfo.metaData.getString("com.netease.nim.appKey");
                } else {
                    appKey = readPrivatizationAppkey(context);
                }
            }
            return appKey;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String readPrivatizationAppkey(Context context) {
        String appKey = DemoPrivatizationConfig.getAppKey(context);
        if (!TextUtils.isEmpty(appKey)) {
            return appKey;
        }
        return "";
    }

}
