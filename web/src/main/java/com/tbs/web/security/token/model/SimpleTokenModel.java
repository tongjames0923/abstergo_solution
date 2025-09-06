package com.tbs.web.security.token.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.token.ITokenModel;

import lombok.Data;

/**
 * token实体模型
 *
 * @author tongj
 */
@Data
public class SimpleTokenModel implements ITokenModel {
	private static final long serialVersionUID = 1L;
	/**
	 * token
	 */
	private String token;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * token状态
	 */
	private TokenStatusEnum statusEnum;

	private TokenTypeEnum typeEnum;

	/**
	 * 过期时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date expireTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private TokenCreateTypeEnum createType;

	protected SimpleTokenModel() {
	}

	/**
	 * 创建TokenModel
	 *
	 * @param token
	 * @param source
	 * @param sourceField
	 * @param sourceValue
	 * @param expireTime
	 * @return
	 */
	public static SimpleTokenModel create(String token,
			Date expireTime) {
		return create(token, expireTime, TokenCreateTypeEnum.LOGIN);
	}

	public static SimpleTokenModel createByRefresh(String token, Date expireTime) {
		return create(token, expireTime, TokenCreateTypeEnum.REFRESH);
	}

	public static SimpleTokenModel create(String token, Date expireTime, TokenCreateTypeEnum createType) {
		SimpleTokenModel model = new SimpleTokenModel();
		model.token = token;
		model.expireTime = expireTime;
		model.createTime = new Date();
		model.createType = createType;
		return model;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public TokenCreateTypeEnum getCreateType() {
		return createType;
	}

	@Override
	public TokenStatusEnum getStatus() {
		return statusEnum;
	}

	@Override
	public void setStatus(TokenStatusEnum status) {
		this.statusEnum = status;
	}

	@Override
	public TokenTypeEnum getTokenType() {
		return typeEnum;
	}
}
