package com.cloudm.sign.dao;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jay
 * @version v1.0
 * @description 抽象的业务session 子类可以继承扩展
 * @date 2018-03-02 11:12
 */
@Data
public  class ApplicationSessionContext implements Serializable{


    /**
     * 用户Id
     */
    protected Integer id;

    /**
     * 账号名称
     */
    protected String accountName;


    /**
     * 构造器
     *
     * @param id
     * @param accountName
     */
    public ApplicationSessionContext(Integer id, String accountName){
        this.id = id;
        this.accountName = accountName;
    }

}
