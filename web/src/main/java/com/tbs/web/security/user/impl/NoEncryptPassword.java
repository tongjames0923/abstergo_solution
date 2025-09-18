package com.tbs.web.security.user.impl;

import com.tbs.web.security.user.IPasswordEncrypt;

/**
 * 密码不加密
 *
 * @author tongj
 */
public class NoEncryptPassword implements IPasswordEncrypt {
	@Override
	public String encrypt(String password) {
		return password;
	}
}
