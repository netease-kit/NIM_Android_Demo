package com.netease.nim.demo.common.http;

/**
 * Created by huangjun on 2015/3/6.
 */
public class NimHttpException extends RuntimeException {

    private static final long serialVersionUID = -3537304844268409258L;

    private final int httpCode;

    public int getHttpCode() {
        return httpCode;
    }

    public NimHttpException(Throwable e) {
        super(e);
        this.httpCode = -1;
    }

    public NimHttpException(int httpCode) {
        super();
        this.httpCode = httpCode;
    }

    public NimHttpException() {
        this(-1);
    }
}