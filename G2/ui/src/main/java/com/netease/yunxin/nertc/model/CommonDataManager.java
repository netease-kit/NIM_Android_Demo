package com.netease.yunxin.nertc.model;

import com.blankj.utilcode.util.SPUtils;

public class CommonDataManager {

    private static final CommonDataManager instance = new CommonDataManager();

    public static CommonDataManager getInstance() {
        return instance;
    }

    public final static String PER_DATA = "per_profile_manager";
    private static final String PER_ACCESS_TOKEN = "per_access_token";
    private static final String PER_IM_TOKEN = "per_im_token";

    private String token;

    private String imToken;

    private CommonDataManager() {
    }

    public String getAccessToken() {
        if (token == null) {
            loadAccessToken();
        }
        return token;
    }

    public void setAccessToken(String token) {
        this.token = token;
        SPUtils.getInstance(PER_DATA).put(PER_ACCESS_TOKEN, this.token);
    }

    private void loadAccessToken() {
        token = SPUtils.getInstance(PER_DATA).getString(PER_ACCESS_TOKEN, "");
    }

    public String getIMToken() {
        if (imToken == null) {
            loadAccessToken();
        }
        return imToken;
    }

    public void setIMToken(String imToken) {
        this.imToken = imToken;
        SPUtils.getInstance(PER_DATA).put(PER_IM_TOKEN, this.imToken);
    }

    private void loadIMToken() {
        imToken = SPUtils.getInstance(PER_DATA).getString(PER_IM_TOKEN, "");
    }

}
