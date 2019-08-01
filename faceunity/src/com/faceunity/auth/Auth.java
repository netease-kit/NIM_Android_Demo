package com.faceunity.auth;

import com.faceunity.utils.ReflectUtils;

/**
 * FaceUnity授权文件处理，请不要混淆AuthPack类!!!
 * <p>
 * Created by huangjun on 2017/7/17.
 */

public class Auth {

    private static final String AUTH_CLASS_PATH = "com.faceunity.auth.AuthPack";
    private static final String AUTH_METHOD_NAME = "A";

    public static boolean hasAuthFile() {
        try {
            return ReflectUtils.hasMethod(AUTH_CLASS_PATH, AUTH_METHOD_NAME, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static byte[] getFaceUnityAuthToken() {
        try {
            return ReflectUtils.invokeClassMethod(AUTH_CLASS_PATH, AUTH_METHOD_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
