package com.netease.nim.demo.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.demo.DemoCache;
import com.netease.nimlib.push.net.lbs.IPVersion;
import com.netease.nimlib.push.packet.asymmetric.AsymmetricType;
import com.netease.nimlib.push.packet.symmetry.SymmetryType;
import com.netease.nimlib.sdk.NimHandshakeType;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
    private static final String KEY_HANDSHAKE = "handshake";
    private static final String KEY_ASYMMETRIC = "asymmetric";
    private static final String KEY_SYMMETRY = "symmetry";
    private static final String KEY_IPV = "ipv";

    public static void saveUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static void saveUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    public static void saveHandshakeType(NimHandshakeType handshakeType) {
        saveInt(KEY_HANDSHAKE, handshakeType.getValue());
    }

    public static NimHandshakeType getHandshakeType() {
        return NimHandshakeType.value(getInt(KEY_HANDSHAKE, NimHandshakeType.V1.getValue()));
    }

    public static void saveAsymmetric(AsymmetricType asymmetric) {
        saveInt(KEY_ASYMMETRIC, asymmetric.getValue());
    }

    public static AsymmetricType getAsymmetric() {
        return AsymmetricType.value(getInt(KEY_ASYMMETRIC));
    }

    public static void saveSymmetry(SymmetryType symmetry) {
        saveInt(KEY_SYMMETRY, symmetry.getValue());
    }

    public static SymmetryType getSymmetry() {
        return SymmetryType.value(getInt(KEY_SYMMETRY));
    }

    public static void saveIpv(IPVersion ipv) {
        saveInt(KEY_IPV, ipv.getValue());
    }

    public static IPVersion getIpv() {
        return IPVersion.value(getInt(KEY_IPV));
    }

    private static void saveInt(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static int getInt(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    private static int getInt(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }
}
