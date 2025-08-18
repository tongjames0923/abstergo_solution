package com.tbs.app.service.business.user;

/**
 * 密码加密接口
 *
 * @author tongj
 */
public interface IPasswordEncrypt {
    /**
     * 加密
     *
     * @param password
     * @return
     */
    String encrypt(String password);
}
