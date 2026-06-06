package com.tbs.shared.interfaces.constraint;

/**
 * 验证用户名密码合法性
 *
 * @author abstergo
 */
public interface IUserNamePasswordValidator {
	/**
	 * 验证用户名合法性
	 *
	 * @param username
	 * @return
	 */
	boolean validateUserName(String username);

	/**
	 * 验证密码合法性
	 *
	 * @param password
	 * @return
	 */
	boolean validatePassword(String password);
	
}
