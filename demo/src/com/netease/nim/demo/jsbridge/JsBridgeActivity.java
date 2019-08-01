package com.netease.nim.demo.jsbridge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nimlib.jsbridge.core.NIMJsBridge;
import com.netease.nimlib.jsbridge.core.NIMJsBridgeBuilder;
import com.netease.nimlib.jsbridge.extension.ImageInfo;
import com.netease.nimlib.jsbridge.interact.ResponseCode;
import com.netease.nimlib.jsbridge.interfaces.IJavaReplyToJsImageInfo;
import com.netease.nimlib.jsbridge.util.Base64;
import com.netease.nimlib.jsbridge.util.WebViewConfig;

import java.util.ArrayList;

/**
 * Created by hzliuxuanlin on 2016/10/21.
 */

public class JsBridgeActivity extends UI {

    /**
     * 本地资源页面
     */
    private static final String LOCAL_ASSET_HTML = "file:///android_asset/js/page.html";

    private static final int IMAGE_PICKER_REQUEST_ID = 2233;

    private NIMJsBridge jsBridge;

    private WebView webView;

    private IJavaReplyToJsImageInfo pickPictureCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js_bridge_activity);
        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.js_bridge_demonstration;
        setToolBar(R.id.toolbar, options);
        initWebView();
        initData();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, JsBridgeActivity.class);
        context.startActivity(intent);
    }

    private void initWebView() {
        webView = findView(R.id.webView);
        WebSettings settings = webView.getSettings();
        WebViewConfig.setWebSettings(this, settings, this.getApplicationInfo().dataDir);
        WebViewConfig.removeJavascriptInterfaces(webView);
        WebViewConfig.setWebViewAllowDebug(false);
        WebViewConfig.setAcceptThirdPartyCookies(webView);
        webView.loadUrl(LOCAL_ASSET_HTML);
    }

    private void initData() {
        JavaInterfaces javaInterfaces = new JavaInterfaces(this);
        jsBridge = new NIMJsBridgeBuilder().addJavaInterfaceForJS(javaInterfaces).setWebView(webView).create();
    }

    public void pickPicture(IJavaReplyToJsImageInfo cb) {
        this.pickPictureCallback = cb;
        ImagePickerLauncher.pickImage(JsBridgeActivity.this, IMAGE_PICKER_REQUEST_ID, R.string.set_head_image);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICKER_REQUEST_ID) {
            ImageInfo pictureInfo = new ImageInfo();
            if (data != null) {
                ArrayList<GLImage> images = (ArrayList<GLImage>) data.getSerializableExtra(
                        Constants.EXTRA_RESULT_ITEMS);
                if (images != null && !images.isEmpty()) {
                    GLImage image = images.get(0);
                    pictureInfo.path = image.getPath();
                }
            }
            if (!TextUtils.isEmpty(pictureInfo.path)) {
                pictureInfo.base64 = Base64.encodeFile(pictureInfo.path);
                Log.i("demo", "choose picture:" + pictureInfo.toString());
            }
            if (pickPictureCallback != null) {
                this.pickPictureCallback.replyToJs(ResponseCode.RES_SUCCESS, "success", pictureInfo);
            }
        }
    }
}
