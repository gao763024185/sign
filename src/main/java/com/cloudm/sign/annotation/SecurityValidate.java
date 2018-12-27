package com.cloudm.sign.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jay
 * @version v1.0
 * @description 打上此注解表示该接口需要保护
 * @date 2018-07-02 14:31
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityValidate {

}
