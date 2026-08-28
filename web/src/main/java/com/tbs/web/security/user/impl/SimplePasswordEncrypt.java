package com.tbs.web.security.user.impl;

import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.web.security.user.IPasswordEncrypt;

/**
 * 密码加密实现类
 * <p>
 * {@link IPasswordEncrypt} 的默认实现：使用 {@code EncryptUitl} 的 SHA-256 加盐哈希对明文密码加密，
 * 盐值内置于实现中。注意：盐值硬编码存在泄露风险，后续建议改为配置化或随机盐。
 * </p>
 */
public class SimplePasswordEncrypt implements IPasswordEncrypt {
    @Override
    public String encrypt(String password) {
        return EncryptUitl.hash256Encrypt(password, "SYSTEM>_2025_09_18_23!~");
    }
}
