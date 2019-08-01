package com.netease.nim.demo.redpacket;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.netease.nim.uikit.common.ToastHelper;

import com.jrmf360.normallib.JrmfClient;
import com.jrmf360.normallib.base.http.OkHttpModelCallBack;
import com.jrmf360.normallib.rp.JrmfRpClient;
import com.jrmf360.normallib.rp.bean.GrabRpBean;
import com.jrmf360.normallib.rp.http.model.BaseModel;
import com.jrmf360.normallib.rp.utils.callback.GrabRpCallBack;
import com.jrmf360.normallib.wallet.JrmfWalletClient;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.redpacket.RedPacketService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

/**
 * 金融魔方红包SDK接口封装，开发者依赖云信SDK 接入金融魔方需要逻辑流程如下：
 * <p>
 * 1.manifest 清单文件配置金融魔方SDK 渠道id（JRMF_PARTNER_ID）以及红包名称，渠道id 选择云信appkey，红包名称开发者自定义
 * 2.在主进程 application 初始化时调用初始化金融魔方SDK
 * 3.在登陆完成之后使用 RedPacketService#getRedPacketAuthToken 获取金融魔方thridToken，这是以后请求金融魔方sdk的身份凭证
 * 4.红包消息、拆红包消息分别使用自定义消息，参考 {@link com.netease.nim.demo.session.extension.RedPacketAttachment}、
 * {@link com.netease.nim.demo.session.extension.RedPacketOpenedAttachment},在调用金融魔方接口发送红包成功之后，发送
 * 附件为 RedPacketAttachment 的自定义消息，在拆红包成功之后调用附件为 RedPacketOpenedAttachment 的自定义消息。
 * 5.在自己的个人信息更改之后，更新个人信息到金融魔方，包含昵称、头像等，用于红包界面的展示。
 */

public class NIMRedPacketClient {

    private static boolean init;

    private static NimUserInfo selfInfo;

    private static String thirdToken;
    private static Observer<StatusCode> observer = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode == StatusCode.LOGINED) {
                getThirdToken();
            }
        }
    };

    private static Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> nimUserInfo) {
            for (NimUserInfo userInfo : nimUserInfo) {
                if (userInfo.getAccount().equals(DemoCache.getAccount())) {
                    // 更新 jrmf 用户昵称、头像信息
                    selfInfo = userInfo;
                    updateMyInfo();
                    return;
                }
            }
        }
    };

    private static void getRpAuthToken() {
        NIMClient.getService(RedPacketService.class).getRedPacketAuthToken().setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    thirdToken = result;
                } else if (code == ResponseCode.RES_RP_INVALID) {
                    // 红包功能不可用
                    ToastHelper.showToast(DemoCache.getContext(), "红包功能不可用");
                } else if (code == ResponseCode.RES_FORBIDDEN) {
                    // 应用没开通红包功能
                    ToastHelper.showToast(DemoCache.getContext(), "应用没开通红包功能");
                }
            }
        });
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        initJrmfSDK(context);
        init = true;

        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(observer, true);
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, true);

        RpOpenedMessageFilter.startFilter();
    }

    /**
     * 初始化金融魔方SDK
     *
     * @param context
     */
    private static void initJrmfSDK(Context context) {
        //初始化红包sdk
        JrmfClient.isDebug(false);

        JrmfClient.init(context);

        // com.jrmf360.neteaselib.base.utils.LogUtil.init(true);
        // 设置微信appid，如果不使用微信支付可以不调用，此处需要开发者到微信支付申请appid
        // JrmfClient.setWxAppid("xxxxxx");
    }

    public static boolean isEnable() {
        return init;
    }

    private static boolean checkValid() {
        return init && (selfInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount())) != null;
    }

    /**
     * 获取 thirdToken
     *
     * @return thirdToken
     */
    public static String getThirdToken() {
        if (TextUtils.isEmpty(thirdToken)) {
            getRpAuthToken();
        }
        return thirdToken;
    }

    /**
     * 登出之后，清掉token
     */
    public static void clear() {
        thirdToken = null;
    }

    /**
     * 跳转至我的钱包界面
     *
     * @param activity context
     */
    public static void startWalletActivity(Activity activity) {
        if (checkValid()) {
            JrmfWalletClient.intentWallet(activity, DemoCache.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar());
        }
    }

    /**
     * 打开红包发送界面
     *
     * @param activity        context
     * @param sessionTypeEnum 会话类型，支持单聊和群聊
     * @param targetAccount   会话对象目标 account
     * @param requestCode     startActivityForResult requestCode
     */
    public static void startSendRpActivity(Activity activity, SessionTypeEnum sessionTypeEnum, String targetAccount, int requestCode) {
        if (!checkValid()) {
            return;
        }

        if (sessionTypeEnum == SessionTypeEnum.Team) { // 群聊红包
            // 调用群聊红包接口
            Team team = NimUIKit.getTeamProvider().getTeamById(targetAccount);
            int count = team == null ? 0 : team.getMemberCount();
            JrmfRpClient.sendGroupEnvelopeForResult(activity, targetAccount, selfInfo.getAccount(), thirdToken, count, selfInfo.getName(), selfInfo.getAvatar(), requestCode);
        } else { // 单聊红包
            JrmfRpClient.sendSingleEnvelopeForResult(activity, targetAccount, selfInfo.getAccount(), thirdToken, selfInfo.getName(), selfInfo.getAvatar(), requestCode);
        }

    }

    /**
     * 启动拆红包dialog
     *
     * @param activity        context
     * @param sessionTypeEnum 会话类型
     * @param briberyId       红包id
     */
    public static void startOpenRpDialog(final Activity activity, final SessionTypeEnum sessionTypeEnum, final String briberyId, final NIMOpenRpCallback cb) {
        if (!checkValid()) {
            return;
        }
        GrabRpCallBack callBack = new GrabRpCallBack() {
            @Override
            public void grabRpResult(GrabRpBean grabRpBean) {
                if (grabRpBean.isHadGrabRp()) {
                    cb.sendMessage(selfInfo.getAccount(), briberyId, grabRpBean.getHasLeft() == 0);
                }
            }
        };
        if (sessionTypeEnum == SessionTypeEnum.Team) {
            JrmfRpClient.openGroupRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
        } else if (sessionTypeEnum == SessionTypeEnum.P2P) {
            JrmfRpClient.openSingleRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
        }
    }

    /**
     * 打开红包详情界面
     *
     * @param activity context
     * @param packetId 红包id
     */
    public static void startRpDetailActivity(Activity activity, String packetId) {
        if (checkValid()) {
            JrmfRpClient.openRpDetail(activity, selfInfo.getAccount(), getThirdToken(), packetId, selfInfo.getName(), selfInfo.getAvatar());
        }
    }


    /**
     * 更新个人信息到jrmf
     */
    public static void updateMyInfo() {
        if (init && selfInfo != null) {
            JrmfRpClient.updateUserInfo(selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), new OkHttpModelCallBack<BaseModel>() {

                @Override
                public void onSuccess(BaseModel baseModel) {
                    LogUtil.ui("update jrmf userInfo success");
                }

                @Override
                public void onFail(String s) {
                    LogUtil.ui("update jrmf userInfo fail" + s);
                }
            });
        }
    }
}
