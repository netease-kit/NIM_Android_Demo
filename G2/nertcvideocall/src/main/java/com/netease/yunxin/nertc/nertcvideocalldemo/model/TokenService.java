package com.netease.yunxin.nertc.nertcvideocalldemo.model;

import com.netease.nimlib.sdk.RequestCallback;

/**
 *
 */
public interface TokenService {
    void getToken(long uid, RequestCallback<String> callback);
}
