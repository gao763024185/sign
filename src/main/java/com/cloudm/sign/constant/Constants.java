package com.cloudm.sign.constant;

/**
 * @author Jay
 * @version v1.0
 * @description 常量池
 * @date 2018-03-02 14:45
 */
public final class Constants {

    /**
     * 用户登录信息的redis的key
     */
    public final static String USER_KEY = "sign:str:user_key:";

    /**
     * 用户登录信息token的redis的key
     */
    public final static String TOKEN_KEY = "sign:str:token_key:";

    /**
     * token的key
     */
    public final static String TOKEN = "token";

    /**
     * 设置登录token有效期为2天
     */
    public final static Long TWO_DAYS = 2L;

    /**
     * 设置接口有效期60s
     */
    public final static long TEN_SECOND = 60000L;

    /**
     * 请求头的中的sign签名
     */
    public final static String SIGN = "sign";

    /**
     * 时间戳
     */
    public final static String TIMESTAMP = "timeStamp";
}
