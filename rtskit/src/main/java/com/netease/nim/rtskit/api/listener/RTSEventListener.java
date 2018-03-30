package com.netease.nim.rtskit.api.listener;

/**
 * RTS 事件监听器
 * Created by winnie on 2018/3/26.
 */

public interface RTSEventListener {

    /**
     * 发起 rts 呼叫成功
     * @param account 被叫方账号
     */
    void onRTSStartSuccess(String account);

    /**
     * rts 通话结束
     * @param account 被叫方账号
     * @param selfFinish 是否自己结束的通话
     */
    void onRTSFinish(String account, boolean selfFinish);
}
