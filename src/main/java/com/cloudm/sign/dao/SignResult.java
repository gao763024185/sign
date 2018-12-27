package com.cloudm.sign.dao;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jay
 * @version v1.0
 * @description 登录结果
 * @date 2018-03-02 17:11
 */
@Data
public class SignResult implements Serializable{


    private String token;


    private String message;


    private Boolean success;


    /**
     * 构造器
     *
     * @param token
     * @param message
     * @param success
     */
    public SignResult(String token, String message, Boolean success) {
        this.token = token;
        this.message = message;
        this.success = success;
    }
}
