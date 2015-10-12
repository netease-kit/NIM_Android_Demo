package com.netease.nim.demo.contact.protocol;

/**
 * 通讯录数据获取协议
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public interface IContactHttpProtocol {

    /**
     * ************************ constant ******************************
     */

    // api
    String API_NAME_REGISTER = "createDemoUser";

    // code
    int RESULT_CODE_SUCCESS = 200;

    // header
    String HEADER_KEY_APP_KEY = "appkey";
    String HEADER_CONTENT_TYPE = "Content-Type";

    // request
    String REQUEST_USER_NAME = "username";
    String REQUEST_NICK_NAME = "nickname";
    String REQUEST_PASSWORD = "password";

    // result
    String RESULT_KEY_RES = "res";
    String RESULT_KEY_ERROR_MSG = "errmsg";

    /**
     * ************************ protocol interface ******************************
     */

    // 注册
    void register(String account, String nickName, String password, ContactHttpCallback<Void> callback);
}
