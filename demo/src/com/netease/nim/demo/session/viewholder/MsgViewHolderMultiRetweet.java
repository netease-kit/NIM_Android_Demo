package com.netease.nim.demo.session.viewholder;

import android.app.Activity;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.demo.R;
import com.netease.nim.demo.session.activity.WatchMultiRetweetActivity;
import com.netease.nim.demo.session.extension.MultiRetweetAttachment;
import com.netease.nim.uikit.business.session.emoji.EmojiManager;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

import java.util.Locale;
import java.util.regex.Matcher;

public class MsgViewHolderMultiRetweet extends MsgViewHolderBase {

    private static int TIP_LEN_MAX = 30;
    private TextView mTitleTV;
    private TextView mFirstMsgTV;
    private TextView mSecondMsgTV;
    private TextView mFootNoteTV;

    public MsgViewHolderMultiRetweet(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    public int getContentResId() {
        return R.layout.nim_message_item_multi_retweet;
    }

    @Override
    public void inflateContentView() {
        mTitleTV = findViewById(R.id.nim_message_item_tv_title);
        mFirstMsgTV = findViewById(R.id.nim_message_item_tv_msg1);
        mSecondMsgTV = findViewById(R.id.nim_message_item_tv_msg2);
        mFootNoteTV = findViewById(R.id.nim_message_item_tv_foot_note);
    }

    @Override
    public void bindContentView() {
        String sessionName = "";
        String sender1 = "";
        String msg1 = "";
        String sender2 = "";
        String msg2 = "";
        MsgAttachment msgAttachment = message.getAttachment();
        if (msgAttachment != null) {
            //通过json转接的方式进行类型转换
            String attachmentJsonStr = message.getAttachment().toJson(false);
            JSONObject attachmentJson = JSON.parseObject(attachmentJsonStr);
            if (attachmentJson.containsKey("data")) {
                attachmentJson = attachmentJson.getJSONObject("data");
            }
            MultiRetweetAttachment attachment = new MultiRetweetAttachment();
            attachment.fromJson(attachmentJson);

            sessionName = attachment.getSessionName();
            sender1 = attachment.getSender1();
            msg1 = attachment.getMessage1();
            sender2 = attachment.getSender2();
            msg2 = attachment.getMessage2();
        }
        if (isReceivedMessage()) {
            mTitleTV.setTextColor(context.getResources().getColor(R.color.color_black_b3000000));
            mFirstMsgTV.setTextColor(context.getResources().getColor(R.color.color_grey_555555));
            mSecondMsgTV.setTextColor(context.getResources().getColor(R.color.color_grey_555555));
            mFootNoteTV.setTextColor(context.getResources().getColor(R.color.color_grey_555555));
        } else {
            mTitleTV.setTextColor(context.getResources().getColor(R.color.GreyWhite));
            mFirstMsgTV.setTextColor(context.getResources().getColor(R.color.color_gray_d9d9d9));
            mSecondMsgTV.setTextColor(context.getResources().getColor(R.color.color_gray_d9d9d9));
            mFootNoteTV.setTextColor(context.getResources().getColor(R.color.color_gray_d9d9d9));
        }
        mTitleTV.setText(String.format(Locale.CHINA, "%s的聊天记录", sessionName));
        String tip1 = String.format(Locale.CHINA, "%s: %s", sender1, msg1);
        MoonUtil.identifyFaceExpression(context, mFirstMsgTV, cutStrWithEmoji(tip1, TIP_LEN_MAX), ImageSpan.ALIGN_BOTTOM);

        String tip2 = TextUtils.isEmpty(sender2) ? "" : String.format(Locale.CHINA, "%s: %s", sender2, msg2);
        if (TextUtils.isEmpty(tip2)) {
            mSecondMsgTV.setText("");
        } else {
            MoonUtil.identifyFaceExpression(context, mSecondMsgTV, cutStrWithEmoji(tip2, TIP_LEN_MAX), ImageSpan.ALIGN_BOTTOM);
        }
    }

    /**
     * 在不截断emoji的情况下截取字符串的最多前lenLimit个，如果截断，则在最后添加...
     *
     * @param str      源字符串
     * @param lenLimit 截断后的字符串的最大长度
     * @return 截断后字符串
     */
    private String cutStrWithEmoji(String str, int lenLimit) {
        lenLimit = Math.max(0, lenLimit);
        if (str.length() <= lenLimit) {
            return str;
        }

        Matcher matcher = EmojiManager.getPattern().matcher(str);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (end > lenLimit) {
                if (start < lenLimit) {
                    lenLimit = start;
                }
                break;
            }
        }
        return str.substring(0, lenLimit) + "...";
    }


    @Override
    public void onItemClick() {
        if (context instanceof WatchMultiRetweetActivity) {
            WatchMultiRetweetActivity.start((Activity) context, message);
        } else {
            WatchMultiRetweetActivity.startForResult(MessageListPanelEx.REQUEST_CODE_WATCH_MULTI_RETWEET, (Activity) context, message);
        }
    }
}
