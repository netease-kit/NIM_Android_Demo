package com.netease.nim.demo.contact.protocol;

import com.netease.nim.demo.contact.model.User;

import java.util.List;

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
    String API_NAME_GET_TOKEN = "token";
    String API_NAME_GET_ADDRESS_BOOK = "getAddressBook";
    String API_NAME_GET_USER_INFO = "getUserInfo";
    String API_NAME_REGISTER = "createDemoUser";

    // code
    int RESULT_CODE_SUCCESS = 200;

    // header
    String HEADER_KEY_ACCESS_TOKEN = "access-token";
    String HEADER_KEY_APP_KEY = "appkey";
    String HEADER_CONTENT_TYPE = "Content-Type";

    // request
    String REQUEST_KEY_USER_ID = "userid";
    String REQUEST_KEY_SECRET = "secret";
    String REQUEST_KEY_CLIENT_TYPE = "client_type";
    String REQUEST_KEY_UID = "uid";
    String REQUEST_USER_NAME = "username";
    String REQUEST_NICK_NAME = "nickname";
    String REQUEST_PASSWORD = "password";
    int REQUEST_VALUE_CLIENT_TYPE = 0;

    // result
    String RESULT_KEY_ACCESS_TOKEN = "access_token";
    String RESULT_KEY_RES = "res";
    String RESULT_KEY_MSG = "msg";
    String RESULT_KEY_ERROR_MSG = "errmsg";
    String RESULT_KEY_EXPIRE = "expire";
    String RESULT_KEY_TOTAL = "total";
    String RESULT_KEY_LIST = "list";
    String RESULT_KEY_UID = "uid";
    String RESULT_KEY_NAME = "name";
    String RESULT_KEY_ICON = "icon";

    /**
     * ************************ protocol interface ******************************
     */

    // 获取Http口令，保存到本地
    void getHttpToken(String account, String secret, IContactHttpCallback<String> callback);

    // 获取用户信息
    void getUserInfo(List<String> accounts, IContactHttpCallback<List<User>> callback);

    void getUserInfo(String account, IContactHttpCallback<User> callback);

    // 注册
    void register(String account, String nickName, String password, IContactHttpCallback<Void> callback);
}
