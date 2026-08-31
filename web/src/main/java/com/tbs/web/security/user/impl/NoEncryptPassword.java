package com.tbs.web.security.user.impl;

import com.tbs.web.security.user.IPasswordEncrypt;

/**
 * 密码不加密实现
 * <p>
 * {@link IPasswordEncrypt} 的透传实现：直接返回明文，用于测试或明文字节存储的兼容场景。
 * 生产环境应使用 {@code SimplePasswordEncrypt} 等加密实现。
 * </p>
 *
 * @author tongj
 */
public class NoEncryptPassword implements IPasswordEncrypt {
	@Override
	public String encrypt(String password) {
		return password;
	}
}
