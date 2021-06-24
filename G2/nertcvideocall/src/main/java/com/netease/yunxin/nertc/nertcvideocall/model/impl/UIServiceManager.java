/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.nertc.nertcvideocall.model.impl;

import com.netease.yunxin.nertc.nertcvideocall.model.UIService;

/**
 * 保存UIservice 给内部使用
 */
public class UIServiceManager {

    public UIService uiService;

    private UIServiceManager() {

    }

    public static UIServiceManager getInstance() {
        return UIServiceManagerHolder.uiServiceManagerHolder;
    }

    private static class UIServiceManagerHolder {
        public static final UIServiceManager uiServiceManagerHolder = new UIServiceManager();
    }

    public void setUiService(UIService uiService) {
        this.uiService = uiService;
    }

    public UIService getUiService() {
        return uiService;
    }
}
