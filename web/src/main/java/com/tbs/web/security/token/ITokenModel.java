package com.tbs.web.security.token;

import java.io.Serializable;
import java.util.Date;

import com.tbs.web.enums.AccessTokenCreateTypeEnum;
import com.tbs.web.enums.TokenSource;

/**
 * 令牌模型
 *
 * @author abstergo
 */
public interface ITokenModel extends Serializable {
	/**
	 * 获取令牌
	 *
	 * @return
	 */
	String getToken();

	/**
	 * 获取令牌来源
	 *
	 * @return
	 */
	TokenSource getSource();

	/**
	 * 获取令牌来源字段
	 *
	 * @return
	 */
	String getSourceField();

	/**
	 * 获取令牌来源字段值
	 *
	 * @return
	 */
	String getSourceValue();

	/**
	 * 获取令牌过期时间
	 *
	 * @return
	 */
	Date getExpireTime();

	/**
	 * 获取令牌创建时间
	 *
	 * @return
	 */
	Date getCreateTime();

	/**
	 * 获取令牌类型
	 *
	 * @return
	 */
	AccessTokenCreateTypeEnum getCreateType();

}
