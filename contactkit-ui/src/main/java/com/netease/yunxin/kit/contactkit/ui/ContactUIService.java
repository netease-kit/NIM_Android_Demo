package com.netease.yunxin.kit.contactkit.ui;


import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.netease.yunxin.kit.contactkit.ContactService;
import com.netease.yunxin.kit.contactkit.ui.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

@Keep
public class ContactUIService extends ContactService {

    @NonNull
    @Override
    public String getServiceName() {
        return "ContactUIService";
    }

    @NonNull
    @Override
    public ContactService create(@NonNull Context context) {
        XKitRouter.registerRouter(RouterConstant.PATH_SELECTOR_ACTIVITY, ContactSelectorActivity.class);
        return this;
    }
}
