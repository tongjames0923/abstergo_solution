package com.tbs.web.result;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * ajax结果
 *
 * @author tongj
 */
@Data
public class AjaxResult implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 默认成功码
	 */
	public static final String DEFAULT_SUCCESS_MESSAGE = "操作成功";
	/**
	 * 默认失败码
	 */
	public static final String DEFAULT_FAIL_MESSAGE = "操作失败";
	/**
	 * 默认错误码
	 */
	public static final String DEFAULT_ERROR_MESSAGE = "系统异常";
	/**
	 * 默认成功码
	 */
	public static final int DEFAULT_SUCCESS_CODE = 200;
	/**
	 * 默认失败码
	 */
	public static final int DEFAULT_FAIL_CODE = 500;
	/**
	 * 默认错误码
	 */
	public static final int DEFAULT_ERROR_CODE = 500;
	/**
	 * 默认未授权码
	 */
	public static final int DEFAULT_UNAUTHORIZED_CODE = 401;

	/**
	 * 默认重新登录码
	 */
	public static final int RELOGIN_CODE = 405;

	/**
	 * 默认重新登录信息
	 */
	public static final String DEFAULT_RELOGIN_MESSAGE = "请重新登录";

	public static final int REFRESH_FAIL_CODE = 468;

    public static final String REFRESH_FAIL_CODE_MESSAGE = "Token失效且刷新Token失败";

	/**
	 * 默认未授权信息
	 */
	public static final String DEFAULT_UNAUTHORIZED_MESSAGE = "未授权";
	/**
	 * 默认无权限码
	 */
	public static final String DEFAULT_FORBIDDEN_MESSAGE = "无权限";
	/**
	 * 默认无权限码
	 */
	public static final int DEFAULT_FORBIDDEN_CODE = 403;
	/**
	 * 默认数据键
	 */
	public static final String MESSAGE_KEY = "message";
	/**
	 * 默认数据键
	 */
	public static final String CODE_KEY = "code";
	/**
	 * 默认数据键
	 */
	public static final String DATA_KEY = "data";

	private Map<String, Object> map;
}
