package com.netease.nim.demo.contact.protocol;

/**
 * Created by huangjun on 2015/3/7.
 */
public interface ContactHttpCallback<T> {
    void onSuccess(T t);
    void onFailed(int code, String errorMsg);
}
