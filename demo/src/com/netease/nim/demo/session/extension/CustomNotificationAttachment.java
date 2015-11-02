package com.netease.nim.demo.session.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by huangjun on 2015/10/27.
 * 自定义消息中（通知示例）
 */
public class CustomNotificationAttachment extends CustomAttachment {

    private String text;

    public CustomNotificationAttachment() {
        super(CustomAttachmentType.Notification);
    }

    public CustomNotificationAttachment(String notificationText) {
        this();
        this.text = notificationText;
    }

    @Override
    protected void parseData(JSONObject data) {
        text = data.getString("text");
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put("text", text);
        return data;
    }

    public String getNotificationText() {
        return text;
    }
}
