package com.tbs.web.security.user;

/**
 * 密码加密接口
 * <p>
 * 定义用户密码的单向/可逆加密能力，落地时通常对接 BCrypt 等算法。
 * 用于登录校验前对明文密码做规范化处理。
 * </p>
 *
 * @author tongj
 */
public interface IPasswordEncrypt {
    /**
     * 加密密码
     *
     * @param password 待加密的明文密码，不能为空
     * @return 加密后的密码串
     */
    String encrypt(String password);
}
