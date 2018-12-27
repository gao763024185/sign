package com.cloudm.sign.context;

import com.cloudm.framework.common.enums.BaseErrorEnum;
import com.cloudm.framework.common.ex.BusinessCheckFailException;
import com.cloudm.sign.dao.ApplicationSessionContext;
import com.cloudm.sign.context.handler.ContextHolderService;
import org.springframework.util.StringUtils;

/**
 * @author Jay
 * @version v1.0
 * @description 登陆的用户业务session信息
 * @date 2018-01-15 15:22
 */

public abstract class DefaultContextHolder<S extends ApplicationSessionContext> implements ContextHolderService<S>{


    private ThreadLocal<S> sessionContext = new ThreadLocal<>();

    /**
     * 根据userName初始化session信息
     *
     * @param accountName
     */
    @Override
    public void initSessionContext(String accountName){
        if (isSessionContextInitialized()) {
            throw new IllegalStateException("duplicated initialization of sessionContext,check if this method invoked somewhere");
        }
        sessionContext.set((this.doSetSessionContext(accountName)));
    }

    /**
     * 拿到数据源后，设置业务session
     *
     * @param accountName T为能够填充业务session的对象，注意实现该对象前需要对字段判空
     * @return
     */
    @Override
    public S doSetSessionContext(String accountName) {
        if (StringUtils.isEmpty(accountName)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "初始化业务session时，userName不能为空");
        }
        S s = this.setSessionContext(accountName);
        if (s == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),"未设置业务session，返回对象为空");
        }
        return s;
    }

    /**
     * 设置业务session, 并且该业务session对象将会存入redis中, 子类必须实现
     *
     * @param accountName
     * @return
     */
    public abstract S setSessionContext(String accountName);

    /**
     * 获取业务session
     *
     * @return
     */
    public S getSessionContext() {
        return sessionContext.get();
    }

    /**
     * 重置session
     */
    public void resetContext() {
        sessionContext.remove();
    }

    /**
     * 业务session是否被初始化
     *
     * @return
     */
    private boolean isSessionContextInitialized() {
        return sessionContext.get() != null;
    }
}
