package com.cloudm.sign.login;


import com.cloudm.sign.dao.SignResult;

/**
 * @author Jay
 * @version v1.0
 * @description 登录处理器
 * @date 2018-03-01 13:54
 */
public interface SignHandler {

    /**
     * 登陆
     *
     * @param accountName
     * @param password
     * @param isSingleLogin 同时多处登陆
     * @param tag
     * @return token
     */
    SignResult executeLogin(String accountName, String password, Boolean isSingleLogin, Integer tag);

}
