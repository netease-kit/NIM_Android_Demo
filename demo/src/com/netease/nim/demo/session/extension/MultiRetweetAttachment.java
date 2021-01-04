package com.netease.nim.demo.session.extension;


import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/*
{
"sessionID":"session_id",
"sessionName":"session_name",
"url": "https://netease.im",//nos
"md5": "d5ff5301b95fb9a23566ed407ebbc177",//nos file
"compressed": false,
"encrypted": true,
"password": "b95fb9a23566ed40",//rc4
"messageAbstract":
[//消息摘要，前两条消息摘要Json
{
"sender": "allday1",
"message": "123313123123123123"//文本消息,最大32字，超过32使用32字 + "......"
},
{
"sender": "allday1",
"message": "[视频]"
}
]
}
*/
public class MultiRetweetAttachment extends CustomAttachment {
    private static final String KEY_SESSION_ID = "sessionID";
    private static final String KEY_SESSION_NAME = "sessionName";
    private static final String KEY_URL = "url";
    private static final String KEY_MD5 = "md5";
    private static final String KEY_COMPRESSED = "compressed";
    private static final String KEY_ENCRYPTED = "encrypted";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_MESSAGE_ABSTRACT = "messageAbstract";
    private static final String KEY_SENDER = "sender";
    private static final String KEY_MESSAGE = "message";

    private String sessionID;
    private String sessionName;
    /** nos文件存储地址 */
    private String url;
    private String md5;
    private boolean compressed;
    private boolean encrypted;
    private String password;
    /** 第一条消息的发送者ID */
    private String sender1;
    /** 第一条消息在合并转发消息中的展示文案 */
    private String message1;
    /** 第二条消息的发送者ID */
    private String sender2;
    /** 第二条消息在合并转发消息中的展示文案 */
    private String message2;

    public MultiRetweetAttachment() {
        super(CustomAttachmentType.MultiRetweet);
    }

    public MultiRetweetAttachment(String sessionID, String sessionName, String url, String md5, boolean compressed, boolean encrypted, String password, String sender1, String message1, String sender2, String message2) {
        super(CustomAttachmentType.MultiRetweet);
        this.sessionID = sessionID;
        this.sessionName = sessionName;
        this.url = url;
        this.md5 = md5;
        this.compressed = compressed;
        this.encrypted = encrypted;
        this.password = password;
        this.sender1 = sender1;
        this.message1 = message1;
        this.sender2 = sender2;
        this.message2 = message2;
    }

    @Override
    protected void parseData(JSONObject data) {
        //如果Json格式包含外层部分，则先进入内层
        if (data.containsKey("data")) {
            data = data.getJSONObject("data");
        }
        try {
            sessionID = data.getString(KEY_SESSION_ID);
            sessionName = data.getString(KEY_SESSION_NAME);
            url = data.getString(KEY_URL);
            md5 = data.getString(KEY_MD5);
            compressed = data.getBooleanValue(KEY_COMPRESSED);
            encrypted = data.getBooleanValue(KEY_ENCRYPTED);
            password = data.getString(KEY_PASSWORD);

            JSONArray msgAbs = data.getJSONArray(KEY_MESSAGE_ABSTRACT);
            JSONObject obj1 = msgAbs.getJSONObject(0);
            sender1 = obj1.getString(KEY_SENDER);
            message1 = obj1.getString(KEY_MESSAGE);
            if (msgAbs.size()>1){
                JSONObject obj2 = msgAbs.getJSONObject(1);
                sender2 = obj2.getString(KEY_SENDER);
                message2 = obj2.getString(KEY_MESSAGE);
            }
        } catch (Exception e) {
            //转化失败，条目显示null字符
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_SESSION_ID, sessionID);
        data.put(KEY_SESSION_NAME, sessionName);
        data.put(KEY_URL, url);
        data.put(KEY_MD5, md5);
        data.put(KEY_COMPRESSED, compressed);
        data.put(KEY_ENCRYPTED, encrypted);
        data.put(KEY_PASSWORD, password);

        JSONArray messageAbstract = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put(KEY_SENDER, sender1);
        obj1.put(KEY_MESSAGE, message1);
        messageAbstract.add(obj1);
        //只有一条消息时，不传递第二组的字段
        if (!TextUtils.isEmpty(sender2)) {
            JSONObject obj2 = new JSONObject();
            obj2.put(KEY_SENDER, sender2);
            obj2.put(KEY_MESSAGE, message2);
            messageAbstract.add(obj2);
        }
        data.put(KEY_MESSAGE_ABSTRACT, messageAbstract);
        return data;
    }

    public static String getKeySessionId() {
        return KEY_SESSION_ID;
    }

    public static String getKeySessionName() {
        return KEY_SESSION_NAME;
    }

    public static String getKeyUrl() {
        return KEY_URL;
    }

    public static String getKeyMd5() {
        return KEY_MD5;
    }

    public static String getKeyCompressed() {
        return KEY_COMPRESSED;
    }

    public static String getKeyEncrypted() {
        return KEY_ENCRYPTED;
    }

    public static String getKeyPassword() {
        return KEY_PASSWORD;
    }

    public static String getKeyMessageAbstract() {
        return KEY_MESSAGE_ABSTRACT;
    }

    public static String getKeySender() {
        return KEY_SENDER;
    }

    public static String getKeyMessage() {
        return KEY_MESSAGE;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSender1() {
        return sender1;
    }

    public void setSender1(String sender1) {
        this.sender1 = sender1;
    }

    public String getMessage1() {
        return message1;
    }

    public void setMessage1(String message1) {
        this.message1 = message1;
    }

    public String getSender2() {
        return sender2;
    }

    public void setSender2(String sender2) {
        this.sender2 = sender2;
    }

    public String getMessage2() {
        return message2;
    }

    public void setMessage2(String message2) {
        this.message2 = message2;
    }
}
