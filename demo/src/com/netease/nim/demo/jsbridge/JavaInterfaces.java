package com.netease.nim.demo.jsbridge;



import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nimlib.jsbridge.annotation.JavaInterface;
import com.netease.nimlib.jsbridge.annotation.Param;
import com.netease.nimlib.jsbridge.annotation.ParamCallback;
import com.netease.nimlib.jsbridge.interfaces.IJavaReplyToJsImageInfo;

/**
 * Created by hzliuxuanlin on 16/10/21.
 */
public class JavaInterfaces {

    private JsBridgeActivity context;

    public JavaInterfaces(JsBridgeActivity context) {
        this.context = context;
    }

    @JavaInterface("notification")
    public void notification(@Param("msg") String msg) {
        // notification
        NotificationHelper helper = new NotificationHelper(context);
        helper.activeCallingNotification(true, msg);

        // toast
        ToastHelper.showToast(context.getApplicationContext(), "发送成功");
    }

    @JavaInterface("picture")
    public void picture(@ParamCallback IJavaReplyToJsImageInfo jsCallback) {
        context.pickPicture(jsCallback);
    }
}
