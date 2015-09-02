package com.netease.nim.demo.contact.protocol;

/**
 * Created by huangjun on 2015/3/7.
 */
public interface IContactHttpCallback<T> {
    public void onSuccess(T t);
    public void onFailed(int code, String errorMsg);
}
