package com.cloudm.sign.login;

import com.cloudm.sign.dao.SignResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author Jay
 * @version v1.0
 * @description 登陆的处理器
 * @date 2018-03-01 10:58
 */
@Slf4j
public abstract class AbstractSignFactoryBean implements SignHandler {

    /**
     * 登陆
     *
     * @param accountName      账号
     * @param password      密码
     * @param isSingleLogin 是否允许同时多处登陆
     * @return {@link SignResult}
     */
    @Override
    public SignResult executeLogin(String accountName, String password, Boolean isSingleLogin, Integer tag) {
        // 参数校验
        String checkMsg = this.checkAccount(accountName, password);
        if (!StringUtils.isEmpty(checkMsg)) {
            return new SignResult(null, checkMsg, Boolean.FALSE);
        }
        // 密码验证
        String msg = this.judgeAccount(accountName, password, tag);
        if (!StringUtils.isEmpty(msg)) {
            return new SignResult(null, msg, Boolean.FALSE);
        }
        // 是否允许同时多处登陆
        if (!isSingleLogin) {
            // 判断是否登录过，如果登录，则踢出
            this.clearSession(accountName);
        }
        // 生成token
        String token = saveTokenToRedis(accountName);
        return new SignResult(token, null, Boolean.TRUE);
    }


    /**
     * 抽象方法子类必须实现，需要调用{@link AbstractSignFactoryBean}的executeLogin(T account)方法
     *
     * @param accountName
     * @param password
     * @param isSingleLogin
     * @return {@link SignResult}
     */
    public abstract SignResult doLogin(String accountName, String password, Boolean isSingleLogin, Integer tag);


    /**
     * 抽象方法子类必须实现，将token等信息存入redis
     *
     * @param accountName
     * @return token
     */
    public abstract String saveTokenToRedis(String accountName);


    /**
     * 如果登录，则踢出,不允许同时登录一个账号
     *
     * @param accountName
     */
    public abstract void clearSession(String accountName);

    /**
     * 密码校验
     *
     * @param accountName
     * @param password
     * @return 当密码校验正确的还是返回null, 否则返回错误提示信息
     */
    public abstract String judgeAccount(String accountName, String password, Integer tag);


    /*=============================================private method======================================================*/

    /**
     * 参数校验
     *
     * @param accountName
     * @param password
     */
    private String checkAccount(String accountName, String password) {
        if (StringUtils.isEmpty(accountName)) {
            return "账号不能为空";
        }
        if (StringUtils.isEmpty(password)) {
            return "密码不能为空";
        }
        return null;
    }

}
