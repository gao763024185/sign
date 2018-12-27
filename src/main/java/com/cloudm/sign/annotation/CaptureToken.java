package com.cloudm.sign.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author gaobh
 * @version v1.0
 * @description 当使用该注解时，表示有登陆信息时需要验证，无登陆信息时不进行登陆认证
 * @date 2018/9/12
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CaptureToken {

}
