package com.netease.yunxin.nertc.nertcvideocall.model;

import com.netease.nimlib.sdk.RequestCallback;

/**
 *获取token的服务
 */
public interface TokenService {
    void getToken(long uid, RequestCallback<String> callback);
}
