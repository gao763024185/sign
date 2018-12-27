package com.cloudm.sign.enums;


import com.cloudm.framework.common.core.KeyedNamed;

/**
 * @description: 接口安全认证的枚举类
 * @author: Jay
 * @date: 2018/7/2
 * @version: V1.0
 */
public enum SecurityValidateEnum implements KeyedNamed {

    // 接口安全认证失败
    SECURITY_VALIDATE_ERROR(-10087,"接口安全认证失败");

    SecurityValidateEnum(int key, String name){
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
