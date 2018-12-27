package com.cloudm.sign.enums;


import com.cloudm.framework.common.core.KeyedNamed;

/**
 * @description: token相关枚举
 * @author: Jay
 * @date: 2018/1/16
 * @version: V1.0
 */
public enum TokenErrorEnum implements KeyedNamed {

    // token已经失效请重新登录
    TOKEN_OVER_DUE_ERROR(-10086,"token已经失效请重新登录");

    TokenErrorEnum(int key, String name){
        this.key = key ;
        this.name = name ;
    }

    private int key ;
    private String name ;

    /**
     * 状态值
     */
    @Override
    public int getKey() {
        return this.key;
    }

    /**
     * 状态描述
     */
    @Override
    public String getName() {
        return this.name;
    }
}
