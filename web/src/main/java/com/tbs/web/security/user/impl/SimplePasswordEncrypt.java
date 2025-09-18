package com.tbs.web.security.user.impl;

import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.web.security.user.IPasswordEncrypt;

/**
 * 密码加密实现类
 *
 */
public class SimplePasswordEncrypt implements IPasswordEncrypt {
    @Override
    public String encrypt(String password) {
        return EncryptUitl.hash256Encrypt(password, "SYSTEM>_2025_09_18_23!~");
    }
}
