package com.netease.nim.demo.session.viewholder;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.redpacket.NIMRedPacketClient;
import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;


public class MsgViewHolderOpenRedPacket extends MsgViewHolderBase {

    private TextView packetMessageText;

    private RedPacketOpenedAttachment attachment;

    private LinearLayout linearLayout;

    private NimUserInfo userInfo;

    public MsgViewHolderOpenRedPacket(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
    }

    @Override
    protected int getContentResId() {
        return R.layout.red_packet_open_item;
    }

    @Override
    protected void inflateContentView() {
        linearLayout = findViewById(R.id.packet_ll);
        packetMessageText = findViewById(R.id.packet_message);
    }

    @Override
    protected void bindContentView() {
        attachment = (RedPacketOpenedAttachment) message.getAttachment();
        if (attachment == null || !validAttachment(attachment) || !belongToMe(attachment)) {
            setLayoutParams(0, 0, linearLayout);
            return;
        }

        if (userInfo.getAccount().equals(attachment.getOpenAccount())) {
            openedRp(userInfo.getAccount().equals(attachment.getSendAccount()));
        } else if (userInfo.getAccount().equals(attachment.getSendAccount())) {
            othersOpenedRp();
        }
    }

    @Override
    protected boolean shouldDisplayReceipt() {
        return false;
    }

    private void openedRp(boolean myself) {
        String content;
        if (myself) {
            if (attachment.isRpGetDone()) {
                // 最后一个红包
                content = "你领取了自己的红包，你的红包已被领完";
            } else {
                // 不是最后一个红包
                content = "你领取了自己的红包";
            }
        } else {
            // 拆别人的红包
            String targetName = attachment.getSendNickName(message.getSessionType(), message.getSessionId());
            content = "你领取了" + targetName + "的红包";
        }
        setSpannableText(content, content.length() - 2, content.length());
    }

    private void othersOpenedRp() {
        String content;
        if (attachment.isRpGetDone()) {
            // 最后一个红包
            content = attachment.getOpenNickName(message.getSessionType(), message.getSessionId()) + "领取了你的红包，你的红包已被领完";
            setSpannableText(content, content.length() - 11, content.length() - 9);
        } else {
            // 不是最后一个红包
            content = attachment.getOpenNickName(message.getSessionType(), message.getSessionId()) + "领取了你的红包";
            setSpannableText(content, content.length() - 2, content.length());
        }
    }

    private boolean validAttachment(RedPacketOpenedAttachment attachment) {
        return !TextUtils.isEmpty(attachment.getOpenAccount()) && !TextUtils.isEmpty(attachment.getSendAccount());
    }

    // 我发的红包或者是我打开的红包
    private boolean belongToMe(RedPacketOpenedAttachment attachment) {
        return attachment.belongTo(userInfo.getAccount());
    }

    private void setSpannableText(String content, int start, int end) {
        SpannableString tSS = new SpannableString(content);
        RpDetailClickableSpan clickableSpan = new RpDetailClickableSpan();
        tSS.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        packetMessageText.setMovementMethod(LinkMovementMethod.getInstance());
        packetMessageText.setText(tSS);
    }

    private class RpDetailClickableSpan extends ClickableSpan {
        @Override
        public void onClick(View v) {
            //
            NIMRedPacketClient.startRpDetailActivity((Activity) context, attachment.getRedPacketId());
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(context.getResources().getColor(R.color.color_238fea));
            ds.setUnderlineText(false);
        }
    }

    /**
     * ------------------------------显示样式-------------------------
     */

    @Override
    protected boolean isMiddleItem() {
        return true;
    }

    @Override
    protected boolean isShowBubble() {
        return false;
    }

    @Override
    protected boolean isShowHeadImage() {
        return false;
    }

    @Override
    protected boolean onItemLongClick() {
        return true;
    }

    @Override
    protected void onItemClick() {

    }
}
