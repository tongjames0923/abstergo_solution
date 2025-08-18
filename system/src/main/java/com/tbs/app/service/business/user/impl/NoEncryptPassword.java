package com.tbs.app.service.business.user.impl;

import com.tbs.app.service.business.user.IPasswordEncrypt;

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
