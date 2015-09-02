package com.netease.nim.demo.session.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by huangjun on 2015/7/8.
 */
public class RTSAttachment extends CustomAttachment {

    private String content;

    public RTSAttachment() {
        super(CustomAttachmentType.RTS);
    }

    public RTSAttachment(String content) {
        this();
        this.content = content;
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put("content", content);
        return data;
    }

    @Override
    protected void parseData(JSONObject data) {
        content = data.getString("content");
    }

    public String getContent() {
        return content;
    }
}
