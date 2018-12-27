package com.cloudm.sign.login;

import com.cloudm.framework.common.enums.BaseErrorEnum;
import com.cloudm.framework.common.ex.BusinessCheckFailException;
import com.cloudm.sign.dao.ApplicationSessionContext;
import com.cloudm.sign.dao.SignResult;
import com.cloudm.sign.token.DefaultTokenHelper;
import com.cloudm.sign.context.DefaultContextHolder;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Jay
 * @version v1.0
 * @description 默认的实现登录的类
 * @date 2018-03-02 10:53
 */
@Slf4j
public abstract class DefaultSignFactoryBean<S extends ApplicationSessionContext> extends AbstractSignFactoryBean {


    public DefaultContextHolder<S> defaultContextHolder;


    public DefaultTokenHelper<S> defaultTokenHelper;


    /**
     * 构造器
     *
     * @param defaultContextHolder
     * @param defaultTokenHelper
     */
    public DefaultSignFactoryBean(DefaultContextHolder defaultContextHolder, DefaultTokenHelper defaultTokenHelper) {
        this.defaultContextHolder = defaultContextHolder;
        this.defaultTokenHelper = defaultTokenHelper;
    }

    /**
     * 子类必须实现{@link AbstractSignFactoryBean}的executeLogin方法
     *
     * @param accountName
     * @param password
     * @param isSingleLogin
     * @return
     */
    @Override
    public SignResult doLogin(String accountName, String password, Boolean isSingleLogin, Integer tag) {
        return super.executeLogin(accountName, password, isSingleLogin,tag);
    }

    /**
     * 抽象方法子类必须实现，将token等信息存入redis
     *
     * @param accountName
     * @return
     */
    @Override
    public String saveTokenToRedis(String accountName) {
        return defaultTokenHelper.saveUserToRedis(this.getDataForSaveToRedis(accountName));
    }

    /**
     * 如果登录，则踢出,不允许同时登录一个账号
     *
     * @param accountName
     */
    @Override
    public void clearSession(String accountName) {
        Integer id = this.getUserIdByUserName(accountName);
        if (id == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "userId不能为空");
        }
        if (defaultTokenHelper.isLogin(id)) {
            // 清除id对应的缓存信息
            defaultTokenHelper.cleanRedisSessionById(id);
        }
    }

    /**
     * 获取存入redis的信息的业务session对象
     *
     * @param accountName
     * @return
     */
    private S getDataForSaveToRedis(String accountName) {
        return defaultContextHolder.doSetSessionContext(accountName);
    }

    /*=============================================abstract method============================================*/

    /**
     * 根据userName获取userId
     *
     * @param accountName
     * @return
     */
    public abstract Integer getUserIdByUserName(String accountName);

}
