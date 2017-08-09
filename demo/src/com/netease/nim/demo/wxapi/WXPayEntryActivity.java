package com.netease.nim.demo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.jrmf360.neteaselib.JrmfClient;
import com.jrmf360.neteaselib.base.utils.LogUtil;
import com.jrmf360.neteaselib.base.utils.ToastUtil;
import com.jrmf360.neteaselib.rp.JrmfRpClient;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		 setContentView(R.layout.pay_results);

        // appid需换成商户自己开放平台appid
        api = WXAPIFactory.createWXAPI(this, JrmfClient.getWxAppId());
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onReq(BaseReq req) {
        LogUtil.e("微信：onReq");
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            // resp.errCode == -1
            // 原因：支付错误,可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等
            // resp.errCode == -2 原因 用户取消,无需处理。发生场景：用户不支付了，点击取消，返回APP
            if (resp.errCode == 0) {
                ToastUtil.showToast(this, "微信支付成功");
                if (JrmfClient.WX_PAY_TYPE == JrmfClient.WX_PAY_TYPE_RED_PACKET) {
                    // 发红包支付成功-需要关闭支付页面
                    JrmfRpClient.closePayAndSendRpPageWithResult();
                } else if (JrmfClient.WX_PAY_TYPE == JrmfClient.WX_PAY_TYPE_WALLET_PAY) {
                    //关闭收银台支付页面并提示用户
//					JrmfWalletPayClient.closePayPageWithResult();
                }
            } else {
                ToastUtil.showToast(this, "取消" + resp.errCode + "test");
            }
            finish();
        }
    }
}