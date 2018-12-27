package com.cloudm.sign.context.handler;

import com.cloudm.sign.dao.ApplicationSessionContext;

/**
 * @author Jay
 * @version v1.0
 * @description 用户业务session服务
 * @date 2018-03-02 11:26
 */
public interface ContextHolderService<S extends ApplicationSessionContext> {


    /**
     * 设置业务session
     *
     * @param accountName
     * @return
     */
    S doSetSessionContext(String accountName);


    /**
     * 初始化业务session
     *
     * @param userName
     */
    void initSessionContext(String userName);

}
